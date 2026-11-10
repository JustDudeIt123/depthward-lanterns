package dev.depthward.lanterns;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FuelMathTest {
    @Test void efficiencyReducesConsumptionLinearlyIncludingFractionalValues() {
        for(double efficiency:new double[]{0, 12.5, 50, 80}) {
            var state=new FuelMath.TimedState(1000,0,0);
            for(int i=0;i<20000;i++) state=FuelMath.tickScaled(state.remaining(),state.progress(),state.fraction(),1000,20,efficiency);
            assertEquals((int)(1000*efficiency/100),state.remaining(),"Efficiency "+efficiency);
        }
        assertEquals(1,FuelMath.consumptionRate(-100));
        assertEquals(0.2,FuelMath.consumptionRate(100),1e-12);
    }
    @Test void maximumEfficiencyWorksWithSingleTickIntervalAndChangingAttributes() {
        var state=new FuelMath.TimedState(1,0,0);
        for(int i=0;i<4;i++)state=FuelMath.tickScaled(state.remaining(),state.progress(),state.fraction(),1,1,80);
        assertEquals(1,state.remaining());
        state=FuelMath.tickScaled(state.remaining(),state.progress(),state.fraction(),1,1,80);
        assertEquals(0,state.remaining());
        var half=FuelMath.tickScaled(5,0,0,5,20,50);
        var changed=FuelMath.tickScaled(half.remaining(),half.progress(),half.fraction(),5,20,0);
        assertEquals(1,changed.progress());assertEquals(0.5,changed.fraction(),1e-12);
    }

    @Test void burnsOnlyAtConfiguredIntervalAndNeverBelowZero() {
        var s=new FuelMath.State(2,0);
        for(int i=0;i<19;i++) s=FuelMath.tick(s.remaining(),s.progress(),2,20);
        assertEquals(2,s.remaining());
        s=FuelMath.tick(s.remaining(),s.progress(),2,20);assertEquals(1,s.remaining());
        for(int i=0;i<20;i++)s=FuelMath.tick(s.remaining(),s.progress(),2,20);
        assertEquals(new FuelMath.State(0,0),s);
        assertEquals(s,FuelMath.tick(0,999,2,20));
    }
    @Test void loweringIntervalAppliesAccumulatedProgressAndCapacityClamps() {
        assertEquals(new FuelMath.State(7,0),FuelMath.tick(10,59,10,20));
        assertEquals(new FuelMath.State(5,1),FuelMath.tick(100,0,5,20));
    }
    @Test void singleTickFuelAndLargeConfigValuesAreSafe() {
        assertEquals(new FuelMath.State(0,0),FuelMath.tick(1,0,1,1));
        assertEquals(new FuelMath.State(999999,0),FuelMath.tick(1000000,1199999,1000000,1200000));
    }
}
