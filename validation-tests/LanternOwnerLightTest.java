import dev.depthward.lanterns.client.LanternLight;
import dev.depthward.lanterns.client.LightQueryContext;
import net.minecraft.core.BlockPos;
import org.thinkingstudio.ryoamiclights.DynamicLightSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.UUID;
/** Directly exercises the built production light callback, without a graphical client. */
public final class LanternOwnerLightTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        UUID owner=UUID.randomUUID();
        var source=new LanternLight(null,owner);
        source.move(0.5,0.5,0.5,16,0,true,0);
        source.move(0.7,0.5,0.5,16,0,true,1);
        check(Math.abs(source.ryoamicLights$getDynamicLightX()-0.7)<1e-10,"Moving source must update on the next tick");
        source.move(0.76,0.5,0.5,16,0,true,2);
        check(Math.abs(source.ryoamicLights$getDynamicLightX()-0.76)<1e-10,"Small movement must pass the reduced threshold");
        var method=Class.forName("dev.depthward.lanterns.mixin.client.LightRadiusMixin").getDeclaredMethod("lanternRadius",BlockPos.class,DynamicLightSource.class,double.class,CallbackInfoReturnable.class);
        method.setAccessible(true);
        for(int i=0;i<30;i++){
            source.move(i*0.2+0.5,0.5,0.5,16,0,true,i);
            var result=new CallbackInfoReturnable<Double>("test",true,3.0);
            var previous=LightQueryContext.enter(owner);
            try{method.invoke(null,new BlockPos(i/5,0,3),source,3.0,result);}
            finally{LightQueryContext.restore(previous);}
            check(result.getReturnValue()==3,"Own lantern must never add model light, including between updates");
        }
        source.move(0.5,0.5,0.5,16,0,true,40);
        var other=new CallbackInfoReturnable<Double>("test",true,0.0);
        var previous=LightQueryContext.enter(UUID.randomUUID());
        try{method.invoke(null,new BlockPos(0,0,3),source,0.0,other);}
        finally{LightQueryContext.restore(previous);}
        check(other.getReturnValue()>0,"Another entity still receives lantern light");
        var world=new CallbackInfoReturnable<Double>("test",true,0.0);
        method.invoke(null,new BlockPos(0,0,3),source,0.0,world);
        check(world.getReturnValue()>0,"World queries must keep lantern light");
        System.out.println("Production owner filter passed: 30 moving samples, other entity and world retain light.");
    }
}
