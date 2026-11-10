package dev.depthward.lanterns;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Lanterns.ID)
public final class Lanterns {
    public static final String ID = "depthward_lanterns";
    public static final DeferredRegister<net.minecraft.world.entity.ai.attributes.Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ID);
    public static final RegistryObject<net.minecraft.world.entity.ai.attributes.Attribute> FUEL_EFFICIENCY = ATTRIBUTES.register("fuel_efficiency",
        () -> new net.minecraft.world.entity.ai.attributes.RangedAttribute("attribute.depthward_lanterns.fuel_efficiency", 0, 0, 80).setSyncable(true));
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ID);
    public static final RegistryObject<net.minecraft.sounds.SoundEvent> WHISPER = SOUNDS.register("darkness_whisper",
        () -> net.minecraft.sounds.SoundEvent.createVariableRangeEvent(new net.minecraft.resources.ResourceLocation(ID,"darkness_whisper")));
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ID);
    public static final RegistryObject<OilLanternBlock> BLOCK = BLOCKS.register("oil_lantern", OilLanternBlock::new);
    public static final RegistryObject<OilLanternItem> LANTERN = ITEMS.register("oil_lantern", () -> new OilLanternItem(BLOCK.get()));
    public static final RegistryObject<Item> INFERNAL_LENS = ITEMS.register("infernal_lens", InfernalLensItem::new);
    public static final RegistryObject<Item> OIL = ITEMS.register("oil", OilItem::new);
    public static final RegistryObject<BlockEntityType<LanternEntity>> ENTITY = ENTITIES.register("oil_lantern", () -> BlockEntityType.Builder.of(LanternEntity::new, BLOCK.get()).build(null));
    public Lanterns() {
        LanternNetwork.register();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ATTRIBUTES.register(bus); BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); SOUNDS.register(bus); LanternEffects.EFFECTS.register(bus);
        bus.addListener(LanternConfig::loaded);
        bus.addListener(this::attributes);
        bus.addListener(this::creative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, LanternConfig.SPEC);
    }
    private void attributes(net.minecraftforge.event.entity.EntityAttributeModificationEvent event) {
        event.add(net.minecraft.world.entity.EntityType.PLAYER, FUEL_EFFICIENCY.get());
    }
    private void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) { event.accept(LANTERN); event.accept(OIL); event.accept(INFERNAL_LENS); }
    }
}
