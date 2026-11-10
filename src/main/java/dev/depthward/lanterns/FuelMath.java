package dev.depthward.lanterns;
/** Pure fuel transition shared by item and block. Long arithmetic avoids config overflow. */
public final class FuelMath {
    public static double consumptionRate(double efficiency) {
        return 1.0 - (Double.isFinite(efficiency) ? Math.max(0, Math.min(80, efficiency)) : 0) / 100.0;
    }
    public record TimedState(int remaining, int progress, double fraction) {}
    public static TimedState tickScaled(int remaining, int progress, double fraction, int capacity, int interval, double efficiency) {
        remaining = Math.max(0, Math.min(capacity, remaining));
        if (remaining == 0) return new TimedState(0, 0, 0);
        double carry = Double.isFinite(fraction) ? Math.max(0, Math.min(Math.nextDown(1.0), fraction)) : 0;
        double accumulated = carry + consumptionRate(efficiency);
        int wholeTicks = (int)Math.floor(accumulated + 1e-9);
        double nextFraction = Math.max(0, accumulated - wholeTicks);
        long elapsed = Math.max(0L, progress) + wholeTicks;
        int next = (int)Math.max(0L, remaining - elapsed / interval);
        return new TimedState(next, next == 0 ? 0 : (int)(elapsed % interval), next == 0 ? 0 : nextFraction);
    }
    public record State(int remaining, int progress) {}
    public static State tick(int remaining, int progress, int capacity, int interval) {
        remaining = Math.max(0, Math.min(capacity, remaining));
        if (remaining == 0) return new State(0, 0);
        long elapsed = Math.max(0L, progress) + 1L;
        long cost = elapsed / interval;
        int next = (int)Math.max(0L, remaining - cost);
        return new State(next, next == 0 ? 0 : (int)(elapsed % interval));
    }
}
