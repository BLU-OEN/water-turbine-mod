package com.owenjr.waterturbine.smoke;

import com.owenjr.waterturbine.WaterTurbineMod;
import com.owenjr.waterturbine.client.TurbineRenderer;
import com.owenjr.waterturbine.block.entity.TurbineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Only compiled by -I tools/smoke.gradle; never included in the release JAR. */
@EventBusSubscriber(modid = WaterTurbineMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class TurbineModelSmoke {
    private static boolean baked;

    @SubscribeEvent
    public static void baked(ModelEvent.BakingCompleted event) {
        var missing = event.getModelManager().getMissingModel();
        for (var state : WaterTurbineMod.WATER_TURBINE.get().getStateDefinition().getPossibleStates()) {
            check(event.getModels().get(BlockModelShaper.stateToModelLocation(state)), missing, "block " + state);
        }
        check(event.getModels().get(TurbineRenderer.ROTOR_MODEL), missing, "rotor");
        check(event.getModels().get(ModelResourceLocation.inventory(
                ResourceLocation.fromNamespaceAndPath("waterturbine", "water_turbine"))), missing, "item");
        baked = true;
        System.out.println("TURBINE_SMOKE: all 8 block states, rotor and inventory models baked without missing textures");
    }

    private static void check(BakedModel model, BakedModel missing, String name) {
        if (model == null || model == missing) throw new AssertionError("Missing model: " + name);
        var quads = model.getQuads(null, null, RandomSource.create(0));
        if (quads.size() < 100) throw new AssertionError("Incomplete geometry: " + name + " " + quads.size());
        for (var quad : quads) {
            var sprite = quad.getSprite().contents().name();
            if (!sprite.getNamespace().equals("waterturbine") || !sprite.getPath().startsWith("block/turbine_")) {
                throw new AssertionError("Unexpected sprite in " + name + ": " + sprite);
            }
        }
    }

    @EventBusSubscriber(modid = WaterTurbineMod.MODID, value = Dist.CLIENT)
    public static final class Finish {
        private static int ticks;
        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            if (!baked || ++ticks < 20) return;
            var mc = Minecraft.getInstance();
            var turbine = new TurbineBlockEntity(BlockPos.ZERO, WaterTurbineMod.WATER_TURBINE.get().defaultBlockState());
            if (!(mc.getBlockEntityRenderDispatcher().getRenderer(turbine) instanceof TurbineRenderer)) {
                throw new AssertionError("Rotor renderer was not registered");
            }
            System.out.println("TURBINE_SMOKE: PASS; client renderer registered");
            mc.stop();
        }
    }
}
