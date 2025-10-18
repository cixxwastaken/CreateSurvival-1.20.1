package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import com.cixxyt.createsurvival.registry.ModItems;
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
import net.minecraft.core.BlockPos;
//=======
//>>>>>>> master
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

import java.util.List;

public class FlaskItemWater extends Item {

    public static final String SIPS_TAG = "SipsLeft";
    public static final int DEFAULT_SIPS = 5;

    public FlaskItemWater(Properties properties) {
        super(properties);
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
    }

    public static ItemStack createFilledStack(Level level, BlockPos sourcePos) {
        ItemStack stack = new ItemStack(ModItems.FLASK_WATER.get());
        setSips(stack, DEFAULT_SIPS);
        ThirstCompat.applyBlockPurity(stack, level, sourcePos);
        return stack;
    }

    public static void setSips(ItemStack stack, int sips) {
        stack.getOrCreateTag().putInt(SIPS_TAG, Math.max(sips, 0));
    }

    public static int getSips(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(SIPS_TAG)) {
            return stack.getTag().getInt(SIPS_TAG);
        }
        return DEFAULT_SIPS;
    }

    private static void ensureSipsTag(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(SIPS_TAG)) {
            setSips(stack, DEFAULT_SIPS);
        }
//=======
//>>>>>>> master
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

        ensureSipsTag(stack);
        int purity = ThirstCompat.getPurity(stack);

        if (!level.isClientSide) {
            ThirstCompat.drink(player, 2, 1);
        }

//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
//=======
        // Check purity effects
        int purity = ThirstCompat.getPurity(stack);
//>>>>>>> master
        ThirstCompat.givePurityEffects(player, purity);

        if (!player.getAbilities().instabuild) {
            int sipsLeft = getSips(stack) - 1;
            if (sipsLeft <= 0) {
                return new ItemStack(ModItems.FLASK.get());
            }
            setSips(stack, sipsLeft);
        }

        return stack;
    }

    @Override
//<<<<<<< codex/fix-crashing-errors-related-to-tooltip-828nej
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        ensureSipsTag(stack);
        int sips = getSips(stack);
        tooltip.add(Component.literal("Sips left: " + sips).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))));

        ThirstCompat.appendPurityTooltip(stack, tooltip);

        super.appendHoverText(stack, level, tooltip, flag);
//=======
    public void appendHoverText(ItemStack stack, Level level, java.util.List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        int sips = stack.getOrCreateTag().getInt("SipsLeft");
        tooltip.add(Component.literal("Sips left: " + sips).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))));

        if (ThirstCompat.isLoaded()) {
            int purity = ThirstCompat.getPurity(stack);
            String purityText = ThirstCompat.getPurityText(purity);
            int color = ThirstCompat.getPurityColor(purity);
            tooltip.add(Component.literal("Purity: " + purityText).withStyle(style -> style.withColor(TextColor.fromRgb(color))));
        }
//>>>>>>> master
    }
}
