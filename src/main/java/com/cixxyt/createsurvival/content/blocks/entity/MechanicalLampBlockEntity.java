package com.cixxyt.createsurvival.content.blocks.entity;

import com.cixxyt.createsurvival.content.blocks.MechanicalLampBlock;
import com.cixxyt.createsurvival.registry.ModBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity responsible for translating Create's kinetic network data into light output.
 * <p>
 * The parent {@link KineticBlockEntity} already knows how to join the stress graph and compute the
 * delivered rotational speed.  All we have to do is observe that value, scale it to Minecraft's
 * 0-15 brightness scale, and ask the block to update its state when the light level changes.
 */
public class MechanicalLampBlockEntity extends KineticBlockEntity {
    public MechanicalLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MECHANICAL_LAMP.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        int newLight = speedToLight(Math.abs(getSpeed()));
        int stateLight = getBlockState().getValue(MechanicalLampBlock.LIGHT_LEVEL);
        if (newLight != stateLight) {
            MechanicalLampBlock.setLit(level, getBlockPos(), getBlockState(), newLight);
        }
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        // Speed updates can arrive outside the normal tick loop (for example when a gearbox is
        // placed).  Re-running the lighting calculation here keeps the lamp perfectly responsive.
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        int newLight = speedToLight(Math.abs(getSpeed()));
        int stateLight = getBlockState().getValue(MechanicalLampBlock.LIGHT_LEVEL);
        if (newLight != stateLight) {
            MechanicalLampBlock.setLit(level, getBlockPos(), getBlockState(), newLight);
        }
    }

    /**
     * Converts rotational speed into a vanilla-compatible light value.
     * <p>
     * Create reports speed in RPM-equivalent units where typical contraptions hover around 0-256.
     * We normalize that range into 0-1 and then stretch it across the 15 possible light steps.
     */
    private static int speedToLight(float speed) {
        float normalized = Mth.clamp(speed / 256.0f, 0.0f, 1.0f);
        return Mth.clamp(Math.round(normalized * 15.0f), 0, 15);
    }
}
