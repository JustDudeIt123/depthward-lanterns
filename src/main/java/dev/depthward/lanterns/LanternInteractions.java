package dev.depthward.lanterns;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
@Mod.EventBusSubscriber(modid=Lanterns.ID)
public final class LanternInteractions {
    public static InteractionResult pickup(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof LanternEntity be) || player.isSpectator() || !player.mayBuild() || !level.mayInteract(player,pos)) return InteractionResult.FAIL;
        if (player.getInventory().getFreeSlot()<0) {
            if (!level.isClientSide) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.depthward_lanterns.full"),true);
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            var item=be.asItem();
            if (!level.removeBlock(pos,false)) return InteractionResult.FAIL;
            player.getInventory().add(item);
            player.containerMenu.broadcastChanges();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand()!=InteractionHand.MAIN_HAND || !event.getEntity().isShiftKeyDown() || !event.getLevel().getBlockState(event.getPos()).is(Lanterns.BLOCK.get())) return;
        if (!event.getEntity().mayUseItemAt(event.getPos(),event.getFace(),event.getItemStack())) return;
        var result=pickup(event.getLevel(),event.getPos(),event.getEntity());
        event.setCanceled(true);event.setCancellationResult(result);
    }
}
