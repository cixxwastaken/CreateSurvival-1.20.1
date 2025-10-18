package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.compat.coldsweat.ColdSweatCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Utility item that reports the local temperature.  When Cold Sweat is present we surface its
 * exact readings; otherwise the thermometer gives advice based on vanilla biome temperature.
 */
public class SurveyorThermometerItem extends Item {
    public SurveyorThermometerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            Component readout = ColdSweatCompat.describeTemperature(player, level);
            player.displayClientMessage(readout, true);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.createsurvival.surveyor_thermometer.tooltip").withStyle(ChatFormatting.GRAY));
        if (ColdSweatCompat.isLoaded()) {
            tooltip.add(Component.translatable("item.createsurvival.surveyor_thermometer.tooltip.coldsweat").withStyle(ChatFormatting.AQUA));
        }
    }
}
