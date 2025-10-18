package com.cixxyt.createsurvival.content.blocks.entity.renderer;

import com.cixxyt.createsurvival.content.blocks.GyrothermalRegulatorBlock;
import com.cixxyt.createsurvival.content.blocks.entity.GyrothermalRegulatorBlockEntity;
import com.cixxyt.createsurvival.content.blocks.entity.GyrothermalRegulatorBlockEntity.Mode;
import com.simibubi.create.AllBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Custom renderer that adds a lightweight Create-style visualization on top of the regulator.
 * <p>
 * Rather than model an entire Flywheel instance we draw an animated rotor cross using line
 * rendering and reuse Create's shaft block model so a protruding axle visibly spins whenever the
 * machine is powered.  The approach keeps this example approachable for students while still
 * showing how to bridge server-side kinetics with client-side flair.
 */
public class GyrothermalRegulatorRenderer implements BlockEntityRenderer<GyrothermalRegulatorBlockEntity> {
    private static final BlockState SHAFT_TEMPLATE = AllBlocks.SHAFT.getDefaultState()
            .setValue(BlockStateProperties.AXIS, Direction.Axis.Z);

    public GyrothermalRegulatorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GyrothermalRegulatorBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = blockEntity.getRotorAngle(partialTicks);
        drawRotor(blockEntity, poseStack, buffer, angle);
                                                         renderShaft(blockEntity, poseStack, buffer, packedLight, packedOverlay, angle);
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

    private void renderShaft(GyrothermalRegulatorBlockEntity blockEntity, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight, int packedOverlay, float angle) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(GyrothermalRegulatorBlock.HORIZONTAL_FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        // Rotate the local coordinate system so positive Z always points out of the block's "front".
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        // Apply the spin gathered from the block entity so the shaft visibly mirrors gearbox speed.
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        // Slide the shaft forward so the Create axle pokes out of the front casing.
        poseStack.translate(0.0D, 0.0D, 0.3125D);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(SHAFT_TEMPLATE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(GyrothermalRegulatorBlockEntity blockEntity) {
        // Returning true keeps the animated axle visible even when the block is barely off camera,
        // matching Create's theatrical presentation style.
        return true;
    }
}
