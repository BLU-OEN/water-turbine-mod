package com.owenjr.waterturbine.client;

import com.owenjr.waterturbine.WaterTurbineMod;
import com.owenjr.waterturbine.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Client-only registration keeps rendering classes off the dedicated server. */
@EventBusSubscriber(modid = WaterTurbineMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TurbineClient {
    private TurbineClient() {}

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(TurbineRenderer.ROTOR_MODEL);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.TURBINE_BE.get(), TurbineRenderer::new);
    }
}
