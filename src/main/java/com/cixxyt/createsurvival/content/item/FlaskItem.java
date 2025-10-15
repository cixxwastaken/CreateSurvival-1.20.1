package com.cixxyt.createsurvival.content.item;

import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlaskItem extends Item {

    private static final int MAX_FLUID = 1000; // mB total (5 gulps of 200 mB each)
    private static final int GULP_AMOUNT = 200;

    public FlaskItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();
        int amount = tag.getInt("FluidAmount");

        if (amount >= GULP_AMOUNT) {
            amount -= GULP_AMOUNT;
            tag.putInt("FluidAmount", amount);

            if (!level.isClientSide()) {
                ThirstCompat.onDrink(player, stack);
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        int amount = tag.getInt("FluidAmount");
        tooltip.add(Component.literal("Water: " + amount + " / " + MAX_FLUID + " mB").withStyle(ChatFormatting.BLUE));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getOrCreateTag().getInt("FluidAmount") > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int stored = stack.getOrCreateTag().getInt("FluidAmount");
        return Math.round((float) stored / MAX_FLUID * 13);
    }

    // ========== CREATE FLUID HANDLER ==========


    public boolean canFillFromSpout(ItemStack stack, FluidStack fluid) {
        return fluid.getFluid().isSame(Fluids.WATER);
    }


    public int getRequiredAmountForFilledItem(Level level, ItemStack stack, FluidStack availableFluid) {
        CompoundTag tag = stack.getOrCreateTag();
        int current = tag.getInt("FluidAmount");
        return Math.min(MAX_FLUID - current, availableFluid.getAmount());
    }


    public ItemStack getFilledItem(Level level, ItemStack stack, FluidStack fluid) {
        CompoundTag tag = stack.getOrCreateTag();
        int current = tag.getInt("FluidAmount");
        int fill = Math.min(fluid.getAmount(), MAX_FLUID - current);
        tag.putInt("FluidAmount", current + fill);
        return stack;
    }
}
