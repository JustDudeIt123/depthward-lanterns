package dev.depthward.lanterns.client;
import dev.depthward.lanterns.BeamMath;
import dev.depthward.lanterns.LightUpdateMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.thinkingstudio.ryoamiclights.DynamicLightSource;
import java.util.HashSet;
import java.util.Set;
/** One RyoamicLights source; old/new section invalidations are deduplicated. */
public final class LanternLight implements DynamicLightSource {
    private final Level world;
    private final java.util.UUID owner;
    private double x,y,z,radius,fx,fz;
    private boolean beam,initialized,dirty;
    private long lastUpdate=Long.MIN_VALUE;
    private final Set<BlockPos> pending=new HashSet<>();
    private final java.util.Map<BlockPos,LightUpdateMath.State> scheduled=new java.util.HashMap<>();
    public LanternLight(Level world){this(world,null);}
    public LanternLight(Level world,java.util.UUID owner){this.world=world;this.owner=owner;}
    public boolean ownedBy(java.util.UUID entity){return owner!=null && owner.equals(entity);}
    public double radius(){return radius;}
    public void move(double x,double y,double z,double radius){move(x,y,z,radius,0,false,initialized?lastUpdate+1:0);}
    public void move(double x,double y,double z,double radius,float yaw,boolean beam,long tick){
        double radians=Math.toRadians(yaw),nx=-Math.sin(radians),nz=Math.cos(radians);
        boolean shape=this.radius!=radius || this.beam!=beam;
        boolean moved=Math.abs(x-this.x)>0.05 || Math.abs(y-this.y)>0.05 || Math.abs(z-this.z)>0.05;
        boolean turned=beam && nx*fx+nz*fz<Math.cos(Math.toRadians(2));
        if(initialized && (!shape && !moved && !turned || !shape && tick-lastUpdate<1))return;
        if(initialized)collect(pending);
        this.x=x;this.y=y;this.z=z;this.radius=radius;fx=nx;fz=nz;this.beam=beam;
        initialized=true;lastUpdate=tick;collect(pending);dirty=true;
    }
    public double lightAt(double dx,double dy,double dz){return BeamMath.strength(dx,dy,dz,fx,fz,radius,beam);}
    private void collect(Set<BlockPos> sections){
        double extent=radius+2; // include neighboring AO/light samples
        int minX=(int)Math.floor((x-extent)/16),maxX=(int)Math.floor((x+extent)/16);
        int minY=(int)Math.floor((y-extent)/16),maxY=(int)Math.floor((y+extent)/16);
        int minZ=(int)Math.floor((z-extent)/16),maxZ=(int)Math.floor((z+extent)/16);
        for(int cx=minX;cx<=maxX;cx++)for(int cy=minY;cy<=maxY;cy++)for(int cz=minZ;cz<=maxZ;cz++)
            if(BeamMath.section(cx*16+8-x,cy*16+8-y,cz*16+8-z,fx,fz,radius,beam))sections.add(new BlockPos(cx,cy,cz));
    }
    private void flush(LevelRenderer renderer){
        var next=new LightUpdateMath.State(x,y,z,radius,fx,fz,beam);
        for(var p:pending){
            double cx=p.getX()*16+8,cy=p.getY()*16+8,cz=p.getZ()*16+8;
            boolean inside=BeamMath.section(cx-x,cy-y,cz-z,fx,fz,radius,beam);
            var previous=scheduled.get(p);
            if(previous==null && !inside)continue;
            if(previous!=null && inside && LightUpdateMath.bound(previous,next,cx,cy,cz)<=LightUpdateMath.MAX_ERROR)continue;
            renderer.setSectionDirty(p.getX(),p.getY(),p.getZ());
            if(inside)scheduled.put(p,next);else scheduled.remove(p);
        }
        pending.clear();
    }
    @Override public double ryoamicLights$getDynamicLightX(){return x;}
    @Override public double ryoamicLights$getDynamicLightY(){return y;}
    @Override public double ryoamicLights$getDynamicLightZ(){return z;}
    @Override public Level ryoamicLights$getDynamicLightWorld(){return world;}
    @Override public int ryoamicLights$getLuminance(){return 15;}
    @Override public void ryoamicLights$dynamicLightTick(){}
    @Override public boolean ryoamicLights$shouldUpdateDynamicLight(){return dirty;}
    @Override public boolean ryoamiclights$updateDynamicLight(LevelRenderer renderer){if(!dirty)return false;flush(renderer);dirty=false;return true;}
    private void invalidateAll(LevelRenderer renderer){
        collect(pending);pending.addAll(scheduled.keySet());
        for(var p:pending)renderer.setSectionDirty(p.getX(),p.getY(),p.getZ());
        pending.clear();scheduled.clear();dirty=true;
    }
    @Override public void ryoamicLights$resetDynamicLight(){invalidateAll(Minecraft.getInstance().levelRenderer);}
    @Override public void ryoamiclights$scheduleTrackedChunksRebuild(LevelRenderer renderer){invalidateAll(renderer);}
}
