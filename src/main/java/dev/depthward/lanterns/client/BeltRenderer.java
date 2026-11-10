package dev.depthward.lanterns.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
public final class BeltRenderer implements ICurioRenderer {
    public static final float SCALE=0.625f;
    @Override public <T extends LivingEntity,M extends EntityModel<T>> void render(ItemStack stack, SlotContext ctx, PoseStack pose, RenderLayerParent<T,M> parent, MultiBufferSource buffer, int light, float limbSwing, float limbAmount, float partial, float age, float yaw, float pitch) {
        if (!(parent.getModel() instanceof HumanoidModel<?> model)) return;
        pose.pushPose();
        model.body.translateAndRotate(pose);
        // Player's right is negative model X; negative Z is the front.
        pose.translate(-0.27, 0.64, -0.22);
        float sway = (float)Math.sin(limbSwing * 0.65f) * Math.min(1f, limbAmount) * 18f;
        pose.mulPose(Axis.ZP.rotationDegrees(sway));
        pose.mulPose(Axis.XP.rotationDegrees((float)Math.cos(limbSwing * 0.65f) * Math.min(1f,limbAmount) * 9f));
        pose.translate(0,0.1875*SCALE,0);
        pose.mulPose(Axis.XP.rotationDegrees(180));
        pose.scale(SCALE,SCALE,SCALE);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, pose, buffer, ctx.entity().level(), ctx.entity().getId());
        pose.popPose();
    }
}
