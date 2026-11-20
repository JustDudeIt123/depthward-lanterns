package dev.depthward.lanterns;

/** 150 degree bright core and weak radial spill, with linear distance falloff. */
public final class BeamMath {
    private BeamMath() {}
    public static final double SECTION_HALF_EXTENT = 10; // includes a 2-block light/AO sampling halo
    public static final double RANGE_MULTIPLIER = 4.0/3.0;
    public static final double OUTER_COS=Math.cos(Math.toRadians(105));
    public static final double WEAK_STRENGTH=0.2;
    private static final double INNER_COS=Math.cos(Math.toRadians(75));
    public static double strength(double dx,double dy,double dz,double forwardX,double forwardZ,double radius,boolean beam){
        double d2=dx*dx+dy*dy+dz*dz;
        if(radius<=0 || d2>=radius*radius)return 0;
        double angular=1,horizontal=Math.sqrt(dx*dx+dz*dz);
        if(beam && horizontal>1.0e-6){
            double cosine=(dx*forwardX+dz*forwardZ)/horizontal;
            double t=Math.max(0,Math.min(1,(cosine-OUTER_COS)/(INNER_COS-OUTER_COS)));
            angular=WEAK_STRENGTH+(1-WEAK_STRENGTH)*t*t*(3-2*t);
        }
        return 15*(1-Math.sqrt(d2)/radius)*angular;
    }
    /** Conservative sphere test: never excludes a section intersecting the beam. */
    public static boolean section(double dx,double dy,double dz,double fx,double fz,double radius,boolean beam) {
        double ax=Math.max(0,Math.abs(dx)-SECTION_HALF_EXTENT),ay=Math.max(0,Math.abs(dy)-SECTION_HALF_EXTENT),az=Math.max(0,Math.abs(dz)-SECTION_HALF_EXTENT);
        return radius>0 && ax*ax+ay*ay+az*az<radius*radius;
    }
}
