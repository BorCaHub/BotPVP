package com.botpvp.bot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

public class BotEntity extends Monster {

    private final PvPBot pvpBot;

    public BotEntity(ServerLevel world, PvPBot pvpBot) {
        super(EntityType.ZOMBIE, world);
        this.pvpBot = pvpBot;
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
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    public PvPBot getPvpBot() { return pvpBot; }
}
