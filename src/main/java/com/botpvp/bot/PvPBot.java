package com.botpvp.bot;

import com.botpvp.BotPvPMod;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * PvPBot - Represents a single PvP practice bot.
 *
 * Modes:
 *   - Normal (moving): Bot walks toward and attacks the player.
 *   - Static (no movement): Bot stands in place and attacks if player is in range.
 *
 * Difficulty affects damage, speed, attack cooldown, and AI complexity.
 *
 * Bot names are validated against real Minecraft Java/Bedrock username rules.
 *
 * Supported Minecraft versions: 26.1.1, 26.1.2
 */
public class PvPBot {

    // ── Real Minecraft username validation ────────────────────────────────────
    // Java Edition: 3-16 chars, letters/numbers/underscore only
    // Bedrock Edition: 1-16 chars, same charset (prefixed with . on Java servers)
    private static final java.util.regex.Pattern VALID_NAME_PATTERN =
            java.util.regex.Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    /**
     * Validate that a name follows Minecraft Java/Bedrock username rules.
     * Returns null if valid, or an error message if invalid.
     */
    public static String validateName(String name) {
        if (name == null || name.isEmpty()) return null; // auto-generate is fine
        if (name.length() < 3)  return "Bot name must be at least 3 characters (Minecraft username rule).";
        if (name.length() > 16) return "Bot name must be 16 characters or fewer (Minecraft username rule).";
        if (!VALID_NAME_PATTERN.matcher(name).matches())
            return "Bot name can only contain letters, numbers, and underscores (Minecraft username rule).";
        return null; // valid
    }

    // ─────────────────────────────────────────────────────────────────────────

    private final ServerPlayerEntity owner;
    private final ServerWorld world;
    private final String difficulty;
    private final String name;
    private final UUID uuid;
    private final boolean staticMode; // true = no movement, stand in place

    private BotEntity botEntity;
    private boolean alive = false;
    private int tickCounter = 0;

    // Difficulty stats
    private float attackDamage;
    private float movementSpeed;
    private int attackCooldown;
    private int reactionDelay;
    private boolean usesCombos;
    private boolean blockHits;

    public PvPBot(ServerPlayerEntity owner, ServerWorld world,
                  String difficulty, String name, boolean staticMode) {
        this.owner      = owner;
        this.world      = world;
        this.difficulty = difficulty.toLowerCase();
        this.name       = name;
        this.uuid       = UUID.randomUUID();
        this.staticMode = staticMode;
        applyDifficultySettings();
    }

    private void applyDifficultySettings() {
        switch (difficulty) {
            case "easy" -> {
                attackDamage   = 3.0f;
                movementSpeed  = 0.15f;
                attackCooldown = 20;
                reactionDelay  = 40;
                usesCombos     = false;
                blockHits      = false;
            }
            case "medium" -> {
                attackDamage   = 5.0f;
                movementSpeed  = 0.22f;
                attackCooldown = 12;
                reactionDelay  = 20;
                usesCombos     = true;
                blockHits      = false;
            }
            case "hard" -> {
                attackDamage   = 7.0f;
                movementSpeed  = 0.28f;
                attackCooldown = 6;
                reactionDelay  = 10;
                usesCombos     = true;
                blockHits      = true;
            }
            case "nightmare" -> {
                attackDamage   = 10.0f;
                movementSpeed  = 0.35f;
                attackCooldown = 3;
                reactionDelay  = 2;
                usesCombos     = true;
                blockHits      = true;
            }
            default -> {
                attackDamage   = 5.0f;
                movementSpeed  = 0.22f;
                attackCooldown = 12;
                reactionDelay  = 20;
                usesCombos     = true;
                blockHits      = false;
            }
        }
    }

    /** Spawn the bot into the world near the owner. */
    public boolean spawn() {
        try {
            Vec3d ownerPos = owner.getPos();
            double spawnX = ownerPos.x + (Math.random() * 4 - 2);
            double spawnY = ownerPos.y;
            double spawnZ = ownerPos.z + (Math.random() * 4 - 2);

            botEntity = new BotEntity(world, this);
            botEntity.refreshPositionAndAngles(spawnX, spawnY, spawnZ, 0, 0);

            // Name tag: show mode in brackets
            String modeTag = staticMode ? "§7[Static]" : "§c[Bot]";
            botEntity.setCustomName(Text.literal(modeTag + " §f" + name));
            botEntity.setCustomNameVisible(true);

            if (botEntity.getAttributeInstance(EntityAttributes.MAX_HEALTH) != null)
                botEntity.getAttributeInstance(EntityAttributes.MAX_HEALTH).setBaseValue(20.0);
            if (botEntity.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED) != null)
                botEntity.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED)
                        .setBaseValue(staticMode ? 0.0 : movementSpeed);
            if (botEntity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null)
                botEntity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(attackDamage);

            botEntity.setHealth(20.0f);
            equipBot();
            world.spawnEntity(botEntity);
            alive = true;

            BotPvPMod.LOGGER.info("[BotPvP] Bot '{}' spawned at ({}, {}, {}) mode={}",
                    name, spawnX, spawnY, spawnZ, staticMode ? "static" : "moving");
            return true;
        } catch (Exception e) {
            BotPvPMod.LOGGER.error("[BotPvP] Failed to spawn bot '{}'", name, e);
            return false;
        }
    }

    private void equipBot() {
        switch (difficulty) {
            case "easy" -> {
                botEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
                botEntity.equipStack(EquipmentSlot.HEAD,     new ItemStack(Items.LEATHER_HELMET));
                botEntity.equipStack(EquipmentSlot.CHEST,    new ItemStack(Items.LEATHER_CHESTPLATE));
                botEntity.equipStack(EquipmentSlot.LEGS,     new ItemStack(Items.LEATHER_LEGGINGS));
                botEntity.equipStack(EquipmentSlot.FEET,     new ItemStack(Items.LEATHER_BOOTS));
            }
            case "medium" -> {
                botEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                botEntity.equipStack(EquipmentSlot.HEAD,     new ItemStack(Items.IRON_HELMET));
                botEntity.equipStack(EquipmentSlot.CHEST,    new ItemStack(Items.IRON_CHESTPLATE));
                botEntity.equipStack(EquipmentSlot.LEGS,     new ItemStack(Items.IRON_LEGGINGS));
                botEntity.equipStack(EquipmentSlot.FEET,     new ItemStack(Items.IRON_BOOTS));
            }
            case "hard" -> {
                botEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                botEntity.equipStack(EquipmentSlot.HEAD,     new ItemStack(Items.DIAMOND_HELMET));
                botEntity.equipStack(EquipmentSlot.CHEST,    new ItemStack(Items.DIAMOND_CHESTPLATE));
                botEntity.equipStack(EquipmentSlot.LEGS,     new ItemStack(Items.DIAMOND_LEGGINGS));
                botEntity.equipStack(EquipmentSlot.FEET,     new ItemStack(Items.DIAMOND_BOOTS));
            }
            case "nightmare" -> {
                botEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                botEntity.equipStack(EquipmentSlot.HEAD,     new ItemStack(Items.NETHERITE_HELMET));
                botEntity.equipStack(EquipmentSlot.CHEST,    new ItemStack(Items.NETHERITE_CHESTPLATE));
                botEntity.equipStack(EquipmentSlot.LEGS,     new ItemStack(Items.NETHERITE_LEGGINGS));
                botEntity.equipStack(EquipmentSlot.FEET,     new ItemStack(Items.NETHERITE_BOOTS));
            }
        }
    }

    /** Called every server tick to update bot AI. */
    public void tick() {
        if (!alive || botEntity == null || botEntity.isDead()) {
            if (alive) {
                alive = false;
                if (owner.isAlive()) {
                    owner.sendMessage(Text.literal(
                        "§e[BotPvP] §fBot §c" + name + " §fhas died! Use §e/botpvp spawn §fto call another."));
                }
            }
            return;
        }

        tickCounter++;
        if (tickCounter < reactionDelay) return;

        LivingEntity target = owner;
        double distanceSq = botEntity.squaredDistanceTo(target);

        // ── Static mode: just stand and attack if close ─────────────────────
        if (staticMode) {
            faceTarget(target);
            if (distanceSq <= 9 && tickCounter % attackCooldown == 0) {
                performAttack(target);
                if (blockHits) botEntity.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            }
            return;
        }

        // ── Moving mode ──────────────────────────────────────────────────────
        if (distanceSq > 400) {
            // Teleport closer if too far (>20 blocks)
            if (tickCounter % 40 == 0) {
                Vec3d pos = target.getPos();
                botEntity.teleport(pos.x + 3, pos.y, pos.z + 3);
            }
            return;
        }

        faceTarget(target);

        if (distanceSq > 9) {
            moveTowardTarget(target);
        } else {
            if (tickCounter % attackCooldown == 0) performAttack(target);
            if (blockHits && tickCounter % (attackCooldown / 2) == 0)
                botEntity.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        }

        if (usesCombos && tickCounter % (attackCooldown * 3) == 0 && distanceSq < 9)
            performComboAttack(target);
    }

    private void faceTarget(LivingEntity target) {
        Vec3d diff = target.getPos().subtract(botEntity.getPos()).normalize();
        float yaw   = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float pitch = (float) Math.toDegrees(-Math.asin(diff.y));
        botEntity.setYaw(yaw);
        botEntity.setHeadYaw(yaw);
        botEntity.setPitch(pitch);
    }

    private void moveTowardTarget(LivingEntity target) {
        Vec3d dir = target.getPos().subtract(botEntity.getPos()).normalize();
        botEntity.setVelocity(dir.x * movementSpeed * 10, botEntity.getVelocity().y, dir.z * movementSpeed * 10);
    }

    private void performAttack(LivingEntity target) {
        botEntity.swingHand(net.minecraft.util.Hand.MAIN_HAND);
        target.damage(world.getDamageSources().mobAttack(botEntity), attackDamage);
    }

    private void performComboAttack(LivingEntity target) {
        performAttack(target);
        if (target.isAlive()) performAttack(target);
    }

    /** Heal the bot to full HP (or respawn if dead). */
    public void respawn() {
        if (botEntity != null && !botEntity.isDead()) {
            botEntity.setHealth(20.0f);
            owner.sendMessage(Text.literal("§a[BotPvP] §fBot §e" + name + " §fhas been healed to full HP!"));
        } else {
            spawn();
        }
    }

    /** Remove the bot from the world. */
    public void remove() {
        if (botEntity != null) botEntity.discard();
        alive = false;
    }

    public UUID   getUuid()       { return uuid; }
    public String getName()       { return name; }
    public String getDifficulty() { return difficulty; }
    public boolean isStatic()     { return staticMode; }
    public boolean isAlive()      { return alive && botEntity != null && !botEntity.isDead(); }
    public BotEntity getEntity()  { return botEntity; }
}
