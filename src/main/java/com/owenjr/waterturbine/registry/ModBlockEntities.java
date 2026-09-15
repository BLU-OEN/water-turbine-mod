package com.owenjr.waterturbine.registry;

import com.owenjr.waterturbine.WaterTurbineMod;
import com.owenjr.waterturbine.block.entity.TurbineBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WaterTurbineMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TurbineBlockEntity>> TURBINE_BE =
            BLOCK_ENTITIES.register("water_turbine", () -> BlockEntityType.Builder.of(
                    TurbineBlockEntity::new, WaterTurbineMod.WATER_TURBINE.get()).build(null));
}
