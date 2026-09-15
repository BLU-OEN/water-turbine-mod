package com.owenjr.waterturbine;

import com.owenjr.waterturbine.block.TurbineBlock;
import com.owenjr.waterturbine.registry.ModBlockEntities;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(WaterTurbineMod.MODID)
public class WaterTurbineMod {
    public static final String MODID = "waterturbine";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredBlock<TurbineBlock> WATER_TURBINE = BLOCKS.register("water_turbine",
            () -> new TurbineBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.COPPER)
                    .noOcclusion()));

    public static final DeferredItem<BlockItem> WATER_TURBINE_ITEM = ITEMS.registerSimpleBlockItem("water_turbine", WATER_TURBINE);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WATER_TURBINE_TAB = CREATIVE_MODE_TABS.register("water_turbine_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.waterturbine"))
                    .withTabsBefore(CreativeModeTabs.REDSTONE_BLOCKS)
                    .icon(() -> WATER_TURBINE_ITEM.get().getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(WATER_TURBINE_ITEM.get()))
                    .build());

    public WaterTurbineMod(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener((ModConfigEvent event) -> {
            if (event.getConfig().getSpec() == Config.SPEC) {
                Config.refresh();
            }
        });

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.TURBINE_BE.get(),
                (turbine, side) -> turbine.getEnergyStorage());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(WATER_TURBINE_ITEM);
        }
    }
}
