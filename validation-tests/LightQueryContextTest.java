import dev.depthward.lanterns.client.LightQueryContext;
import java.util.UUID;
public final class LightQueryContextTest {
    private static void check(boolean value){if(!value)throw new AssertionError("Entity-light context leaked");}
    public static void main(String[] args)throws Exception{
        UUID first=UUID.randomUUID(),second=UUID.randomUUID();
        check(LightQueryContext.owner()==null);
        UUID previous=LightQueryContext.enter(first);
        try{
            check(first.equals(LightQueryContext.owner()));
            var failure=new java.util.concurrent.atomic.AtomicReference<Throwable>();
            Thread worker=new Thread(()->{check(LightQueryContext.owner()==null);});
            worker.setUncaughtExceptionHandler((t,e)->failure.set(e));
            worker.start();worker.join();
            check(failure.get()==null);
            UUID nested=LightQueryContext.enter(second);
            try{check(second.equals(LightQueryContext.owner()));throw new IllegalStateException("render failure");}
            catch(IllegalStateException expected){}
            finally{LightQueryContext.restore(nested);}
            check(first.equals(LightQueryContext.owner()));
        }finally{LightQueryContext.restore(previous);}
        check(LightQueryContext.owner()==null);
        System.out.println("Entity light context passed: nested queries, exception cleanup, independent worker thread.");
    }
}
