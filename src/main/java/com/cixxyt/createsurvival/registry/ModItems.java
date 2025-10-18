package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import com.cixxyt.createsurvival.content.items.ClockworkCanteenItem;
import com.cixxyt.createsurvival.content.items.SteamCanisterItem;
import com.cixxyt.createsurvival.content.items.SteamRationItem;
import com.cixxyt.createsurvival.content.items.SurveyorThermometerItem;
import com.cixxyt.createsurvival.content.items.ThermoVestItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Master item registry.  Each {@link RegistryObject} lazily constructs the item when Forge asks
 * for it, so it is safe to reference these suppliers anywhere once registration has completed.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreateSurvivalMod.MODID);

    /**
     * The lamp's {@link BlockItem} makes it placeable from an inventory slot.  Because the block
     * relies on Create for power, we tuck the explanation directly in the comment so readers learn
     * that it passively listens for stress without ever generating its own.
     */
    public static final RegistryObject<Item> MECHANICAL_LAMP = ITEMS.register("mechanical_lamp",
            () -> new BlockItem(ModBlocks.MECHANICAL_LAMP.get(), new Item.Properties()));

    /**
     * The regulator's block item includes a tooltip so students remember which Create tools affect
     * its output.  Anonymous subclassing keeps the registration succinct while still allowing us to
     * inject professor-style commentary.
     */
    public static final RegistryObject<Item> GYROTHERMAL_REGULATOR = ITEMS.register("gyrothermal_regulator",
            () -> new BlockItem(ModBlocks.GYROTHERMAL_REGULATOR.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.createsurvival.gyrothermal_regulator").withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.translatable("tooltip.createsurvival.gyrothermal_regulator.controls").withStyle(ChatFormatting.AQUA));
                }
            });

    public static final RegistryObject<Item> CLOCKWORK_CANTEEN = ITEMS.register("clockwork_canteen",
            () -> new ClockworkCanteenItem(new Item.Properties()));

    public static final RegistryObject<Item> STEAM_RATION = ITEMS.register("steam_ration",
            () -> new SteamRationItem(new Item.Properties()));

    public static final RegistryObject<Item> STEAM_CANISTER = ITEMS.register("steam_canister",
            () -> new SteamCanisterItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    public static final RegistryObject<Item> SURVEYOR_THERMOMETER = ITEMS.register("surveyor_thermometer",
            () -> new SurveyorThermometerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> THERMO_VEST = ITEMS.register("thermo_vest",
            () -> new ThermoVestItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
