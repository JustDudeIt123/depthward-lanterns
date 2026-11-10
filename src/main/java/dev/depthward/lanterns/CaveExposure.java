package dev.depthward.lanterns;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;

/** Only reads already loaded chunks. A leaf, branch or ordinary roof is not a cave. */
public final class CaveExposure {
 private static final TagKey<Block> ROOF=TagKey.create(Registries.BLOCK,new ResourceLocation(Lanterns.ID,"daytime_cave_roof"));
 private static final int[][] COLUMNS={{0,0},{2,0},{-2,0},{0,2},{0,-2}};
 private record Cached(BlockPos pos,long tick,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,boolean cave){}
 private static final Map<ServerPlayer,Cached> CACHE=new WeakHashMap<>();
 public static boolean isCave(ServerPlayer player){
  var level=player.serverLevel();var pos=player.blockPosition();long tick=level.getGameTime();var old=CACHE.get(player);
  if(old!=null && old.dimension.equals(level.dimension()) && old.pos.equals(pos) && tick>=old.tick && tick-old.tick<10)return old.cave;
  int votes=0;int minimumDepth=LanternConfig.DAY_CAVE_DEPTH.get(),minimumRoof=LanternConfig.DAY_CAVE_ROOF.get();
  var cursor=new BlockPos.MutableBlockPos();
  for(var offset:COLUMNS){
   int x=pos.getX()+offset[0],z=pos.getZ()+offset[1];
   var chunk=level.getChunkSource().getChunkNow(x>>4,z>>4);
   if(chunk==null)continue; // Unloaded space cannot trigger synchronous chunk generation.
   int depth=chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x&15,z&15)+1-pos.getY();
   if(depth<minimumDepth)continue;
   int run=0,maximumRun=0;
   int head=pos.getY()+2,top=Math.min(level.getMaxBuildHeight()-1,head+LanternConfig.DAY_CAVE_SCAN.get());
   for(int y=head;y<=top;y++){
    cursor.set(x,y,z);
    if(chunk.getBlockState(cursor).is(ROOF)){maximumRun=Math.max(maximumRun,++run);if(maximumRun>=minimumRoof)break;}
    else run=0;
   }
   if(CaveRules.roofColumn(depth,maximumRun,minimumDepth,minimumRoof))votes++;
  }
  boolean cave=CaveRules.underground(votes);CACHE.put(player,new Cached(pos.immutable(),tick,level.dimension(),cave));return cave;
 }
 public static void forget(ServerPlayer player){CACHE.remove(player);}
 public static void clear(){CACHE.clear();}
 private CaveExposure(){}
}
