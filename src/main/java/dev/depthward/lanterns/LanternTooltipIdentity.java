package dev.depthward.lanterns;
import net.minecraft.world.item.ItemStack;
/** Comparison-only snapshot: never replace the inventory stack or its fuel data. */
public final class LanternTooltipIdentity {
    public static ItemStack snapshot(ItemStack stack) {
        if(!stack.is(Lanterns.LANTERN.get()))return ItemStack.EMPTY;
        boolean lit=Fuel.lit(stack);
        var result=stack.copy();
        result.removeTagKey("Fuel");result.removeTagKey("FuelTicks");result.removeTagKey("FuelFraction");
        result.getOrCreateTag().putBoolean("DepthwardTooltipLit",lit);
        return result;
    }
}
