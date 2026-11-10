package dev.depthward.lanterns;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
public final class LanternEntity extends BlockEntity {
    private ItemStack original = ItemStack.EMPTY;
    private int fuel = -1;
    private int progress;
    private int lastLightEmission=-1;
    private double fraction;
    public LanternEntity(BlockPos pos, BlockState state) { super(Lanterns.ENTITY.get(), pos, state); }
    @Override public void onLoad() {
        super.onLoad();
        if (level instanceof net.minecraft.server.level.ServerLevel)HazardLights.register(this);
        if (level != null && level.isClientSide) net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,
            () -> () -> dev.depthward.lanterns.LanternCompat.track(getBlockPos()));
    }
    @Override public void setRemoved(){HazardLights.remove(this);super.setRemoved();}
    public int fuel() { return fuel < 0 ? Fuel.capacity() : Math.min(fuel, Fuel.capacity()); }
    public void setFuel(ItemStack stack) { original = stack.copyWithCount(1); fuel = Fuel.remaining(stack); progress = stack.getOrCreateTag().getInt("FuelTicks"); fraction = stack.getOrCreateTag().getDouble("FuelFraction"); update(); }
    public void refill() { fuel = Fuel.capacity(); progress = 0; fraction = 0; update(); }
    public ItemStack asItem() {
        var item = original.isEmpty() ? new ItemStack(Lanterns.LANTERN.get()) : original.copyWithCount(1);
        item.getOrCreateTag().putInt("Fuel", fuel()); item.getOrCreateTag().putInt("FuelTicks", progress); item.getOrCreateTag().putDouble("FuelFraction", fraction); return item;
    }
    private void update() { update(true); }
    private void update(boolean force) {
        if (level != null && !level.isClientSide) {
            var old = getBlockState(); var next = old.setValue(BlockStateProperties.LIT, fuel() > 0 && (original.isEmpty() || Fuel.enabled(original)));
            if (old != next) { setChanged(); level.setBlock(worldPosition, next, 3); }
            else if (force) { setChanged(); level.sendBlockUpdated(worldPosition, old, next, 3); }
        } else setChanged();
    }
    public static void tick(Level level, BlockPos pos, BlockState state, LanternEntity be) {
        int emission=state.getLightEmission(level,pos);
        if(be.lastLightEmission!=emission) {
            be.lastLightEmission=emission;
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
        if (!be.original.isEmpty() && !Fuel.enabled(be.original)) return;
        var next = FuelMath.tick(be.fuel(), be.progress, Fuel.capacity(), LanternConfig.TICKS.get());
        boolean changed = next.remaining() != be.fuel();
        be.fuel = next.remaining(); be.progress = next.progress();
        // Persist timer progress without sending block packets or neighbor updates every tick.
        level.getChunkAt(pos).setUnsaved(true);
        if (changed) be.update(false);
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if (!original.isEmpty()) tag.put("OriginalItem", original.save(new CompoundTag())); tag.putInt("Fuel", fuel()); tag.putInt("FuelTicks", progress); tag.putDouble("FuelFraction", fraction); }
    @Override public void load(CompoundTag tag) { super.load(tag); original = tag.contains("OriginalItem") ? ItemStack.of(tag.getCompound("OriginalItem")) : ItemStack.EMPTY; fuel = tag.contains("Fuel") ? Math.max(0,tag.getInt("Fuel")) : -1; progress = Math.max(0,tag.getInt("FuelTicks")); fraction = tag.getDouble("FuelFraction"); }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
