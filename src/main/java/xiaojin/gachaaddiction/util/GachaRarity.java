package xiaojin.gachaaddiction.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.api.ItemStackEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GachaRarity {
    public static final int MAX_LEVEL = 4;

    private static final int[] COLORS = {0xBFC6D1, 0x5BE05B, 0x49A6FF, 0xC15BFF, 0xFFB43C};
    private static final float[] PROBABILITY_STEPS = {0.20f, 0.10f, 0.04f, 0.015f};
    private static final float[] DOMINANCE_STEPS = {0.30f, 0.60f, 0.80f, 0.93f};

    private GachaRarity() {
    }

    public static int colorOf(int level) {
        return COLORS[Math.clamp(level, 0, COLORS.length - 1)];
    }

    public static Scale scaleOf(List<ItemStackEntry> entries) {
        Map<Item, Integer> weights = new HashMap<>();
        int total = 0;
        for (ItemStackEntry entry : entries) {
            int weight = entry.weight();
            if (weight <= 0) {
                continue;
            }
            total += weight;
            if (entry.stack().isEmpty()) {
                continue;
            }
            weights.merge(entry.stack().getItem(), weight, Integer::sum);
        }
        return new Scale(weights, total);
    }

    private static int stepOf(float[] steps, float value, boolean atLeast) {
        for (int i = 0; i < steps.length; i++) {
            if (atLeast ? value >= steps[i] : value < steps[i]) {
                return i;
            }
        }
        return steps.length;
    }

    public static final class Scale {
        private final Map<Item, Integer> weights;
        private final int total;

        private Scale(Map<Item, Integer> weights, int total) {
            this.weights = weights;
            this.total = total;
        }

        public boolean isUsable() {
            return total > 0 && weights.size() > 1;
        }

        public int levelOf(ItemStack stack) {
            if (!isUsable()) {
                return -1;
            }
            Integer weight = weights.get(stack.getItem());
            if (weight == null || weight <= 0) {
                return -1;
            }

            float probability = weight / (float) total;
            int commoner = 0;
            for (int other : weights.values()) {
                if (other > weight) {
                    commoner += other;
                }
            }
            float dominance = commoner / (float) total;

            return Math.min(stepOf(PROBABILITY_STEPS, probability, true),
                    stepOf(DOMINANCE_STEPS, dominance, false));
        }
    }
}
