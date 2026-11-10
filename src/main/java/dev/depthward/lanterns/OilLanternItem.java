package dev.depthward.lanterns;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import java.util.List;

public final class OilLanternItem extends BlockItem implements ICurioItem {
    public OilLanternItem(OilLanternBlock block) { super(block, new Item.Properties().stacksTo(1)); }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof net.minecraft.world.entity.LivingEntity living && (living.getMainHandItem() == stack || living.getOffhandItem() == stack)) Fuel.tick(stack, living);
    }
    @Override public void curioTick(SlotContext ctx, ItemStack stack) { if (!ctx.entity().level().isClientSide) Fuel.tick(stack, ctx.entity()); }
    @Override public boolean canEquipFromUse(SlotContext ctx, ItemStack stack) { return true; }
    @Override public boolean canSync(SlotContext ctx, ItemStack stack) { return true; }
    @Override public net.minecraft.nbt.CompoundTag writeSyncData(SlotContext ctx, ItemStack stack) { return stack.getOrCreateTag().copy(); }
    @Override public void readSyncData(SlotContext ctx, net.minecraft.nbt.CompoundTag tag, ItemStack stack) { stack.setTag(tag.copy()); }
    @Override public boolean isBarVisible(ItemStack stack) { return Fuel.remaining(stack) < Fuel.capacity(); }
    @Override public int getBarWidth(ItemStack stack) { return Math.round(13f * Fuel.remaining(stack) / Fuel.capacity()); }
    @Override public int getBarColor(ItemStack stack) { return 0xE6AD32; }
    @Override public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        boolean enabled = Fuel.enabled(stack);
        lines.add(Component.translatable("tooltip.depthward_lanterns.state")
            .withStyle(net.minecraft.ChatFormatting.WHITE)
            .append(Component.literal(" "))
            .append(Component.translatable(enabled ? "tooltip.depthward_lanterns.enabled" : "tooltip.depthward_lanterns.disabled")
                .withStyle(enabled ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.RED)));
        if(Fuel.upgraded(stack)) lines.add(Component.translatable("tooltip.depthward_lanterns.upgraded").withStyle(net.minecraft.ChatFormatting.AQUA));
    }
    @Override protected boolean placeBlock(net.minecraft.world.item.context.BlockPlaceContext ctx, net.minecraft.world.level.block.state.BlockState state) {
        if (!super.placeBlock(ctx, state)) return false;
        if (ctx.getLevel().getBlockEntity(ctx.getClickedPos()) instanceof LanternEntity be) be.setFuel(ctx.getItemInHand());
        return true;
    }
}
