import dev.depthward.lanterns.BeamMath;
import java.util.Random;

/** Run with javac/java; tests shared lighting geometry without starting Minecraft. */
public final class LanternSectorTest {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){
        double radius=12*BeamMath.RANGE_MULTIPLIER;
        check(Math.abs(radius-16)<1e-10,"16 block range");
        check(BeamMath.strength(0,0,15,0,1,radius,true)>0,"Extra forward range");
        check(BeamMath.strength(0,0,16,0,1,radius,true)==0,"Range boundary");
        double front=BeamMath.strength(0,0,5,0,1,radius,true);
        check(Math.abs(BeamMath.strength(0,0,-5,0,1,radius,true)/front-0.2)<1e-10,"Weak rear stays 20 percent");
        check(BeamMath.strength(5,0,0,0,1,radius,true)<front,"Side transition remains dimmer than core");
        check(Math.abs(BeamMath.strength(5*Math.sin(Math.toRadians(75)),0,5*Math.cos(Math.toRadians(75)),0,1,radius,true)-front)<1e-10,"Bright core is 150 degrees");
        check(BeamMath.strength(0,0,-2,0,1,19,false)>0,"Placed lantern remains radial");
        check(BeamMath.strength(0,0,0,0,1,0,true)==0,"Disabled light");
        var random=new Random(120);
        int lit=0;
        for(int i=0;i<100000;i++){
            double sx=random.nextDouble()*32-16,sy=random.nextDouble()*32-16,sz=random.nextDouble()*32-16;
            double yaw=random.nextDouble()*Math.PI*2,fx=-Math.sin(yaw),fz=Math.cos(yaw);
            double dx=random.nextDouble()*40-20,dy=random.nextDouble()*40-20,dz=random.nextDouble()*40-20;
            if(BeamMath.strength(dx,dy,dz,fx,fz,radius,true)<=0)continue;
            double cx=Math.floor((sx+dx)/16)*16+8-sx;
            double cy=Math.floor((sy+dy)/16)*16+8-sy;
            double cz=Math.floor((sz+dz)/16)*16+8-sz;
            check(BeamMath.section(cx,cy,cz,fx,fz,radius,true),"Lit point's section must be invalidated");
            lit++;
        }
        check(lit>1000,"Random coverage");
        System.out.println("Lantern sector geometry passed; "+lit+" illuminated samples covered by section invalidation.");
    }
}
