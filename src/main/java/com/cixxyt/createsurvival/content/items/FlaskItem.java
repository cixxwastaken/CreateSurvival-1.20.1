package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import com.cixxyt.createsurvival.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class FlaskItem extends Item {

    public FlaskItem(Properties properties) {
        super(properties);
    }

    // Right-click water to fill
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = player.blockPosition();

        if (!level.isClientSide && level.getFluidState(pos).isSource() && level.getFluidState(pos).getType() == Fluids.WATER) {
            // Create filled flask
            ItemStack filled = new ItemStack(ModItems.FLASK_WATER.get());
            filled.getOrCreateTag().putInt("SipsLeft", 5);

            // Add purity if the Thirst mod is available
            ThirstCompat.applyBlockPurity(filled, level, pos);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1f, 1f);

            return InteractionResultHolder.success(filled);
        }

        return InteractionResultHolder.pass(stack);
    }
}
