package com.owenjr.waterturbine.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A helmet-slot diving mask. Grants water breathing and night vision for as long as it's worn,
 * anywhere - but only spends durability (1 point/second) while the wearer is fully submerged,
 * so surface use is free and a full charge is good for about 15 minutes underwater.
 */
public class RebreatherHelmetItem extends ArmorItem {
    /**
     * Night vision's screen effect starts pulsing once its remaining duration is within 200
     * ticks of expiring (see GameRenderer#getNightVisionScale). Keeping the refresh threshold
     * comfortably above that - never letting duration fall below 230 - keeps it rock solid
     * for as long as the mask is worn.
     */
    private static final int EFFECT_DURATION_TICKS = 260;
    private static final int EFFECT_REFRESH_THRESHOLD_TICKS = 230;
    private static final int DURABILITY_TICK_INTERVAL_TICKS = 20;

    public RebreatherHelmetItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, ArmorItem.Type.HELMET, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide()
                || !(entity instanceof LivingEntity living)
                || living.getItemBySlot(EquipmentSlot.HEAD) != stack) {
            return;
        }

        refreshEffect(living, MobEffects.WATER_BREATHING);
        refreshEffect(living, MobEffects.NIGHT_VISION);

        if (living.isUnderWater() && level.getGameTime() % DURABILITY_TICK_INTERVAL_TICKS == 0) {
            stack.hurtAndBreak(1, living, EquipmentSlot.HEAD);
        }
    }

    /**
     * Tops the effect back up before it lapses instead of re-applying every tick, so worn
     * gear doesn't spam potion-effect sync packets while still never visibly running out.
     */
    private static void refreshEffect(LivingEntity living, Holder<MobEffect> effect) {
        MobEffectInstance current = living.getEffect(effect);
        if (current == null || current.getDuration() <= EFFECT_REFRESH_THRESHOLD_TICKS) {
            living.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, 0, false, false, true));
        }
    }
}
