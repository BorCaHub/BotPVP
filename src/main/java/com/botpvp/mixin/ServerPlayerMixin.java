package com.botpvp.mixin;

import com.botpvp.BotManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ServerPlayerMixin - Intercepts player death to notify about bots.
 *
 * When a player dies, their bots stay active. The player is reminded
 * to use /botpvp killall if they want to clean them up.
 *
 * Compatible with Minecraft 26.1.1 and 26.1.2
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin {

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        int total = BotManager.getInstance().getTotalBotCount();
        if (total > 0) {
            player.sendMessage(Text.literal(
                "§e[BotPvP] §fYour bots are still active! Use §e/botpvp killall §fto remove them."));
        }
    }
}
