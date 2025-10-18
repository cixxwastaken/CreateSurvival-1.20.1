package com.cixxyt.createsurvival.content.blocks.entity.renderer;

import com.cixxyt.createsurvival.content.blocks.entity.GyrothermalRegulatorBlockEntity;
import com.cixxyt.createsurvival.content.blocks.entity.GyrothermalRegulatorBlockEntity.Mode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/**
 * Custom renderer that adds a lightweight Create-style visualization on top of the regulator.
 * <p>
 * Rather than model an entire Flywheel instance we draw an animated rotor cross using line
 * rendering and orbiting particle halos.  The approach keeps this example approachable for
 * students while still showing how to bridge server-side kinetics with client-side flair.
 */
public class GyrothermalRegulatorRenderer implements BlockEntityRenderer<GyrothermalRegulatorBlockEntity> {
    private static final int HALO_INTERVAL_TICKS = 5;

    public GyrothermalRegulatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GyrothermalRegulatorBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = blockEntity.getRotorAngle(partialTicks);
        drawRotor(blockEntity, poseStack, buffer, angle);
        spawnParticles(blockEntity);
    }

    private void drawRotor(GyrothermalRegulatorBlockEntity blockEntity, PoseStack poseStack,
                           MultiBufferSource buffer, float angle) {
        double intensity = blockEntity.getIntensity();
        Mode mode = blockEntity.getClientMode();

        float warm = 0.5f + (float) intensity * 0.5f;
        float cool = 0.4f + (float) intensity * 0.5f;

        float red;
        float green;
        float blue;
        switch (mode) {
            case HEATING -> {
                red = 1.0f;
                green = warm;
                blue = 0.2f;
            }
            case COOLING -> {
                red = 0.2f;
                green = 0.6f;
                blue = cool;
            }
            default -> {
                red = 0.7f;
                green = 0.7f;
                blue = 0.7f;
            }
        }

        float radius = 0.35f + (float) intensity * 0.25f;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        var pose = poseStack.last();

        consumer.vertex(pose.pose(), -radius, 0.0F, 0.0F)
                .color(red, green, blue, 1.0F)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(pose.pose(), radius, 0.0F, 0.0F)
                .color(red, green, blue, 1.0F)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(pose.pose(), 0.0F, 0.0F, -radius)
                .color(red, green, blue, 1.0F)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
        consumer.vertex(pose.pose(), 0.0F, 0.0F, radius)
                .color(red, green, blue, 1.0F)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
        poseStack.popPose();
    }

    private void spawnParticles(GyrothermalRegulatorBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (!(level instanceof ClientLevel clientLevel)) {
            // Renderers occasionally wake up before the client world finishes wiring itself in,
            // especially while the player is joining a save.  By politely bailing out unless the
            // block entity already knows about a client-level instance we avoid the null pointer
            // crash the user reported and demonstrate the defensive checks professional modders
            // lean on when bridging logical sides.
            return;
        }

        if (blockEntity.getClientMode() == Mode.IDLE) {
            return;
        }

        long gameTime = clientLevel.getGameTime();
        if (blockEntity.getLastHaloTick() == gameTime || gameTime % HALO_INTERVAL_TICKS != 0) {
            return;
        }

        blockEntity.markHaloTick(gameTime);
        double intensity = blockEntity.getIntensity();
        double radius = 0.45D + intensity * 0.3D;
        Vector3f color;
        if (blockEntity.getClientMode() == Mode.HEATING) {
            color = new Vector3f(1.0F, 0.5F + (float) intensity * 0.4F, 0.2F);
        } else {
            color = new Vector3f(0.2F, 0.6F, 0.9F + (float) intensity * 0.1F);
        }
        DustParticleOptions particle = new DustParticleOptions(color, 1.0F);

        for (int i = 0; i < 4; i++) {
            double theta = (gameTime / 6.0D) + (Math.PI / 2.0D) * i;
            double x = blockEntity.getBlockPos().getX() + 0.5D + Math.cos(theta) * radius;
            double z = blockEntity.getBlockPos().getZ() + 0.5D + Math.sin(theta) * radius;
            double y = blockEntity.getBlockPos().getY() + 0.8D + Math.sin(theta * 2.0D) * 0.05D;
            clientLevel.addParticle(particle, x, y, z, 0.0D, 0.002D, 0.0D);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(GyrothermalRegulatorBlockEntity blockEntity) {
        // Returning true keeps the halo visible even when the block is barely off camera, matching
        // Create's theatrical presentation style.
        return true;
    }
}
