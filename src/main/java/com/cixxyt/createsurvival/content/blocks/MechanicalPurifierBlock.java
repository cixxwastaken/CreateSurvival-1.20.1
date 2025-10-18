package com.cixxyt.createsurvival.content.blocks;

import com.cixxyt.createsurvival.content.items.ClockworkCanteenItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;

public class MechanicalPurifierBlock extends Block {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public MechanicalPurifierBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof ClockworkCanteenItem)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!ClockworkCanteenItem.isFilled(held)) {
            player.displayClientMessage(Component.translatable("block.createsurvival.mechanical_purifier.empty").withStyle(ChatFormatting.RED), true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!state.getValue(POWERED)) {
            player.displayClientMessage(Component.translatable("block.createsurvival.mechanical_purifier.unpowered").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        int before = ClockworkCanteenItem.getPurity(held);
        //ClockworkCanteenItem.boostPurity(held);
        int after = ClockworkCanteenItem.getPurity(held);
        if (after > before) {
            level.levelEvent(2005, pos, 0);
            player.displayClientMessage(Component.translatable("block.createsurvival.mechanical_purifier.purified", after).withStyle(ChatFormatting.AQUA), true);
        } else {
            player.displayClientMessage(Component.translatable("block.createsurvival.mechanical_purifier.maxed").withStyle(ChatFormatting.GRAY), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return true;
    }
}
