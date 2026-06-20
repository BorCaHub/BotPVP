package com.botpvp;

import com.botpvp.bot.PvPBot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;

/**
 * BotManager - Singleton manager for all active PvP bots.
 *
 * Handles spawning, removing, listing, healing, and ticking all bots
 * across different players and worlds.
 *
 * Supported Minecraft versions: 26.1.1, 26.1.2
 */
public class BotManager {

    private static BotManager instance;

    /** Map from bot UUID to PvPBot. */
    private final Map<UUID, PvPBot> activeBots = new HashMap<>();

    /** Map from player UUID to list of their bot UUIDs. */
    private final Map<UUID, List<UUID>> playerBots = new HashMap<>();

    private BotManager() {}

    public static BotManager getInstance() {
        if (instance == null) instance = new BotManager();
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Spawn
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Spawn a new PvP bot.
     *
     * @param player      The player requesting the bot
     * @param world       Target world
     * @param difficulty  easy / medium / hard / nightmare
     * @param botName     Custom name (must pass Minecraft username rules), or null to auto-generate
     * @param staticMode  true = no movement; false = normal chasing AI
     */
    public PvPBot spawnBot(ServerPlayerEntity player, ServerWorld world,
                           String difficulty, String botName, boolean staticMode) {

        // Enforce per-player bot limit
        List<UUID> bots = playerBots.getOrDefault(player.getUuid(), new ArrayList<>());
        if (bots.size() >= 5) {
            player.sendMessage(Text.literal("§c[BotPvP] §fYou have reached the maximum of 5 bots!"));
            return null;
        }

        // Validate name against Minecraft username rules
        if (botName != null && !botName.isEmpty()) {
            String error = PvPBot.validateName(botName);
            if (error != null) {
                player.sendMessage(Text.literal("§c[BotPvP] §fInvalid bot name: §e" + error));
                return null;
            }
        }

        String finalName = (botName != null && !botName.isEmpty())
                ? botName
                : generateBotName(difficulty);

        PvPBot bot = new PvPBot(player, world, difficulty, finalName, staticMode);

        if (bot.spawn()) {
            UUID botId = bot.getUuid();
            activeBots.put(botId, bot);
            bots.add(botId);
            playerBots.put(player.getUuid(), bots);

            String modeLabel = staticMode ? "§7(static)" : "§b(moving)";
            player.sendMessage(Text.literal(
                "§a[BotPvP] §fSpawned bot §e" + finalName +
                " §7[§b" + difficulty + "§7] " + modeLabel));
            return bot;
        } else {
            player.sendMessage(Text.literal("§c[BotPvP] §fFailed to spawn bot. Please try again."));
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kill
    // ─────────────────────────────────────────────────────────────────────────

    public boolean killBot(ServerPlayerEntity player, String botName) {
        List<UUID> bots = playerBots.getOrDefault(player.getUuid(), new ArrayList<>());
        for (UUID botId : new ArrayList<>(bots)) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null && bot.getName().equalsIgnoreCase(botName)) {
                bot.remove();
                activeBots.remove(botId);
                bots.remove(botId);
                player.sendMessage(Text.literal("§a[BotPvP] §fBot §e" + botName + " §fremoved."));
                return true;
            }
        }
        player.sendMessage(Text.literal("§c[BotPvP] §fNo bot named §e" + botName + " §ffound."));
        return false;
    }

    public int killAllBots(ServerPlayerEntity player) {
        List<UUID> bots = playerBots.getOrDefault(player.getUuid(), new ArrayList<>());
        int count = bots.size();
        for (UUID botId : new ArrayList<>(bots)) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null) { bot.remove(); activeBots.remove(botId); }
        }
        bots.clear();
        playerBots.put(player.getUuid(), bots);
        if (count > 0)
            player.sendMessage(Text.literal("§a[BotPvP] §fRemoved all §e" + count + " §fbot(s)."));
        else
            player.sendMessage(Text.literal("§e[BotPvP] §fYou have no active bots."));
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // List
    // ─────────────────────────────────────────────────────────────────────────

    public void listBots(ServerPlayerEntity player) {
        List<UUID> bots = playerBots.getOrDefault(player.getUuid(), new ArrayList<>());
        if (bots.isEmpty()) {
            player.sendMessage(Text.literal("§e[BotPvP] §fYou have no active bots."));
            return;
        }
        player.sendMessage(Text.literal("§6[BotPvP] §fYour active bots §e(" + bots.size() + "/5)§f:"));
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null) {
                String status = bot.isAlive() ? "§aAlive" : "§cDead";
                String mode   = bot.isStatic() ? "§7Static" : "§bMoving";
                player.sendMessage(Text.literal(
                    "  §7» §e" + bot.getName() +
                    " §7| Difficulty: §b" + bot.getDifficulty() +
                    " §7| Mode: " + mode +
                    " §7| Status: " + status));
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Heal
    // ─────────────────────────────────────────────────────────────────────────

    public boolean healBot(ServerPlayerEntity player, String botName) {
        List<UUID> bots = playerBots.getOrDefault(player.getUuid(), new ArrayList<>());
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null && bot.getName().equalsIgnoreCase(botName)) {
                bot.respawn();
                return true;
            }
        }
        player.sendMessage(Text.literal("§c[BotPvP] §fNo bot named §e" + botName + " §ffound."));
        return false;
    }

    public void healAllBots(ServerPlayerEntity player) {
        List<UUID> bots = playerBots.getOrDefault(player.getUuid(), new ArrayList<>());
        if (bots.isEmpty()) {
            player.sendMessage(Text.literal("§e[BotPvP] §fYou have no active bots to heal."));
            return;
        }
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null) bot.respawn();
        }
        player.sendMessage(Text.literal("§a[BotPvP] §fAll bots have been healed!"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tick
    // ─────────────────────────────────────────────────────────────────────────

    public void tickBots(MinecraftServer server) {
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, PvPBot> entry : activeBots.entrySet()) {
            try {
                entry.getValue().tick();
            } catch (Exception e) {
                BotPvPMod.LOGGER.error("[BotPvP] Error ticking bot {}", entry.getValue().getName(), e);
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(activeBots::remove);
    }

    public int getTotalBotCount() { return activeBots.size(); }

    public void reset() { activeBots.clear(); playerBots.clear(); }

    // ─────────────────────────────────────────────────────────────────────────
    // Name generation (valid Minecraft usernames, 3-16 chars, a-zA-Z0-9_)
    // ─────────────────────────────────────────────────────────────────────────

    private String generateBotName(String difficulty) {
        String[] easy      = {"Noob_Bot", "Baby_Bot", "Starter", "NewbieXD"};
        String[] medium    = {"Fighter", "Warrior", "BladeBot", "Storm_PvP"};
        String[] hard      = {"Elite_PvP", "Savage_Bot", "Demon_PvP", "Fury"};
        String[] nightmare = {"Shadow_GG", "GodBot", "Overlord", "NightBot"};

        String[] pool = switch (difficulty.toLowerCase()) {
            case "easy"      -> easy;
            case "medium"    -> medium;
            case "hard"      -> hard;
            case "nightmare" -> nightmare;
            default          -> medium;
        };
        return pool[(int) (Math.random() * pool.length)];
    }
}
