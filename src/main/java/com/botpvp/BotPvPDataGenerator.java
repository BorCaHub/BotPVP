package com.botpvp;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * BotPvPDataGenerator - Fabric data generation entrypoint.
 * Required for Fabric's data generation system.
 */
public class BotPvPDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        // No data generation needed for this mod
        // Reserved for future use (loot tables, recipes, etc.)
    }
}
