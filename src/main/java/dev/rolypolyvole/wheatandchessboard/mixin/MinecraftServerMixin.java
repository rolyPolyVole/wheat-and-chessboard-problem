package dev.rolypolyvole.wheatandchessboard.mixin;

import dev.rolypolyvole.wheatandchessboard.challenge.Challenge;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    // Fabric's tick events do not fire while a dedicated server is paused for being empty, the head of tickServer does
    @Inject(method = "tickServer", at = @At("HEAD"))
    private void tickChallenge(BooleanSupplier haveTime, CallbackInfo ci) {
        Challenge.INSTANCE.tick((MinecraftServer) (Object) this);
    }
}
