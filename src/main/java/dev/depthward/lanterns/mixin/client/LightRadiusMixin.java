package dev.depthward.lanterns.mixin.client;
import dev.depthward.lanterns.client.LanternLight;
import dev.depthward.lanterns.client.LightQueryContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import org.thinkingstudio.ryoamiclights.DynamicLightSource;
import org.thinkingstudio.ryoamiclights.RyoamicLights;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=RyoamicLights.class, remap=false)
public abstract class LightRadiusMixin {
    @Inject(method="maxDynamicLightLevel", at=@At("HEAD"), cancellable=true, remap=false)
    private static void lanternRadius(BlockPos pos, DynamicLightSource source, double current, CallbackInfoReturnable<Double> cir) {
        if (source instanceof LanternLight lantern) {
            if(lantern.ownedBy(LightQueryContext.owner())){cir.setReturnValue(current);return;}
            double dx=pos.getX()+0.5-source.ryoamicLights$getDynamicLightX();
            double dy=pos.getY()+0.5-source.ryoamicLights$getDynamicLightY();
            double dz=pos.getZ()+0.5-source.ryoamicLights$getDynamicLightZ();
            double light=lantern.lightAt(dx,dy,dz);
            cir.setReturnValue(Math.max(current,light));
        }
    }
    @Redirect(method="getLightmapWithDynamicLight(Lnet/minecraft/world/entity/Entity;I)I",at=@At(value="INVOKE",target="Lorg/thinkingstudio/ryoamiclights/RyoamicLights;getDynamicLightLevel(Lnet/minecraft/core/BlockPos;)D"),remap=false)
    private double depthward$entityLight(RyoamicLights lights,BlockPos pos,Entity entity,int packed){
        var previous=LightQueryContext.enter(entity.getUUID());
        try{return lights.getDynamicLightLevel(pos);}
        finally{LightQueryContext.restore(previous);}
    }
}
