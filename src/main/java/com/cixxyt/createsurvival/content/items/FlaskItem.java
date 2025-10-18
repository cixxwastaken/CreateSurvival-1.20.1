package com.cixxyt.createsurvival.content.items;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FlaskItem extends Item {

    public FlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockHitResult blockHit = (BlockHitResult) hitResult;
        BlockPos pos = blockHit.getBlockPos();
        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos, blockHit.getDirection(), stack)) {
            return InteractionResultHolder.fail(stack);
        }

        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isSource() || fluidState.getType() != Fluids.WATER) {
            return InteractionResultHolder.pass(stack);
        }

        ItemStack filled = FlaskItemWater.createFilledStack(level, pos);

        level.playSound(player, pos, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1f, 1f);
        ItemStack result = ItemUtils.createFilledResult(stack, player, filled);
        return InteractionResultHolder.sidedSuccess(result, level.isClientSide);
    }
}
