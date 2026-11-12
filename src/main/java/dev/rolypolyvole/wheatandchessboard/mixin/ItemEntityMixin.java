package dev.rolypolyvole.wheatandchessboard.mixin;

import dev.rolypolyvole.wheatandchessboard.game.Challenge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow
    private int pickupDelay;

    @Shadow
    private UUID target;

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void collectChallengeItem(Player player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (pickupDelay != 0 || (target != null && !target.equals(player.getUUID()))) return;

        ItemEntity entity = (ItemEntity) (Object) this;
        Challenge.INSTANCE.onPickup(serverPlayer, entity);

        if (entity.isRemoved()) ci.cancel();
    }
}
