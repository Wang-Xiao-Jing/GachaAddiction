package xiaojin.gachaaddiction.client.render;

public class GachaChestAnimation {
    public static final long OPEN_MILLIS = 450;
    public static final long HOLD_MILLIS = 180;
    public static final long DROP_MILLIS = 320;
    public static final long ACTIVATE_MILLIS = OPEN_MILLIS + HOLD_MILLIS;
    public static final long TOTAL_MILLIS = ACTIVATE_MILLIS + DROP_MILLIS;

    public static float opening(long elapsedMillis) {
        return GachaAnimationRenderer.smoothProgress((float) elapsedMillis / OPEN_MILLIS);
    }

    public static float drop(long elapsedMillis) {
        return GachaAnimationRenderer.smoothProgress((float) (elapsedMillis - ACTIVATE_MILLIS) / DROP_MILLIS);
    }
}
