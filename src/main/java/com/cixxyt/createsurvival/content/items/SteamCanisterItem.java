package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.systems.TemperatureSystem;
import com.cixxyt.createsurvival.systems.ThirstSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Emergency ration that vents condensed steam.  The burst hydrates our in-house thirst meter,
 * cools the custom temperature system, and still gives vanilla players a regeneration safety net.
 */
public class SteamCanisterItem extends Item {
    public SteamCanisterItem(Properties properties) {
        super(properties.stacksTo(8));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!level.isClientSide()) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 8, 0));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, 0));
                TemperatureSystem.applyCooling(player, 0.3D);
                ThirstSystem.drink(player, 2, 2);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 0.6F, 1.2F);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.createsurvival.steam_canister.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.createsurvival.steam_canister.tooltip.temperature").withStyle(ChatFormatting.AQUA));
        ThirstSystem.appendHydrationTooltip(tooltip);
    }
}
