package com.cixxyt.createsurvival.content.blocks;

import com.cixxyt.createsurvival.content.blocks.entity.GyrothermalRegulatorBlockEntity;
import com.cixxyt.createsurvival.registry.ModBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import java.util.Map;

/**
 * Rotary climate control for Create machines.
 * <p>
 * The regulator inherits from {@link HorizontalKineticBlock} so it behaves like other shafted
 * appliances: players can aim the exposed axle using the block's horizontal facing, then feed it
 * rotational energy using gearboxes, clutches, or any other Create mechanism.  We also implement
 * {@link IBE} to bridge directly to {@link GyrothermalRegulatorBlockEntity}, keeping kinetic logic
 * encapsulated on the server while still allowing rich client-side visualization.
 */
public class GyrothermalRegulatorBlock extends HorizontalKineticBlock implements IBE<GyrothermalRegulatorBlockEntity> {
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(5, 5, 15, 11, 11, 18)
    );
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(5, 5, -2, 11, 11, 1)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(15, 5, 5, 18, 11, 11)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(-2, 5, 5, 1, 11, 11)
    );
    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.SOUTH, SHAPE_SOUTH,
            Direction.NORTH, SHAPE_NORTH,
            Direction.EAST, SHAPE_EAST,
            Direction.WEST, SHAPE_WEST
    );

    public GyrothermalRegulatorBlock(Properties properties) {
        super(properties);
        // Default new placements so that the exposed shaft faces the player.  This mirrors how
        // Create's own kinetic blocks behave and prevents accidental backwards installations when
        // a student is first experimenting with rotational power flows.
        // HorizontalKineticBlock already wires in the HORIZONTAL_FACING property, so we only adjust
        // the default orientation here.  Students sometimes attempt to re-register the property and
        // run into Forge's "duplicate property" crash; leaving the configuration in the constructor
        // highlights that the base class has already done the heavy lifting for us.
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // The physical outline mirrors the new model: a slightly inset brass chassis plus the shaft
        // nub that now protrudes from the front face.  Explicitly rotating the hitbox teaches
        // students that block shapes can extend beyond the default cube to match bespoke models.
        return SHAPES.getOrDefault(state.getValue(HORIZONTAL_FACING), SHAPE_SOUTH);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Orient the shaft so it exits the face the player was looking at when the block was placed.
        // This ensures the "front" always advertises where power must be injected.
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, direction);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        // Create's kinetic graph constantly queries each block for its shaft axis so it can stitch
        // the stress network together.  We simply reuse the stored facing, effectively telling the
        // engine that the block's axle projects straight out of the front casing.
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        // By only advertising a shaft on the "front" face we give the machine a clear baseline for
        // its thermal decisions: clockwise power (positive speed) enters from the front and signals
        // heating mode, while counter-rotation drives the same shaft backwards to toggle cooling.
        return face == state.getValue(HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return IBE.super.newBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // The ticker hands off to specialized static methods on the block entity so we can keep
        // server-only thermal logic separate from client visualizations.  Forge will call both
        // tickers every tick, but each routine immediately ignores the wrong side.  Rather than
        // relying on Create's overload-specific helper (which changed signatures between minor
        // versions), we perform the type guard manually so the compiler remains satisfied on 1.20.1
        // Forge.  Doing it ourselves makes the flow obvious to students studying the call graph and
        // avoids surprises if Create tweaks its utility APIs in the future.
        if (type != ModBlockEntityTypes.GYROTHERMAL_REGULATOR.get()) {
            return null;
        }

        // Earlier revisions tried to funnel this logic through IBE#createTickerHelper, but Forge's 1.20.1
        // generics make that helper hard to resolve and triggered the "cannot find symbol" error the user
        // reported.  By caching the base ticker explicitly we stay within vanilla generics rules while still
        // documenting how the server and client responsibilities diverge.
        BlockEntityTicker<GyrothermalRegulatorBlockEntity> baseTicker = level.isClientSide
                ? GyrothermalRegulatorBlockEntity::clientTick
                : GyrothermalRegulatorBlockEntity::serverTick;

        // Once we confirm the queried type actually matches our block entity we can safely return the
        // side-appropriate ticker.  The explicit cast looks scary, but Forge guarantees it will only ever
        // invoke the callback on instances of our block entity when the type check passes.
        @SuppressWarnings("unchecked")
        BlockEntityTicker<T> castTicker = (BlockEntityTicker<T>) baseTicker;
        return castTicker;
    }

    @Override
    public Class<GyrothermalRegulatorBlockEntity> getBlockEntityClass() {
        return GyrothermalRegulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GyrothermalRegulatorBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.GYROTHERMAL_REGULATOR.get();
    }
}
