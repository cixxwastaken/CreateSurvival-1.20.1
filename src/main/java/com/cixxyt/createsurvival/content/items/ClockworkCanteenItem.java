package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.systems.ThirstSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Multi-use water container that stores the purity of the source block used to fill it.  The
 * implementation mirrors Create's mechanical vibe while teaching how to juggle Forge's item
 * lifecycle hooks and a professor-friendly survival manager instead of delegating to third-party
 * hydration mods.
 */
public class ClockworkCanteenItem extends Item {
    private static final String TAG_SIPS = "Sips";
    private static final String TAG_LAST_PURITY = "Purity";
    public static final int MAX_SIPS = 6;
    public static final int MAX_PURITY = 3;

    public ClockworkCanteenItem(Properties properties) {
        // A singleton stack keeps the bookkeeping simple and matches the "cherished tool" fantasy.
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isFilled(stack)) {
            // Empty canteens try to collect water from the targeted block.  Forge's helper performs
            // the ray trace, automatically respecting sneaking and fluid filtering rules.
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = hitResult.getBlockPos();
                FluidState fluid = level.getFluidState(pos);
                if (fluid.isSource() && fluid.is(FluidTags.WATER)) {
                    if (!level.isClientSide()) {
                        fillFromSource(stack, level, pos);
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                }
            }
            return InteractionResultHolder.pass(stack);
        }

        // Filled canteens behave like vanilla potions: hold the button to start drinking.
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
            // Our in-house thirst system mirrors the old API but keeps everything inside this mod.
            ThirstSystem.applyPurityEffects(player, purity);
            ThirstSystem.drink(player, Math.max(2, purity + 1), purity);
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.createsurvival.clockwork_canteen.sips", getSips(stack), MAX_SIPS).withStyle(ChatFormatting.GRAY));
        int purity = getPurity(stack);
        if (purity > 0) {
            tooltip.add(ThirstSystem.createPurityTooltip(purity));
        } else {
            tooltip.add(Component.translatable("item.createsurvival.clockwork_canteen.purity", purity).withStyle(ChatFormatting.DARK_RED));
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
        return ThirstSystem.getPurityColor(purity);
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
        return 0;
    }

    public static void setPurity(ItemStack stack, int purity) {
        purity = Math.min(Math.max(0, purity), MAX_PURITY);
        stack.getOrCreateTag().putInt(TAG_LAST_PURITY, purity);
    }

    public static void clearPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_LAST_PURITY);
        }
    }

    public static void fillFromSource(ItemStack stack, Level level, BlockPos pos) {
        setSips(stack, MAX_SIPS);
        int purity = ThirstSystem.calculatePurity(level, pos);
        if (purity == 0) {
            purity = level.getFluidState(pos).isSource() ? MAX_PURITY : 1;
        }
        setPurity(stack, purity);
    }
}
