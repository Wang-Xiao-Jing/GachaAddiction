package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.api.client.ReelHost;
import xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.config.ClientConfig;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SlotMachineScreen extends BasicGachaScreen {
    public static final String SKIP_HINT = TranslationKeys.GUI_SLOT_MACHINES_KIP_HINT;
    protected final ClientConfig.SlotMachineConfig slotMachineConfig = clientConfig.slotMachineConfig;
    private final List<VerticalReelWidget> verticalReelWidgets = new ArrayList<>();
    private static final int BASE_SPIN = 18;
    private static final int SPIN_PER_RANK = 7;
    private static final int SPIN_JITTER = 3;
    private static final int SPIN_TAIL = 6;

    private int currentPage;
    private int pageSize;
    private int blinkTicks;
    private boolean passSoundPlayedThisFrame;

    public SlotMachineScreen(@Nullable Screen originalScreen, List<@Nullable Identifier> lootTableId, List<ItemStackEntry> itemStackEntries) {
        super(Component.empty(), originalScreen, lootTableId, itemStackEntries);
    }

    @Override
    public void updateRewards(NonNullList<ItemStack> items) {
        for (ItemStack item : items) {
            rewardReceived(item, false);
        }
        refreshWidgets();
    }

    @Override
    protected void refreshWidgets() {
        clearReels();
        RandomSource random = RandomSource.create();
        int perPage = computePageSize();
        for (int start = 0; start < rewards.size(); start += perPage) {
            int end = Math.min(start + perPage, rewards.size());
            List<RewardData> group = rewards.subList(start, end);
            int[] ranks = stopRanks(group);
            for (int i = 0; i < group.size(); i++) {
                verticalReelWidgets.add(createReelWidget(this, group.get(i), itemStackEntries,
                        slotMachineConfig, random, ranks[i], group.size()));
            }
        }
        initReels();
    }

    private int computePageSize() {
        if (width <= 0) {
            return 1;
        }
        return Math.max(1, width / 3 * 2 / VerticalReelWidget.RAIL_STEP);
    }

    private static int[] stopRanks(List<RewardData> group) {
        List<Integer> byRarity = new ArrayList<>();
        for (int i = 0; i < group.size(); i++) {
            byRarity.add(i);
        }
        byRarity.sort(Comparator.comparingInt(index -> group.get(index).getRarityLevel()));
        int[] ranks = new int[group.size()];
        for (int rank = 0; rank < byRarity.size(); rank++) {
            ranks[byRarity.get(rank)] = rank;
        }
        return ranks;
    }

    @Override
    protected void init() {
        super.init();
        initReels();
    }

    private void initReels() {
        initializePage(0);
    }

    private void removeAllReelWidgets() {
        children().removeIf(w -> w instanceof VerticalReelWidget);
        renderables.removeIf(w -> w instanceof VerticalReelWidget);
    }

    private void clearReels() {
        for (var v : verticalReelWidgets) {
            removeWidget(v);
        }
        verticalReelWidgets.clear();
    }

    public boolean tryClaimPassSound() {
        if (!slotMachineConfig.passSoundEffectsOptimize.get()) return true;
        if (passSoundPlayedThisFrame) return false;
        passSoundPlayedThisFrame = true;
        return true;
    }

    /**
     * 创建单个转轮控件的共享工厂。
     * <p>
     * 供真实屏幕与配置界面预览面板复用，保证预览与实机生成转轮的逻辑完全一致。
     *
     * @param host             转轮宿主（真实屏幕或预览宿主）
     * @param data             奖励数据
     * @param entries          可抽取的物品池
     * @param slotMachineConfig 老虎机配置
     * @param random           随机源
     */
    public static VerticalReelWidget createReelWidget(ReelHost host, RewardData data,
                                                      List<ItemStackEntry> entries,
                                                      ClientConfig.SlotMachineConfig slotMachineConfig,
                                                      RandomSource random) {
        return createReelWidget(host, data, entries, slotMachineConfig, random, 0, 1);
    }

    public static VerticalReelWidget createReelWidget(ReelHost host, RewardData data,
                                                      List<ItemStackEntry> entries,
                                                      ClientConfig.SlotMachineConfig slotMachineConfig,
                                                      RandomSource random,
                                                      int stopRank, int reelCount) {
        int slotSize = slotMachineConfig.itemSlotSize.get();
        int spin = BASE_SPIN + stopRank * SPIN_PER_RANK + random.nextInt(SPIN_JITTER);
        int decoyCount = spin + slotSize + SPIN_TAIL;

        List<ItemStack> decoys = ItemStackEntry.generateGachaResult(entries, decoyCount, random);

        int resultIndex = Math.clamp(spin, 0, decoys.size());
        decoys.add(resultIndex, data.getItemStack().getItem().getDefaultInstance());

        double fastest = slotMachineConfig.speedVarianceMax.get();
        double slowest = slotMachineConfig.speedVarianceMin.get();
        float ratio = reelCount <= 1 ? 0f : stopRank / (float) (reelCount - 1);
        float speed = (float) (fastest + (slowest - fastest) * ratio);

        return new VerticalReelWidget(host, data, decoys, resultIndex, speed);
    }

    private void initializePage(int page) {
        removeAllReelWidgets();
        pageSize = computePageSize();
        int from = page * pageSize;
        int to = Math.min(from + pageSize, verticalReelWidgets.size());

        if (from >= verticalReelWidgets.size()) {
            return;
        }

        List<VerticalReelWidget> pageWidgets = verticalReelWidgets.subList(from, to);
        int totalWidth = pageWidgets.size() * VerticalReelWidget.RAIL_STEP;
        int startX = (width - totalWidth) / 2 + VerticalReelWidget.RAIL_STEP / 2;

        for (int i = 0; i < pageWidgets.size(); i++) {
            VerticalReelWidget w = pageWidgets.get(i);
            int x = startX + i * VerticalReelWidget.RAIL_STEP;
            w.setX(x);
            w.setY(height / 2);
            addRenderableWidget(w);
        }
    }

    private boolean handleSpaceKey() {
        List<VerticalReelWidget> limit = verticalReelWidgets.stream().skip((long) currentPage * pageSize).limit(pageSize).toList();
        boolean allDone = limit.stream().allMatch(VerticalReelWidget::isComplete);
        if (!allDone) {
            Optional<VerticalReelWidget> first = limit.stream().filter(verticalReelWidget -> !verticalReelWidget.isComplete()).findFirst();
            if (first.isEmpty()) {
                return false;
            }
            first.get().skipToEnd();
            return true;
        }

        int nextFrom = (currentPage + 1) * pageSize;
        if (nextFrom >= verticalReelWidgets.size()) {
            closeGacha();
        } else {
            currentPage++;
        }

        removeAllReelWidgets();
        initializePage(currentPage);

        return true;
    }

    private void closeGacha() {
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (clientConfig.rewardSoundEffectsOptimize.get()) {
            currentRenderHighestNewCompleteLevel = verticalReelWidgets.stream()
                    .filter(w -> w.isComplete() && !w.hasPlayedSound())
                    .mapToInt(VerticalReelWidget::getResultLevel)
                    .max()
                    .orElse(-1);
            completeSoundPlayedThisFrame = false;
        }
        passSoundPlayedThisFrame = false;
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Component text = Component.translatable(SKIP_HINT);
        float alpha = (float) ((Math.sin((blinkTicks + partialTick) * 0.15f) + 1.0) / 2.0);
        alpha = 0.3f + 0.7f * alpha;
        int textAlpha = (int) (Math.clamp(alpha, 0f, 1f) * 255f) & 0xFF;
        if (textAlpha > 0) {
            guiGraphics.drawString(font, text, (width - font.width(text)) / 2, height - 30,
                    (textAlpha << 24) | 0xFFFFFF, true);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        blinkTicks++;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == GLFW.GLFW_KEY_SPACE && handleSpaceKey()) {
            return true;
        }
        return super.keyPressed(keyEvent);
    }
}
