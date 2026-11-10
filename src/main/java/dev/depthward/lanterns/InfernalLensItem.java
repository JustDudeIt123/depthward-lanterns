package dev.depthward.lanterns;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.CuriosApi;
public final class InfernalLensItem extends Item {
    public InfernalLensItem(){super(new Item.Properties().stacksTo(16));}
    public static boolean upgrade(Player player,ItemStack lantern,ItemStack lens) {
        if(!lantern.is(Lanterns.LANTERN.get()) || Fuel.upgraded(lantern) || !lens.is(Lanterns.INFERNAL_LENS.get()))return false;
        lantern.getOrCreateTag().putBoolean("Upgraded",true);
        if(!player.getAbilities().instabuild)lens.shrink(1);
        return true;
    }
    @Override public boolean overrideStackedOnOther(ItemStack lens,Slot slot,ClickAction action,Player player) {
        if(action!=ClickAction.SECONDARY || !slot.getItem().is(Lanterns.LANTERN.get()) || !slot.allowModification(player))return false;
        if(upgrade(player,slot.getItem(),lens))slot.setChanged();
        return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var lens=player.getItemInHand(hand);
        var other=player.getItemInHand(hand==InteractionHand.MAIN_HAND?InteractionHand.OFF_HAND:InteractionHand.MAIN_HAND);
        var target=other.is(Lanterns.LANTERN.get())?other:CuriosApi.getCuriosInventory(player).resolve()
            .flatMap(inv->inv.findFirstCurio(stack->stack.is(Lanterns.LANTERN.get()) && !Fuel.upgraded(stack))).map(result->result.stack()).orElse(ItemStack.EMPTY);
        if(target.isEmpty() || Fuel.upgraded(target))return InteractionResultHolder.pass(lens);
        if(!level.isClientSide)upgrade(player,target,lens);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),level.isClientSide);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player=context.getPlayer();var level=context.getLevel();
        if(player==null || !(level.getBlockEntity(context.getClickedPos()) instanceof LanternEntity lantern))return InteractionResult.PASS;
        var item=lantern.asItem();if(Fuel.upgraded(item))return InteractionResult.PASS;
        if(!level.isClientSide && upgrade(player,item,context.getItemInHand()))lantern.setFuel(item);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
