package dev.depthward.lanterns.client;
import dev.depthward.lanterns.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
@Mod.EventBusSubscriber(modid=Lanterns.ID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class ClientEvents {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(Lanterns.BLOCK.get(),net.minecraft.client.renderer.RenderType.cutout());
            ItemProperties.register(Lanterns.LANTERN.get(), new ResourceLocation(Lanterns.ID,"empty"), (s,l,e,i) -> Fuel.lit(s) ? 0 : 1);
            LanternCompat.installTracker(LightTracker::track);
            CuriosRendererRegistry.register(Lanterns.LANTERN.get(), BeltRenderer::new);
        });
    }
    // No ItemColor callback: vanilla's default tint is white, preserving the
    // glass/oil sprite without registering a redundant functional interface.
}
