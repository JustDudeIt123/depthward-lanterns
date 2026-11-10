package dev.depthward.lanterns;
import java.lang.ref.WeakReference;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.CuriosApi;
/** Server approximation of this mod's visual lights; no chunk loads or world-wide block scans. */
public final class HazardLights {
    private static final Map<ServerLevel,Map<BlockPos,WeakReference<LanternEntity>>> PLACED=new WeakHashMap<>();
    public static void register(LanternEntity be) {
        if(be.getLevel() instanceof ServerLevel level)PLACED.computeIfAbsent(level,key->new HashMap<>()).put(be.getBlockPos(),new WeakReference<>(be));
    }
    public static void remove(LanternEntity be) {
        if(be.getLevel() instanceof ServerLevel level){var map=PLACED.get(level);if(map!=null && map.get(be.getBlockPos())!=null && map.get(be.getBlockPos()).get()==be)map.remove(be.getBlockPos());}
    }
    public static boolean carriesLight(LivingEntity entity,boolean upgradedOnly) {
        if(LanternConfig.radius(false,entity.isInWater())<=0)return false;
        java.util.function.Predicate<ItemStack> valid=stack->Fuel.lit(stack) && (!upgradedOnly || Fuel.upgraded(stack));
        return valid.test(entity.getMainHandItem()) || valid.test(entity.getOffhandItem())
            || CuriosApi.getCuriosInventory(entity).resolve().map(inv->inv.findFirstCurio(valid).isPresent()).orElse(false);
    }
    public static boolean illuminates(ServerPlayer player) {
        if(carriesLight(player,false) || HeldItemLights.held(player)>0)return true;
        var level=player.serverLevel();var point=new Vec3(player.getX(),player.getY()+0.8,player.getZ());
        for(var other:level.players()) {
            if(other==player || other.isSpectator() || !other.isAlive())continue;
            int luminance=HeldItemLights.held(other);boolean lamp=carriesLight(other,false);if(!lamp && luminance<=0)continue;
            if(HazardMath.inLightRadius(point.distanceToSqr(new Vec3(other.getX(),other.getY()+0.8,other.getZ())),(lamp?LanternConfig.radius(false,other.isInWater()):7.75*luminance/15.0)))return true;
        }
        var map=PLACED.get(level);if(map==null)return false;
        var iterator=map.entrySet().iterator();
        while(iterator.hasNext()) {
            var entry=iterator.next();var be=entry.getValue().get();
            if(be==null || be.isRemoved() || !level.hasChunkAt(entry.getKey())){iterator.remove();continue;}
            var state=be.getBlockState();if(!state.getValue(BlockStateProperties.LIT))continue;
            if(HazardMath.inLightRadius(point.distanceToSqr(Vec3.atCenterOf(entry.getKey())),LanternConfig.radius(true,state.getValue(BlockStateProperties.WATERLOGGED))))return true;
        }
        return false;
    }
    public static void clear(){PLACED.clear();}
}
