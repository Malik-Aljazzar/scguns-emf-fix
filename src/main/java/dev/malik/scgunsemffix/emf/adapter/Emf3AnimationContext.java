package dev.malik.scgunsemffix.emf.adapter;

import dev.malik.scgunsemffix.emf.EmfAnimationContext;
import dev.malik.scgunsemffix.emf.EmfAnimationContexts;
import traben.entity_model_features.EMFAnimationApi;
import traben.entity_model_features.models.animation.state.EMFEntityRenderState;
import traben.entity_model_features.models.animation.state.EMFState;

/**
 * Adapter for EMF 3.x (verified against 3.3.12), where the static EMF 2.x
 * {@code EMFAnimationEntityContext} was replaced by per-render {@link EMFEntityRenderState}s:
 * <ul>
 *     <li>{@code EMFAnimationEntityContext.isFirstPersonHand} -> {@code EMFState.state().isFirstPersonHand()}</li>
 *     <li>{@code EMFAnimationEntityContext.getEMFEntity()} -> {@code EMFAnimationApi.getCurrentEntity()}
 *     (the public API; EMF mixes {@code EMFEntity} into {@code Entity}, so this is the entity itself)</li>
 * </ul>
 *
 * Only loaded by {@link EmfAnimationContexts} after EMF 3 classes were found. Any LinkageError (EMF
 * changed these signatures in a later version) disables the bridge instead of crashing, returning
 * the same fallback values ScorchedGuns' own code returns when its reflection fails.
 */
public final class Emf3AnimationContext implements EmfAnimationContext {

    @Override
    public boolean isFirstPersonHand() {
        try {
            EMFEntityRenderState state = EMFState.state();
            return state != null && state.isFirstPersonHand();
        } catch (LinkageError e) {
            EmfAnimationContexts.disable(e);
            return false;
        }
    }

    @Override
    public Object currentEntity() {
        try {
            return EMFAnimationApi.getCurrentEntity();
        } catch (LinkageError e) {
            EmfAnimationContexts.disable(e);
            return null;
        }
    }
}
