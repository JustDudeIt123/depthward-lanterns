package dev.depthward.lanterns;
/** Conservative upper bound on a source's change throughout an entire section. */
public final class LightUpdateMath {
    private LightUpdateMath() {}
    public static final double MAX_ERROR=0.25;
    public record State(double x,double y,double z,double radius,double fx,double fz,boolean beam) {}
    private static double plateau(State s,double cx,double cz){
        if(!s.beam)return 1;
        boolean core=true,weak=true;
        double limit=Math.cos(Math.toRadians(75));
        for(int ix=-1;ix<=1;ix+=2)for(int iz=-1;iz<=1;iz+=2){
            double dx=cx+ix*BeamMath.SECTION_HALF_EXTENT-s.x,dz=cz+iz*BeamMath.SECTION_HALF_EXTENT-s.z;
            double projection=dx*s.fx+dz*s.fz,length=Math.sqrt(dx*dx+dz*dz);
            if(projection<limit*length)core=false;
            if(-projection<limit*length)weak=false;
        }
        return core?1:weak?BeamMath.WEAK_STRENGTH:-1;
    }
    public static double bound(State a,State b,double cx,double cy,double cz){
        if(a.radius!=b.radius || a.beam!=b.beam || a.radius<=0)return Double.POSITIVE_INFINITY;
        double dx=a.x-b.x,dy=a.y-b.y,dz=a.z-b.z,movement=Math.sqrt(dx*dx+dy*dy+dz*dz);
        double first=plateau(a,cx,cz),second=plateau(b,cx,cz);
        if(first>=0 && first==second)return 15*first*Math.min(1,movement/a.radius);
        double minX=Math.max(0,Math.abs(cx-a.x)-BeamMath.SECTION_HALF_EXTENT),minZ=Math.max(0,Math.abs(cz-a.z)-BeamMath.SECTION_HALF_EXTENT);
        double horizontal=Math.sqrt(minX*minX+minZ*minZ),shift=Math.sqrt(dx*dx+dz*dz);
        double turn=Math.abs(Math.atan2(a.fx*b.fz-a.fz*b.fx,a.fx*b.fx+a.fz*b.fz));
        double angle=shift==0?0:horizontal>shift?Math.asin(Math.min(1,shift/horizontal)):Math.PI;
        double angular=Math.min(0.8,(0.8*1.5/(2*Math.cos(Math.toRadians(75))))*(turn+angle));
        double minY=Math.max(0,Math.abs(cy-a.y)-BeamMath.SECTION_HALF_EXTENT);
        double radial=Math.max(0,1-Math.sqrt(minX*minX+minY*minY+minZ*minZ)/a.radius);
        return 15*(Math.min(1,movement/a.radius)+radial*angular);
    }
}
