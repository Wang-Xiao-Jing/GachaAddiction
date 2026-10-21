package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntry;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardInboxEntryKind;
import xiaojin.gachaaddiction.network.cts.ClaimRewardInboxPayload;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;

public final class RewardInboxScreen extends Screen {
    private static final int ENTRIES_PER_PAGE = 6;
    private static final int REWARD_COLUMNS = 9;
    private static final int REWARD_ROWS = 4;
    private static final int REWARDS_PER_PAGE = REWARD_COLUMNS * REWARD_ROWS;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int ENTRY_PANEL_WIDTH = 145;
    private static final int SLOT_SIZE = 18;

    private final List<GachaRewardInboxEntry> entries;
    private int entryPage;
    private int selectedIndex;
    private int rewardPage;

    public RewardInboxScreen(List<GachaRewardInboxEntry> entries) {
        super(Component.translatable(TranslationKeys.GUI_REWARD_INBOX_TITLE));
        this.entries = List.copyOf(entries);
        this.selectedIndex = entries.isEmpty() ? -1 : 0;
    }

    private static String shortId(GachaRewardInboxEntry entry) {
        return entry.transactionId().value().toString().substring(0, 8);
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        int left = width / 2 - 165;
        int top = 42;
        int firstIndex = entryPage * ENTRIES_PER_PAGE;
        int lastIndex = Math.min(firstIndex + ENTRIES_PER_PAGE, entries.size());
        for (int index = firstIndex; index < lastIndex; index++) {
            GachaRewardInboxEntry entry = entries.get(index);
            int selected = index;
            Component kind = Component.translatable(entry.kind() == GachaRewardInboxEntryKind.REWARD
                    ? TranslationKeys.GUI_REWARD_INBOX_KIND_REWARD : TranslationKeys.GUI_REWARD_INBOX_KIND_RETURN);
            addRenderableWidget(Button.builder(Component.translatable(TranslationKeys.GUI_REWARD_INBOX_ENTRY,
                            kind, shortId(entry), entry.rewards().size()), ignored -> select(selected))
                    .bounds(left, top + (index - firstIndex) * (BUTTON_HEIGHT + BUTTON_GAP),
                            ENTRY_PANEL_WIDTH, BUTTON_HEIGHT).build());
        }
        int navigationY = top + ENTRIES_PER_PAGE * (BUTTON_HEIGHT + BUTTON_GAP);
        Button previousEntries = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_PREVIOUS), ignored -> changeEntryPage(-1))
                .bounds(left, navigationY, 70, BUTTON_HEIGHT).build());
        Button nextEntries = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_NEXT), ignored -> changeEntryPage(1))
                .bounds(left + ENTRY_PANEL_WIDTH - 70, navigationY, 70, BUTTON_HEIGHT).build());
        previousEntries.active = entryPage > 0;
        nextEntries.active = (entryPage + 1) * ENTRIES_PER_PAGE < entries.size();
        int rewardNavigationX = width / 2 + 15;
        Button previousRewards = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_PREVIOUS), ignored -> changeRewardPage(-1))
                .bounds(rewardNavigationX, navigationY, 70, BUTTON_HEIGHT).build());
        Button nextRewards = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_NEXT), ignored -> changeRewardPage(1))
                .bounds(rewardNavigationX + REWARD_COLUMNS * SLOT_SIZE - 70, navigationY, 70, BUTTON_HEIGHT).build());
        int rewardCount = selectedIndex < 0 ? 0 : entries.get(selectedIndex).rewards().size();
        previousRewards.active = rewardPage > 0;
        nextRewards.active = (rewardPage + 1) * REWARDS_PER_PAGE < rewardCount;
        Button claim = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_REWARD_INBOX_CLAIM), ignored -> claimSelected())
                .bounds(rewardNavigationX, navigationY + BUTTON_HEIGHT + BUTTON_GAP,
                        REWARD_COLUMNS * SLOT_SIZE, BUTTON_HEIGHT).build());
        claim.active = selectedIndex >= 0;
    }

    private void select(int index) {
        selectedIndex = index;
        rewardPage = 0;
        rebuildWidgets();
    }

    private void changeEntryPage(int offset) {
        entryPage += offset;
        selectedIndex = Math.min(entryPage * ENTRIES_PER_PAGE, entries.size() - 1);
        rewardPage = 0;
        rebuildWidgets();
    }

    private void changeRewardPage(int offset) {
        rewardPage += offset;
        rebuildWidgets();
    }

    private void claimSelected() {
        if (selectedIndex < 0) {
            return;
        }
        PacketDistributor.sendToServer(new ClaimRewardInboxPayload(
                entries.get(selectedIndex).transactionId().value()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        if (entries.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_REWARD_INBOX_EMPTY),
                    width / 2, height / 2, 0xA0A0A0);
            return;
        }
        renderRewards(graphics, mouseX, mouseY);
    }

    private void renderRewards(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ItemStack> rewards = entries.get(selectedIndex).rewards();
        int firstIndex = rewardPage * REWARDS_PER_PAGE;
        int lastIndex = Math.min(firstIndex + REWARDS_PER_PAGE, rewards.size());
        int left = width / 2 + 15;
        int top = 42;
        ItemStack hovered = ItemStack.EMPTY;
        for (int index = firstIndex; index < lastIndex; index++) {
            int localIndex = index - firstIndex;
            int x = left + localIndex % REWARD_COLUMNS * SLOT_SIZE;
            int y = top + localIndex / REWARD_COLUMNS * SLOT_SIZE;
            ItemStack reward = rewards.get(index);
            graphics.renderItem(reward, x, y);
            graphics.renderItemDecorations(font, reward, x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                hovered = reward;
            }
        }
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
