package com.cixxyt.createsurvival.compat.thirst;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;

import java.lang.reflect.Method;

public class ThirstCompat {

    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }

    public static void init() {
        try {
            Class.forName("dev.ghen.thirst.foundation.common.capability.ModCapabilities");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
    }

    public static void drink(Player player, int thirst, int quenched) {
        if (!loaded) return;
        try {
            Class<?> modCapsClass = Class.forName("dev.ghen.thirst.foundation.common.capability.ModCapabilities");
            Object PLAYER_THIRST_CAP = modCapsClass.getField("PLAYER_THIRST").get(null);
            Object cap = player.getClass().getMethod("getCapability", Class.class).invoke(player, PLAYER_THIRST_CAP);

            if (cap != null) {
                Class<?> iThirstClass = Class.forName("dev.ghen.thirst.foundation.common.capability.IThirst");
                Method drinkMethod = iThirstClass.getMethod("drink", Player.class, int.class, int.class);
                drinkMethod.invoke(cap, player, thirst, quenched);
            }
        } catch (Exception ignored) {}
    }
}
