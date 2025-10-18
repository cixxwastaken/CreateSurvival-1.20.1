package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.systems.TemperatureSystem;
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
 * While worn it keeps the player comfortable by nudging our in-house temperature system and, when
 * necessary, falling back to vanilla resistances for clarity.
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

        boolean handled = TemperatureSystem.applyComfort(player, 0.25D);
        if (!handled) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, RESISTANCE_DURATION, 0, true, false));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.createsurvival.thermo_vest.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.createsurvival.thermo_vest.tooltip.temperature").withStyle(ChatFormatting.AQUA));
    }


    public boolean canEquip(ItemStack stack, EquipmentSlot slot, net.minecraft.world.entity.LivingEntity entity) {
        return slot == EquipmentSlot.CHEST;
    }
}
