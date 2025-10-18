package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.compat.coldsweat.ColdSweatCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Chest-slot armor piece that bridges Create's mechanical aesthetic with survival utilities.
 * While worn it keeps the player comfortable by nudging Cold Sweat's temperature system (when
 * available) or, as a fallback, by supplying a brief vanilla resistance boost.
 */
public class ThermoVestItem extends ArmorItem {
    private static final int RESISTANCE_DURATION = 20 * 6;

    public ThermoVestItem(Properties properties) {
        super(CreateSurvivalArmorMaterials.CLOCKWORK, Type.CHESTPLATE, properties);
    }


    public void onArmorTick(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return;
        }

        // First attempt the Cold Sweat integration.  The compat helper returns true when the
        // mod is present and accepted our temperature adjustment, letting us skip the fallback.
        boolean handledByColdSweat = ColdSweatCompat.applyComfort(player, 0.25D);
        if (!handledByColdSweat) {
            // The short resistance effect simulates the vest absorbing environmental extremes.
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, RESISTANCE_DURATION, 0, true, false));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.createsurvival.thermo_vest.tooltip").withStyle(ChatFormatting.GRAY));
        if (ColdSweatCompat.isLoaded()) {
            tooltip.add(Component.translatable("item.createsurvival.thermo_vest.tooltip.coldsweat").withStyle(ChatFormatting.AQUA));
        }
    }


    public boolean canEquip(ItemStack stack, EquipmentSlot slot, net.minecraft.world.entity.LivingEntity entity) {
        return slot == EquipmentSlot.CHEST;
    }
}
