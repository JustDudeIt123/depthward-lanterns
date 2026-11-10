package dev.depthward.lanterns;
import net.minecraft.world.effect.*;
import net.minecraftforge.registries.*;
public final class LanternEffects {
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,Lanterns.ID);
    public static final RegistryObject<MobEffect> DARKNESS=EFFECTS.register("shrouded_in_darkness",()->new EnvironmentalEffect(0x30223F));
    public static final RegistryObject<MobEffect> HELLFIRE=EFFECTS.register("hellfire",()->new EnvironmentalEffect(0xF04B22));
    /** Damage is driven by HazardSystem while the environment remains dangerous. */
    private static final class EnvironmentalEffect extends MobEffect {
        EnvironmentalEffect(int color){super(MobEffectCategory.HARMFUL,color);}
    }
}
