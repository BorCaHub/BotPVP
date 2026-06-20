package com.botpvp.commands;

import com.botpvp.BotManager;
import com.botpvp.bot.PvPBot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BotPvPCommand {

    private static final String[] DIFFICULTIES = {"easy", "medium", "hard", "nightmare"};

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

                .then(Commands.literal("info")
                    .executes(ctx -> executeInfo(ctx.getSource())))
        );
    }

    private static int executeHelp(CommandSourceStack source) {
        source.sendSystemMessage(Component.literal(
            "§6§l╔════════════════════════════════════╗\n" +
            "§6§l║      §eBotPvP Mod  §7v1.0.0           §6§l║\n" +
            "§6§l╚════════════════════════════════════╝\n" +
            "§e/botpvp spawn §7[difficulty] [name]\n" +
            "§e/botpvp spawn static §7[difficulty] [name]\n" +
            "§e/botpvp kill §7<name>  §e/botpvp killall\n" +
            "§e/botpvp list  §e/botpvp heal §7[name]  §e/botpvp info\n" +
            "§7Difficulties: §aeasy §7| §bmedium §7| §chard §7| §4nightmare"
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

    private static int executeInfo(CommandSourceStack source) {
        int total = BotManager.getInstance().getTotalBotCount();
        source.sendSystemMessage(Component.literal(
            "§6[BotPvP] §7v1.0.0 | MC 26.1.2 | Fabric | Active bots: §e" + total));
        return 1;
    }
}
