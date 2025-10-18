package com.cixxyt.createsurvival;

import com.cixxyt.createsurvival.registry.ModCreativeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Helper that defines the dedicated creative tab for Create: Survival goodies.
 */
public final class CreateSurvivalTab {
    private CreateSurvivalTab() {}

    /**
     * Convenience helper that exposes the registry key.  Anywhere in the codebase we can compare
     * against this method to see if Forge is currently operating on our tab without grabbing the
     * full {@link CreativeModeTab} instance.
     */
    public static ResourceKey<CreativeModeTab> key() {
        return ModCreativeTabs.CREATE_SURVIVAL_TAB_KEY;
    }

    /**
     * Retrieves the fully constructed creative tab.  The heavy lifting happens in
     * {@link ModCreativeTabs}, and calling {@code get()} here keeps the rest of the mod blissfully
     * unaware of the registration plumbing.
     */
    public static CreativeModeTab tab() {
        return ModCreativeTabs.CREATE_SURVIVAL_TAB.get();
    }
}
