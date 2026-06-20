package com.botpvp.bot;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * BotEntity - Custom mob entity that represents a PvP bot.
 * 
 * Looks like a player-style entity, follows PvP bot AI logic,
 * and supports equipment/armor like a real player.
 * 
 * Compatible with Minecraft 26.1.1 and 26.1.2
 */
public class BotEntity extends HostileEntity {

    private final PvPBot pvpBot;

    // Store equipped items manually
    private final ItemStack[] equipment = new ItemStack[6];

    public BotEntity(ServerWorld world, PvPBot pvpBot) {
        super(EntityType.ZOMBIE, world);  // Use zombie as base humanoid entity
        this.pvpBot = pvpBot;

        // Init equipment slots
        for (int i = 0; i < equipment.length; i++) {
            equipment[i] = ItemStack.EMPTY;
        }
    }

    /**
     * Default attributes for the bot entity.
     * These are overridden by PvPBot after spawning.
     */
    public static DefaultAttributeContainer.Builder createBotAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, 20.0)
                .add(EntityAttributes.ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.FOLLOW_RANGE, 32.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void initGoals() {
        // We handle AI manually in PvPBot#tick(), so no goals needed here
    }

    @Override
    public boolean canPickUpLoot() {
        return false;  // Bot should not pick up items
    }

    @Override
    public boolean isPersistent() {
        return true;  // Don't despawn
    }

    @Override
    public boolean isUndead() {
        return false;  // Bot is not undead
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        int index = slot.getEntitySlotId();
        if (index >= 0 && index < equipment.length) {
            return equipment[index];
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        int index = slot.getEntitySlotId();
        if (index >= 0 && index < equipment.length) {
            equipment[index] = stack;
        }
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        java.util.List<ItemStack> armor = new java.util.ArrayList<>();
        armor.add(equipment[EquipmentSlot.FEET.getEntitySlotId()]);
        armor.add(equipment[EquipmentSlot.LEGS.getEntitySlotId()]);
        armor.add(equipment[EquipmentSlot.CHEST.getEntitySlotId()]);
        armor.add(equipment[EquipmentSlot.HEAD.getEntitySlotId()]);
        return armor;
    }

    public PvPBot getPvpBot() {
        return pvpBot;
    }
}
