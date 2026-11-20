package dev.depthward.lanterns.client;
import dev.depthward.lanterns.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import org.thinkingstudio.ryoamiclights.RyoamicLights;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.*;
@Mod.EventBusSubscriber(modid=dev.depthward.lanterns.Lanterns.ID,value=Dist.CLIENT)
public final class LightTracker {
    private static ClientLevel world;
    private static final Map<Object,LanternLight> sources=new HashMap<>();
    private static final Set<BlockPos> blocks=new HashSet<>();
    @SubscribeEvent public static void chunkLoad(ChunkEvent.Load event) {
        if(event.getLevel() instanceof ClientLevel level) {
            if(world!=level) reset(level);
            if(event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk) {
                chunk.getBlockEntities().forEach((p,be)->{if(be instanceof LanternEntity)blocks.add(p.immutable());});
            }
        }
    }
    @SubscribeEvent public static void chunkUnload(ChunkEvent.Unload event) {
        if(event.getLevel().isClientSide()) {var cp=event.getChunk().getPos(); blocks.removeIf(p->(p.getX()>>4)==cp.x && (p.getZ()>>4)==cp.z);}
    }
    // BE data updates also discover newly placed blocks without scanning chunk contents every tick.
    public static void track(BlockPos pos) { blocks.add(pos.immutable()); }
    private static void reset(ClientLevel next) {
        for(var light:sources.values()) RyoamicLights.get().removeLightSource(light);
        sources.clear(); blocks.clear(); world=next;
    }
    private static boolean carries(LivingEntity e) {
        if(Fuel.lit(e.getMainHandItem()) || Fuel.lit(e.getOffhandItem())) return true;
        return CuriosApi.getCuriosInventory(e).resolve().map(inv->inv.findFirstCurio(Fuel::lit).isPresent()).orElse(false);
    }
    private static void source(Object key,double x,double y,double z,double radius,float yaw,boolean beam,Set<Object> alive) {
        if(radius<=0)return;
        alive.add(key);
        var light=sources.computeIfAbsent(key,k->new LanternLight(world,k instanceof UUID id?id:null));light.move(x,y,z,radius,yaw,beam,world.getGameTime());
        RyoamicLights.get().addLightSource(light);
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var level=Minecraft.getInstance().level;
        if(level!=world)reset(level);
        if(level==null)return;
        var alive=new HashSet<Object>();
        for(var entity:level.entitiesForRendering()) if(entity instanceof LivingEntity e && carries(e)) {
            // Configured server radii are synchronized to the client.
            source(e.getUUID(),e.getX(),e.getY()+0.8,e.getZ(),LanternConfig.radius(false,e.isInWater())*BeamMath.RANGE_MULTIPLIER,e.yBodyRot,true,alive);
        }
        blocks.removeIf(p->!level.hasChunkAt(p) || !(level.getBlockEntity(p) instanceof LanternEntity));
        var camera=Minecraft.getInstance().getCameraEntity();
        for(var pos:blocks) {
            if(camera!=null && pos.distToCenterSqr(camera.position())>128*128)continue;
            var state=level.getBlockState(pos);
            if(state.getValue(BlockStateProperties.LIT)) source(pos,pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5,LanternConfig.radius(true,state.getValue(BlockStateProperties.WATERLOGGED)),0,false,alive);
        }
        sources.entrySet().removeIf(e->{if(alive.contains(e.getKey()))return false;RyoamicLights.get().removeLightSource(e.getValue());return true;});
    }
}
