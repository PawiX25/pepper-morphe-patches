package app.pepper.patches.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.pepper.patches.shared.PEPPER_FAMILY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Hide banner ads in feed
 *
 * In stock Pepper, banner ads load via Pubmatic OpenWrap (POBBannerView)
 * inside the renderer method — the ViewHolder bind-method for banner-ad cells
 * in the deal RecyclerView.
 *
 * Two effects we want:
 *   1. POBBannerView.loadAd() never gets called → no ad request.
 *   2. The empty AdViewContainer cell (gray box) is collapsed so it leaves
 *      zero visible trace BETWEEN deal cards in the Hot Deals feed.
 *
 * Two renderer shapes are supported:
 *
 * Old (v8.13.x): void-returning binder that fetches a root View field from
 *   the ViewHolder. We replace the entire body with a compact stub that
 *   hides/collapses the root view.
 *
 * New (v8.20.x): returns an object handle wrapping the created POBBannerView.
 *   The caller stores/releases this handle, so we cannot replace the body.
 *   Instead we:
 *     - Hide/collapse the ViewHolder's itemView (the whole `item_ad` cell) at
 *       bind time. Hiding only the inner AdViewContainer leaves the striped
 *       background, label and action button visible.
 *     - Replace the renderer call in the ViewHolder binder with null, which
 *       suppresses both Pubmatic and AATKit feed ads and skips the request.
 *     - Belt-and-braces: nop POBBannerView.loadAd() and hide the container in
 *       the renderer itself.
 */
@Suppress("unused")
val hideBannerAdsPatch = bytecodePatch(
    name = "Hide banner ads in feed",
    description = "Removes the banner ads in the deal feed.",
) {
    compatibleWith(*PEPPER_FAMILY)

    execute {
        val method = BannerAdRendererFingerprint.method
        val impl = method.implementation!!

        val receiverReg = if ((method.accessFlags and AccessFlags.STATIC.value) != 0) "p0" else "p1"

        if (method.returnType == "V") {
            // --- Old renderer: void-returning binder with root-view field ---
            val rootViewField = impl.instructions
                .filterIsInstance<ReferenceInstruction>()
                .mapNotNull { it.reference as? FieldReference }
                .firstOrNull { it.type == "Landroid/view/View;" }
                ?: throw PatchException(
                    "Couldn't find root-view field accessor in renderer. " +
                        "ViewHolder structure may have changed."
                )

            val originalCount = impl.instructions.toList().size

            method.removeInstructions(0, originalCount)
            method.addInstructions(
                0,
                """
                iget-object v0, $receiverReg, ${rootViewField.definingClass}->${rootViewField.name}:Landroid/view/View;
                const/16 v1, 0x8
                invoke-virtual { v0, v1 }, Landroid/view/View;->setVisibility(I)V
                invoke-virtual { v0 }, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                move-result-object v1
                if-eqz v1, :end
                const/4 v2, 0x0
                iput v2, v1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                instance-of v3, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;
                if-eqz v3, :apply
                check-cast v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;
                iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->topMargin:I
                iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->bottomMargin:I
                iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->leftMargin:I
                iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->rightMargin:I
                :apply
                invoke-virtual { v0, v1 }, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup${'$'}LayoutParams;)V
                :end
                return-void
                """.trimIndent()
            )
        } else {
            // --- New renderer: returns object handle (POBBannerView wrapper) ---
            val instructions = impl.instructions.toList()

            // Nop out POBBannerView.loadAd() to prevent the ad request.
            val loadAdIdx = instructions.indexOfFirst { insn ->
                insn is ReferenceInstruction &&
                    (insn.reference as? MethodReference)?.toString()
                        ?.contains("POBBannerView;->loadAd()V") == true
            }
            if (loadAdIdx >= 0) {
                method.replaceInstruction(loadAdIdx, "nop")
            }

            // The renderer is called through an interface from the feed
            // ViewHolder binder. Find that binder and stop the call entirely:
            // the renderer only handles Pubmatic, while the dispatcher also
            // has an AATKit path that must not run either.
            val rendererParams = method.parameterTypes
            val rendererReturnType = method.returnType
            var binderFound = false
            var binderPatched = false

            classDefForEach { classDef ->
                if (binderPatched) return@classDefForEach

                val binderCandidate = classDef.methods.firstOrNull { candidate ->
                    if (candidate.returnType != "V") return@firstOrNull false
                    val params = candidate.parameterTypes
                    if (params.size != 2) return@firstOrNull false
                    if (params[0].toString() != "Landroidx/recyclerview/widget/RecyclerView\$ViewHolder;") {
                        return@firstOrNull false
                    }

                    candidate.implementation?.instructions?.any { insn ->
                        if (insn.opcode != Opcode.INVOKE_INTERFACE) return@any false
                        val reference = (insn as? ReferenceInstruction)?.reference as? MethodReference
                            ?: return@any false
                        reference.returnType == rendererReturnType &&
                            reference.parameterTypes == rendererParams
                    } == true
                } ?: return@classDefForEach

                binderFound = true
                val mutableClass = mutableClassDefBy(classDef)
                val binder = mutableClass.methods.firstOrNull { mutable ->
                    mutable.name == binderCandidate.name &&
                        mutable.parameterTypes == binderCandidate.parameterTypes &&
                        mutable.returnType == binderCandidate.returnType
                } ?: return@classDefForEach

                val binderImpl = binder.implementation ?: return@classDefForEach
                val binderInstructions = binderImpl.instructions.toList()
                val rendererCallIdx = binderInstructions.indexOfFirst { insn ->
                    if (insn.opcode != Opcode.INVOKE_INTERFACE) return@indexOfFirst false
                    val reference = (insn as? ReferenceInstruction)?.reference as? MethodReference
                        ?: return@indexOfFirst false
                    reference.returnType == rendererReturnType &&
                        reference.parameterTypes == rendererParams
                }
                if (rendererCallIdx < 0 || rendererCallIdx + 1 >= binderInstructions.size) {
                    return@classDefForEach
                }

                val moveResult = binderInstructions[rendererCallIdx + 1]
                if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) {
                    return@classDefForEach
                }
                val resultReg = (moveResult as OneRegisterInstruction).registerA

                val binderStatic = (binder.accessFlags and AccessFlags.STATIC.value) != 0
                val holderReg = if (binderStatic) "p0" else "p1"
                val parameterRegs = binder.parameterTypes.size + if (binderStatic) 0 else 1
                val localRegs = binderImpl.registerCount - parameterRegs
                if (localRegs < 3) {
                    throw PatchException(
                        "Feed ad binder has only $localRegs local registers; " +
                            "expected at least 3 for hide/collapse code."
                    )
                }

                val zeroInsn = when {
                    resultReg <= 15 -> "const/4 v$resultReg, 0x0"
                    resultReg <= 255 -> "const/16 v$resultReg, 0x0"
                    else -> "const v$resultReg, 0x0"
                }

                // Replace `invoke-interface ... renderer(...)` plus its
                // `move-result-object` with a null result. The caller already
                // accepts a null renderer handle.
                binder.removeInstructions(rendererCallIdx, 2)
                binder.addInstructions(rendererCallIdx, zeroInsn)

                // The feed cell root is `item_ad` (StripedBackgroundFrameLayout),
                // not the inner AdViewContainer. Hide and collapse the whole cell.
                binder.addInstructions(
                    0,
                    """
                    iget-object v0, $holderReg, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                    const/16 v1, 0x8
                    invoke-virtual { v0, v1 }, Landroid/view/View;->setVisibility(I)V
                    invoke-virtual { v0 }, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                    move-result-object v1
                    if-eqz v1, :end
                    const/4 v2, 0x0
                    iput v2, v1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                    instance-of v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;
                    if-eqz v2, :apply
                    check-cast v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;
                    const/4 v2, 0x0
                    iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->topMargin:I
                    iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->bottomMargin:I
                    iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->leftMargin:I
                    iput v2, v1, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->rightMargin:I
                    :apply
                    invoke-virtual { v0, v1 }, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup${'$'}LayoutParams;)V
                    :end
                    """.trimIndent()
                )
                binderPatched = true
            }

            if (!binderFound) {
                throw PatchException(
                    "Couldn't find the feed ad ViewHolder binder. " +
                        "The ad cell structure may have changed."
                )
            }
            if (!binderPatched) {
                throw PatchException(
                    "Found the feed ad ViewHolder binder, but couldn't replace " +
                        "its renderer call."
                )
            }

            // Belt and braces: if a renderer is ever called directly, hide the
            // inner container and prevent loading. On 8.20 the binder above
            // returns null before this point.
            method.addInstructions(
                0,
                """
                const/16 v0, 0x8
                invoke-virtual { $receiverReg, v0 }, Landroid/view/View;->setVisibility(I)V
                invoke-virtual { $receiverReg }, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                move-result-object v0
                if-eqz v0, :end
                const/4 v1, 0x0
                iput v1, v0, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                instance-of v2, v0, Landroid/view/ViewGroup${'$'}MarginLayoutParams;
                if-eqz v2, :apply
                check-cast v0, Landroid/view/ViewGroup${'$'}MarginLayoutParams;
                iput v1, v0, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->topMargin:I
                iput v1, v0, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->bottomMargin:I
                iput v1, v0, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->leftMargin:I
                iput v1, v0, Landroid/view/ViewGroup${'$'}MarginLayoutParams;->rightMargin:I
                :apply
                invoke-virtual { $receiverReg, v0 }, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup${'$'}LayoutParams;)V
                :end
                """.trimIndent()
            )
        }
    }
}
