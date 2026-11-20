package dev.depthward.lanterns.mixin.client;
import dev.depthward.lanterns.Lanterns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thinkingstudio.ryoamiclights.api.item.ItemLightSources;
@Mixin(value=ItemLightSources.class,remap=false)
public abstract class ItemLightSourcesMixin {
    @Inject(method="getLuminance",at=@At("HEAD"),cancellable=true,remap=false)
    private static void lanternOwnSource(ItemStack stack,boolean water,CallbackInfoReturnable<Integer> cir) {
        // Our state-aware LightTracker is the sole source, including when the lantern is lit.
        if(stack.is(Lanterns.LANTERN.get()))cir.setReturnValue(0);
    }
}
