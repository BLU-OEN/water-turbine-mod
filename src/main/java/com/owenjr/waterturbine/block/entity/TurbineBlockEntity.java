package com.owenjr.waterturbine.block.entity;

import com.owenjr.waterturbine.Config;
import com.owenjr.waterturbine.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Generates Forge Energy every server tick while its block is waterlogged, then pushes as much
 * of its buffer as it can into any adjacent block that exposes an energy capability.
 */
public class TurbineBlockEntity extends BlockEntity {
    /**
     * maxReceive is 0 so external sources can't charge the turbine via the public
     * receiveEnergy API - but that also blocks our own generation from using it, so
     * {@link #generate} adds energy directly instead of going through receiveEnergy.
     */
    private static final class GeneratorEnergyStorage extends EnergyStorage {
        GeneratorEnergyStorage(int capacity, int maxExtract) {
            super(capacity, 0, maxExtract);
        }

        void generate(int amount) {
            energy = Math.min(capacity, energy + amount);
        }
    }

    private final GeneratorEnergyStorage energyStorage = new GeneratorEnergyStorage(Config.energyCapacity, Config.maxTransfer);

    // Client-local animation; energy, persistence and the existing bubble cone are unchanged.
    private float rotorAngle;
    private float previousRotorAngle;

    public float getRotorAngle(float partialTick) {
        return previousRotorAngle + (rotorAngle - previousRotorAngle) * partialTick;
    }

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
            turbine.energyStorage.generate(Config.generationRate);
        }

        turbine.pushEnergyToNeighbors(level, pos);

        if (turbine.energyStorage.getEnergyStored() != before) {
            turbine.setChanged();
        }
    }

    /**
     * Cone-shaped bubble stream: on the back (intake) face bubbles spawn wide and drift in
     * toward the block, and on the front (outtake) face they spawn at the block and flare
     * outward, both using the same facing-aligned velocity so they read as one continuous flow.
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, TurbineBlockEntity turbine) {
        turbine.previousRotorAngle = turbine.rotorAngle;
        if (!state.getValue(BlockStateProperties.WATERLOGGED)) {
            return;
        }

        turbine.rotorAngle += 6.0F;
        if (turbine.rotorAngle >= 360.0F) {
            turbine.rotorAngle -= 360.0F;
            turbine.previousRotorAngle -= 360.0F;
        }

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        RandomSource random = level.getRandom();

        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 dir = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize();

        double maxDistance = 1.3;
        double maxRadius = 0.55;
        double speed = 0.06;
        int particlesPerSide = 2;

        for (int i = 0; i < particlesPerSide; i++) {
            double t = random.nextDouble();
            Vec3 lateral = coneOffset(right, up, maxRadius * t, random);
            Vec3 particlePos = center.add(dir.scale(0.5 + maxDistance * t)).add(lateral);
            Vec3 velocity = dir.scale(speed);
            level.addParticle(ParticleTypes.BUBBLE, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }

        for (int i = 0; i < particlesPerSide; i++) {
            double t = 0.4 + random.nextDouble() * 0.6;
            Vec3 lateral = coneOffset(right, up, maxRadius * t, random);
            Vec3 particlePos = center.subtract(dir.scale(0.5 + maxDistance * t)).add(lateral);
            Vec3 velocity = dir.scale(speed);
            level.addParticle(ParticleTypes.BUBBLE, particlePos.x, particlePos.y, particlePos.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private static Vec3 coneOffset(Vec3 right, Vec3 up, double radius, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        return right.scale(radius * Math.cos(angle)).add(up.scale(radius * Math.sin(angle)));
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
