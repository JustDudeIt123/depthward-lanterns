package dev.depthward.lanterns;
import net.minecraft.core.BlockPos;
import java.util.function.Consumer;
public final class LanternCompat {
 private static Consumer<BlockPos> tracker=p->{};
 public static void installTracker(Consumer<BlockPos> value){tracker=value;}
 public static void track(BlockPos pos){tracker.accept(pos);}
}
