package com.owenjr.waterturbine.block.entity;

import com.owenjr.waterturbine.Config;
import com.owenjr.waterturbine.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Generates Forge Energy every server tick while its block is waterlogged, then pushes as much
 * of its buffer as it can into any adjacent block that exposes an energy capability.
 */
public class TurbineBlockEntity extends BlockEntity {
    private final EnergyStorage energyStorage = new EnergyStorage(Config.energyCapacity, 0, Config.maxTransfer);

    public TurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURBINE_BE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TurbineBlockEntity turbine) {
        boolean generating = state.getValue(BlockStateProperties.WATERLOGGED);
        int before = turbine.energyStorage.getEnergyStored();
        if (generating) {
            turbine.energyStorage.receiveEnergy(Config.generationRate, false);
        }

        turbine.pushEnergyToNeighbors(level, pos);

        if (turbine.energyStorage.getEnergyStored() != before) {
            turbine.setChanged();
        }
    }

    private void pushEnergyToNeighbors(Level level, BlockPos pos) {
        if (energyStorage.getEnergyStored() <= 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (energyStorage.getEnergyStored() <= 0) {
                return;
            }

            BlockPos neighborPos = pos.relative(direction);
            if (!level.isLoaded(neighborPos)) {
                continue;
            }

            IEnergyStorage neighbor = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
            if (neighbor == null || !neighbor.canReceive()) {
                continue;
            }

            int offered = energyStorage.extractEnergy(Config.maxTransfer, true);
            if (offered <= 0) {
                continue;
            }

            int accepted = neighbor.receiveEnergy(offered, false);
            if (accepted > 0) {
                energyStorage.extractEnergy(accepted, false);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Energy")) {
            energyStorage.deserializeNBT(registries, tag.get("Energy"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Energy", energyStorage.serializeNBT(registries));
    }
}
