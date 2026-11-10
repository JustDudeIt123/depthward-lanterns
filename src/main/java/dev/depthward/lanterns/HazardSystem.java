package dev.depthward.lanterns;
import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=Lanterns.ID)
public final class HazardSystem {
    private static final Map<UUID,Exposure> STATES=new HashMap<>();
    private static final class Exposure {int warning,damageTicks,hellTicks;long damageStep;boolean whispered;}
    public record Snapshot(int warningTicks,int damageTicks,long damageStep,int hellTicks,boolean whispered){}
    public static Snapshot snapshot(ServerPlayer player){var s=STATES.get(player.getUUID());return s==null?new Snapshot(0,0,0,0,false):new Snapshot(s.warning,s.damageTicks,s.damageStep,s.hellTicks,s.whispered);}
    public static boolean isDark(ServerPlayer player) {
        if(!LanternConfig.DARKNESS_ENABLED.get())return false;
        var level=player.serverLevel();var pos=player.blockPosition();
        if(level.getBrightness(LightLayer.BLOCK,pos)>LanternConfig.DARK_BLOCK_LIGHT.get()
            || HeldItemLights.held(player)>LanternConfig.DARK_BLOCK_LIGHT.get() || HazardLights.illuminates(player))return false;
        long solar=level.dimensionType().fixedTime().orElse(level.getDayTime());
        if(CaveRules.daytime(level.dimensionType().hasSkyLight(),solar))
            return level.getBrightness(LightLayer.SKY,pos)<=LanternConfig.DAY_SKY_LIGHT.get() && CaveExposure.isCave(player);
        return level.getMaxLocalRawBrightness(pos)<=LanternConfig.DARK_SKY_LIGHT.get();
    }
    public static boolean hellfireUnsafe(ServerPlayer player) {
        return LanternConfig.HELLFIRE_ENABLED.get() && player.level().dimension()==Level.NETHER && !HazardLights.carriesLight(player,true);
    }
    public static void tickPlayer(ServerPlayer player){
        if(!player.isAlive() || player.isCreative() || player.isSpectator()){reset(player);return;}
        advance(player,isDark(player),hellfireUnsafe(player));
    }
    static void advance(ServerPlayer player,boolean dark,boolean hellfire) {
        if(!player.isAlive() || player.isCreative() || player.isSpectator()){reset(player);return;}
        var state=STATES.computeIfAbsent(player.getUUID(),key->new Exposure());
        if(dark) {
            if(!state.whispered){player.playNotifySound(Lanterns.WHISPER.get(),SoundSource.AMBIENT,(float)(double)LanternConfig.WHISPER_VOLUME.get(),1);state.whispered=true;}
            if(state.warning<LanternConfig.DARK_WARNING_TICKS.get()){state.warning++;player.removeEffect(LanternEffects.DARKNESS.get());}
            else {
                refresh(player,LanternEffects.DARKNESS.get());
                if(++state.damageTicks>=LanternConfig.DARK_DAMAGE_INTERVAL.get()) {
                    state.damageTicks=0;
                    float damage=HazardMath.darknessDamage(state.damageStep,LanternConfig.DARK_INITIAL_DAMAGE.get(),LanternConfig.DARK_DAMAGE_INCREASE.get(),LanternConfig.DARK_MAX_DAMAGE.get());
                    player.hurt(HazardDamage.source(player.level(),HazardDamage.DARKNESS),damage);
                    state.damageStep=Math.min(1000000,state.damageStep+1);
                }
            }
        } else {state.warning=0;state.damageTicks=0;state.damageStep=0;state.whispered=false;player.removeEffect(LanternEffects.DARKNESS.get());}
        if(hellfire) {
            refresh(player,LanternEffects.HELLFIRE.get());
            if(++state.hellTicks>=LanternConfig.HELLFIRE_DAMAGE_INTERVAL.get()) {state.hellTicks=0;player.hurt(HazardDamage.source(player.level(),HazardDamage.HELLFIRE),LanternConfig.HELLFIRE_DAMAGE.get().floatValue());}
        } else {state.hellTicks=0;player.removeEffect(LanternEffects.HELLFIRE.get());}
        if(!dark && !hellfire)STATES.remove(player.getUUID());
    }
    private static void refresh(ServerPlayer player,MobEffect effect) {
        var current=player.getEffect(effect);
        if(current==null || current.getDuration()<=20)player.addEffect(new MobEffectInstance(effect,60,0,false,false,true));
    }
    public static void reset(ServerPlayer player){STATES.remove(player.getUUID());CaveExposure.forget(player);player.removeEffect(LanternEffects.DARKNESS.get());player.removeEffect(LanternEffects.HELLFIRE.get());}
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event){if(event.phase==TickEvent.Phase.END && event.player instanceof ServerPlayer player)tickPlayer(player);}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player)reset(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){if(event.getEntity() instanceof ServerPlayer player)reset(player);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event){if(event.getEntity() instanceof ServerPlayer player)reset(player);}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){if(event.getEntity() instanceof ServerPlayer player)reset(player);}
    @SubscribeEvent public static void stopped(ServerStoppedEvent event){STATES.clear();HazardLights.clear();CaveExposure.clear();}
}
