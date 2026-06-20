package com.botpvp.bot;

import com.botpvp.BotPvPMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import java.util.regex.Pattern;

public class PvPBot {

    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    public static String validateName(String name) {
        if (name == null || name.isEmpty()) return null;
        if (name.length() < 3)  return "Bot name must be at least 3 characters.";
        if (name.length() > 16) return "Bot name must be 16 characters or fewer.";
        if (!VALID_NAME.matcher(name).matches())
            return "Bot name can only contain letters, numbers, and underscores.";
        return null;
    }

    private final ServerPlayer owner;
    private final ServerLevel world;
    private final String difficulty;
    private final String name;
    private final UUID uuid;
    private final boolean staticMode;

    private BotEntity botEntity;
    private boolean alive = false;
    private int tickCounter = 0;

    private float attackDamage;
    private float movementSpeed;
    private int attackCooldown;
    private int reactionDelay;
    private boolean usesCombos;
    private boolean blockHits;

    public PvPBot(ServerPlayer owner, ServerLevel world, String difficulty, String name, boolean staticMode) {
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
            case "easy"      -> { attackDamage=3f; movementSpeed=0.15f; attackCooldown=20; reactionDelay=40; usesCombos=false; blockHits=false; }
            case "medium"    -> { attackDamage=5f; movementSpeed=0.22f; attackCooldown=12; reactionDelay=20; usesCombos=true;  blockHits=false; }
            case "hard"      -> { attackDamage=7f; movementSpeed=0.28f; attackCooldown=6;  reactionDelay=10; usesCombos=true;  blockHits=true;  }
            case "nightmare" -> { attackDamage=10f;movementSpeed=0.35f; attackCooldown=3;  reactionDelay=2;  usesCombos=true;  blockHits=true;  }
            default          -> { attackDamage=5f; movementSpeed=0.22f; attackCooldown=12; reactionDelay=20; usesCombos=true;  blockHits=false; }
        }
    }

    public boolean spawn() {
        try {
            Vec3 ownerPos = owner.position();
            double spawnX = ownerPos.x + (Math.random() * 4 - 2);
            double spawnY = ownerPos.y;
            double spawnZ = ownerPos.z + (Math.random() * 4 - 2);

            botEntity = new BotEntity(world, this);
            botEntity.setPos(spawnX, spawnY, spawnZ);
            botEntity.setYRot(0);
            botEntity.setXRot(0);

            String modeTag = staticMode ? "§7[Static]" : "§c[Bot]";
            botEntity.setCustomName(Component.literal(modeTag + " §f" + name));
            botEntity.setCustomNameVisible(true);

            if (botEntity.getAttribute(Attributes.MAX_HEALTH) != null)
                botEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0);
            if (botEntity.getAttribute(Attributes.MOVEMENT_SPEED) != null)
                botEntity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(staticMode ? 0.0 : movementSpeed);
            if (botEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null)
                botEntity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attackDamage);

            botEntity.setHealth(20.0f);
            equipBot();
            world.addFreshEntity(botEntity);
            alive = true;

            BotPvPMod.LOGGER.info("[BotPvP] Bot '{}' spawned at ({}, {}, {})", name, spawnX, spawnY, spawnZ);
            return true;
        } catch (Exception e) {
            BotPvPMod.LOGGER.error("[BotPvP] Failed to spawn bot '{}'", name, e);
            return false;
        }
    }

    private void equipBot() {
        switch (difficulty) {
            case "easy" -> {
                botEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WOODEN_SWORD));
                botEntity.setItemSlot(EquipmentSlot.HEAD,     new ItemStack(Items.LEATHER_HELMET));
                botEntity.setItemSlot(EquipmentSlot.CHEST,    new ItemStack(Items.LEATHER_CHESTPLATE));
                botEntity.setItemSlot(EquipmentSlot.LEGS,     new ItemStack(Items.LEATHER_LEGGINGS));
                botEntity.setItemSlot(EquipmentSlot.FEET,     new ItemStack(Items.LEATHER_BOOTS));
            }
            case "medium" -> {
                botEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                botEntity.setItemSlot(EquipmentSlot.HEAD,     new ItemStack(Items.IRON_HELMET));
                botEntity.setItemSlot(EquipmentSlot.CHEST,    new ItemStack(Items.IRON_CHESTPLATE));
                botEntity.setItemSlot(EquipmentSlot.LEGS,     new ItemStack(Items.IRON_LEGGINGS));
                botEntity.setItemSlot(EquipmentSlot.FEET,     new ItemStack(Items.IRON_BOOTS));
            }
            case "hard" -> {
                botEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                botEntity.setItemSlot(EquipmentSlot.HEAD,     new ItemStack(Items.DIAMOND_HELMET));
                botEntity.setItemSlot(EquipmentSlot.CHEST,    new ItemStack(Items.DIAMOND_CHESTPLATE));
                botEntity.setItemSlot(EquipmentSlot.LEGS,     new ItemStack(Items.DIAMOND_LEGGINGS));
                botEntity.setItemSlot(EquipmentSlot.FEET,     new ItemStack(Items.DIAMOND_BOOTS));
            }
            case "nightmare" -> {
                botEntity.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
                botEntity.setItemSlot(EquipmentSlot.HEAD,     new ItemStack(Items.NETHERITE_HELMET));
                botEntity.setItemSlot(EquipmentSlot.CHEST,    new ItemStack(Items.NETHERITE_CHESTPLATE));
                botEntity.setItemSlot(EquipmentSlot.LEGS,     new ItemStack(Items.NETHERITE_LEGGINGS));
                botEntity.setItemSlot(EquipmentSlot.FEET,     new ItemStack(Items.NETHERITE_BOOTS));
            }
        }
    }

    public void tick() {
        if (!alive || botEntity == null || botEntity.isDeadOrDying()) {
            if (alive) {
                alive = false;
                if (owner.isAlive()) {
                    owner.sendSystemMessage(Component.literal(
                        "§e[BotPvP] §fBot §c" + name + " §fhas died! Use §e/botpvp spawn §fto call another."));
                }
            }
            return;
        }

        tickCounter++;
        if (tickCounter < reactionDelay) return;

        LivingEntity target = owner;
        double distanceSq = botEntity.distanceToSqr(target);

        if (staticMode) {
            faceTarget(target);
            if (distanceSq <= 9 && tickCounter % attackCooldown == 0) {
                performAttack(target);
                if (blockHits) botEntity.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
            return;
        }

        if (distanceSq > 400) {
            if (tickCounter % 40 == 0) {
                Vec3 pos = target.position();
                botEntity.teleportTo(pos.x + 3, pos.y, pos.z + 3);
            }
            return;
        }

        faceTarget(target);

        if (distanceSq > 9) {
            moveTowardTarget(target);
        } else {
            if (tickCounter % attackCooldown == 0) performAttack(target);
            if (blockHits && tickCounter % (attackCooldown / 2) == 0)
                botEntity.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }

        if (usesCombos && tickCounter % (attackCooldown * 3) == 0 && distanceSq < 9)
            performComboAttack(target);
    }

    private void faceTarget(LivingEntity target) {
        Vec3 diff = target.position().subtract(botEntity.position()).normalize();
        float yaw   = (float) Math.toDegrees(Math.atan2(-diff.x, diff.z));
        float pitch = (float) Math.toDegrees(-Math.asin(diff.y));
        botEntity.setYRot(yaw);
        botEntity.setYHeadRot(yaw);
        botEntity.setXRot(pitch);
    }

    private void moveTowardTarget(LivingEntity target) {
        Vec3 dir = target.position().subtract(botEntity.position()).normalize();
        botEntity.setDeltaMovement(dir.x * movementSpeed * 10, botEntity.getDeltaMovement().y, dir.z * movementSpeed * 10);
    }

    private void performAttack(LivingEntity target) {
        botEntity.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        target.hurt(world.damageSources().mobAttack(botEntity), attackDamage);
    }

    private void performComboAttack(LivingEntity target) {
        performAttack(target);
        if (target.isAlive()) performAttack(target);
    }

    public void respawn() {
        if (botEntity != null && !botEntity.isDeadOrDying()) {
            botEntity.setHealth(20.0f);
            owner.sendSystemMessage(Component.literal("§a[BotPvP] §fBot §e" + name + " §fhas been healed!"));
        } else {
            spawn();
        }
    }

    public void remove() {
        if (botEntity != null) botEntity.discard();
        alive = false;
    }

    public UUID    getUuid()       { return uuid; }
    public String  getName()       { return name; }
    public String  getDifficulty() { return difficulty; }
    public boolean isStatic()      { return staticMode; }
    public boolean isAlive()       { return alive && botEntity != null && !botEntity.isDeadOrDying(); }
    public BotEntity getEntity()   { return botEntity; }
}
