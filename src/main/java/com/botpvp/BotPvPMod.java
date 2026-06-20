package com.botpvp;

import com.botpvp.commands.BotPvPCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotPvPMod implements ModInitializer {

    public static final String MOD_ID = "botpvp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[BotPvP] Loading BotPvP v1.0.0");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                BotPvPCommand.register(dispatcher));

        ServerTickEvents.END_SERVER_TICK.register(BotPvPMod::onServerTick);

        LOGGER.info("[BotPvP] Mod loaded! Use /botpvp help in-game.");
    }

    private static void onServerTick(MinecraftServer server) {
        BotManager.getInstance().tickBots(server);
    }
}
