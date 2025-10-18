package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import com.cixxyt.createsurvival.content.blocks.entity.MechanicalLampBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Houses all custom {@link BlockEntityType} registrations.  Forge keeps block entities in their own
 * registry because they carry runtime logic, so grouping them here mirrors the pattern used for
 * items and blocks elsewhere in the mod.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlockEntityTypes {
    private ModBlockEntityTypes() {}

    /** Deferred register that announces our block entity contributions to Forge. */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreateSurvivalMod.MODID);

    /**
     * The mechanical lamp is a passive consumer: it listens for kinetic input and never outputs any.
     * Registering its {@link BlockEntityType} up front means we can safely reference the supplier
     * from both the block and its renderer without worrying about load order.
     */
    public static final RegistryObject<BlockEntityType<MechanicalLampBlockEntity>> MECHANICAL_LAMP =
            BLOCK_ENTITY_TYPES.register("mechanical_lamp",
                    () -> BlockEntityType.Builder.of(MechanicalLampBlockEntity::new,
                            ModBlocks.MECHANICAL_LAMP.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
