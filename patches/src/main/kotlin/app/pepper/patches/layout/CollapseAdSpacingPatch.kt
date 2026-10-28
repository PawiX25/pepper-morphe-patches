package app.pepper.patches.layout

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.util.smali.ExternalLabel
import app.pepper.patches.ads.hideBannerAdsPatch
import app.pepper.patches.shared.PEPPER_FAMILY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21t
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Fix spacing around hidden ad cells in deal-detail RecyclerView
 *
 * Companion fix for [hideBannerAdsPatch]. When ad cells are collapsed to
 * `height=0` + zero margins, the section ItemDecoration still adds a bottom
 * offset under each ad AND paints a `shadow_divider` drawable on top of the
 * next view — RecyclerView ItemDecoration runs for every adapter position
 * regardless of view visibility. Result: doubled section gap and a shadow
 * line clipping the next subheader.
 *
 * Patch: insert two compact guards into the decoration.
 *
 *   • `getItemOffsets`: if the row's view type matches small_ad / medium_ad /
 *     large_ad, write `Rect.set(0, 0, 0, 0)` and return. Skips the offset
 *     entirely for collapsed ad cells.
 *
 *   • `onDraw`: inside the per-item iteration, if the type is an ad, branch
 *     to the existing "skip this item" target. Skips both the shadow_divider
 *     draw and the colored-rect paint for ad rows.
 *
 * v8.12 had all three methods on one obfuscated class (`m07`). v8.20 moved
 * getItemOffsets/onDraw into abstract base `Ln4f`, while the view-type helper
 * `i(I)Z` lives on the concrete subclass `Lev7`. The fingerprints therefore
 * locate each method by structure instead of requiring a shared class.
 *
 * Resource IDs are configurable per package — each Pepper variant has its
 * own `R.id.adapter_delegate_view_type_*_ad`. Defaults match mydealz
 * 8.21.01; the known 8.13/8.20 ID sets are also tried automatically, so a
 * shifted ID space does not break the patch.
 */
@Suppress("unused")
val collapseAdSpacingPatch = bytecodePatch(
    name = "Fix spacing around hidden ad cells",
    description = "Removes the empty space and shadow divider left behind by " +
        "hidden banner-ad cells in deal-detail screens.",
) {
    compatibleWith(*PEPPER_FAMILY)

    dependsOn(hideBannerAdsPatch)

    val smallAdViewTypeIdOpt by stringOption(
        key = "smallAdViewTypeId",
        default = "0x7f0a009b",
        title = "small_ad view-type R.id (hex)",
        description = "Resource ID of `adapter_delegate_view_type_small_ad`. " +
            "Default is correct for mydealz v8.21.01; the known 8.20 and 8.13 " +
            "IDs are also tried automatically. Look up the package's value in " +
            "resources.arsc (or res/values/public.xml) if a future build shifts ID slots.",
        required = true,
    )

    val mediumAdViewTypeIdOpt by stringOption(
        key = "mediumAdViewTypeId",
        default = "0x7f0a0081",
        title = "medium_ad view-type R.id (hex)",
        description = "Resource ID of `adapter_delegate_view_type_medium_ad`. " +
            "Default is correct for mydealz v8.21.01.",
        required = true,
    )

    val largeAdViewTypeIdOpt by stringOption(
        key = "largeAdViewTypeId",
        default = "0x7f0a0078",
        title = "large_ad view-type R.id (hex)",
        description = "Resource ID of `adapter_delegate_view_type_large_ad`. " +
            "Default is correct for mydealz v8.21.01.",
        required = true,
    )

    execute {
        fun parseHex(s: String): Int =
            (if (s.startsWith("0x") || s.startsWith("0X")) s.substring(2) else s)
                .toLong(16).toInt()

        val configuredSmallId = parseHex(smallAdViewTypeIdOpt!!)
        val configuredMediumId = parseHex(mediumAdViewTypeIdOpt!!)
        val configuredLargeId = parseHex(largeAdViewTypeIdOpt!!)

        val eMethod = ItemDecorationEFingerprint.method
        val aMethod = ItemDecorationGetItemOffsetsFingerprint.method
        val bMethod = ItemDecorationOnDrawFingerprint.method

        // In 8.20+ the view-type helper lives on the concrete subclass while
        // getItemOffsets/onDraw are inherited from the abstract base, so the
        // three methods no longer share a defining class. Validate the ID set
        // against the helper's literal list before touching anything.
        val helperLiterals = eMethod.implementation?.instructions
            ?.filterIsInstance<NarrowLiteralInstruction>()
            ?.map { it.narrowLiteral }
            ?.toSet()
            ?: emptySet()

        // Resource IDs shift between app releases even though the layouts and
        // view-type names stay the same:
        //   8.21.01: 009b / 0081 / 0078
        //   8.20.00: 00a3 / 0089 / 007f
        //   8.13.02: 009e / 0084 / 007c
        // Try the user's configured IDs first, then the known sets. This keeps
        // old saved options working after an app update.
        val idCandidates = listOf(
            intArrayOf(configuredSmallId, configuredMediumId, configuredLargeId),
            intArrayOf(0x7f0a009b, 0x7f0a0081, 0x7f0a0078),
            intArrayOf(0x7f0a00a3, 0x7f0a0089, 0x7f0a007f),
            intArrayOf(0x7f0a009e, 0x7f0a0084, 0x7f0a007c),
        )
        val configuredIds = listOf(configuredSmallId, configuredMediumId, configuredLargeId)
        val configuredText = configuredIds.joinToString { "0x%08x".format(it) }
        val chosenIds = idCandidates.firstOrNull { ids -> ids.all { it in helperLiterals } }
            ?: throw PatchException(
                "No known ad view-type ID set found in " +
                    "${eMethod.definingClass}->${eMethod.name}(I)Z. " +
                    "Configured=$configuredText; known sets: 8.21, 8.20, 8.13. " +
                    "The resource IDs probably shifted — update the patch options."
            )
        val smallId = chosenIds[0]
        val mediumId = chosenIds[1]
        val largeId = chosenIds[2]

        // ----- Patch getItemOffsets: zero-offset guard ---------------------
        // The body contains (right after the position-validation block):
        //
        //     invoke-virtual {adapter, pos}, ...->getItemViewType(I)I
        //     move-result vX                            # currentType
        //     add-int/lit8 vPos, vPos, 0x1              # next position
        //
        // Older builds obfuscate getItemViewType as `c(I)I`; both names are
        // accepted by the pattern finder.
        //
        // Two-step injection:
        //  1. Append a zero-block right before the method's final return-void:
        //         const/4 v0, 0x0
        //         invoke-virtual {p1, v0, v0, v0, v0}, Rect;->set(IIII)V
        //         return-void
        //  2. At the original guard position, inject the type-checking branches
        //     and bind their target externally to the zero-block's first
        //     instruction.
        //
        // .locals is 1 (only v0 is a local), so we reuse v0 — restoring its
        // `0` value at the end of the no-match fallthrough, since downstream
        // code expects v0 == 0 for the Rect.set(0, 0, 0, bottom) calls.
        val aImpl = aMethod.implementation
            ?: throw PatchException("getItemOffsets has no implementation")
        val aInsnsInitial = aImpl.instructions.toList()
        val aCurrTypeIdx = aInsnsInitial.findFirstAdapterGetItemViewTypePattern()
            ?: throw PatchException(
                "getItemOffsets: getItemViewType + move-result + add-int/lit8 pattern not found"
            )
        val aCurrTypeReg = (aInsnsInitial[aCurrTypeIdx + 1] as OneRegisterInstruction).registerA

        // Locate the FINAL return-void of the method — the trailing one in the
        // method body, after all the original return paths. Inserting before it
        // preserves every existing branch target (none of them aim at the very
        // last instruction).
        val aLastReturnIdx = aInsnsInitial.indexOfLast { it.opcode == Opcode.RETURN_VOID }
        if (aLastReturnIdx < 0) {
            throw PatchException("getItemOffsets has no return-void instruction")
        }

        aMethod.addInstructions(
            aLastReturnIdx,
            """
            const/4 v0, 0x0
            invoke-virtual { p1, v0, v0, v0, v0 }, Landroid/graphics/Rect;->set(IIII)V
            return-void
            """.trimIndent()
        )

        // After the append, the instruction at aLastReturnIdx is our newly-inserted
        // const/4 v0, 0x0 — the zero-block's first instruction, which is what we
        // want as the if-eq external branch target.
        val aZeroBlockStart = aImpl.instructions.toList()[aLastReturnIdx]

        aMethod.addInstructionsWithLabels(
            aCurrTypeIdx + 2,
            """
            const v0, $smallId
            if-eq v$aCurrTypeReg, v0, :collapse_ad_zero
            const v0, $mediumId
            if-eq v$aCurrTypeReg, v0, :collapse_ad_zero
            const v0, $largeId
            if-eq v$aCurrTypeReg, v0, :collapse_ad_zero
            const/4 v0, 0x0
            """.trimIndent(),
            ExternalLabel("collapse_ad_zero", aZeroBlockStart),
        )

        // ----- Patch onDraw: skip-draw guard -------------------------------
        // The per-item loop begins with:
        //
        //     invoke-virtual {adapter, pos}, ...->getItemViewType(I)I
        //     move-result vX                            # currentType
        //     add-int/lit8 vNext, pos, 0x1
        //     ...
        //     invoke-virtual {layoutManager, pos}, ...->B(I)Landroid/view/View;
        //     move-result-object vView
        //     if-eqz vView, :skip_iteration             # null view → skip
        //
        // We hook the same "skip this iteration" target: if currentType is an
        // ad cell, jump there. That's the existing register-restore +
        // goto-loop-tail path, so we don't need to replicate it.
        val bImpl = bMethod.implementation
            ?: throw PatchException("onDraw has no implementation")
        val bInsns = bImpl.instructions.toList()
        val bCurrTypeIdx = bInsns.findFirstAdapterGetItemViewTypePattern()
            ?: throw PatchException(
                "onDraw: getItemViewType + move-result + add-int/lit8 pattern not found"
            )
        val bCurrTypeReg = (bInsns[bCurrTypeIdx + 1] as OneRegisterInstruction).registerA

        // Locate the if-eqz that follows `move-result-object vView` from the
        // getChildAt call — its branch target IS the skip-iteration path.
        val bGetChildAtIdx = bInsns.indexOfFirst { insn ->
            insn.opcode == Opcode.INVOKE_VIRTUAL &&
                (insn as? ReferenceInstruction)?.reference?.toString()
                    ?.endsWith("->B(I)Landroid/view/View;") == true
        }
        if (bGetChildAtIdx < 0 || bGetChildAtIdx + 2 >= bInsns.size) {
            throw PatchException("onDraw: getChildAt + move-result-object + if-eqz pattern not found")
        }
        val ifeqz = bInsns[bGetChildAtIdx + 2] as? BuilderInstruction21t
            ?: throw PatchException(
                "onDraw: instruction after getChildAt move-result is not a builder if-eqz"
            )
        if (ifeqz.opcode != Opcode.IF_EQZ) {
            throw PatchException(
                "onDraw: expected if-eqz after getChildAt move-result, got ${ifeqz.opcode}"
            )
        }
        val skipIterationInsn = ifeqz.target.location.instruction
            ?: throw PatchException("onDraw: if-eqz branch target has no instruction")

        // .locals is 15 (v0..v14) in 8.20, so v6 is a free scratch register at
        // this point in the iteration body — overwritten by the original code
        // only later (when computing the bottom-Y for the drawable bounds).
        bMethod.addInstructionsWithLabels(
            bCurrTypeIdx + 2,
            """
            const v6, $smallId
            if-eq v$bCurrTypeReg, v6, :collapse_ad_skip
            const v6, $mediumId
            if-eq v$bCurrTypeReg, v6, :collapse_ad_skip
            const v6, $largeId
            if-eq v$bCurrTypeReg, v6, :collapse_ad_skip
            """.trimIndent(),
            ExternalLabel("collapse_ad_skip", skipIterationInsn),
        )
    }
}

/**
 * Find the index of the FIRST `invoke-virtual {?, ?}, ?->getItemViewType(I)I`
 * (or its obfuscated `c(I)I` equivalent) followed by `move-result vX`
 * followed by `add-int/lit8 vY, ?, 0x1`. Returns the index of the invoke
 * itself, so callers read the move-result at idx+1 and insert at idx+2.
 *
 * The "+1 increment after move-result" suffix anchors this to the
 * "currentType, then next position" pattern used by both getItemOffsets and
 * onDraw's loop — distinguishing it from later `getItemViewType` calls (which
 * fetch nextType and are followed by different operations).
 */
private fun List<com.android.tools.smali.dexlib2.iface.instruction.Instruction>.findFirstAdapterGetItemViewTypePattern(): Int? {
    for (idx in 0 until size - 2) {
        val invoke = this[idx]
        if (invoke.opcode != Opcode.INVOKE_VIRTUAL) continue
        val ref = (invoke as? ReferenceInstruction)?.reference as? MethodReference ?: continue
        if (ref.name != "getItemViewType" && ref.name != "c") continue
        if (ref.parameterTypes.size != 1 || ref.parameterTypes[0].toString() != "I") continue
        if (ref.returnType != "I") continue
        if (this[idx + 1].opcode != Opcode.MOVE_RESULT) continue
        if (this[idx + 2].opcode != Opcode.ADD_INT_LIT8) continue
        val add = this[idx + 2] as? NarrowLiteralInstruction ?: continue
        if (add.narrowLiteral != 1) continue
        return idx
    }
    return null
}
