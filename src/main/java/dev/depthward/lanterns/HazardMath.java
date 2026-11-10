package dev.depthward.lanterns;
public final class HazardMath {
    public static float darknessDamage(long step,double initial,double increase,double maximum) {
        return (float)Math.min(maximum,Math.max(0,initial+Math.max(0,step)*increase));
    }
    public static boolean inLightRadius(double distanceSquared,double radius) {return radius>0 && distanceSquared<radius*radius;}
}
