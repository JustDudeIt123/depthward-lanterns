package dev.depthward.lanterns.client;
import dev.depthward.lanterns.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
@Mod.EventBusSubscriber(modid=Lanterns.ID,value=Dist.CLIENT)
public final class LanternKeys {
    public static final KeyMapping TOGGLE=new KeyMapping("key.depthward_lanterns.toggle",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_R,"key.categories.depthward_lanterns");
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        while(TOGGLE.consumeClick()) {
            var mc=Minecraft.getInstance();
            if(mc.player!=null && mc.screen==null && mc.getConnection()!=null) LanternNetwork.CHANNEL.sendToServer(new LanternNetwork.Toggle());
        }
    }
    @Mod.EventBusSubscriber(modid=Lanterns.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event){event.register(TOGGLE);}
    }
}
