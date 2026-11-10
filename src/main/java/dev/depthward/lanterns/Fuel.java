package dev.depthward.lanterns;
import net.minecraft.world.item.ItemStack;
public final class Fuel {
    public static int capacity() { return LanternConfig.SPEC.isLoaded() ? LanternConfig.DURABILITY.get() : LanternConfig.DURABILITY.getDefault(); }
    public static int remaining(ItemStack stack) {
        if (!stack.is(Lanterns.LANTERN.get())) return 0;
        var tag = stack.getTag();
        return tag == null || !tag.contains("Fuel") ? capacity() : Math.max(0, Math.min(capacity(), tag.getInt("Fuel")));
    }
    public static boolean upgraded(ItemStack stack) { return stack.is(Lanterns.LANTERN.get()) && stack.getTag()!=null && stack.getTag().getBoolean("Upgraded"); }
    public static boolean enabled(ItemStack stack) { return stack.is(Lanterns.LANTERN.get()) && (stack.getTag() == null || !stack.getTag().getBoolean("Disabled")); }
    public static boolean lit(ItemStack stack) { return enabled(stack) && remaining(stack) > 0; }
    public static void setEnabled(ItemStack stack, boolean enabled) { stack.getOrCreateTag().putBoolean("Disabled", !enabled); }
    public static void refill(ItemStack stack) { stack.getOrCreateTag().putInt("Fuel", capacity()); stack.getOrCreateTag().putInt("FuelTicks", 0); stack.getOrCreateTag().putDouble("FuelFraction", 0); }
    public static void tick(ItemStack stack) { tick(stack, null); }
    public static void tick(ItemStack stack, net.minecraft.world.entity.LivingEntity holder) {
        if (!enabled(stack)) return;
        var tag = stack.getOrCreateTag();
        double efficiency = holder instanceof net.minecraft.world.entity.player.Player && holder.getAttribute(Lanterns.FUEL_EFFICIENCY.get()) != null
            ? holder.getAttributeValue(Lanterns.FUEL_EFFICIENCY.get()) : 0;
        var next = FuelMath.tickScaled(remaining(stack), tag.getInt("FuelTicks"), tag.getDouble("FuelFraction"), capacity(), LanternConfig.TICKS.get(), efficiency);
        tag.putInt("Fuel", next.remaining()); tag.putInt("FuelTicks", next.progress()); tag.putDouble("FuelFraction", next.fraction());
    }
}
