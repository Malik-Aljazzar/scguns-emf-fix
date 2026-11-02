package dev.malik.scgunsemffix.emf;

import dev.malik.scgunsemffix.ScgunsEmfFix;
import dev.malik.scgunsemffix.emf.adapter.Emf3AnimationContext;
import top.ribs.scguns.client.compat.OptionalClientCompat;

/**
 * Resolves which {@link EmfAnimationContext} bridge (if any) ScorchedGuns needs, once per session.
 *
 * <ul>
 *     <li>EMF 2.x (legacy context class present): no bridge - ScorchedGuns' own reflective code
 *     works as written (and is kept cheap by the caching redirects in OptionalClientCompatMixin).</li>
 *     <li>EMF 3.x (render-state API present, legacy class gone): {@link Emf3AnimationContext}.</li>
 *     <li>No EMF: no bridge - ScorchedGuns' non-EMF fallbacks apply unchanged.</li>
 * </ul>
 *
 * Detection only checks class presence without initializing anything, using the same class loader
 * ScorchedGuns itself uses, so the EMF 3 adapter class is only ever loaded when EMF 3 is installed.
 * Called on the render thread only (EMF animation + first-person hand rendering).
 */
public final class EmfAnimationContexts {

    public static final String LEGACY_CONTEXT_CLASS = "traben.entity_model_features.models.animation.EMFAnimationEntityContext";
    private static final String EMF3_STATE_CLASS = "traben.entity_model_features.models.animation.state.EMFState";
    private static final String EMF3_API_CLASS = "traben.entity_model_features.EMFAnimationApi";

    private static boolean resolved = false;
    private static EmfAnimationContext bridge;

    private EmfAnimationContexts() {
    }

    /** The bridge ScorchedGuns should use instead of its legacy reflection, or null when none is needed/possible. */
    public static EmfAnimationContext bridge() {
        if (!resolved) {
            resolved = true;
            bridge = resolve();
        }
        return bridge;
    }

    /**
     * Permanently drops the bridge after the EMF API turned out not to match what the adapter was
     * compiled against, so ScorchedGuns falls back to its own (non-crashing) legacy path instead
     * of throwing every frame.
     */
    public static void disable(Throwable cause) {
        if (bridge != null) {
            bridge = null;
            ScgunsEmfFix.LOGGER.warn("EMF 3 animation context bridge disabled - installed EMF API does not match; ScorchedGuns falls back to its non-EMF behaviour", cause);
        }
    }

    private static EmfAnimationContext resolve() {
        if (isClassPresent(LEGACY_CONTEXT_CLASS)) {
            ScgunsEmfFix.LOGGER.info("EMF 2.x animation context found - ScorchedGuns' own EMF compat is used as-is");
            return null;
        }
        if (isClassPresent(EMF3_STATE_CLASS) && isClassPresent(EMF3_API_CLASS)) {
            try {
                EmfAnimationContext context = new Emf3AnimationContext();
                ScgunsEmfFix.LOGGER.info("EMF 3.x render-state API found - bridging ScorchedGuns' EMF compat to it");
                return context;
            } catch (LinkageError e) {
                ScgunsEmfFix.LOGGER.warn("EMF 3.x detected but its API could not be linked - ScorchedGuns' EMF compat stays inactive", e);
                return null;
            }
        }
        return null;
    }

    private static boolean isClassPresent(String name) {
        try {
            Class.forName(name, false, OptionalClientCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
