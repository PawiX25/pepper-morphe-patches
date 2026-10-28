package app.pepper.patches.ads

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * The renderer references the literal String "liveramp.com" (PubMatic external-user-ID setup).
 * That string also appears in ANUserId.<init>, so the custom check must rule it out.
 *
 * Two known shapes:
 *   v8.13.x:  d(Llo9;Lia;)V                          - void binder, 2 ref params, reads View field
 *   v8.20.x:  a(Lcom/pepper/presentation/ads/AdViewContainer;Lse;Lak1;)Lwq0;
 *             - instance object-returning builder, 3 ref params, calls POBBannerView.loadAd()
 */
internal object BannerAdRendererFingerprint : Fingerprint(
    strings = listOf("liveramp.com"),
    custom = { method, _ ->
        val params = method.parameterTypes
        val paramOk = params.size in 2..3 &&
            params.all { p -> p.toString().startsWith("L") && p.toString().endsWith(";") }
        if (!paramOk) {
            false
        } else {
            // ANUserId.<init> has the same parameter profile, so require a renderer-specific
            // reference: a View field read (old shape) or POBBannerView.loadAd() (new shape).
            val refs = method.implementation?.instructions
                ?.mapNotNull { (it as? ReferenceInstruction)?.reference }
                .orEmpty()
            refs.any { ref ->
                (ref is FieldReference && ref.type == "Landroid/view/View;") ||
                    (ref is MethodReference && ref.definingClass.contains("POBBannerView") && ref.name == "loadAd")
            }
        }
    },
)
