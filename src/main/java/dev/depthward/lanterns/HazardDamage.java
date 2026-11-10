package dev.depthward.lanterns;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
public final class HazardDamage {
    public static final ResourceKey<DamageType> DARKNESS=key("shrouded_in_darkness"), HELLFIRE=key("hellfire");
    private static ResourceKey<DamageType> key(String name){return ResourceKey.create(Registries.DAMAGE_TYPE,new ResourceLocation(Lanterns.ID,name));}
    public static DamageSource source(Level level,ResourceKey<DamageType> key){return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));}
}
