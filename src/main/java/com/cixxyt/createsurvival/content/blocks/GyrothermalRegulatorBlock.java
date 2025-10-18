package com.cixxyt.createsurvival.content.blocks;

import com.cixxyt.createsurvival.content.blocks.entity.GyrothermalRegulatorBlockEntity;
import com.cixxyt.createsurvival.registry.ModBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;

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
    public GyrothermalRegulatorBlock(Properties properties) {
        super(properties);
        // Default new placements so that the exposed shaft faces the player.  This mirrors how
        // Create's own kinetic blocks behave and prevents accidental backwards installations when
        // a student is first experimenting with rotational power flows.
        this.registerDefaultState(this.defaultBlockState().setValue(HORIZONTAL_FACING, Direction.SOUTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HORIZONTAL_FACING);
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
        // tickers every tick, but each routine immediately ignores the wrong side.
        // IBE exposes a convenience helper that only wires the ticker when the looked-up type
        // matches our block entity type.  Passing the level and state keeps Create's safety checks
        // intact while our ternary picks the appropriate side-specific tick routine.
        return IBE.createTickerHelper(level, state, type, ModBlockEntityTypes.GYROTHERMAL_REGULATOR.get(),
                level.isClientSide ? GyrothermalRegulatorBlockEntity::clientTick : GyrothermalRegulatorBlockEntity::serverTick);
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
