package xiaojin.gachaaddiction.client.render;

import net.minecraft.client.gui.GuiGraphics;

public class ModernGachaEntrance {
    private static final long INITIAL_MILLIS = 600;
    private static final long CHANGE_MILLIS = 300;
    private static final long NOT_STARTED = -1;
    private static final float ITEM_START = 80.0F / INITIAL_MILLIS;
    private static final float ITEM_END = 450.0F / INITIAL_MILLIS;
    private static final float BACKGROUND_END = 150.0F / INITIAL_MILLIS;
    private static final float RIM_START = 350.0F / INITIAL_MILLIS;
    private static final float ENTRY_HEIGHT = 16.0F;
    private static final float COMPACT_DISTANCE = 0.5F;
    private static final float INITIAL_SCALE = 0.95F;
    private long startedAt = NOT_STARTED;
    private boolean compact;
    private boolean complete;

    public void reset(boolean compact) {
        this.compact = compact;
        startedAt = NOT_STARTED;
        complete = false;
    }

    public Frame sample(long now) {
        if (complete) {
            return Frame.VISIBLE;
        }
        if (startedAt == NOT_STARTED) {
            startedAt = now;
        }
        float progress = Math.clamp((float) (now - startedAt) / (compact ? CHANGE_MILLIS : INITIAL_MILLIS), 0.0F, 1.0F);
        complete = progress >= 1.0F;
        float motion = GachaAnimationRenderer.smoothProgress((progress - ITEM_START) / (ITEM_END - ITEM_START));
        float backdrop = GachaAnimationRenderer.smoothProgress(progress / BACKGROUND_END);
        float rim = GachaAnimationRenderer.smoothProgress((progress - RIM_START) / (1.0F - RIM_START));
        return new Frame(motion, ENTRY_HEIGHT * (compact ? COMPACT_DISTANCE : 1.0F) * (1.0F - motion),
                INITIAL_SCALE + (1.0F - INITIAL_SCALE) * motion, backdrop, rim);
    }

    public boolean isComplete() {
        return complete;
    }

    public void finish() {
        complete = true;
    }

    public record Frame(float opacity, float offsetY, float scale, float backdrop, float rim) {
        public static final Frame VISIBLE = new Frame(1.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        public void apply(GuiGraphics graphics, float centerX, float centerY) {
            graphics.pose().translate(centerX, centerY + offsetY, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-centerX, -centerY, 0.0F);
        }
    }
}
