package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.client.gui.widget.ReelWidget;
import xiaojin.gachaaddiction.util.DisplayEntry;
import xiaojin.gachaaddiction.util.RarityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GachaScreen extends Screen {
    private final Screen originalScreen;
    private final List<ResourceKey<LootTable>> lootTableResourceKey;
    private final List<DisplayEntry> entries;
    private final List<ReelWidget> reelWidgets = new ArrayList<>();
    private int currentPage;
    private int pageSize;
    private int blinkTicks;
    private int currentRenderHighestNewCompleteLevel = -1;
    private boolean completeSoundPlayedThisFrame;

    public GachaScreen(Screen originalScreen, List<ResourceKey<LootTable>> lootTableResourceKey, List<DisplayEntry> entries) {
        super(Component.empty());
        this.originalScreen = originalScreen;
        this.lootTableResourceKey = lootTableResourceKey;
        this.entries = entries;
    }

    // TODO 获取包之前提示玩家等待
    public void initializeContents() {
        if (!(originalScreen instanceof MenuAccess<?> menuAccess)) {
            return;
        }
        AbstractContainerMenu menu = menuAccess.getMenu();
        NonNullList<ItemStack> items = menu.getItems();

        // 排除玩家本身的物品
        if (minecraft != null && minecraft.player != null) {
            items.removeAll(minecraft.player.inventoryMenu.getItems());
        }
        items.removeIf(ItemStack::isEmpty);

        clearReels();
        buildReelWidgets(items);
        initReels();
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
        children().removeIf(w -> w instanceof ReelWidget);
        renderables.removeIf(w -> w instanceof ReelWidget);
    }

    private void clearReels() {
        for (var v : reelWidgets) {
            removeWidget(v);
        }
        reelWidgets.clear();
    }

    private void buildReelWidgets(List<ItemStack> items) {
        RandomSource random = RandomSource.create();
        for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
            ItemStack itemStack = items.get(i);
            reelWidgets.add(createReelWidget(i, itemStack, random));
        }
    }

    private ReelWidget createReelWidget(int index, ItemStack itemStack, RandomSource random) {
        int rarityLevel = RarityUtil.getRarityLevel(itemStack);
        int decoyCount = 40;
        if (rarityLevel < 0) {
            decoyCount -= random.nextInt(Math.min(20, -rarityLevel * 2));
        } else if (rarityLevel > 0) {
            int min = rarityLevel * 2;
            int max = rarityLevel * ReelConfig.SLOT_SIZE;
            if (max > min) decoyCount += random.nextInt(min, max);
        } else {
            decoyCount += random.nextInt(0, ReelConfig.SLOT_SIZE);
        }

        List<ItemStack> decoys = DisplayEntry.generateGachaResult(entries, decoyCount, random);

        int resultIndex = decoys.size() > ReelConfig.SLOT_SIZE
                ? random.nextInt(decoys.size() - ReelConfig.SLOT_SIZE) + ReelConfig.SLOT_SIZE
                : Math.max(0, decoys.size() - 1);

        decoys.add(resultIndex, itemStack.getItem().getDefaultInstance());
        return new ReelWidget(this, index, itemStack, decoys, resultIndex);
    }

    private void initializePage(int page) {
        removeAllReelWidgets();
        pageSize = Math.max(1, width / 3 * 2 / 32);
        int from = page * pageSize;
        int to = Math.min(from + pageSize, reelWidgets.size());

        if (from >= reelWidgets.size()) {
            return;
        }

        List<ReelWidget> pageWidgets = reelWidgets.subList(from, to);
        int totalWidth = pageWidgets.size() * 32;
        int startX = (width - totalWidth) / 2 + 16;

        for (int i = 0; i < pageWidgets.size(); i++) {
            ReelWidget w = pageWidgets.get(i);
            int x = startX + i * 32;
            w.setX(x);
            w.setY(height / 2);
            addRenderableWidget(w);
        }
    }

    public Screen getOriginalScreen() {
        return originalScreen;
    }

    public int getCurrentRenderHighestNewCompleteLevel() {
        return currentRenderHighestNewCompleteLevel;
    }

    public boolean tryClaimCompleteSound() {
        if (completeSoundPlayedThisFrame) return false;
        completeSoundPlayedThisFrame = true;
        return true;
    }

    private boolean handleSpaceKey() {
        List<ReelWidget> limit = reelWidgets.stream().skip((long) currentPage * pageSize).limit(pageSize).toList();
        boolean allDone = limit.stream().allMatch(ReelWidget::isComplete);
        if (!allDone) {
            Optional<ReelWidget> first = limit.stream().filter(reelWidget -> !reelWidget.isComplete()).findFirst();
            if (first.isEmpty()) {
                return false;
            }
            first.get().skipToEnd();
            return true;
        }

        int nextFrom = (currentPage + 1) * pageSize;
        if (nextFrom >= reelWidgets.size()) {
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
        guiGraphics.flush();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        currentRenderHighestNewCompleteLevel = reelWidgets.stream()
                .filter(w -> w.isComplete() && !w.hasPlayedSound())
                .mapToInt(ReelWidget::getResultLevel)
                .max()
                .orElse(-1);
        completeSoundPlayedThisFrame = false;
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        Component text = Component.translatable("gui.gachaaddiction.skip_hint");
        float alpha = (float) ((Math.sin((blinkTicks + partialTick) * 0.15f) + 1.0) / 2.0);
        alpha = 0.3f + 0.7f * alpha;
        guiGraphics.pose().pushPose();
        guiGraphics.setColor(1, 1, 1, alpha);
        guiGraphics.pose().translate(0, 0, 1000);
        guiGraphics.drawString(font, text, (width - font.width(text)) / 2, height - 30, 0xFFFFFF, true);
        guiGraphics.setColor(1, 1, 1, 1);
        guiGraphics.flush();
        guiGraphics.pose().popPose();
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE && handleSpaceKey()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public List<ResourceKey<LootTable>> getLootTableResourceKey() {
        return lootTableResourceKey;
    }

    public static class ReelConfig {
        /**
         * 每秒经过的物品数量
         */
        public static final float ITEMS_PER_SECOND = 5f;
        /**
         * 物品垂直间距
         */
        public static final float ITEM_SPACING = 18f;
        /**
         * 可见物品数量
         */
        public static final int VISIBLE_COUNT = 5;
        /**
         * 转盘槽位大小：decoy 生成数量范围和 result 插入位置的计算基准
         */
        public static final int SLOT_SIZE = 5;
        /**
         * 物品最小透明度
         */
        public static final float MIN_ALPHA = 0.0f;
        /**
         * 物品最大透明度
         */
        public static final float MAX_ALPHA = 1.0f;

        /**
         * 滚动减速区：距离结果还剩几个物品时开始减速
         */
        public static final float DECEL_ZONE = 4f;
        /**
         * 减速阶段最低速度倍率，防止完全停住
         */
        public static final float MIN_SPEED = 0.05f;
        /**
         * 随机速度倍率范围：最低
         */
        public static final float SPEED_VARIANCE_MIN = 0.5f;
        /**
         * 随机速度倍率范围：最高
         */
        public static final float SPEED_VARIANCE_MAX = 1.5f;
        /**
         * 退出动画：每 tick 的进度推进量
         */
        public static final float EXIT_SPEED = 0.1f;
        /**
         * 退出动画：最大放大倍率（1 + 此值）
         */
        public static final float EXIT_MAX_SCALE = 0.6f;

    }
}
