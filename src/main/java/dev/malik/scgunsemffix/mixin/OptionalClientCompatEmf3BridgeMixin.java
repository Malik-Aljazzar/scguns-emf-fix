package dev.malik.scgunsemffix.mixin;

import dev.malik.scgunsemffix.emf.EmfAnimationContext;
import dev.malik.scgunsemffix.emf.EmfAnimationContexts;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.ribs.scguns.client.compat.OptionalClientCompat;

/**
 * Makes ScorchedGuns 1.5's EMF compat work on EMF 3.x - see specification/SPEC.md.
 *
 * ScorchedGuns gates its whole EMF compat (clean first-person gun arms, third-person gun pose
 * re-applied after Fresh Animations, the {@code is_scguns_gun_held} EMF variable) on
 * {@code isEntityModelFeaturesLoaded()}, which requires EMF 2.x's
 * {@code EMFAnimationEntityContext}. EMF 3.x removed that class, so the compat silently switched
 * off and Fresh Animations' first-person arm animation was applied to the gun-holding arms.
 *
 * Fix: when {@link EmfAnimationContexts#bridge()} supplies a bridge (EMF 3.x only), count the
 * legacy context class as present and answer the three context reads from the bridge. With EMF
 * 2.x or no EMF the bridge is null and every original instruction runs untouched.
 */
@Mixin(OptionalClientCompat.class)
public abstract class OptionalClientCompatEmf3BridgeMixin {

    @Shadow(remap = false)
    private static boolean isClassPresent(String className) {
        throw new AssertionError();
    }

    @Redirect(
            method = "isEntityModelFeaturesLoaded()Z",
            at = @At(value = "INVOKE", target = "Ltop/ribs/scguns/client/compat/OptionalClientCompat;isClassPresent(Ljava/lang/String;)Z"),
            remap = false
    )
    private static boolean dev$isClassPresentOrBridged(String className) {
        if (isClassPresent(className)) {
            return true;
        }
        return EmfAnimationContexts.LEGACY_CONTEXT_CLASS.equals(className) && EmfAnimationContexts.bridge() != null;
    }

    @Inject(method = "isEntityModelFeaturesFirstPersonHand()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dev$bridgedFirstPersonHand(CallbackInfoReturnable<Boolean> cir) {
        EmfAnimationContext bridge = EmfAnimationContexts.bridge();
        if (bridge != null) {
            cir.setReturnValue(bridge.isFirstPersonHand());
        }
    }

    @Inject(method = "getEntityModelFeaturesContextEntity()Ljava/lang/Object;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dev$bridgedContextEntity(CallbackInfoReturnable<Object> cir) {
        EmfAnimationContext bridge = EmfAnimationContexts.bridge();
        if (bridge != null) {
            cir.setReturnValue(bridge.currentEntity());
        }
    }

    // Mirrors the original's logic exactly: the context entity's own gun if it's a player,
    // otherwise (non-player entity or no context) the local player's.
    @Inject(method = "isScorchedGunHeldByEntityModelFeaturesContext()Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dev$bridgedGunHeldByContext(CallbackInfoReturnable<Boolean> cir) {
        EmfAnimationContext bridge = EmfAnimationContexts.bridge();
        if (bridge != null) {
            cir.setReturnValue(bridge.currentEntity() instanceof Player player
                    ? OptionalClientCompat.isScorchedGunHeldByPlayer(player)
                    : OptionalClientCompat.isScorchedGunHeldByLocalPlayer());
        }
    }
}
