package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.compat.thirst.ThirstCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class ClockworkCanteenItem extends Item {
    private static final String TAG_SIPS = "Sips";
    private static final String TAG_LAST_PURITY = "Purity";
    public static final int MAX_SIPS = 6;
    public static final int MAX_PURITY = 3;

    public ClockworkCanteenItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isFilled(stack)) {
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                FluidState fluid = level.getFluidState(pos);
                if (fluid.isSource() && fluid.is(ThirstCompat.getWaterTag())) {
                    if (!level.isClientSide()) {
                        fillFromSource(stack, level, pos);
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }
            return InteractionResultHolder.pass(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 24;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!(living instanceof Player player)) {
            return super.finishUsingItem(stack, level, living);
        }

        int purity = getPurity(stack);
        if (!level.isClientSide()) {
            ThirstCompat.applyPurityEffects(player, stack);
            ThirstCompat.drink(player, Math.max(2, purity + 1), Math.max(1, purity));
        }

        if (!player.getAbilities().instabuild) {
            setSips(stack, Math.max(0, getSips(stack) - 1));
            if (!isFilled(stack)) {
                clearPurity(stack);
            }
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5F, 1.0F);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        int sips = getSips(stack);
        tooltip.add(Component.translatable("item.createsurvival.clockwork_canteen.sips", sips, MAX_SIPS).withStyle(ChatFormatting.GRAY));
        ThirstCompat.appendPurityTooltip(stack, tooltip);
        if (!ThirstCompat.isLoaded()) {
            int purity = getPurity(stack);
            if (purity > 0) {
                tooltip.add(Component.translatable("item.createsurvival.clockwork_canteen.purity", purity).withStyle(ChatFormatting.AQUA));
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isFilled(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getSips(stack) / (float) MAX_SIPS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int purity = getPurity(stack);
        return ThirstCompat.getPurityColor(purity);
    }

    public static boolean isFilled(ItemStack stack) {
        return getSips(stack) > 0;
    }

    public static int getSips(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(TAG_SIPS) : 0;
    }

    public static void setSips(ItemStack stack, int sips) {
        CompoundTag tag = stack.getOrCreateTag();
        if (sips <= 0) {
            tag.remove(TAG_SIPS);
        } else {
            tag.putInt(TAG_SIPS, Math.min(sips, MAX_SIPS));
        }
    }

    public static int getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_LAST_PURITY)) {
            return Math.min(MAX_PURITY, tag.getInt(TAG_LAST_PURITY));
        }
        return ThirstCompat.getPurity(stack);
    }

    public static void setPurity(ItemStack stack, int purity) {
        purity = Math.min(Math.max(0, purity), MAX_PURITY);
        ThirstCompat.setPurity(stack, purity);
        stack.getOrCreateTag().putInt(TAG_LAST_PURITY, purity);
    }

    public static void clearPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_LAST_PURITY);
        }
        ThirstCompat.setPurity(stack, 0);
    }

    public static void boostPurity(ItemStack stack) {
        setPurity(stack, Math.min(MAX_PURITY, getPurity(stack) + 1));
    }

    public static void fillFromSource(ItemStack stack, Level level, BlockPos pos) {
        setSips(stack, MAX_SIPS);
        ThirstCompat.applyBlockPurity(stack, level, pos);
        int purity = ThirstCompat.getPurity(stack);
        if (purity == 0) {
            purity = level.getFluidState(pos).isSource() ? MAX_PURITY : 1;
        }
        setPurity(stack, purity);
    }
}
