package com.cixxyt.createsurvival.content.blocks.entity;

import com.cixxyt.createsurvival.compat.coldsweat.ColdSweatCompat;
import com.cixxyt.createsurvival.registry.ModBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

/**
 * Block entity that translates shaft speed into localized heating or cooling.
 * <p>
 * The Create API already handles stress calculations for us via {@link KineticBlockEntity}; our
 * job is to observe the delivered {@link #getSpeed()} and decide what sort of climate boost to
 * apply.  Positive rotation is interpreted as "heat the room" while negative rotation signals
 * "pull heat out".  When the rotor idles the machine politely goes dormant, avoiding needless
 * Cold Sweat updates.
 */
public class GyrothermalRegulatorBlockEntity extends KineticBlockEntity {
    private static final float MIN_OPERATION_SPEED = 8.0f;
    private static final int EFFECT_INTERVAL_TICKS = 40;

    private Mode mode = Mode.IDLE;
    private double intensity;
    private double lastAppliedStrength;
    private int effectCooldown;

    private float visualRotorSpeed;
    private float rotorAngle;

    public GyrothermalRegulatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.GYROTHERMAL_REGULATOR.get(), pos, state);
    }

    /** Server-side tick that monitors kinetic input and nudges surrounding players. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, GyrothermalRegulatorBlockEntity blockEntity) {
        if (level == null || level.isClientSide) {
            return;
        }

        blockEntity.serverTickInternal(level);
    }

    /** Client-side tick dedicated to smoothing out rotor animation state. */
    public static void clientTick(Level level, BlockPos pos, BlockState state, GyrothermalRegulatorBlockEntity blockEntity) {
        if (level == null || !level.isClientSide) {
            return;
        }

        blockEntity.clientTickInternal();
    }

    private void serverTickInternal(Level level) {
        float rawSpeed = getSpeed();
        float magnitude = Math.abs(rawSpeed);

        Mode computedMode = Mode.IDLE;
        double computedIntensity = 0.0D;
        if (magnitude >= MIN_OPERATION_SPEED) {
            // Positive RPM means the shaft turns clockwise when viewed head-on.  We interpret that
            // as pushing heated air into the room.  Negative values reverse the fan blades and draw
            // warmth out to provide a cooling breeze.
            computedMode = rawSpeed >= 0 ? Mode.HEATING : Mode.COOLING;
            computedIntensity = Mth.clamp(magnitude / 128.0D, 0.0D, 1.0D);
        }

        if (computedMode != mode || Math.abs(computedIntensity - intensity) > 0.0005D) {
            mode = computedMode;
            intensity = computedIntensity;
            if (mode == Mode.IDLE) {
                lastAppliedStrength = 0.0D;
            }
            syncToClient();
        }

        if (mode == Mode.IDLE) {
            // When no useful rotation arrives we reset the cooldown so the next burst happens
            // immediately after power resumes.  That makes the block feel responsive to gearbox
            // tinkering.
            effectCooldown = 0;
            return;
        }

        effectCooldown++;
        if (effectCooldown < EFFECT_INTERVAL_TICKS) {
            return;
        }

        effectCooldown = 0;
        double strength = 0.25D + intensity * 0.75D;
        double radius = 4.0D + intensity * 2.0D;
        lastAppliedStrength = strength;

        if (mode == Mode.HEATING) {
            // Heating mode walks nearby students through how Create power can warm a camp.
            ColdSweatCompat.applyAmbientWarmth(level, getBlockPos(), strength, radius);
        } else {
            // Cooling mode mirrors the branch above but leans into evaporative relief instead.
            ColdSweatCompat.applyAmbientCooling(level, getBlockPos(), strength, radius);
        }
        syncToClient();
    }

    private void clientTickInternal() {
        // We compute a signed speed so the rotor visibly reverses when the player flips their power
        // train.  The smooth interpolation keeps the motion fluid even when gearboxes step the RPM
        // up or down abruptly.
        float target = (float) (intensity * 32.0D);
        if (mode == Mode.COOLING) {
            target = -target;
        }
        visualRotorSpeed = Mth.lerp(0.2f, visualRotorSpeed, target);
        rotorAngle = (rotorAngle + visualRotorSpeed) % 360.0f;
    }

    /** Returns the mode synchronized from the server for rendering/tooltip purposes. */
    public Mode getClientMode() {
        return mode;
    }

    /** Fractional intensity (0-1) describing how hard the regulator is currently pushing. */
    public double getIntensity() {
        return intensity;
    }

    /** Strength applied during the last server-side area pulse. */
    public double getLastAppliedStrength() {
        return lastAppliedStrength;
    }

    /** Angle used by the renderer to orient the rotor overlay. */
    public float getRotorAngle(float partialTicks) {
        return (rotorAngle + visualRotorSpeed * partialTicks) % 360.0f;
    }

    private void syncToClient() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        // Smart block entities expose sendData(), which wraps the boilerplate for dispatching a
        // ClientboundBlockEntityDataPacket.  We still call setChanged() so the chunk saves the new
        // fields the next time it serializes to disk.
        setChanged();
        sendData();
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putString("Mode", mode.name());
        tag.putDouble("Intensity", intensity);
        tag.putDouble("Strength", lastAppliedStrength);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (tag.contains("Mode")) {
            try {
                mode = Mode.valueOf(tag.getString("Mode"));
            } catch (IllegalArgumentException ignored) {
                mode = Mode.IDLE;
            }
        }
        intensity = tag.getDouble("Intensity");
        lastAppliedStrength = tag.getDouble("Strength");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // create(this) asks SmartBlockEntity to call write(tag, true), giving us the same fields we
        // teach in read(..., true) below.  This is the standard Create pattern for syncing custom
        // state to the client renderer.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            // The boolean flag tells our read method this data flowed over the network, prompting it
            // to update only transient client fields if we ever add them in the future.
            read(tag, true);
        }
    }

    /**
     * Enumerates the three distinct states the machine can be in.
     * <ul>
     *     <li>{@link #HEATING}: Positive shaft speed, so we warm nearby players.</li>
     *     <li>{@link #COOLING}: Negative shaft speed, so we siphon heat away.</li>
     *     <li>{@link #IDLE}: Magnitude below {@link #MIN_OPERATION_SPEED}, so we avoid doing work.</li>
     * </ul>
     */
    public enum Mode {
        HEATING,
        COOLING,
        IDLE
    }
}
