package com.botpvp.mixin;

import com.botpvp.BotManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "die", at = @At("TAIL"))
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        int total = BotManager.getInstance().getTotalBotCount();
        if (total > 0) {
            player.sendSystemMessage(Component.literal(
                "§e[BotPvP] §fYour bots are still active! Use §e/botpvp killall §fto remove them."));
        }
    }
}
