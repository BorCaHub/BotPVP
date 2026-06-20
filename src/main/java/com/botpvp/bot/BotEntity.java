package com.botpvp.bot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

import java.util.ArrayList;
import java.util.List;

public class BotEntity extends Monster {

    private final PvPBot pvpBot;
    private final ItemStack[] equipment = new ItemStack[6];

    public BotEntity(ServerLevel world, PvPBot pvpBot) {
        super(EntityType.ZOMBIE, world);
        this.pvpBot = pvpBot;
        for (int i = 0; i < equipment.length; i++) equipment[i] = ItemStack.EMPTY;
    }

    public static AttributeSupplier.Builder createBotAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.0);
    }

    @Override
    protected void registerGoals() {}

    @Override
    public boolean canPickUpLoot() { return false; }

    @Override
    public boolean isPersistenceRequired() { return true; }

    @Override
    public boolean isUndead() { return false; }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        int idx = slot.getIndex();
        return (idx >= 0 && idx < equipment.length) ? equipment[idx] : ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        int idx = slot.getIndex();
        if (idx >= 0 && idx < equipment.length) equipment[idx] = stack;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        List<ItemStack> armor = new ArrayList<>();
        armor.add(equipment[EquipmentSlot.FEET.getIndex()]);
        armor.add(equipment[EquipmentSlot.LEGS.getIndex()]);
        armor.add(equipment[EquipmentSlot.CHEST.getIndex()]);
        armor.add(equipment[EquipmentSlot.HEAD.getIndex()]);
        return armor;
    }

    public PvPBot getPvpBot() { return pvpBot; }
}
