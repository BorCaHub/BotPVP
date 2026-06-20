package com.botpvp;

import com.botpvp.commands.BotPvPCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BotPvPMod - Main mod entry point.
 *
 * A Fabric mod for practicing PvP against AI bots using /botpvp.
 * Supports Minecraft 26.1.1 and 26.1.2.
 *
 * Features:
 *   - Moving bots (chase + attack)
 *   - Static bots (stand still, attack in range)
 *   - 4 difficulty levels: easy, medium, hard, nightmare
 *   - Bot names validated against Minecraft username rules
 *   - Mod icon visible in the Mods screen
 */
public class BotPvPMod implements ModInitializer {

    public static final String MOD_ID = "botpvp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[BotPvP] Loading BotPvP v1.0.0 for Minecraft 26.1.1 / 26.1.2");

        // Register /botpvp and all subcommands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                BotPvPCommand.register(dispatcher));

        // Tick all active bots every server tick
        ServerTickEvents.END_SERVER_TICK.register(BotPvPMod::onServerTick);

        LOGGER.info("[BotPvP] Mod loaded successfully! Use /botpvp help in-game.");
    }

    private static void onServerTick(MinecraftServer server) {
        BotManager.getInstance().tickBots(server);
    }
}
