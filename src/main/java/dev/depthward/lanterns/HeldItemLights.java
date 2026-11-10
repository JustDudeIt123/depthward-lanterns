package dev.depthward.lanterns;
import com.google.gson.*;
import java.io.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
/** Server-safe mirror of the installed RyoamicLights item rules, including NBT block states. */
public final class HeldItemLights {
 private static JsonObject rules;
 private static JsonObject rules(){
  if(rules==null)try(var in=HeldItemLights.class.getResourceAsStream("/data/depthward_lanterns/held_item_lights.json")){
   rules=JsonParser.parseReader(new InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
  }catch(IOException e){throw new IllegalStateException(e);}return rules;
 }
 public static int luminance(ItemStack s,boolean water){
  if(s.isEmpty())return 0;
  if(s.is(Lanterns.LANTERN.get()))return Fuel.lit(s) && LanternConfig.radius(false,water)>0?15:0;
  JsonObject rule=rules().getAsJsonObject(BuiltInRegistries.ITEM.getKey(s.getItem()).toString());
  BlockState state=null;
  if(rule!=null){
   if(water && rule.has("water_sensitive") && rule.get("water_sensitive").getAsBoolean())return 0;
   var light=rule.get("luminance").getAsJsonPrimitive();if(light.isNumber())return Math.max(0,Math.min(15,light.getAsInt()));
   String value=light.getAsString();if(!value.equals("block"))state=BuiltInRegistries.BLOCK.get(new ResourceLocation(value)).defaultBlockState();
  }
  if(state==null && s.getItem() instanceof BlockItem item)state=item.getBlock().defaultBlockState();
  if(state==null)return 0;
  if(s.hasTag()){
   var tag=s.getTag().getCompound("BlockStateTag");for(String key:tag.getAllKeys()){
    var property=state.getBlock().getStateDefinition().getProperty(key);if(property!=null)state=with(state,property,tag.getString(key));
   }
  }
  return state.getLightEmission();
 }
 private static <T extends Comparable<T>> BlockState with(BlockState s,Property<T> p,String value){return p.getValue(value).map(v->s.setValue(p,v)).orElse(s);}
 public static int held(net.minecraft.world.entity.LivingEntity e){return Math.max(luminance(e.getMainHandItem(),e.isInWater()),luminance(e.getOffhandItem(),e.isInWater()));}
}
