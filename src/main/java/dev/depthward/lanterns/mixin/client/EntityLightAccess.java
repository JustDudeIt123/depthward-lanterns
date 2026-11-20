package dev.depthward.lanterns.mixin.client;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(EntityRenderer.class)
public interface EntityLightAccess {
    @Invoker("getBlockLightLevel") int depthward$blockLight(Entity entity,BlockPos pos);
}
