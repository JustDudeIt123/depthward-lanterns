package dev.depthward.lanterns.client;
import java.util.UUID;
/** Entity light queries are scoped with try/finally; chunk workers have no excluded owner. */
public final class LightQueryContext {
    private static final ThreadLocal<UUID> OWNER=new ThreadLocal<>();
    private LightQueryContext() {}
    public static UUID owner(){return OWNER.get();}
    public static UUID enter(UUID owner){UUID previous=OWNER.get();OWNER.set(owner);return previous;}
    public static void restore(UUID previous){if(previous==null)OWNER.remove();else OWNER.set(previous);}
}
