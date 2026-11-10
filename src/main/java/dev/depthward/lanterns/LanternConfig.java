package dev.depthward.lanterns;
import net.minecraftforge.common.ForgeConfigSpec;
public final class LanternConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue DURABILITY;
    public static final ForgeConfigSpec.IntValue TICKS;
    public static final ForgeConfigSpec.DoubleValue HELD_RADIUS, HELD_WATER_RADIUS, PLACED_RADIUS, PLACED_WATER_RADIUS;
    public static double radius(boolean placed,boolean underwater) {
        var value=placed?(underwater?PLACED_WATER_RADIUS:PLACED_RADIUS):(underwater?HELD_WATER_RADIUS:HELD_RADIUS);
        return SPEC.isLoaded()?value.get():value.getDefault();
    }
    public static int blockLight(boolean underwater) { return Math.min(15,(int)Math.ceil(radius(true,underwater))); }
    public static final ForgeConfigSpec.BooleanValue DARKNESS_ENABLED, HELLFIRE_ENABLED;
    public static final ForgeConfigSpec.IntValue DARK_BLOCK_LIGHT, DARK_SKY_LIGHT, DARK_WARNING_TICKS, DARK_DAMAGE_INTERVAL, HELLFIRE_DAMAGE_INTERVAL;
    public static final ForgeConfigSpec.IntValue DAY_SKY_LIGHT, DAY_CAVE_DEPTH, DAY_CAVE_ROOF, DAY_CAVE_SCAN;
    public static final ForgeConfigSpec.DoubleValue DARK_INITIAL_DAMAGE, DARK_DAMAGE_INCREASE, DARK_MAX_DAMAGE, WHISPER_VOLUME, HELLFIRE_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue RADIUS_19_APPLIED;
    public static void loaded(net.minecraftforge.fml.event.config.ModConfigEvent.Loading event) {
        if(event.getConfig().getSpec()!=SPEC || RADIUS_19_APPLIED.get()) return;
        HELD_RADIUS.set(19.0); PLACED_RADIUS.set(19.0); RADIUS_19_APPLIED.set(true);
        event.getConfig().save();
    }
    static {
        var b = new ForgeConfigSpec.Builder();
        b.push("fuel");
        DURABILITY = b.comment("Fuel capacity. Zero fuel extinguishes the lantern, never destroys it.").defineInRange("durability", 1200, 1, 1000000);
        TICKS = b.comment("Ticks of active use per one fuel point. 20 ticks = 1 second.").defineInRange("damageIntervalTicks", 20, 1, 1200000);
        b.pop();
        b.push("light");
        RADIUS_19_APPLIED=b.comment("One-time migration to the 19-block pack profile; later manual radius changes are preserved.").define("radius19Applied",false);
        HELD_RADIUS=b.comment("Light radius in blocks when held or equipped in Curios. Zero disables this light.").defineInRange("heldRadius",19.0,0.0,64.0);
        HELD_WATER_RADIUS=b.comment("Held/Curios light radius underwater, in blocks.").defineInRange("heldUnderwaterRadius",3.875,0.0,64.0);
        PLACED_RADIUS=b.comment("Light radius of placed lanterns, in blocks.").defineInRange("placedRadius",19.0,0.0,64.0);
        PLACED_WATER_RADIUS=b.comment("Placed lantern light radius underwater, in blocks.").defineInRange("placedUnderwaterRadius",7.0,0.0,64.0);
        b.pop();
        b.push("darkness");
        DARKNESS_ENABLED=b.define("enabled",true);
        DARK_BLOCK_LIGHT=b.comment("Maximum block light for darkness danger; held item light is evaluated on the server too.").defineInRange("maximumBlockLight",0,0,15);
        DARK_SKY_LIGHT=b.comment("Maximum effective sky/combined light, after night darkening. Deterministic upper bound of vanilla hostile spawn light test.").defineInRange("maximumSkyLight",7,0,15);
        DAY_SKY_LIGHT=b.comment("During solar daytime only dark underground areas are dangerous. Rain does not turn daytime into night.").defineInRange("daytimeMaximumSkyLight",2,0,15);
        DAY_CAVE_DEPTH=b.comment("Minimum depth below the surface without leaves. Three of five nearby columns must qualify.").defineInRange("daytimeMinimumDepth",6,1,64);
        DAY_CAVE_ROOF=b.comment("Consecutive natural ceiling blocks from the daytime_cave_roof block tag. Ordinary roofs are excluded.").defineInRange("daytimeMinimumRoofThickness",3,1,16);
        DAY_CAVE_SCAN=b.comment("Bounded upward scan height in loaded chunks only.").defineInRange("daytimeRoofScanHeight",96,8,256);
        DARK_WARNING_TICKS=b.comment("Ticks after the initial whisper before the darkness effect begins. 200 ticks = 10 seconds.").defineInRange("warningTicks",200,0,1200000);
        DARK_DAMAGE_INTERVAL=b.defineInRange("damageIntervalTicks",40,1,1200000);
        DARK_INITIAL_DAMAGE=b.comment("Health points, not hearts. Damage sequence = initialDamage + hitIndex * damageIncrease, capped by maximumDamage.").defineInRange("initialDamage",1.0,0.0,1024.0);
        DARK_DAMAGE_INCREASE=b.defineInRange("damageIncrease",1.0,0.0,1024.0);
        DARK_MAX_DAMAGE=b.defineInRange("maximumDamage",20.0,0.0,1024.0);
        WHISPER_VOLUME=b.defineInRange("whisperVolume",0.8,0.0,4.0);
        b.pop();
        b.push("hellfire");
        HELLFIRE_ENABLED=b.define("enabled",true);
        HELLFIRE_DAMAGE_INTERVAL=b.defineInRange("damageIntervalTicks",40,1,1200000);
        HELLFIRE_DAMAGE=b.comment("Periodic health damage in the Nether without an upgraded lit lantern in hands or Curios. Does not ignite the player.").defineInRange("damage",1.0,0.0,1024.0);
        b.pop(); SPEC = b.build();
    }
}
