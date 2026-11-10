package dev.depthward.lanterns;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import top.theillusivec4.curios.api.CuriosApi;
public final class LanternNetwork {
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(Lanterns.ID,"controls"),()->"1","1"::equals,"1"::equals);
    public record Toggle() {}
    public static void register() {
        CHANNEL.messageBuilder(Toggle.class,0,NetworkDirection.PLAY_TO_SERVER)
            .encoder((message,buffer)->{}).decoder(buffer->new Toggle())
            .consumerMainThread((message,context)->{
                var sender=context.get().getSender();if(sender!=null)toggle(sender);
                context.get().setPacketHandled(true);
            }).add();
    }
    public static boolean toggle(ServerPlayer player) {
        var targets=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<net.minecraft.world.item.ItemStack,Boolean>());
        if(player.getMainHandItem().is(Lanterns.LANTERN.get()))targets.add(player.getMainHandItem());
        if(player.getOffhandItem().is(Lanterns.LANTERN.get()))targets.add(player.getOffhandItem());
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(inv->inv.findCurios(stack->stack.is(Lanterns.LANTERN.get())).forEach(result->targets.add(result.stack())));
        if(targets.isEmpty()) { player.displayClientMessage(Component.translatable("message.depthward_lanterns.no_lantern"),true);return false; }
        boolean enabled=targets.stream().noneMatch(Fuel::enabled);
        for(var stack:targets)Fuel.setEnabled(stack,enabled);
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(Component.translatable(enabled?"tooltip.depthward_lanterns.on":"tooltip.depthward_lanterns.off"),true);
        return true;
    }
}
