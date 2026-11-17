package xiaojin.gachaaddiction.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import xiaojin.gachaaddiction.api.GachaRandomType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.api.RewardData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ModernGachaSilhouette {
    public static final int HIDDEN_COLOR = 0xD0D0D0;
    private static final String QUESTION_MARK = "？";
    private static final float QUESTION_SCALE_DENOMINATOR = 48.0F;
    private static final float MAXIMUM_QUESTION_SCALE = 6.0F;
    private static final int OVERLAY_DEPTH = 1000;
    private final Map<RewardData, ItemStack> silhouettes = new HashMap<>();
    private GachaRandomType cachedType = GachaRandomType.NONE;
    private List<ItemStack> candidates = List.of();

    public ItemStack item(RewardData reward, GachaRandomType type, List<ItemStackEntry> entries) {
        if (type == GachaRandomType.NONE) {
            return reward.getItemStack();
        }

        if (type != cachedType) {
            cachedType = type;
            silhouettes.clear();
            var stacks = ItemStackLinkedSet.createTypeAndComponentsSet();
            for (ItemStackEntry entry : entries) {
                if (entry.weight() > 0 && !entry.stack().isEmpty()) {
                    stacks.add(entry.stack().copyWithCount(1));
                }
            }

            if (type == GachaRandomType.FULL_RANDOM) {
                for (var registered : BuiltInRegistries.ITEM) {
                    ItemStack stack = registered.getDefaultInstance();
                    if (!stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }

            candidates = List.copyOf(stacks);
        }

        return silhouettes.computeIfAbsent(reward, ignored -> candidates.isEmpty() ? ItemStack.EMPTY
                : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).copy());
    }

    public void renderQuestion(GuiGraphics graphics, Font font, int left, int top, int width, int height,
                               ModernGachaEntrance.Frame entrance) {
        int alpha = (int) (255 * entrance.opacity());
        if (alpha < 4) {
            return;
        }

        float centerX = left + width / 2.0F;
        float centerY = top + height / 2.0F;
        float scale = Math.clamp(Math.min(width, height) / QUESTION_SCALE_DENOMINATOR, 1.0F, MAXIMUM_QUESTION_SCALE);
        graphics.pose().pushPose();
        try {
            entrance.apply(graphics, centerX, centerY);
            graphics.pose().translate(centerX, centerY, OVERLAY_DEPTH);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawCenteredString(font, QUESTION_MARK, 0, -font.lineHeight / 2, alpha << 24 | 0xFFFFFF);
        } finally {
            graphics.pose().popPose();
        }
    }
}
