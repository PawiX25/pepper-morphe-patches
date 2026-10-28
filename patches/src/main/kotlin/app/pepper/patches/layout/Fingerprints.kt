package app.pepper.patches.layout

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * The `(I)Z` helper that maps a RecyclerView view type to a section/divider mode.
 *
 * v8.12: `m07->e(I)Z`, with getItemOffsets/onDraw in the same class.
 * v8.20: `Lev7->i(I)Z`, the concrete ItemDecoration subclass; its
 *        getItemOffsets/onDraw overrides moved up into abstract base `Ln4f`.
 *
 * No other method has >=10 distinct R.id literals inside one `(I)Z` body, so
 * the literal-count threshold uniquely picks this helper out. The patch uses
 * it only as a sanity check that the configured ad view-type IDs exist in the
 * target build.
 */
internal object ItemDecorationEFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("I"),
    returnType = "Z",
    custom = { method, _ ->
        val impl = method.implementation
        if (impl == null) {
            false
        } else {
            val rIdLiterals = impl.instructions
                .filterIsInstance<NarrowLiteralInstruction>()
                .map { it.narrowLiteral }
                .filter { it in 0x7f0a0000..0x7f0a00ff }
                .toSet()
            rIdLiterals.size >= 10
        }
    },
)

/**
 * `RecyclerView.ItemDecoration.getItemOffsets` override.
 *
 * Signature: `(Landroid/graphics/Rect; Landroid/view/View;
 *               Landroidx/recyclerview/widget/RecyclerView; L<state>;)V`
 *
 * Body invariants:
 *  - calls `Rect.set(IIII)V` to assign the offset
 *  - calls its OWN `(I)Z` and `(II)Z` helpers (the section/divider dispatch
 *    pair; in 8.20 these are `i(I)Z` and `h(II)Z`)
 *  - calls the adapter's `getItemViewType(I)I` (obfuscated as `c(I)I` in
 *    older builds)
 *
 * The own-class (I)Z + (II)Z pair distinguishes this decoration from the
 * sibling `Lvpc;` decoration, which only has a `(II)Z` helper.
 */
internal object ItemDecorationGetItemOffsetsFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    custom = { method, _ ->
        val params = method.parameterTypes
        val impl = method.implementation
        if (params.size != 4 ||
            params[0].toString() != "Landroid/graphics/Rect;" ||
            params[1].toString() != "Landroid/view/View;" ||
            params[2].toString() != "Landroidx/recyclerview/widget/RecyclerView;" ||
            !params[3].toString().matches(Regex("L[^;]+;")) ||
            impl == null
        ) {
            false
        } else {
            val ownClass = method.definingClass.toString()
            val refs = impl.instructions.mapNotNull { (it as? ReferenceInstruction)?.reference }

            val callsRectSet = refs.any { ref ->
                ref is MethodReference &&
                    ref.definingClass == "Landroid/graphics/Rect;" &&
                    ref.name == "set" &&
                    ref.parameterTypes.size == 4
            }
            val callsOwnTypeHelper = refs.any { ref ->
                ref is MethodReference &&
                    ref.definingClass.toString() == ownClass &&
                    ref.parameterTypes.size == 1 &&
                    ref.parameterTypes[0].toString() == "I" &&
                    ref.returnType == "Z"
            }
            val callsOwnPairHelper = refs.any { ref ->
                ref is MethodReference &&
                    ref.definingClass.toString() == ownClass &&
                    ref.parameterTypes.size == 2 &&
                    ref.parameterTypes.all { it.toString() == "I" } &&
                    ref.returnType == "Z"
            }
            val callsAdapterTypeLookup = refs.any { ref ->
                ref is MethodReference &&
                    (ref.name == "getItemViewType" || ref.name == "c") &&
                    ref.parameterTypes.size == 1 &&
                    ref.parameterTypes[0].toString() == "I" &&
                    ref.returnType == "I"
            }
            callsRectSet && callsOwnTypeHelper && callsOwnPairHelper && callsAdapterTypeLookup
        }
    },
)

/**
 * `RecyclerView.ItemDecoration.onDraw` override.
 *
 * Signature: `(Landroid/graphics/Canvas; Landroidx/recyclerview/widget/RecyclerView;
 *               L<state>;)V`
 *
 * Body invariants:
 *  - walks visible items via the layout manager's `B(I)Landroid/view/View;`
 *    (obfuscated `getChildAt`)
 *  - calls its OWN `(I)Z` and `(II)Z` helpers (in 8.20 `i(I)Z` / `h(II)Z`)
 *
 * v8.12 painted the `shadow_divider` drawable directly here; v8.20 moved the
 * actual drawing into the abstract subclass hooks (`f`/`g`), so the base
 * method only orchestrates the loop and calls `Canvas.drawRect`. The own-class
 * helper pair is still what excludes the sibling `Lvpc;` decoration, whose
 * onDraw has the canvas/getChildAt shape but only a `(II)Z` helper.
 */
internal object ItemDecorationOnDrawFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    custom = { method, _ ->
        val params = method.parameterTypes
        val impl = method.implementation
        if (params.size != 3 ||
            params[0].toString() != "Landroid/graphics/Canvas;" ||
            params[1].toString() != "Landroidx/recyclerview/widget/RecyclerView;" ||
            !params[2].toString().matches(Regex("L[^;]+;")) ||
            impl == null
        ) {
            false
        } else {
            val ownClass = method.definingClass.toString()
            val refs = impl.instructions.mapNotNull { (it as? ReferenceInstruction)?.reference }
            val refStrings = refs.map { it.toString() }

            val callsGetChildAt = refStrings.any {
                it.endsWith("->B(I)Landroid/view/View;")
            }
            val callsOwnTypeHelper = refs.any { ref ->
                ref is MethodReference &&
                    ref.definingClass.toString() == ownClass &&
                    ref.parameterTypes.size == 1 &&
                    ref.parameterTypes[0].toString() == "I" &&
                    ref.returnType == "Z"
            }
            val callsOwnPairHelper = refs.any { ref ->
                ref is MethodReference &&
                    ref.definingClass.toString() == ownClass &&
                    ref.parameterTypes.size == 2 &&
                    ref.parameterTypes.all { it.toString() == "I" } &&
                    ref.returnType == "Z"
            }
            callsGetChildAt && callsOwnTypeHelper && callsOwnPairHelper
        }
    },
)
