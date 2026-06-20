package com.botpvp.commands;

import com.botpvp.BotManager;
import com.botpvp.bot.PvPBot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

public class BotPvPCommand {

    private static final String[] DIFFICULTIES = {"easy", "medium", "hard", "nightmare"};
    private static final String[] SLOTS = {"mainhand", "offhand", "head", "chest", "legs", "feet"};

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("botpvp")
                .executes(ctx -> executeHelp(ctx.getSource()))
                .then(Commands.literal("help")
                    .executes(ctx -> executeHelp(ctx.getSource())))

                .then(Commands.literal("spawn")
                    .executes(ctx -> executeSpawn(ctx.getSource(), "medium", null, false))
                    .then(Commands.literal("static")
                        .executes(ctx -> executeSpawn(ctx.getSource(), "medium", null, true))
                        .then(Commands.argument("difficulty", StringArgumentType.word())
                            .suggests((ctx, b) -> { for (String d : DIFFICULTIES) b.suggest(d); return b.buildFuture(); })
                            .executes(ctx -> executeSpawn(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "difficulty"), null, true))
                            .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> executeSpawn(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "difficulty"),
                                        StringArgumentType.getString(ctx, "name"), true)))))
                    .then(Commands.argument("difficulty", StringArgumentType.word())
                        .suggests((ctx, b) -> { for (String d : DIFFICULTIES) b.suggest(d); return b.buildFuture(); })
                        .executes(ctx -> executeSpawn(ctx.getSource(),
                                StringArgumentType.getString(ctx, "difficulty"), null, false))
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(ctx -> executeSpawn(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "difficulty"),
                                    StringArgumentType.getString(ctx, "name"), false)))))

                .then(Commands.literal("kill")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> executeKill(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))

                .then(Commands.literal("killall")
                    .executes(ctx -> executeKillAll(ctx.getSource())))

                .then(Commands.literal("list")
                    .executes(ctx -> executeList(ctx.getSource())))

                .then(Commands.literal("heal")
                    .executes(ctx -> executeHealAll(ctx.getSource()))
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> executeHeal(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))

                .then(Commands.literal("additems")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("slot", StringArgumentType.word())
                            .suggests((ctx, b) -> { for (String s : SLOTS) b.suggest(s); return b.buildFuture(); })
                            .then(Commands.argument("item", StringArgumentType.word())
                                .executes(ctx -> executeAddItem(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "name"),
                                        StringArgumentType.getString(ctx, "slot"),
                                        StringArgumentType.getString(ctx, "item")))))))

                .then(Commands.literal("setitems")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.literal("toplayer")
                            .executes(ctx -> executeSetItems(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "name"), PvPBot.ItemPose.TO_PLAYER, 0, 0)))
                        .then(Commands.literal("up")
                            .executes(ctx -> executeSetItems(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "name"), PvPBot.ItemPose.UP, 0, 0)))
                        .then(Commands.literal("custom")
                            .then(Commands.argument("yaw", FloatArgumentType.floatArg(-180, 180))
                                .then(Commands.argument("pitch", FloatArgumentType.floatArg(-90, 90))
                                    .executes(ctx -> executeSetItems(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "name"), PvPBot.ItemPose.CUSTOM,
                                            FloatArgumentType.getFloat(ctx, "yaw"),
                                            FloatArgumentType.getFloat(ctx, "pitch"))))))))

                .then(Commands.literal("unlimitedhealth")
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> executeUnlimitedHealth(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))

                .then(Commands.literal("info")
                    .executes(ctx -> executeInfo(ctx.getSource())))
        );
    }

    private static int executeHelp(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal(
            "§6§l╔══════════════════════════════╗\n" +
            "§6§l║   §eBotPvP Mod  §7v1.0.0        §6§l║\n" +
            "§6§l╚══════════════════════════════╝\n" +
            "§e/botpvp spawn §7[difficulty] [name]\n" +
            "§e/botpvp spawn static §7[difficulty] [name]\n" +
            "§e/botpvp kill §7<name>\n" +
            "§e/botpvp killall\n" +
            "§e/botpvp list\n" +
            "§e/botpvp heal §7[name]\n" +
            "§e/botpvp additems §7<bot> <slot> <item>\n" +
            "§e/botpvp setitems §7<bot> <toplayer|up|custom yaw pitch>\n" +
            "§e/botpvp unlimitedhealth §7<bot>\n" +
            "§e/botpvp info\n" +
            "§7Difficulties: §aeasy §7| §bmedium §7| §chard §7| §4nightmare\n" +
            "§7Slots: mainhand, offhand, head, chest, legs, feet"
        ));
        return 1;
    }

    private static int executeSpawn(CommandSourceStack source, String difficulty,
                                    String name, boolean staticMode) {
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("§c[BotPvP] Player only!"));
            return 0;
        }
        boolean validDiff = false;
        for (String d : DIFFICULTIES) if (d.equals(difficulty.toLowerCase())) { validDiff = true; break; }
        if (!validDiff) {
            source.sendFailure(Component.literal("§c[BotPvP] Invalid difficulty!"));
            return 0;
        }
        if (name != null && !name.isEmpty()) {
            String err = PvPBot.validateName(name);
            if (err != null) { source.sendFailure(Component.literal("§c[BotPvP] §f" + err)); return 0; }
        }
        ServerPlayer player = source.getPlayer();
        ServerLevel world = source.getLevel();
        var bot = BotManager.getInstance().spawnBot(player, world, difficulty, name, staticMode);
        return bot != null ? 1 : 0;
    }

    private static int executeKill(CommandSourceStack source, String botName) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        return BotManager.getInstance().killBot(source.getPlayer(), botName) ? 1 : 0;
    }

    private static int executeKillAll(CommandSourceStack source) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        BotManager.getInstance().killAllBots(source.getPlayer());
        return 1;
    }

    private static int executeList(CommandSourceStack source) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        BotManager.getInstance().listBots(source.getPlayer());
        return 1;
    }

    private static int executeHeal(CommandSourceStack source, String botName) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        return BotManager.getInstance().healBot(source.getPlayer(), botName) ? 1 : 0;
    }

    private static int executeHealAll(CommandSourceStack source) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        BotManager.getInstance().healAllBots(source.getPlayer());
        return 1;
    }

    private static int executeAddItem(CommandSourceStack source, String botName, String slotName, String itemName) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(slotName.toUpperCase());
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§c[BotPvP] Invalid slot! Use: mainhand, offhand, head, chest, legs, feet"));
            return 0;
        }
        String key = itemName.contains(":") ? itemName.substring(itemName.indexOf(':') + 1) : itemName;
        Item item = BuiltInRegistries.ITEM.stream()
                .filter(i -> BuiltInRegistries.ITEM.getKey(i).getPath().equalsIgnoreCase(key))
                .findFirst().orElse(null);
        if (item == null) {
            source.sendFailure(Component.literal("§c[BotPvP] Unknown item: §e" + itemName));
            return 0;
        }
        boolean ok = BotManager.getInstance().addItemToBot(source.getPlayer(), botName, slot, item);
        if (ok) source.sendSystemMessage(Component.literal(
                "§a[BotPvP] §fGave §e" + itemName + " §fto §e" + botName + " §7(" + slotName + ")"));
        return ok ? 1 : 0;
    }

    private static int executeSetItems(CommandSourceStack source, String botName, PvPBot.ItemPose pose, float yaw, float pitch) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        boolean ok = BotManager.getInstance().setBotItemPose(source.getPlayer(), botName, pose, yaw, pitch);
        if (ok) source.sendSystemMessage(Component.literal("§a[BotPvP] §fBot §e" + botName + " §fpose updated."));
        return ok ? 1 : 0;
    }

    private static int executeUnlimitedHealth(CommandSourceStack source, String botName) {
        if (!source.isPlayer()) { source.sendFailure(Component.literal("§c[BotPvP] Player only!")); return 0; }
        return BotManager.getInstance().toggleUnlimitedHealth(source.getPlayer(), botName) ? 1 : 0;
    }

    private static int executeInfo(CommandSourceStack source) {
        int total = BotManager.getInstance().getTotalBotCount();
        source.sendSystemMessage(Component.literal(
            "§6[BotPvP] §7v1.0.0 | MC 26.1.2 | Fabric | Active bots: §e" + total));
        return 1;
    }
}
