package com.cixxyt.createsurvival.registry;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import com.cixxyt.createsurvival.content.blocks.entity.renderer.GyrothermalRegulatorRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only wiring that binds each block entity type to its renderer.  Forge fires the
 * {@link EntityRenderersEvent.RegisterRenderers} hook during the client setup phase, and we use it to
 * plug our bespoke renderer into the registry.
 */
@Mod.EventBusSubscriber(modid = CreateSurvivalMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModBlockEntityRenderers {
    private ModBlockEntityRenderers() {}

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntityTypes.GYROTHERMAL_REGULATOR.get(), GyrothermalRegulatorRenderer::new);
    }
}
