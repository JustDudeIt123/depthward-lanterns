package dev.depthward.lanterns;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
public final class OilItem extends Item {
    public OilItem() { super(new Item.Properties().stacksTo(16)); }
    public static void consume(Player player, ItemStack oil) {
        if (player.getAbilities().instabuild) return;
        oil.shrink(1);
        var bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (!player.addItem(bottle)) player.drop(bottle, false);
    }
    @Override public boolean overrideStackedOnOther(ItemStack oil, net.minecraft.world.inventory.Slot slot, net.minecraft.world.inventory.ClickAction action, Player player) {
        if (action != net.minecraft.world.inventory.ClickAction.SECONDARY || !slot.getItem().is(Lanterns.LANTERN.get()) || !slot.allowModification(player)) return false;
        var lantern = slot.getItem();
        if (Fuel.remaining(lantern) < Fuel.capacity()) { Fuel.refill(lantern); consume(player, oil); slot.setChanged(); }
        return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var oil = player.getItemInHand(hand);
        var other = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        ItemStack target = other.is(Lanterns.LANTERN.get()) ? other : CuriosApi.getCuriosInventory(player).resolve()
            .flatMap(inv -> inv.findFirstCurio(s -> s.is(Lanterns.LANTERN.get()) && Fuel.remaining(s) < Fuel.capacity()))
            .map(result -> result.stack()).orElse(ItemStack.EMPTY);
        if (target.isEmpty() || Fuel.remaining(target) >= Fuel.capacity()) return InteractionResultHolder.pass(oil);
        if (!level.isClientSide) { Fuel.refill(target); consume(player, oil); }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}
