package com.owenjr.waterturbine.block;

import com.mojang.serialization.MapCodec;
import com.owenjr.waterturbine.block.entity.TurbineBlockEntity;
import com.owenjr.waterturbine.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

import org.jetbrains.annotations.Nullable;

/**
 * A compact hydro generator. Placing it directly into a water source (or flowing water)
 * waterlogs it, and while waterlogged its block entity generates Forge Energy every tick.
 */
public class TurbineBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<TurbineBlock> CODEC = simpleCodec(TurbineBlock::new);

    private static final VoxelShape CABLE_BASE = box(4, 0, 4, 12, 4, 12);
    private static final VoxelShape NORTH_SHAPE = Shapes.or(CABLE_BASE,
            box(3, 3, 2, 13, 13, 11), box(5, 5, 0, 11, 11, 3), box(1, 1, 12, 15, 15, 15));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(CABLE_BASE,
            box(3, 3, 5, 13, 13, 14), box(5, 5, 13, 11, 11, 16), box(1, 1, 1, 15, 15, 4));
    private static final VoxelShape EAST_SHAPE = Shapes.or(CABLE_BASE,
            box(5, 3, 3, 14, 13, 13), box(13, 5, 5, 16, 11, 11), box(1, 1, 1, 4, 15, 15));
    private static final VoxelShape WEST_SHAPE = Shapes.or(CABLE_BASE,
            box(2, 3, 3, 11, 13, 13), box(0, 5, 5, 3, 11, 11), box(12, 1, 1, 15, 15, 15));

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    public TurbineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BlockStateProperties.WATERLOGGED, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED, BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidState.is(FluidTags.WATER);
        return this.defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, waterlogged)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType pathComputationType) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TurbineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.TURBINE_BE.get(), TurbineBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.TURBINE_BE.get(), TurbineBlockEntity::serverTick);
    }
}
