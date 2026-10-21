package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.client.presentation.GachaPresentationClient;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CardPoolResultScreen extends Screen {
    private static final int COLUMNS = 9;
    private static final int ROWS = 4;
    private static final int ITEMS_PER_PAGE = COLUMNS * ROWS;
    private static final int SLOT_SIZE = 20;
    private static final int BUTTON_HEIGHT = 20;

    private final UUID transactionId;
    private final ResourceLocation cardPoolId;
    private final ResourceLocation packageId;
    private final List<ItemStack> rewards;
    private int page;

    public CardPoolResultScreen(UUID transactionId, ResourceLocation cardPoolId, ResourceLocation packageId,
                                List<ItemStack> rewards) {
        super(GachaPresentationClient.cardPoolTitle(cardPoolId));
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.cardPoolId = Objects.requireNonNull(cardPoolId, "cardPoolId");
        this.packageId = Objects.requireNonNull(packageId, "packageId");
        this.rewards = rewards.stream().map(ItemStack::copy).toList();
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        int center = width / 2;
        int buttonY = height / 2 + ROWS * SLOT_SIZE / 2 + 30;
        Button previous = addRenderableWidget(Button.builder(
                        Component.translatable(TranslationKeys.GUI_CARD_POOL_PREVIOUS),
                        ignored -> changePage(-1))
                .bounds(center - 90, buttonY, 56, BUTTON_HEIGHT).build());
        Button done = addRenderableWidget(Button.builder(
                        Component.translatable(TranslationKeys.GUI_CARD_POOL_RESULT_DONE),
                        ignored -> onClose())
                .bounds(center - 28, buttonY, 56, BUTTON_HEIGHT).build());
        Button next = addRenderableWidget(Button.builder(
                        Component.translatable(TranslationKeys.GUI_CARD_POOL_NEXT),
                        ignored -> changePage(1))
                .bounds(center + 34, buttonY, 56, BUTTON_HEIGHT).build());
        previous.active = page > 0;
        next.active = (page + 1) * ITEMS_PER_PAGE < rewards.size();
        done.active = true;
    }

    private void changePage(int offset) {
        page += offset;
        rebuildWidgets();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - COLUMNS * SLOT_SIZE / 2;
        int top = height / 2 - ROWS * SLOT_SIZE / 2;
        graphics.drawCenteredString(font, title, width / 2, top - 32, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(
                TranslationKeys.GUI_CARD_POOL_RESULT_CONTEXT, cardPoolId, packageId,
                transactionId.toString()), width / 2, top - 18, 0xA0A0A0);
        int firstIndex = page * ITEMS_PER_PAGE;
        int lastIndex = Math.min(firstIndex + ITEMS_PER_PAGE, rewards.size());
        for (int index = firstIndex; index < lastIndex; index++) {
            int localIndex = index - firstIndex;
            int x = left + localIndex % COLUMNS * SLOT_SIZE + 2;
            int y = top + localIndex / COLUMNS * SLOT_SIZE + 2;
            ItemStack stack = rewards.get(index);
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.renderTooltip(font, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
