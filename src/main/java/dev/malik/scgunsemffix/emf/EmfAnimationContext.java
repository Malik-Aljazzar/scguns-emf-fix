package dev.malik.scgunsemffix.emf;

/**
 * Port: the two pieces of EMF's "what is being animated right now" context that ScorchedGuns'
 * OptionalClientCompat reads - whether the current animation pass is the first-person hand, and
 * which entity is being animated.
 *
 * ScorchedGuns 1.5 reads both reflectively from EMF 2.x's
 * {@code traben.entity_model_features.models.animation.EMFAnimationEntityContext}, which EMF 3.x
 * removed in favour of render states. Implementations adapt a specific EMF generation to this
 * port so the ScorchedGuns-facing mixin stays independent of EMF internals.
 */
public interface EmfAnimationContext {

    /** Same contract as ScorchedGuns' {@code isEntityModelFeaturesFirstPersonHand()}: false when unknown. */
    boolean isFirstPersonHand();

    /** Same contract as ScorchedGuns' {@code getEntityModelFeaturesContextEntity()}: null when unknown. */
    Object currentEntity();
}
