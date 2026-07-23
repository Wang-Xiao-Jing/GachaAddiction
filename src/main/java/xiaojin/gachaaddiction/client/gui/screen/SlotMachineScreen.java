package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import xiaojin.gachaaddiction.api.RewardData;
import xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.config.ClientConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotMachineScreen extends BasicGachaScreen {
    public static final String SKIP_HINT = "gui.gachaaddiction.slot_machines.kip_hint";
    protected final ClientConfig.SlotMachineConfig slotMachineConfig = clientConfig.slotMachineConfig;
    private final List<VerticalReelWidget> verticalReelWidgets = new ArrayList<>();
    private int currentPage;
    private int pageSize;
    private int blinkTicks;
    private boolean passSoundPlayedThisFrame;

    public SlotMachineScreen(@Nullable Screen originalScreen, List<@Nullable ResourceLocation> lootTableId, List<ItemStackEntry> itemStackEntries) {
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
        for (RewardData data : rewards) {
            verticalReelWidgets.add(createReelWidget(data, random));
        }
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

    private VerticalReelWidget createReelWidget(RewardData data, RandomSource random) {
        int rarityLevel = data.getRarityLevel();
        int slotSize = slotMachineConfig.itemSlotSize.get();
        int decoyCount = 40;
        if (rarityLevel < 0) {
            decoyCount -= random.nextInt(Math.min(20, -rarityLevel * 2));
        } else if (rarityLevel > 0) {
            int min = rarityLevel * 2;
            int max = rarityLevel * slotSize;
            if (max > min) decoyCount += random.nextInt(min, max);
        } else {
            decoyCount += random.nextInt(0, slotSize);
        }

        List<ItemStack> decoys = ItemStackEntry.generateGachaResult(itemStackEntries, decoyCount, random);

        int resultIndex = decoys.size() > slotSize
                ? random.nextInt(decoys.size() - slotSize) + slotSize
                : Math.max(0, decoys.size() - 1);

        decoys.add(resultIndex, data.getItemStack().getItem().getDefaultInstance());
        return new VerticalReelWidget(this, data, decoys, resultIndex);
    }

    private void initializePage(int page) {
        removeAllReelWidgets();
        pageSize = Math.max(1, width / 3 * 2 / 32);
        int from = page * pageSize;
        int to = Math.min(from + pageSize, verticalReelWidgets.size());

        if (from >= verticalReelWidgets.size()) {
            return;
        }

        List<VerticalReelWidget> pageWidgets = verticalReelWidgets.subList(from, to);
        int totalWidth = pageWidgets.size() * 32;
        int startX = (width - totalWidth) / 2 + 16;

        for (int i = 0; i < pageWidgets.size(); i++) {
            VerticalReelWidget w = pageWidgets.get(i);
            int x = startX + i * 32;
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
        guiGraphics.flush();
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
}
