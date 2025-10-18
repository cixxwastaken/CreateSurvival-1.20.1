package com.cixxyt.createsurvival.content.items;

import com.cixxyt.createsurvival.CreateSurvivalMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Simple armor-material definition for Create: Survival wearables.  Forge expects an
 * {@link ArmorMaterial} implementation so it can ask about durability, defense, and repair
 * ingredients.  We define a single entry for now, but the enum structure keeps the door open for
 * future gear families.
 */
public enum CreateSurvivalArmorMaterials implements ArmorMaterial {
    CLOCKWORK("clockwork", new int[]{13, 15, 16, 11}, new int[]{3, 6, 7, 3}, 15, 1.0F, 0.05F);

    private final String name;
    private final int[] durabilityBySlot;
    private final int[] defenseBySlot;
    private final int enchantValue;
    private final float toughness;
    private final float knockbackResistance;

    CreateSurvivalArmorMaterials(String name, int[] durabilityBySlot, int[] defenseBySlot,
                                 int enchantValue, float toughness, float knockbackResistance) {
        this.name = name;
        this.durabilityBySlot = durabilityBySlot;
        this.defenseBySlot = defenseBySlot;
        this.enchantValue = enchantValue;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return durabilityBySlot[type.ordinal()] * 12;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return defenseBySlot[type.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_IRON;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // Brass ingots do not exist in vanilla, so we leave the repair ingredient empty for now.
        // A later update can plug in a Create material once the dependency is bundled.
        return Ingredient.EMPTY;
    }

    @Override
    public String getName() {
        return CreateSurvivalMod.MODID + ":" + name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
