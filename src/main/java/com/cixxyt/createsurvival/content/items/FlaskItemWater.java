package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import com.cixxyt.createsurvival.registry.ModItems;
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
        super(properties);
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
            ThirstCompat.drink(player, 2, 1);
        }

        // Check purity effects
        int purity = ThirstCompat.getPurity(stack);
        ThirstCompat.givePurityEffects(player, purity);

        // If empty, return empty flask
        if (sipsLeft <= 0) {
            return new ItemStack(ModItems.FLASK.get());
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        int sips = stack.getOrCreateTag().getInt("SipsLeft");
        tooltip.add(Component.literal("Sips left: " + sips).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))));

        if (ThirstCompat.isLoaded()) {
            int purity = ThirstCompat.getPurity(stack);
            String purityText = ThirstCompat.getPurityText(purity);
            int color = ThirstCompat.getPurityColor(purity);
            tooltip.add(Component.literal("Purity: " + purityText).withStyle(style -> style.withColor(TextColor.fromRgb(color))));
        }
    }
}
