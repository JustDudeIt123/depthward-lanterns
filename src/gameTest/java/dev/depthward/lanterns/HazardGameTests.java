package dev.depthward.lanterns;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.*;
import top.theillusivec4.curios.api.CuriosApi;
@GameTestHolder(Lanterns.ID)
@PrefixGameTestTemplate(false)
public final class HazardGameTests {
    private static final class MortalPlayer extends FakePlayer {
        int whispers;
        MortalPlayer(ServerLevel level){super(level,new GameProfile(UUID.randomUUID(),"HazardTest"));setGameMode(GameType.SURVIVAL);getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);setHealth(100);
            // FakePlayer.tick() is a no-op, so vanilla spawn invulnerability would otherwise never expire.
            try {var immunity=ServerPlayer.class.getDeclaredField("spawnInvulnerableTime");immunity.setAccessible(true);immunity.setInt(this,0);}catch(ReflectiveOperationException e){throw new AssertionError(e);}
        }
        @Override public boolean isInvulnerableTo(DamageSource source){return getAbilities().invulnerable;}
        @Override public void playNotifySound(SoundEvent sound,SoundSource category,float volume,float pitch){if(sound==Lanterns.WHISPER.get())whispers++;}
    }
    private static MortalPlayer player(GameTestHelper t){return new MortalPlayer(t.getLevel());}
    private static ItemStack lamp(int fuel){var s=new ItemStack(Lanterns.LANTERN.get());s.getOrCreateTag().putInt("Fuel",fuel);return s;}
    @GameTest(template="empty") public static void whisperThenTenSecondsThenArithmeticDamage(GameTestHelper t) {
        var p=player(t);int delay=LanternConfig.DARK_WARNING_TICKS.get(),interval=LanternConfig.DARK_DAMAGE_INTERVAL.get();
        double initial=LanternConfig.DARK_INITIAL_DAMAGE.get(),increment=LanternConfig.DARK_DAMAGE_INCREASE.get(),max=LanternConfig.DARK_MAX_DAMAGE.get();
        try {
            LanternConfig.DARK_WARNING_TICKS.set(200);LanternConfig.DARK_DAMAGE_INTERVAL.set(40);LanternConfig.DARK_INITIAL_DAMAGE.set(1.0);LanternConfig.DARK_DAMAGE_INCREASE.set(1.0);LanternConfig.DARK_MAX_DAMAGE.set(20.0);
            for(int i=0;i<200;i++)HazardSystem.advance(p,true,false);
            t.assertTrue(p.whispers==1 && !p.hasEffect(LanternEffects.DARKNESS.get()) && p.getHealth()==100,"Whisper once immediately, with ten seconds free of effects and damage");
            HazardSystem.advance(p,true,false);
            t.assertTrue(p.hasEffect(LanternEffects.DARKNESS.get()) && p.getHealth()==100,"Darkness effect must start only after the warning");
            for(int i=1;i<40;i++)HazardSystem.advance(p,true,false);
            t.assertTrue(p.getHealth()==99,"First hit must deal one health point");
            for(int i=0;i<40;i++)HazardSystem.advance(p,true,false);
            t.assertTrue(p.getHealth()==97,"Second hit must deal two health points");
            for(int i=0;i<40;i++)HazardSystem.advance(p,true,false);
            t.assertTrue(p.getHealth()==94 && p.whispers==1,"Third hit must deal three health points without repeating whisper");
            HazardSystem.advance(p,false,false);
            t.assertTrue(!p.hasEffect(LanternEffects.DARKNESS.get()) && HazardSystem.snapshot(p).damageStep()==0,"Light must remove darkness and reset increasing damage");
            HazardSystem.advance(p,true,false);t.assertTrue(p.whispers==2 && !p.hasEffect(LanternEffects.DARKNESS.get()),"New darkness exposure must start a fresh warning");
        } finally {HazardSystem.reset(p);LanternConfig.DARK_WARNING_TICKS.set(delay);LanternConfig.DARK_DAMAGE_INTERVAL.set(interval);LanternConfig.DARK_INITIAL_DAMAGE.set(initial);LanternConfig.DARK_DAMAGE_INCREASE.set(increment);LanternConfig.DARK_MAX_DAMAGE.set(max);}
        t.succeed();
    }
    @GameTest(template="empty") public static void lensInventoryUpgradePreservesItemAndTooltip(GameTestHelper t) {
        var p=player(t);var s=lamp(17);s.setHoverName(net.minecraft.network.chat.Component.literal("My lamp"));s.enchant(net.minecraft.world.item.enchantment.Enchantments.MENDING,1);s.getOrCreateTag().putInt("FuelTicks",9);s.getOrCreateTag().putDouble("FuelFraction",0.4);Fuel.setEnabled(s,false);
        var before=s.copy();var menu=p.inventoryMenu;var slot=menu.getSlot(9);slot.set(s);menu.setCarried(new ItemStack(Lanterns.INFERNAL_LENS.get(),2));
        menu.clicked(9,1,ClickType.PICKUP,p);
        t.assertTrue(Fuel.upgraded(slot.getItem()) && menu.getCarried().getCount()==1,"Inventory right click must upgrade and consume one lens");
        var compared=slot.getItem().copy();compared.removeTagKey("Upgraded");t.assertTrue(ItemStack.matches(before,compared),"Fuel, switch, name and enchantments must all survive upgrade");
        menu.clicked(9,1,ClickType.PICKUP,p);t.assertTrue(menu.getCarried().getCount()==1,"Already upgraded lantern cannot consume another lens");
        var lines=new java.util.ArrayList<net.minecraft.network.chat.Component>();Lanterns.LANTERN.get().appendHoverText(slot.getItem(),t.getLevel(),lines,TooltipFlag.NORMAL);
        t.assertTrue(lines.size()==2 && lines.get(1).getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents c && c.getKey().equals("tooltip.depthward_lanterns.upgraded"),"Upgraded label must be an independent translation key");
        t.assertTrue(lines.get(1).getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(net.minecraft.ChatFormatting.AQUA)),"Upgrade label must be light blue");
        Fuel.refill(s);t.assertTrue(Fuel.upgraded(s),"Oil refill must preserve upgrade");
        var be=new LanternEntity(t.absolutePos(new BlockPos(1,2,1)),Lanterns.BLOCK.get().defaultBlockState());be.setFuel(s);
        var copy=new LanternEntity(be.getBlockPos(),be.getBlockState());copy.load(be.saveWithoutMetadata());
        t.assertTrue(Fuel.upgraded(copy.asItem()),"Upgrade must survive block save and pickup");t.succeed();
    }
    @GameTest(template="empty") public static void lensWorksFromHandsCuriosAndOnPlacedLantern(GameTestHelper t) {
        var p=player(t);var held=lamp(24);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Lanterns.INFERNAL_LENS.get(),2));p.setItemSlot(EquipmentSlot.OFFHAND,held);
        Lanterns.INFERNAL_LENS.get().use(t.getLevel(),p,InteractionHand.MAIN_HAND);
        t.assertTrue(Fuel.upgraded(held) && Fuel.remaining(held)==24,"Lens use must upgrade other-hand lantern without refilling");
        p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);var worn=lamp(30);CuriosApi.getCuriosInventory(p).resolve().orElseThrow().getStacksHandler("belt").orElseThrow().getStacks().setStackInSlot(0,worn);
        Lanterns.INFERNAL_LENS.get().use(t.getLevel(),p,InteractionHand.MAIN_HAND);t.assertTrue(Fuel.upgraded(worn) && p.getMainHandItem().isEmpty(),"Lens use must upgrade Curios lantern and consume last lens");
        var pos=new BlockPos(1,2,1);t.setBlock(pos.below(),Blocks.STONE);t.setBlock(pos,Lanterns.BLOCK.get().defaultBlockState());var abs=t.absolutePos(pos);var be=(LanternEntity)t.getLevel().getBlockEntity(abs);be.setFuel(lamp(11));
        p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Lanterns.INFERNAL_LENS.get()));
        var context=new UseOnContext(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(abs),Direction.UP,abs,false));
        var result=Lanterns.INFERNAL_LENS.get().useOn(context);
        t.assertTrue(result.consumesAction() && Fuel.upgraded(be.asItem()) && be.fuel()==11 && p.getMainHandItem().isEmpty(),"Placed upgrade must preserve fuel and consume lens");t.succeed();
    }
    @GameTest(template="empty") public static void hellfireRequiresOwnUpgradedLitHeldOrCuriosLantern(GameTestHelper t) {
        var nether=t.getLevel().getServer().getLevel(Level.NETHER);t.assertTrue(nether!=null,"Nether dimension must exist");var p=new MortalPlayer(nether);
        var s=lamp(100);p.setItemSlot(EquipmentSlot.MAINHAND,s);
        t.assertTrue(HazardSystem.hellfireUnsafe(p),"Ordinary lit lantern cannot prevent hellfire");
        InfernalLensItem.upgrade(p,s,new ItemStack(Lanterns.INFERNAL_LENS.get()));t.assertTrue(!HazardSystem.hellfireUnsafe(p),"Upgraded lit lantern in main hand must protect");
        Fuel.setEnabled(s,false);t.assertTrue(HazardSystem.hellfireUnsafe(p),"Disabled upgraded lantern cannot protect");Fuel.setEnabled(s,true);s.getOrCreateTag().putInt("Fuel",0);t.assertTrue(HazardSystem.hellfireUnsafe(p),"Empty upgraded lantern cannot protect");Fuel.refill(s);
        p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.OFFHAND,s);t.assertTrue(!HazardSystem.hellfireUnsafe(p),"Offhand upgraded light must protect");
        p.setItemSlot(EquipmentSlot.OFFHAND,ItemStack.EMPTY);p.getInventory().setItem(9,s);t.assertTrue(HazardSystem.hellfireUnsafe(p),"Backpack upgraded lantern cannot protect");p.getInventory().setItem(9,ItemStack.EMPTY);
        var belt=CuriosApi.getCuriosInventory(p).resolve().orElseThrow().getStacksHandler("belt").orElseThrow();belt.getStacks().setStackInSlot(0,s);t.assertTrue(!HazardSystem.hellfireUnsafe(p),"Curios upgraded lit lantern must protect");
        double radius=LanternConfig.HELD_RADIUS.get();try{LanternConfig.HELD_RADIUS.set(0.0);t.assertTrue(HazardSystem.hellfireUnsafe(p),"Zero-radius lantern does not actually shine and cannot protect");}finally{LanternConfig.HELD_RADIUS.set(radius);}
        var overworld=player(t);t.assertTrue(!HazardSystem.hellfireUnsafe(overworld),"Hellfire is restricted to the Nether");t.succeed();
    }
    @GameTest(template="empty") public static void hellfireDoesPeriodicDamageWithoutIgnitingAndStopsWhenProtected(GameTestHelper t) {
        var p=player(t);int interval=LanternConfig.HELLFIRE_DAMAGE_INTERVAL.get();double damage=LanternConfig.HELLFIRE_DAMAGE.get();
        try {
            LanternConfig.HELLFIRE_DAMAGE_INTERVAL.set(2);LanternConfig.HELLFIRE_DAMAGE.set(1.0);
            HazardSystem.advance(p,false,true);t.assertTrue(p.hasEffect(LanternEffects.HELLFIRE.get()) && p.getHealth()==100,"Hellfire effect must precede periodic damage");
            HazardSystem.advance(p,false,true);t.assertTrue(p.getHealth()==99 && !p.isOnFire(),"Hellfire must deal health damage without setting player on fire");
            HazardSystem.advance(p,false,false);t.assertTrue(!p.hasEffect(LanternEffects.HELLFIRE.get()) && HazardSystem.snapshot(p).hellTicks()==0,"Protection must remove effect and reset damage timer");
            p.setGameMode(GameType.CREATIVE);HazardSystem.advance(p,true,true);t.assertTrue(!p.hasEffect(LanternEffects.DARKNESS.get()) && !p.hasEffect(LanternEffects.HELLFIRE.get()),"Creative players must be immune");
            p.setGameMode(GameType.SPECTATOR);HazardSystem.advance(p,true,true);t.assertTrue(p.getHealth()==99 && !p.hasEffect(LanternEffects.HELLFIRE.get()),"Spectators must be immune");
        } finally {HazardSystem.reset(p);LanternConfig.HELLFIRE_DAMAGE_INTERVAL.set(interval);LanternConfig.HELLFIRE_DAMAGE.set(damage);}
        t.succeed();
    }
    @GameTest(template="hazard_room",timeoutTicks=100) public static void actualDarkRoomAndServerDynamicLanternProtection(GameTestHelper t) {
        var pos=new BlockPos(3,2,3);var abs=t.absolutePos(pos);var p=player(t);p.setPos(abs.getX()+0.5,abs.getY(),abs.getZ()+0.5);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=4;y<=6;y++)t.setBlock(pos.offset(x,y,z),Blocks.STONE);
        for(var direction:Direction.values())t.setBlock(pos.relative(direction),Blocks.STONE);t.setBlock(pos,Blocks.AIR);
        t.runAfterDelay(6,()->{
            HazardLights.clear();t.assertTrue(HazardSystem.isDark(p),"Unlit enclosed room must meet hostile-spawn-like darkness threshold");
            var s=lamp(100);p.setItemSlot(EquipmentSlot.MAINHAND,s);t.assertTrue(!HazardSystem.isDark(p),"Own dynamic lantern must protect even though client-only light does not affect vanilla spawning");
            Fuel.setEnabled(s,false);t.assertTrue(HazardSystem.isDark(p),"Switched-off lantern must not protect from darkness");p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            var far=pos.offset(16,0,0);t.setBlock(far.below(),Blocks.STONE);t.setBlock(far,Lanterns.BLOCK.get().defaultBlockState());
            t.runAfterDelay(3,()->{
                var be=(LanternEntity)t.getLevel().getBlockEntity(t.absolutePos(far));
                t.assertTrue(t.getLevel().getBrightness(LightLayer.BLOCK,abs)==0,"Far lantern must be outside vanilla block light at player");
                t.assertTrue(!HazardSystem.isDark(p),"Extended configured radius of placed lantern must protect on server too");
                var item=be.asItem();Fuel.setEnabled(item,false);be.setFuel(item);t.assertTrue(HazardSystem.isDark(p),"Extinguishing nearby placed source must restore darkness condition");
                t.succeed();
            });
        });
    }
    @GameTest(template="hazard_room",timeoutTicks=100)
    public static void blockAndHeldSourcesProtectFromDarkness(GameTestHelper t){
        var pos=new BlockPos(3,2,3);var absolute=t.absolutePos(pos);var p=player(t);p.setPos(absolute.getX()+.5,absolute.getY(),absolute.getZ()+.5);
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=4;y<=6;y++)t.setBlock(pos.offset(x,y,z),Blocks.STONE);
        for(var direction:Direction.values())t.setBlock(pos.relative(direction),Blocks.STONE);
        t.setBlock(pos,Blocks.AIR);t.setBlock(pos.above(),Blocks.GLOWSTONE);
        t.runAfterDelay(8,()->{
            t.assertTrue(t.getLevel().getBrightness(LightLayer.BLOCK,absolute)>0 && !HazardSystem.isDark(p),"Stationary glowstone must protect again");
            t.setBlock(pos.above(),Blocks.STONE);
            t.runAfterDelay(8,()->{
                t.assertTrue(HazardSystem.isDark(p),"Unlit room fixture");
                for(var item:new Item[]{Items.GLOWSTONE_DUST,Items.TORCH,Items.LAVA_BUCKET,Items.GLOW_INK_SAC}){
                    p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(item));t.assertTrue(!HazardSystem.isDark(p),"Server must recognize held light: "+item);
                }
                var lamp=new ItemStack(Items.REDSTONE_LAMP);lamp.getOrCreateTag().getCompound("BlockStateTag");
                var tag=new CompoundTag();tag.putString("lit","false");lamp.getOrCreateTag().put("BlockStateTag",tag);
                t.assertTrue(HeldItemLights.luminance(lamp,false)==0,"Unlit NBT lamp must not shine");tag.putString("lit","true");
                t.assertTrue(HeldItemLights.luminance(lamp,false)>0,"Lit NBT lamp must shine");
                p.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);p.getInventory().setItem(9,new ItemStack(Items.GLOWSTONE_DUST));
                t.assertTrue(HazardSystem.isDark(p),"Backpack light item does not protect");t.succeed();
            });
        });
    }
}
