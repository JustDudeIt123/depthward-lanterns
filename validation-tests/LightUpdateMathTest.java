import dev.depthward.lanterns.BeamMath;
import dev.depthward.lanterns.LightUpdateMath;
import dev.depthward.lanterns.LightUpdateMath.State;
import java.util.*;
public final class LightUpdateMathTest {
    private static State state(double x,double y,double z,double yaw){return new State(x,y,z,16,-Math.sin(yaw),Math.cos(yaw),true);}
    private static double light(State s,double x,double y,double z){return BeamMath.strength(x-s.x(),y-s.y(),z-s.z(),s.fx(),s.fz(),s.radius(),s.beam());}
    public static void main(String[] args){
        var random=new Random(150210);long checked=0;
        for(int i=0;i<20000;i++){
            State a=state(random.nextDouble()*32-16,random.nextDouble()*32-16,random.nextDouble()*32-16,random.nextDouble()*6.28);
            State b=state(a.x()+random.nextDouble()*2-1,a.y()+random.nextDouble()-0.5,a.z()+random.nextDouble()*2-1,Math.atan2(-a.fx(),a.fz())+random.nextDouble()-0.5);
            double cx=random.nextInt(5)*16-24,cy=random.nextInt(5)*16-24,cz=random.nextInt(5)*16-24;
            double bound=LightUpdateMath.bound(a,b,cx,cy,cz);
            for(int j=0;j<100;j++){
                double x=cx+random.nextDouble()*20-10,y=cy+random.nextDouble()*20-10,z=cz+random.nextDouble()*20-10;
                if(Math.abs(light(a,x,y,z)-light(b,x,y,z))>bound+1e-9)throw new AssertionError("Bound missed a lighting change");
                checked++;
            }
        }
        // Compare requested section rebuilds on a reproducible walking/turning trajectory.
        var scheduled=new HashMap<String,State>();long oldCount=0,newCount=0,exactCount=0;
        State old=null;
        for(int tick=0;tick<400;tick++){
            State next=state(8+Math.sin(tick*0.025)*8,40,8+tick*0.13,Math.sin(tick*0.03));
            var candidates=new HashSet<String>();
            var legacy=new HashSet<String>();
            for(State s:old==null?List.of(next):List.of(old,next)){
                for(int x=(int)Math.floor((s.x()-18)/16);x<=Math.floor((s.x()+18)/16);x++)
                for(int y=(int)Math.floor((s.y()-18)/16);y<=Math.floor((s.y()+18)/16);y++)
                for(int z=(int)Math.floor((s.z()-18)/16);z<=Math.floor((s.z()+18)/16);z++){
                    double dx=x*16+8-s.x(),dy=y*16+8-s.y(),dz=z*16+8-s.z();
                    String key=x+","+y+","+z;
                    if(BeamMath.section(dx,dy,dz,0,1,16,true))candidates.add(key);
                    if(x>=Math.floor((s.x()-16)/16) && x<=Math.floor((s.x()+16)/16)
                        && y>=Math.floor((s.y()-16)/16) && y<=Math.floor((s.y()+16)/16)
                        && z>=Math.floor((s.z()-16)/16) && z<=Math.floor((s.z()+16)/16)
                        && Math.sqrt(dx*dx+dy*dy+dz*dz)<=16+Math.sqrt(3)*8)legacy.add(key);
                }
            }
            oldCount+=legacy.size();
            for(String key:candidates){
                String[] parts=key.split(",");double cx=Integer.parseInt(parts[0])*16+8,cy=Integer.parseInt(parts[1])*16+8,cz=Integer.parseInt(parts[2])*16+8;
                boolean inside=BeamMath.section(cx-next.x(),cy-next.y(),cz-next.z(),0,1,16,true);
                State previous=scheduled.get(key);
                boolean oldInside=old!=null && BeamMath.section(cx-old.x(),cy-old.y(),cz-old.z(),0,1,16,true);
                if(inside||oldInside)exactCount++;
                if(previous==null && !inside)continue;
                if(previous!=null && inside && LightUpdateMath.bound(previous,next,cx,cy,cz)<=LightUpdateMath.MAX_ERROR)continue;
                newCount++;if(inside)scheduled.put(key,next);else scheduled.remove(key);
            }
            old=next;
        }
        if(newCount>=oldCount)throw new AssertionError("Synthetic walking workload did not improve");
        System.out.println("Bounds passed for "+checked+" sampled points. Synthetic section rebuild requests: old="+oldCount+", exact sphere="+exactCount+", optimized="+newCount+" (not an FPS benchmark).");
    }
}
