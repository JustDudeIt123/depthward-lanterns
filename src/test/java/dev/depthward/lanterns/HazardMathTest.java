package dev.depthward.lanterns;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HazardMathTest {
    @Test void damageIsArithmeticAndCapped(){assertEquals(1,HazardMath.darknessDamage(0,1,1,20));assertEquals(2,HazardMath.darknessDamage(1,1,1,20));assertEquals(3,HazardMath.darknessDamage(2,1,1,20));assertEquals(20,HazardMath.darknessDamage(1000000,1,1,20));}
    @Test void customDamageSettingsSupportFractionsAndZeroIncrease(){assertEquals(1.5,HazardMath.darknessDamage(2,0.5,0.5,10));assertEquals(2,HazardMath.darknessDamage(100,2,0,10));assertEquals(0,HazardMath.darknessDamage(0,0,1,20));}
    @Test void lightRadiusHasNoProtectionOutsideOrAtZero(){assertTrue(HazardMath.inLightRadius(25,6));assertFalse(HazardMath.inLightRadius(36,6));assertFalse(HazardMath.inLightRadius(0,0));}
}
