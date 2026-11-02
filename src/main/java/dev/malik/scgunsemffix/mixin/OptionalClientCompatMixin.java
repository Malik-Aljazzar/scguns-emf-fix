package dev.malik.scgunsemffix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.ribs.scguns.client.compat.OptionalClientCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Caches ScorchedGuns' EMF (Entity Model Features) reflective lookups, which otherwise run fully
 * uncached on every call - see specification/SPEC.md for the verified root cause.
 *
 * Three methods on the mixed-in class each resolve the same {@code EMFAnimationEntityContext}
 * class (plus a Field or Method on it) via {@code Class.forName(...)} from scratch on every call,
 * instead of caching like the class's own {@code isEntityModelFeaturesLoaded()} already does for
 * a similar "is EMF present" check. {@code isEntityModelFeaturesFirstPersonHand()} in particular
 * runs on EMF's generic per-model-part animate hook - every body part of every EMF-animated
 * entity, every frame - confirmed via spark profiler (Render thread) correlating severe FPS drops
 * (60 -> 10) with villager-crowd density.
 *
 * Fix approach: redirect only the three expensive reflective calls (Class.forName, Class.getField,
 * Class.getMethod) to cached resolutions, leaving every other instruction - the instanceof check,
 * the invoke, the exception table, the non-EMF fallback paths - in all three original methods
 * completely untouched. A failed lookup (the class/member genuinely absent, e.g. EMF not
 * installed at all) is cached too by re-throwing the same cached exception instance, so a
 * permanently-missing dependency doesn't retry the expensive failing lookup every frame either -
 * this preserves the original's exact exception-driven fallback behaviour, just without redoing
 * the reflection work after the first call.
 */
@Mixin(OptionalClientCompat.class)
public abstract class OptionalClientCompatMixin {

    private static boolean dev$classResolved = false;
    private static Class<?> dev$classCache;
    private static ClassNotFoundException dev$classNotFoundCache;

    private static boolean dev$fieldResolved = false;
    private static Field dev$fieldCache;
    private static NoSuchFieldException dev$fieldNotFoundCache;

    private static boolean dev$methodResolved = false;
    private static Method dev$methodCache;
    private static NoSuchMethodException dev$methodNotFoundCache;

    @Redirect(
            method = {
                    "isEntityModelFeaturesFirstPersonHand()Z",
                    "getEntityModelFeaturesContextEntity()Ljava/lang/Object;",
                    "isScorchedGunHeldByEntityModelFeaturesContext()Z"
            },
            at = @At(value = "INVOKE", target = "Ljava/lang/Class;forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;"),
            remap = false
    )
    private static Class<?> dev$cachedForName(String name, boolean initialize, ClassLoader loader) throws ClassNotFoundException {
        if (!dev$classResolved) {
            dev$classResolved = true;
            try {
                dev$classCache = Class.forName(name, initialize, loader);
            } catch (ClassNotFoundException e) {
                dev$classNotFoundCache = e;
            }
        }
        if (dev$classCache != null) {
            return dev$classCache;
        }
        throw dev$classNotFoundCache;
    }

    @Redirect(
            method = "isEntityModelFeaturesFirstPersonHand()Z",
            at = @At(value = "INVOKE", target = "Ljava/lang/Class;getField(Ljava/lang/String;)Ljava/lang/reflect/Field;"),
            remap = false
    )
    private static Field dev$cachedGetField(Class<?> clazz, String name) throws NoSuchFieldException {
        if (!dev$fieldResolved) {
            dev$fieldResolved = true;
            try {
                dev$fieldCache = clazz.getField(name);
            } catch (NoSuchFieldException e) {
                dev$fieldNotFoundCache = e;
            }
        }
        if (dev$fieldCache != null) {
            return dev$fieldCache;
        }
        throw dev$fieldNotFoundCache;
    }

    @Redirect(
            method = {
                    "getEntityModelFeaturesContextEntity()Ljava/lang/Object;",
                    "isScorchedGunHeldByEntityModelFeaturesContext()Z"
            },
            at = @At(value = "INVOKE", target = "Ljava/lang/Class;getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;"),
            remap = false
    )
    private static Method dev$cachedGetMethod(Class<?> clazz, String name, Class<?>[] paramTypes) throws NoSuchMethodException {
        if (!dev$methodResolved) {
            dev$methodResolved = true;
            try {
                dev$methodCache = clazz.getMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                dev$methodNotFoundCache = e;
            }
        }
        if (dev$methodCache != null) {
            return dev$methodCache;
        }
        throw dev$methodNotFoundCache;
    }
}
