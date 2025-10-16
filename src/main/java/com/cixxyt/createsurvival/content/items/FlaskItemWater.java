package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.registry.ModItems;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class FlaskItemWater extends Item {

    public FlaskItemWater(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;

        // Consume a sip
        int sipsLeft = stack.getOrCreateTag().getInt("SipsLeft");
        sipsLeft--;
        stack.getOrCreateTag().putInt("SipsLeft", sipsLeft);

        // Apply Thirst effects
        if (!level.isClientSide) {
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(thirst -> {
                thirst.drink(player, 2, 1); // 2 units, 1 saturation
            });
        }

        // Check purity effects
        int purity = WaterPurity.getPurity(stack);
        WaterPurity.givePurityEffects(player, purity);

        // If empty, return empty flask
        if (sipsLeft <= 0) {
            return new ItemStack(ModItems.FLASK.get());
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        int sips = stack.getOrCreateTag().getInt("SipsLeft");
        int purity = WaterPurity.getPurity(stack);
        String purityText = WaterPurity.getPurityText(purity);

        tooltip.add(Component.literal("Sips left: " + sips).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))));
        tooltip.add(Component.literal("Purity: " + purityText).withStyle(style -> style.withColor(TextColor.fromRgb(WaterPurity.getPurityColor(purity)))));
    }
}
