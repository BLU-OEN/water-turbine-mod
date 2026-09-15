package com.owenjr.waterturbine;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common config for the Water Turbine mod. Loaded on both client and dedicated server so that
 * server packs (like modpacks) can tune generation without a client-only config diverging from it.
 */
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue GENERATION_RATE = BUILDER
            .comment("Forge Energy generated per tick while the turbine is submerged in water.")
            .defineInRange("generationRate", 20, 1, 100_000);

    private static final ModConfigSpec.IntValue ENERGY_CAPACITY = BUILDER
            .comment("Maximum Forge Energy the turbine's internal buffer can hold.")
            .defineInRange("energyCapacity", 32_000, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue MAX_TRANSFER = BUILDER
            .comment("Maximum Forge Energy the turbine can push to an adjacent block per tick, per side.")
            .defineInRange("maxTransfer", 200, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static int generationRate = 20;
    public static int energyCapacity = 32_000;
    public static int maxTransfer = 200;

    public static void refresh() {
        generationRate = GENERATION_RATE.get();
        energyCapacity = ENERGY_CAPACITY.get();
        maxTransfer = MAX_TRANSFER.get();
    }
}
