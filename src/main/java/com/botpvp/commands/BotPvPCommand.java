package com.botpvp.commands;

import com.botpvp.BotManager;
import com.botpvp.bot.PvPBot;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * BotPvPCommand - Registers all /botpvp subcommands.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  /botpvp                              → Show help                       │
 * │  /botpvp help                         → Show help                       │
 * │  /botpvp spawn [difficulty] [name]    → Spawn moving bot                │
 * │  /botpvp spawn static [difficulty] [name]  → Spawn static (no movement) │
 * │  /botpvp kill <name>                  → Remove a specific bot           │
 * │  /botpvp killall                      → Remove all your bots            │
 * │  /botpvp list                         → List your active bots           │
 * │  /botpvp heal [name]                  → Heal bot(s) to full HP          │
 * │  /botpvp info                         → Mod version info                │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * Bot names follow real Minecraft username rules:
 *   - 3-16 characters
 *   - Letters, numbers, underscores only
 *
 * Supported Minecraft: 26.1.1, 26.1.2
 */
public class BotPvPCommand {

    private static final String[] DIFFICULTIES = {"easy", "medium", "hard", "nightmare"};

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("botpvp")

                // /botpvp  or  /botpvp help
                .executes(ctx -> executeHelp(ctx.getSource()))
                .then(CommandManager.literal("help")
                    .executes(ctx -> executeHelp(ctx.getSource())))

                // ── /botpvp spawn ───────────────────────────────────────────
                .then(CommandManager.literal("spawn")

                    // /botpvp spawn  (defaults: moving, medium)
                    .executes(ctx -> executeSpawn(ctx.getSource(), "medium", null, false))

                    // /botpvp spawn static  (no movement, medium)
                    .then(CommandManager.literal("static")
                        .executes(ctx -> executeSpawn(ctx.getSource(), "medium", null, true))

                        // /botpvp spawn static <difficulty>
                        .then(CommandManager.argument("difficulty", StringArgumentType.word())
                            .suggests((ctx, b) -> { for (String d : DIFFICULTIES) b.suggest(d); return b.buildFuture(); })
                            .executes(ctx -> executeSpawn(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "difficulty"), null, true))

                            // /botpvp spawn static <difficulty> <name>
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> executeSpawn(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "difficulty"),
                                        StringArgumentType.getString(ctx, "name"), true))
                            )
                        )
                    )

                    // /botpvp spawn <difficulty>
                    .then(CommandManager.argument("difficulty", StringArgumentType.word())
                        .suggests((ctx, b) -> { for (String d : DIFFICULTIES) b.suggest(d); return b.buildFuture(); })
                        .executes(ctx -> executeSpawn(ctx.getSource(),
                                StringArgumentType.getString(ctx, "difficulty"), null, false))

                        // /botpvp spawn <difficulty> <name>
                        .then(CommandManager.argument("name", StringArgumentType.word())
                            .executes(ctx -> executeSpawn(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "difficulty"),
                                    StringArgumentType.getString(ctx, "name"), false))
                        )
                    )
                )

                // ── /botpvp kill <name> ─────────────────────────────────────
                .then(CommandManager.literal("kill")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> executeKill(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))

                // ── /botpvp killall ─────────────────────────────────────────
                .then(CommandManager.literal("killall")
                    .executes(ctx -> executeKillAll(ctx.getSource())))

                // ── /botpvp list ────────────────────────────────────────────
                .then(CommandManager.literal("list")
                    .executes(ctx -> executeList(ctx.getSource())))

                // ── /botpvp heal [name] ─────────────────────────────────────
                .then(CommandManager.literal("heal")
                    .executes(ctx -> executeHealAll(ctx.getSource()))
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> executeHeal(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name")))))

                // ── /botpvp info ────────────────────────────────────────────
                .then(CommandManager.literal("info")
                    .executes(ctx -> executeInfo(ctx.getSource())))
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Handlers
    // ─────────────────────────────────────────────────────────────────────────

    private static int executeHelp(ServerCommandSource source) {
        source.sendMessage(Text.literal(
            "§6§l╔════════════════════════════════════╗\n" +
            "§6§l║      §eBotPvP Mod  §7v1.0.0           §6§l║\n" +
            "§6§l╚════════════════════════════════════╝\n" +
            "§e/botpvp spawn §7[difficulty] [name]\n" +
            "  §7Spawn a §bmoving §7bot that chases you.\n" +
            "§e/botpvp spawn static §7[difficulty] [name]\n" +
            "  §7Spawn a §7static §7bot (no movement, stands still).\n" +
            "§e/botpvp kill §7<name>  §8— §7Remove a specific bot\n" +
            "§e/botpvp killall        §8— §7Remove all your bots\n" +
            "§e/botpvp list           §8— §7List all your active bots\n" +
            "§e/botpvp heal §7[name]  §8— §7Heal bot (empty = heal all)\n" +
            "§e/botpvp info           §8— §7Mod version & stats\n" +
            "§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "§7Difficulties: §aeasy §7| §bmedium §7| §chard §7| §4nightmare\n" +
            "§7Name rules: §f3-16 chars, letters/numbers/underscore\n" +
            "§7Max bots per player: §e5\n" +
            "§7Minecraft versions: §a26.1.1 §7& §a26.1.2"
        ));
        return 1;
    }

    private static int executeSpawn(ServerCommandSource source, String difficulty,
                                    String name, boolean staticMode) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("§c[BotPvP] This command can only be used by a player!"));
            return 0;
        }

        // Validate difficulty
        boolean validDiff = false;
        for (String d : DIFFICULTIES) if (d.equals(difficulty.toLowerCase())) { validDiff = true; break; }
        if (!validDiff) {
            source.sendError(Text.literal(
                "§c[BotPvP] Invalid difficulty! Use: §eeasy§c, §emedium§c, §ehard§c, §enightmare"));
            return 0;
        }

        // Validate name (if provided)
        if (name != null && !name.isEmpty()) {
            String err = PvPBot.validateName(name);
            if (err != null) {
                source.sendError(Text.literal("§c[BotPvP] §f" + err));
                return 0;
            }
        }

        ServerPlayerEntity player = source.getPlayer();
        ServerWorld world = source.getWorld();
        var bot = BotManager.getInstance().spawnBot(player, world, difficulty, name, staticMode);
        return bot != null ? 1 : 0;
    }

    private static int executeKill(ServerCommandSource source, String botName) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("§c[BotPvP] This command can only be used by a player!"));
            return 0;
        }
        return BotManager.getInstance().killBot(source.getPlayer(), botName) ? 1 : 0;
    }

    private static int executeKillAll(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("§c[BotPvP] This command can only be used by a player!"));
            return 0;
        }
        BotManager.getInstance().killAllBots(source.getPlayer());
        return 1;
    }

    private static int executeList(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("§c[BotPvP] This command can only be used by a player!"));
            return 0;
        }
        BotManager.getInstance().listBots(source.getPlayer());
        return 1;
    }

    private static int executeHeal(ServerCommandSource source, String botName) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("§c[BotPvP] This command can only be used by a player!"));
            return 0;
        }
        return BotManager.getInstance().healBot(source.getPlayer(), botName) ? 1 : 0;
    }

    private static int executeHealAll(ServerCommandSource source) {
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("§c[BotPvP] This command can only be used by a player!"));
            return 0;
        }
        BotManager.getInstance().healAllBots(source.getPlayer());
        return 1;
    }

    private static int executeInfo(ServerCommandSource source) {
        int total = BotManager.getInstance().getTotalBotCount();
        source.sendMessage(Text.literal(
            "§6§l╔════════════════════════════════╗\n" +
            "§6§l║        §eBotPvP Mod Info          §6§l║\n" +
            "§6§l╚════════════════════════════════╝\n" +
            "§7Mod Version:      §av1.0.0\n" +
            "§7Minecraft:        §a26.1.1 §7& §a26.1.2\n" +
            "§7Platform:         §bFabric\n" +
            "§7Java:             §a25+\n" +
            "§7Total Active Bots: §e" + total + "\n" +
            "§7Inspired by:      §bCarpet PvP Practice\n" +
            "§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        ));
        return 1;
    }
}
