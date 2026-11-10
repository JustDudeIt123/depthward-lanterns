package dev.depthward.lanterns;

/** Pure classification rules, independent of world access and rendering. */
public final class CaveRules {
 public static boolean daytime(boolean sky,long time){long solar=Math.floorMod(time,24000L);return sky && (solar<13000 || solar>=23000);}
 public static boolean roofColumn(int depth,int naturalRun,int minimumDepth,int minimumRoof){return depth>=minimumDepth && naturalRun>=minimumRoof;}
 public static boolean underground(int roofVotes){return roofVotes>=3;}
 private CaveRules(){}
}
