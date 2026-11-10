package dev.depthward.lanterns;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
public final class OilLanternBlock extends LanternBlock implements EntityBlock {
    public OilLanternBlock() {
        super(Properties.copy(Blocks.LANTERN).lightLevel(s -> s.getValue(BlockStateProperties.LIT) ? LanternConfig.blockLight(s.getValue(WATERLOGGED)) : 0));
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true));
    }
    @Override public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(BlockStateProperties.LIT)?LanternConfig.blockLight(state.getValue(WATERLOGGED)):0;
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { super.createBlockStateDefinition(b); b.add(BlockStateProperties.LIT); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new LanternEntity(pos, state); }
    @Override @SuppressWarnings("unchecked") public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide && type == Lanterns.ENTITY.get() ? (l,p,s,e) -> LanternEntity.tick(l,p,s,(LanternEntity)e) : null;
    }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var oil = player.getItemInHand(hand);
        if (oil.is(Lanterns.OIL.get()) && level.getBlockEntity(pos) instanceof LanternEntity be && be.fuel() < Fuel.capacity()) {
            if (!level.isClientSide) { be.refill(); OilItem.consume(player,oil); }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return List.of(be instanceof LanternEntity lantern ? lantern.asItem() : new ItemStack(Lanterns.LANTERN.get()));
    }
    @Override public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        // HUD/pick-block icon must not change NBT on each fuel tick. Actual pickup uses asItem().
        var item=level.getBlockEntity(pos) instanceof LanternEntity be ? be.asItem() : new ItemStack(Lanterns.LANTERN.get());
        item.removeTagKey("FuelTicks");item.removeTagKey("FuelFraction");
        item.getOrCreateTag().putInt("Fuel",state.getValue(BlockStateProperties.LIT)?Fuel.capacity():0);
        return item;
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        super.onRemove(state, level, pos, next, moving);
        if (state.getBlock() != next.getBlock()) level.removeBlockEntity(pos);
    }
}
