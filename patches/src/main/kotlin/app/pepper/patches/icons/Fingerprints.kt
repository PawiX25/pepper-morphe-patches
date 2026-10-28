package app.pepper.patches.icons

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Locate the icon-picker builder method by characteristic analytics-name strings
 * "custom_app_icon_tier{1,2,3}_default" — these are stable across releases.
 * The method itself is a synthetic multi-case dispatcher in `sa0`.
 */
internal object TierIconsBuilderFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    strings = listOf(
        "custom_app_icon_tier1_default",
        "custom_app_icon_tier2_default",
        "custom_app_icon_tier3_default",
    ),
)

/**
 * Same picker builder as TierIconsBuilderFingerprint, but located via the
 * 4 event-icon analytics names — used as a separate fingerprint so the patch
 * can run independently of the tier-icons patch.
 */
internal object EventIconsFilterFingerprint : Fingerprint(
    strings = listOf(
        "custom_app_icon_event_theming_summer_sales",
        "custom_app_icon_event_theming_autumn_sales",
        "custom_app_icon_event_theming_black_friday",
        "custom_app_icon_event_theming_el_buen_fin",
    ),
)

/**
 * `(boolean, Continuation)Object` — ShouldRestoreEventThemingAppIconToDefaultUseCase.
 * Pin via:
 *  - 2 params: boolean + Continuation reference
 *  - returns Object (suspend Boolean)
 *  - old shape (<= 8.12): body reads TimeUnit.MILLISECONDS directly
 *  - new shape (8.20): body fetches EventTheming (J field = activeUntilDate),
 *    reads the current time via a long-returning clock call and compares with CMP_LONG
 */
internal object ShouldRestoreEventIconFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    custom = { method, _ ->
        if (method.parameterTypes.size != 2) false
        else if (method.parameterTypes[0].toString() != "Z") false
        else {
            val secondParam = method.parameterTypes[1].toString()
            if (!secondParam.startsWith("L") || !secondParam.endsWith(";")) false
            else {
                val impl = method.implementation
                if (impl == null) false
                else {
                    var hasTimeUnit = false
                    var hasLongReturningCall = false
                    var hasLongField = false
                    var hasLongCompare = false
                    impl.instructions.forEach { insn ->
                        if (insn.opcode == Opcode.CMP_LONG) hasLongCompare = true
                        if (insn is ReferenceInstruction) {
                            when (val reference = insn.reference) {
                                is MethodReference -> {
                                    if (reference.toString().contains(
                                            "Ljava/util/concurrent/TimeUnit;->MILLISECONDS",
                                        )
                                    ) hasTimeUnit = true
                                    if (reference.returnType == "J") hasLongReturningCall = true
                                }
                                is FieldReference -> if (reference.type == "J") hasLongField = true
                            }
                        }
                    }
                    hasTimeUnit || (hasLongReturningCall && hasLongField && hasLongCompare)
                }
            }
        }
    },
)
