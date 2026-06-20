package com.botpvp;

import com.botpvp.bot.PvPBot;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.*;

public class BotManager {

    private static BotManager instance;
    private final Map<UUID, PvPBot> activeBots = new HashMap<>();
    private final Map<UUID, List<UUID>> playerBots = new HashMap<>();

    private BotManager() {}

    public static BotManager getInstance() {
        if (instance == null) instance = new BotManager();
        return instance;
    }

    public PvPBot spawnBot(ServerPlayer player, ServerLevel world,
                           String difficulty, String botName, boolean staticMode) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());

        if (botName != null && !botName.isEmpty()) {
            String error = PvPBot.validateName(botName);
            if (error != null) {
                player.sendSystemMessage(Component.literal("§c[BotPvP] §fInvalid bot name: §e" + error));
                return null;
            }
        }

        String finalName = (botName != null && !botName.isEmpty()) ? botName : generateBotName(difficulty);
        PvPBot bot = new PvPBot(player, world, difficulty, finalName, staticMode);

        if (bot.spawn()) {
            UUID botId = bot.getUuid();
            activeBots.put(botId, bot);
            bots.add(botId);
            playerBots.put(player.getUUID(), bots);
            String modeLabel = staticMode ? "§7(static)" : "§b(moving)";
            player.sendSystemMessage(Component.literal(
                "§a[BotPvP] §fSpawned bot §e" + finalName + " §7[§b" + difficulty + "§7] " + modeLabel));
            return bot;
        } else {
            player.sendSystemMessage(Component.literal("§c[BotPvP] §fFailed to spawn bot."));
            return null;
        }
    }

    public boolean killBot(ServerPlayer player, String botName) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());
        for (UUID botId : new ArrayList<>(bots)) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null && bot.getName().equalsIgnoreCase(botName)) {
                bot.remove();
                activeBots.remove(botId);
                bots.remove(botId);
                player.sendSystemMessage(Component.literal("§a[BotPvP] §fBot §e" + botName + " §fremoved."));
                return true;
            }
        }
        player.sendSystemMessage(Component.literal("§c[BotPvP] §fNo bot named §e" + botName + " §ffound."));
        return false;
    }

    public int killAllBots(ServerPlayer player) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());
        int count = bots.size();
        for (UUID botId : new ArrayList<>(bots)) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null) { bot.remove(); activeBots.remove(botId); }
        }
        bots.clear();
        playerBots.put(player.getUUID(), bots);
        if (count > 0)
            player.sendSystemMessage(Component.literal("§a[BotPvP] §fRemoved all §e" + count + " §fbot(s)."));
        else
            player.sendSystemMessage(Component.literal("§e[BotPvP] §fYou have no active bots."));
        return count;
    }

    public void listBots(ServerPlayer player) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());
        if (bots.isEmpty()) {
            player.sendSystemMessage(Component.literal("§e[BotPvP] §fYou have no active bots."));
            return;
        }
        player.sendSystemMessage(Component.literal("§6[BotPvP] §fYour active bots §e(" + bots.size() + ")§f:"));
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null) {
                String status = bot.isAlive() ? "§aAlive" : "§cDead";
                String mode = bot.isStatic() ? "§7Static" : "§bMoving";
                player.sendSystemMessage(Component.literal(
                    "  §7» §e" + bot.getName() + " §7| §b" + bot.getDifficulty() + " §7| " + mode + " §7| " + status));
            }
        }
    }

    public boolean healBot(ServerPlayer player, String botName) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null && bot.getName().equalsIgnoreCase(botName)) {
                bot.respawn();
                return true;
            }
        }
        player.sendSystemMessage(Component.literal("§c[BotPvP] §fNo bot named §e" + botName + " §ffound."));
        return false;
    }

    public void healAllBots(ServerPlayer player) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());
        if (bots.isEmpty()) {
            player.sendSystemMessage(Component.literal("§e[BotPvP] §fYou have no active bots to heal."));
            return;
        }
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null) bot.respawn();
        }
        player.sendSystemMessage(Component.literal("§a[BotPvP] §fAll bots have been healed!"));
    }

    public PvPBot findBot(ServerPlayer player, String botName) {
        List<UUID> bots = playerBots.getOrDefault(player.getUUID(), new ArrayList<>());
        for (UUID botId : bots) {
            PvPBot bot = activeBots.get(botId);
            if (bot != null && bot.getName().equalsIgnoreCase(botName)) return bot;
        }
        return null;
    }

    public boolean addItemToBot(ServerPlayer player, String botName, EquipmentSlot slot, Item item) {
        PvPBot bot = findBot(player, botName);
        if (bot == null) {
            player.sendSystemMessage(Component.literal("§c[BotPvP] §fNo bot named §e" + botName + " §ffound."));
            return false;
        }
        bot.addItem(slot, item);
        return true;
    }

    public boolean setBotItemPose(ServerPlayer player, String botName, PvPBot.ItemPose pose, float yaw, float pitch) {
        PvPBot bot = findBot(player, botName);
        if (bot == null) {
            player.sendSystemMessage(Component.literal("§c[BotPvP] §fNo bot named §e" + botName + " §ffound."));
            return false;
        }
        bot.setItemPose(pose, yaw, pitch);
        return true;
    }

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
