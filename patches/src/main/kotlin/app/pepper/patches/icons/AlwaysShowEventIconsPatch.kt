package app.pepper.patches.icons

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.pepper.patches.shared.PEPPER_FAMILY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21s
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Always show all 4 EventTheming icons in picker
 *
 * Stock filter logic for event icons (Black Friday / Summer Sales / Autumn Sales /
 * El Buen Fin):
 *  - if no event currently active: hide all 4 event icons
 *  - if e.g. Black Friday active: show ONLY BlackFriday icon, hide other 3
 *
 * Each `re2` data-class instance has a `boolean f` = isEventThemingAppIcon.
 * The filter loop reads it via `iget-boolean v?, v?, Lre2;->f:Z`.
 *
 * Patch: replace those `iget-boolean ...->f:Z` reads with `const/4 v?, 0x0`.
 * Filter never sees an icon as "event-theming" → all 18 icons (incl. 4 events)
 * always visible.
 */
@Suppress("unused")
val alwaysShowEventIconsPatch = bytecodePatch(
    name = "Always show event-theming icons",
    description = "Shows every event-themed app icon in the picker, even when " +
        "its event is not currently active.",
) {
    compatibleWith(*PEPPER_FAMILY)

    dependsOn(unlockTierIconsPatch) // event icons need to be DEFAULT-tier first

    execute {
        val method = EventIconsFilterFingerprint.method
        val impl = method.implementation!!

        val instructions = impl.instructions.toList()

        // The icon model is the class in this method that stores a
        // `ManifestAliasId` field (conventionally `c`). Pinning to that class
        // avoids touching unrelated boolean `f` fields on other obfuscated
        // classes in the same method (e.g. reward `isNew`).
        val eventIconClasses = instructions
            .filterIsInstance<ReferenceInstruction>()
            .mapNotNull { it.reference as? FieldReference }
            .filter { it.type == "Lcom/pepper/presentation/customappicon/ManifestAliasId;" }
            .map { it.definingClass }
            .toSet()
        if (eventIconClasses.isEmpty()) {
            throw PatchException(
                "Could not identify the custom app-icon model from its " +
                    "ManifestAliasId field."
            )
        }

        // Replace each `iget-boolean v?, v?, L<icon-model>;->f:Z` read with
        // `const/16 v<dest>, 0x0`. `const/16` has the same 2-code-unit width as
        // `iget-boolean`; a 1-unit `const/4` shifts every following instruction
        // and can leave branch/try targets in the wrong place (VerifyError in
        // the 8.21 coroutine state machine).
        var patched = 0
        for (idx in instructions.indices.reversed()) {
            val insn = instructions[idx]
            if (insn.opcode != Opcode.IGET_BOOLEAN) continue
            val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference ?: continue
            if (ref.name != "f" || ref.type != "Z") continue
            if (ref.definingClass !in eventIconClasses) continue

            val destReg = (insn as OneRegisterInstruction).registerA
            val newInsn = BuilderInstruction21s(Opcode.CONST_16, destReg, 0)
            impl.replaceInstruction(idx, newInsn)
            patched++
        }
        if (patched == 0) {
            throw PatchException(
                "No iget-boolean isEventThemingAppIcon reads found. " +
                "The field may have been renamed/removed."
            )
        }
    }
}
