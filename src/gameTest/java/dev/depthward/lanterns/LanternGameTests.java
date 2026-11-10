package dev.depthward.lanterns;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.FakePlayerFactory;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import top.theillusivec4.curios.api.*;
@GameTestHolder(Lanterns.ID)
@PrefixGameTestTemplate(false)
public final class LanternGameTests {
    private static net.minecraftforge.common.util.FakePlayer player(GameTestHelper t) {
        return FakePlayerFactory.get(t.getLevel(),new GameProfile(UUID.randomUUID(),"LanternTest"));
    }
    @GameTest(template="empty") public static void tooltipIdentityIgnoresFuelButKeepsVisualChanges(GameTestHelper t) {
        var p=player(t);var stack=new ItemStack(Lanterns.LANTERN.get());Fuel.refill(stack);
        var snapshot=LanternTooltipIdentity.snapshot(stack);var before=stack.save(new net.minecraft.nbt.CompoundTag());
        for(int i=0;i<60;i++)Fuel.tick(stack,p);
        t.assertTrue(!before.equals(stack.save(new net.minecraft.nbt.CompoundTag())),"Server fuel must continue changing normally");
        t.assertTrue(ItemStack.matches(snapshot,LanternTooltipIdentity.snapshot(stack)),"Preview identity must stay stable as fuel and timer change");
        var renamed=stack.copy();renamed.setHoverName(net.minecraft.network.chat.Component.literal("New name"));
        t.assertTrue(!ItemStack.matches(snapshot,LanternTooltipIdentity.snapshot(renamed)),"Renaming must invalidate preview identity");
        Fuel.setEnabled(stack,false);
        t.assertTrue(!ItemStack.matches(snapshot,LanternTooltipIdentity.snapshot(stack)),"Switching off must invalidate preview identity");
        Fuel.setEnabled(stack,true);stack.getOrCreateTag().putInt("Fuel",0);
        t.assertTrue(!ItemStack.matches(snapshot,LanternTooltipIdentity.snapshot(stack)),"Burnout must update the preview model");
        t.assertTrue(!stack.getOrCreateTag().contains("DepthwardTooltipLit"),"Comparison marker must never modify actual inventory data");
        t.succeed();
    }
    @GameTest(template="empty") public static void configuredLightRadiiControlDynamicAndBlockLight(GameTestHelper t) {
        double held=LanternConfig.HELD_RADIUS.get(),wet=LanternConfig.HELD_WATER_RADIUS.get(),placed=LanternConfig.PLACED_RADIUS.get(),placedWet=LanternConfig.PLACED_WATER_RADIUS.get();
        try {
            LanternConfig.HELD_RADIUS.set(12.0);LanternConfig.HELD_WATER_RADIUS.set(3.0);LanternConfig.PLACED_RADIUS.set(5.25);LanternConfig.PLACED_WATER_RADIUS.set(2.0);
            t.assertTrue(LanternConfig.radius(false,false)==12 && LanternConfig.radius(false,true)==3,"Held and underwater radii must use config");
            t.assertTrue(LanternConfig.radius(true,false)==5.25 && LanternConfig.radius(true,true)==2,"Placed radii must use config");
            var state=Lanterns.BLOCK.get().defaultBlockState();var abs=t.absolutePos(new BlockPos(1,2,1));
            t.assertTrue(state.getLightEmission(t.getLevel(),abs)==6,"Small radius must reduce vanilla block light too");
            t.assertTrue(state.setValue(BlockStateProperties.WATERLOGGED,true).getLightEmission(t.getLevel(),abs)==2,"Underwater block light must use config");
            LanternConfig.PLACED_RADIUS.set(0.0);t.assertTrue(state.getLightEmission(t.getLevel(),abs)==0,"Zero radius must disable actual placed light");
            LanternConfig.PLACED_RADIUS.set(40.0);t.assertTrue(state.getLightEmission(t.getLevel(),abs)==15,"Block light must remain within Minecraft limit");
        } finally {LanternConfig.HELD_RADIUS.set(held);LanternConfig.HELD_WATER_RADIUS.set(wet);LanternConfig.PLACED_RADIUS.set(placed);LanternConfig.PLACED_WATER_RADIUS.set(placedWet);}
        t.succeed();
    }
    @GameTest(template="empty") public static void oldWorldRadiusUpgradesOnceAndKeepsWaterSettings(GameTestHelper t) {
        var config=net.minecraftforge.fml.config.ConfigTracker.INSTANCE.configSets().get(net.minecraftforge.fml.config.ModConfig.Type.SERVER).stream().filter(c->c.getModId().equals(Lanterns.ID)).findFirst().orElseThrow();
        double held=LanternConfig.HELD_RADIUS.get(),placed=LanternConfig.PLACED_RADIUS.get(),water=LanternConfig.HELD_WATER_RADIUS.get();
        boolean applied=LanternConfig.RADIUS_19_APPLIED.get();
        try {
            LanternConfig.RADIUS_19_APPLIED.set(false);LanternConfig.HELD_RADIUS.set(10.075);LanternConfig.PLACED_RADIUS.set(19.5);
            LanternConfig.loaded(new net.minecraftforge.fml.event.config.ModConfigEvent.Loading(config));
            t.assertTrue(LanternConfig.HELD_RADIUS.get()==19 && LanternConfig.PLACED_RADIUS.get()==19,"Old world gets the requested 19-block radius");
            t.assertTrue(LanternConfig.HELD_WATER_RADIUS.get()==water,"Water radius stays unchanged");
            LanternConfig.HELD_RADIUS.set(17.0);LanternConfig.loaded(new net.minecraftforge.fml.event.config.ModConfigEvent.Loading(config));
            t.assertTrue(LanternConfig.HELD_RADIUS.get()==17,"Later manual edits are not overwritten");
        } finally {LanternConfig.HELD_RADIUS.set(held);LanternConfig.PLACED_RADIUS.set(placed);LanternConfig.RADIUS_19_APPLIED.set(applied);config.save();}
        t.succeed();
    }
    @GameTest(template="empty") public static void toggleIncludesBothHandsAndCurios(GameTestHelper t) {
        var p=player(t);
        var main=new ItemStack(Lanterns.LANTERN.get());var off=new ItemStack(Lanterns.LANTERN.get());var worn=new ItemStack(Lanterns.LANTERN.get());
        p.setItemSlot(EquipmentSlot.MAINHAND,main);p.setItemSlot(EquipmentSlot.OFFHAND,off);
        var belt=CuriosApi.getCuriosInventory(p).resolve().orElseThrow().getStacksHandler("belt").orElseThrow();belt.getStacks().setStackInSlot(0,worn);
        t.assertTrue(LanternNetwork.toggle(p),"Toggle must find held and worn lanterns");
        for(var stack:java.util.List.of(main,off,worn)) {
            t.assertTrue(!Fuel.enabled(stack) && !Fuel.lit(stack),"All held and worn lanterns must switch off together");
            var before=stack.save(new net.minecraft.nbt.CompoundTag());Fuel.tick(stack,p);
            t.assertTrue(before.equals(stack.save(new net.minecraft.nbt.CompoundTag())),"Disabled held lantern cannot consume fuel");
        }
        LanternNetwork.toggle(p);
        for(var stack:java.util.List.of(main,off,worn))t.assertTrue(Fuel.lit(stack),"Second toggle must enable all lanterns");
        belt.getStacks().setStackInSlot(0,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        t.assertTrue(LanternNetwork.toggle(p) && !Fuel.enabled(off),"Offhand-only lantern must be controllable without Curios");
        t.succeed();
    }
    @GameTest(template="empty") public static void burningIconIsStableButPickupKeepsActualFuel(GameTestHelper t) {
        var pos=new BlockPos(1,2,1);t.setBlock(pos.below(),Blocks.STONE);t.setBlock(pos,Lanterns.BLOCK.get().defaultBlockState());
        var abs=t.absolutePos(pos);var be=(LanternEntity)t.getLevel().getBlockEntity(abs);
        var stack=new ItemStack(Lanterns.LANTERN.get());stack.getOrCreateTag().putInt("Fuel",3);be.setFuel(stack);
        var icon=Lanterns.BLOCK.get().getCloneItemStack(t.getLevel(),abs,be.getBlockState());
        for(int i=0;i<LanternConfig.TICKS.get();i++)LanternEntity.tick(t.getLevel(),abs,be.getBlockState(),be);
        t.assertTrue(Fuel.remaining(be.asItem())==2,"Real fuel must still be consumed and saved");
        t.assertTrue(ItemStack.matches(icon,Lanterns.BLOCK.get().getCloneItemStack(t.getLevel(),abs,be.getBlockState())),"HUD icon must not change as lit fuel drains");
        var saved=be.saveWithoutMetadata();var restored=new LanternEntity(abs,be.getBlockState());restored.load(saved);
        t.assertTrue(Fuel.remaining(restored.asItem())==2,"Stable icon must not overwrite actual saved fuel");
        for(int i=0;i<LanternConfig.TICKS.get()*2;i++)LanternEntity.tick(t.getLevel(),abs,be.getBlockState(),be);
        t.assertTrue(!be.getBlockState().getValue(BlockStateProperties.LIT),"Zero fuel must update visible block state");
        t.assertTrue(!Fuel.lit(Lanterns.BLOCK.get().getCloneItemStack(t.getLevel(),abs,be.getBlockState())),"Icon must switch to extinguished only on burnout");
        t.succeed();
    }
    @GameTest(template="empty") public static void sneakPickupPreservesWholeItemAndWater(GameTestHelper t) {
        var p=player(t);p.setShiftKeyDown(true);
        var pos=new BlockPos(1,2,1);t.setBlock(pos.below(),Blocks.STONE);
        t.setBlock(pos,Lanterns.BLOCK.get().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,true));
        var abs=t.absolutePos(pos);var be=(LanternEntity)t.getLevel().getBlockEntity(abs);
        var stack=new ItemStack(Lanterns.LANTERN.get());Fuel.setEnabled(stack,false);
        stack.setHoverName(net.minecraft.network.chat.Component.literal("My lantern"));
        stack.enchant(net.minecraft.world.item.enchantment.Enchantments.MENDING,1);
        stack.getOrCreateTag().putInt("Fuel",42);stack.getOrCreateTag().putInt("FuelTicks",3);stack.getOrCreateTag().putDouble("FuelFraction",0.4);
        stack.getOrCreateTag().putString("CustomProperty","preserved");be.setFuel(stack);
        var data=be.saveWithoutMetadata();be.load(data);
        LanternEntity.tick(t.getLevel(),abs,be.getBlockState(),be);
        t.assertTrue(ItemStack.matches(stack,be.asItem()),"Disabled block must preserve all item data and pause fuel");
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(abs),net.minecraft.core.Direction.UP,abs,false);
        var event=new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(p,net.minecraft.world.InteractionHand.MAIN_HAND,abs,hit);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        t.assertTrue(event.isCanceled() && event.getCancellationResult().consumesAction(),"Sneak click must be handled");
        t.assertTrue(t.getBlockState(pos).is(Blocks.WATER),"Picking up a waterlogged lantern must leave water");
        var picked=p.getInventory().items.stream().filter(item->item.is(Lanterns.LANTERN.get())).findFirst().orElseThrow();
        t.assertTrue(ItemStack.matches(stack,picked),"Pickup must preserve fuel, name, enchantments, toggle and custom NBT");
        t.succeed();
    }
    @GameTest(template="empty") public static void fullInventoryDoesNotRemoveLantern(GameTestHelper t) {
        var p=player(t);for(int i=0;i<p.getInventory().items.size();i++)p.getInventory().items.set(i,new ItemStack(Items.STONE,64));
        var pos=new BlockPos(1,2,1);t.setBlock(pos.below(),Blocks.STONE);t.setBlock(pos,Lanterns.BLOCK.get().defaultBlockState());
        var result=LanternInteractions.pickup(t.getLevel(),t.absolutePos(pos),p);
        t.assertTrue(result==net.minecraft.world.InteractionResult.FAIL && t.getBlockState(pos).is(Lanterns.BLOCK.get()),"Full inventory must leave block untouched");
        t.succeed();
    }
    @GameTest(template="empty") public static void inventoryRightClickRefillsExactlyOnce(GameTestHelper t) {
        var p=player(t);var menu=p.inventoryMenu;
        var lantern=new ItemStack(Lanterns.LANTERN.get());lantern.getOrCreateTag().putInt("Fuel",0);Fuel.setEnabled(lantern,false);
        lantern.setHoverName(net.minecraft.network.chat.Component.literal("Named"));
        var slot=menu.getSlot(9);slot.set(lantern);menu.setCarried(new ItemStack(Lanterns.OIL.get(),2));
        menu.clicked(9,1,net.minecraft.world.inventory.ClickType.PICKUP,p);
        t.assertTrue(Fuel.remaining(slot.getItem())==Fuel.capacity(),"Inventory right click must refill lantern");
        t.assertTrue(menu.getCarried().is(Lanterns.OIL.get()) && menu.getCarried().getCount()==1,"Must consume exactly one cursor oil");
        t.assertTrue(!Fuel.enabled(slot.getItem()) && slot.getItem().getHoverName().getString().equals("Named"),"Refill must preserve switch and name");
        t.assertTrue(p.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE)),"Bottle must be returned");
        menu.clicked(9,1,net.minecraft.world.inventory.ClickType.PICKUP,p);
        t.assertTrue(menu.getCarried().getCount()==1,"Full lantern must not consume oil");
        slot.getItem().getOrCreateTag().putInt("Fuel",0);
        menu.clicked(9,1,net.minecraft.world.inventory.ClickType.PICKUP,p);
        t.assertTrue(menu.getCarried().isEmpty() && Fuel.remaining(slot.getItem())==Fuel.capacity(),"Last cursor oil must refill without swapping or duplication");
        t.succeed();
    }
    @GameTest(template="empty") public static void serverTogglePausesAndSynchronizesCurios(GameTestHelper t) {
        var p=player(t);var stack=new ItemStack(Lanterns.LANTERN.get());stack.getOrCreateTag().putInt("Fuel",1);
        var belt=CuriosApi.getCuriosInventory(p).resolve().orElseThrow().getStacksHandler("belt").orElseThrow();
        belt.getStacks().setStackInSlot(0,stack);var ctx=new SlotContext("belt",p,0,false,true);
        t.assertTrue(LanternNetwork.toggle(p) && !Fuel.enabled(stack) && !Fuel.lit(stack),"Server toggle must switch equipped lantern off");
        var before=stack.save(new net.minecraft.nbt.CompoundTag());
        for(int i=0;i<100;i++)Lanterns.LANTERN.get().curioTick(ctx,stack);
        t.assertTrue(before.equals(stack.save(new net.minecraft.nbt.CompoundTag())),"Disabled Curios lantern must not consume fuel or alter timer");
        var clientStack=new ItemStack(Lanterns.LANTERN.get());
        Lanterns.LANTERN.get().readSyncData(ctx,Lanterns.LANTERN.get().writeSyncData(ctx,stack),clientStack);
        t.assertTrue(!Fuel.enabled(clientStack) && !Fuel.lit(clientStack),"Curios synchronization must preserve disabled state");
        var lines=new java.util.ArrayList<net.minecraft.network.chat.Component>();Lanterns.LANTERN.get().appendHoverText(stack,t.getLevel(),lines,net.minecraft.world.item.TooltipFlag.NORMAL);
        t.assertTrue(lines.size()==1,"Only the state tooltip should remain");
        t.assertTrue(lines.get(0).getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals("tooltip.depthward_lanterns.state"),"State label must be translatable");
        t.assertTrue(lines.get(0).getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(net.minecraft.ChatFormatting.WHITE)),"State label must be white");
        var value=lines.get(0).getSiblings().get(1);
        t.assertTrue(value.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals("tooltip.depthward_lanterns.disabled"),"Disabled value must be translatable");
        t.assertTrue(value.getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(net.minecraft.ChatFormatting.RED)),"Disabled value must be red");
        t.assertTrue(LanternNetwork.toggle(p) && Fuel.lit(stack),"Second toggle must enable lantern");
        lines.clear();Lanterns.LANTERN.get().appendHoverText(stack,t.getLevel(),lines,net.minecraft.world.item.TooltipFlag.NORMAL);
        var enabledValue=lines.get(0).getSiblings().get(1);
        t.assertTrue(enabledValue.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals("tooltip.depthward_lanterns.enabled"),"Enabled value must be translatable");
        t.assertTrue(enabledValue.getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(net.minecraft.ChatFormatting.GREEN)),"Enabled value must be green");
        for(int i=0;i<LanternConfig.TICKS.get();i++)Lanterns.LANTERN.get().curioTick(ctx,stack);
        t.assertTrue(Fuel.remaining(stack)==0,"Enabled lantern must resume fuel consumption");
        belt.getStacks().setStackInSlot(0,ItemStack.EMPTY);t.assertTrue(!LanternNetwork.toggle(p),"No equipped lantern must be handled safely");
        t.succeed();
    }
    @GameTest(template="empty") public static void playerEfficiencyAppliesToHandsAndCurios(GameTestHelper t) {
        var p=player(t);var attribute=p.getAttribute(Lanterns.FUEL_EFFICIENCY.get());
        t.assertTrue(attribute!=null && attribute.getValue()==0,"Players must start with zero fuel efficiency");
        attribute.setBaseValue(80);
        for(var slot:new EquipmentSlot[]{EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND}) {
            var stack=new ItemStack(Lanterns.LANTERN.get());stack.getOrCreateTag().putInt("Fuel",1);p.setItemSlot(slot,stack);
            for(int i=0;i<LanternConfig.TICKS.get()*5-1;i++)Lanterns.LANTERN.get().inventoryTick(stack,t.getLevel(),p,0,true);
            t.assertTrue(Fuel.remaining(stack)==1,"80 efficiency must preserve hand fuel until five intervals");
            Lanterns.LANTERN.get().inventoryTick(stack,t.getLevel(),p,0,true);
            t.assertTrue(Fuel.remaining(stack)==0 && stack.getCount()==1,"Hand fuel must expire at five intervals");
        }
        var curio=new ItemStack(Lanterns.LANTERN.get());curio.getOrCreateTag().putInt("Fuel",1);
        var context=new SlotContext("belt",p,0,false,true);
        for(int i=0;i<LanternConfig.TICKS.get()*5-1;i++)Lanterns.LANTERN.get().curioTick(context,curio);
        t.assertTrue(Fuel.remaining(curio)==1,"Curios must use player fuel efficiency");
        Lanterns.LANTERN.get().curioTick(context,curio);t.assertTrue(Fuel.remaining(curio)==0,"Curios expires at five intervals");
        var carried=new ItemStack(Lanterns.LANTERN.get());Fuel.tick(carried,p);
        var be=new LanternEntity(t.absolutePos(new BlockPos(1,2,1)),Lanterns.BLOCK.get().defaultBlockState());be.setFuel(carried);
        var saved=be.saveWithoutMetadata();var restored=new LanternEntity(be.getBlockPos(),be.getBlockState());restored.load(saved);
        t.assertTrue(Math.abs(restored.asItem().getTag().getDouble("FuelFraction")-0.2)<1e-9,"Fractional fuel progress must survive block saves");
        Fuel.refill(carried);t.assertTrue(carried.getTag().getDouble("FuelFraction")==0,"Refill resets fractional progress");
        t.succeed();
    }
    @GameTest(template="empty") public static void heldFuelBurnsOutButItemSurvives(GameTestHelper t) {
        var p=player(t);var stack=new ItemStack(Lanterns.LANTERN.get());
        p.setItemSlot(EquipmentSlot.MAINHAND,stack);
        stack.getOrCreateTag().putInt("Fuel",1);
        stack.getOrCreateTag().putInt("FuelTicks",LanternConfig.TICKS.get()-1);
        Lanterns.LANTERN.get().inventoryTick(stack,t.getLevel(),p,0,true);
        t.assertTrue(Fuel.remaining(stack)==0 && stack.getCount()==1,"Empty lantern must survive");
        Fuel.refill(stack);
        t.assertTrue(Fuel.remaining(stack)==Fuel.capacity(),"Refill restores full capacity");
        p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
        Lanterns.LANTERN.get().inventoryTick(stack,t.getLevel(),p,1,false);
        t.assertTrue(stack.getTag().getInt("FuelTicks")==0,"Backpack lantern must not burn");
        t.succeed();
    }
    @GameTest(template="empty") public static void placedLanternExtinguishesAndPreservesFuel(GameTestHelper t) {
        var pos=new BlockPos(1,2,1);t.setBlock(pos.below(),Blocks.STONE);
        t.setBlock(pos,Lanterns.BLOCK.get().defaultBlockState());
        var abs=t.absolutePos(pos);var be=(LanternEntity)t.getLevel().getBlockEntity(abs);
        var stack=new ItemStack(Lanterns.LANTERN.get());stack.getOrCreateTag().putInt("Fuel",1);
        stack.getOrCreateTag().putInt("FuelTicks",LanternConfig.TICKS.get()-1);be.setFuel(stack);
        LanternEntity.tick(t.getLevel(),abs,be.getBlockState(),be);
        t.assertTrue(!t.getBlockState(pos).getValue(BlockStateProperties.LIT),"Block must extinguish");
        t.assertTrue(t.getBlockState(pos).getLightEmission(t.getLevel(),abs)==0,"Empty block emits no light");
        t.assertTrue(Fuel.remaining(be.asItem())==0,"Taking empty lantern cannot refill it");
        var saved=be.saveWithoutMetadata();var copy=new LanternEntity(abs,be.getBlockState());copy.load(saved);
        t.assertTrue(copy.fuel()==0,"Empty fuel survives saving");
        be.refill();t.assertTrue(t.getBlockState(pos).getValue(BlockStateProperties.LIT),"Refill relights block");
        t.assertTrue(Fuel.remaining(be.asItem())==Fuel.capacity(),"Picked item keeps fuel");
        var wet=be.getBlockState().setValue(BlockStateProperties.WATERLOGGED,true);
        t.assertTrue(wet.getLightEmission(t.getLevel(),abs)==7,"Underwater block uses seven light levels");
        t.succeed();
    }
    @GameTest(template="empty") public static void curiosFuelAndOilRefill(GameTestHelper t) {
        var p=player(t);var stack=new ItemStack(Lanterns.LANTERN.get());
        stack.getOrCreateTag().putInt("Fuel",1);stack.getOrCreateTag().putInt("FuelTicks",LanternConfig.TICKS.get()-1);
        var ctx=new SlotContext("belt",p,0,false,true);
        Lanterns.LANTERN.get().curioTick(ctx,stack);
        t.assertTrue(Fuel.remaining(stack)==0 && !stack.isEmpty(),"Curios burns fuel without breaking item");
        var inventory=CuriosApi.getCuriosInventory(p).resolve().orElseThrow();
        var belt=inventory.getStacksHandler("belt").orElseThrow();belt.getStacks().setStackInSlot(0,stack);
        var oil=new ItemStack(Lanterns.OIL.get(),1);p.setItemSlot(EquipmentSlot.MAINHAND,oil);
        Lanterns.OIL.get().use(t.getLevel(),p,net.minecraft.world.InteractionHand.MAIN_HAND);
        t.assertTrue(Fuel.remaining(belt.getStacks().getStackInSlot(0))==Fuel.capacity(),"Oil refills equipped Curios lantern");
        t.assertTrue(oil.isEmpty(),"Refill consumes one oil");
        t.assertTrue(p.getInventory().contains(new ItemStack(Items.GLASS_BOTTLE)),"Oil returns its bottle");
        t.succeed();
    }
}
