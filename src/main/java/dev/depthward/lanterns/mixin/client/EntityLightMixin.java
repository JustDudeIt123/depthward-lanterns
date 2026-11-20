package dev.depthward.lanterns.mixin.client;
import dev.depthward.lanterns.client.LightQueryContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(EntityRenderer.class)
public abstract class EntityLightMixin {
    @Redirect(method="getPackedLightCoords",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/entity/EntityRenderer;getBlockLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I"))
    private int depthward$excludeOwnLantern(EntityRenderer<?> renderer,Entity entity,BlockPos pos){
        var previous=LightQueryContext.enter(entity.getUUID());
        try{return ((EntityLightAccess)renderer).depthward$blockLight(entity,pos);}
        finally{LightQueryContext.restore(previous);}
    }
}
