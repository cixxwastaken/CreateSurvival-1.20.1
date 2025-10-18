package com.cixxyt.createsurvival.content.blocks;

import com.cixxyt.createsurvival.content.blocks.entity.MechanicalLampBlockEntity;
import com.cixxyt.createsurvival.registry.ModBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * A Create-powered lantern that brightens in proportion to the rotational energy it receives.
 * <p>
 * The block extends {@link RotatedPillarKineticBlock} so players can place it along any axis just like
 * vanilla logs.  By also implementing {@link IBE} we gain a strongly-typed bridge to the matching
 * {@link MechanicalLampBlockEntity}, which keeps the rendering and kinetic plumbing nicely separated.
 */
public class MechanicalLampBlock extends RotatedPillarKineticBlock implements IBE<MechanicalLampBlockEntity> {
    /**
     * We store the computed brightness in the block state so vanilla lighting can react instantly.
     * The range mirrors Minecraft's light scale: {@code 0} means dark, {@code 15} rivals glowstone.
     */
    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light", 0, 15);

    public MechanicalLampBlock(Properties properties) {
        super(properties);
        // Every new block state begins unlit and aligned vertically, making its behavior intuitive
        // for players who simply plop the lamp onto the floor before hooking it into a machine.
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(LIGHT_LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_LEVEL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Logs use the clicked face to decide their axis; mirroring that convention helps the lamp
        // feel native to Minecraft while still delivering Create-style mechanics.
        Direction.Axis axis = context.getClickedFace().getAxis();
        return this.defaultBlockState().setValue(AXIS, axis);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Forge asks this question whenever nearby lighting might need to change.  Delegating to the
        // stored property means the block entity only has to push updates when its kinetic input
        // meaningfully changes, which keeps light propagation inexpensive.
        return state.getValue(LIGHT_LEVEL);
    }

    @Override
    public Class<MechanicalLampBlockEntity> getBlockEntityClass() {
        return MechanicalLampBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalLampBlockEntity> getBlockEntityType() {
        return ModBlockEntityTypes.MECHANICAL_LAMP.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        // The lamp is designed as a passive appliance: it only accepts power from the shaft below.
        // Allowing side connections would let it transmit stress, which could confuse kinetic loops.
        return face == Direction.DOWN;
    }

    /**
     * Helper invoked by the block entity to push lighting changes into the world safely.
     *
     * @param level       The world the block lives in.
     * @param pos         The block's position.
     * @param state       The current block state (before modification).
     * @param lightLevel  Desired brightness in the vanilla 0-15 scale.
     */
    public static void setLit(Level level, BlockPos pos, BlockState state, int lightLevel) {
        int clamped = Mth.clamp(lightLevel, 0, 15);
        if (state.getValue(LIGHT_LEVEL) == clamped) {
            return;
        }
        level.setBlock(pos, state.setValue(LIGHT_LEVEL, clamped), Block.UPDATE_ALL);
    }
}
