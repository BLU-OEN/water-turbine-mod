package com.owenjr.waterturbine.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.owenjr.waterturbine.WaterTurbineMod;
import com.owenjr.waterturbine.block.entity.TurbineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class TurbineRenderer implements BlockEntityRenderer<TurbineBlockEntity> {
    public static final ModelResourceLocation ROTOR_MODEL = ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(WaterTurbineMod.MODID, "block/turbine_rotor"));

    public TurbineRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TurbineBlockEntity turbine, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        var state = turbine.getBlockState();
        var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var minecraft = Minecraft.getInstance();
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        // Match the baked body's blockstate rotation (north = zero).
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        pose.mulPose(Axis.ZP.rotationDegrees(turbine.getRotorAngle(partialTick)));
        pose.translate(-0.5, -0.5, -0.5);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
                pose.last(), buffers.getBuffer(RenderType.cutout()), state,
                minecraft.getModelManager().getModel(ROTOR_MODEL),
                1.0F, 1.0F, 1.0F, light, overlay);
        pose.popPose();
    }
}
