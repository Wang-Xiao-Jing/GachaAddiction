package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.client.presentation.GachaPresentationClient;
import xiaojin.gachaaddiction.client.compat.ldlib2.element.LDLib2GachaRewardElement;
import xiaojin.gachaaddiction.client.gui.screen.CardPoolResultScreen;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;
import java.util.UUID;

public class LDLib2GachaResultScreen extends ModularUIScreen {
    private final UUID transactionId;
    private final ResourceLocation cardPoolId;
    private final ResourceLocation packageId;
    private final List<ItemStack> rewards;
    private final LDLib2GachaResources.View view;
    private int page;
    private boolean fallbackStarted;

    public LDLib2GachaResultScreen(UUID transactionId, ResourceLocation cardPoolId,
                                   ResourceLocation packageId, List<ItemStack> rewards) {
        this(transactionId, cardPoolId, packageId, rewards, LDLib2GachaResources.load());
    }

    private LDLib2GachaResultScreen(UUID transactionId, ResourceLocation cardPoolId,
                                    ResourceLocation packageId, List<ItemStack> rewards,
                                    LDLib2GachaResources.View view) {
        super(ModularUI.of(UI.of(view.root())), GachaPresentationClient.cardPoolTitle(cardPoolId));
        this.transactionId = transactionId;
        this.cardPoolId = cardPoolId;
        this.packageId = packageId;
        this.rewards = rewards.stream().map(ItemStack::copy).toList();
        this.view = view;
        view.title().setText(title);
        view.context().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_RESULT_CONTEXT,
                cardPoolId, packageId, transactionId.toString()));
        view.previous().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_PREVIOUS))
                .setOnClick(event -> changePage(-1));
        view.next().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_NEXT))
                .setOnClick(event -> changePage(1));
        view.skip().setActive(false).setDisplay(false);
        view.close().setText(Component.translatable(TranslationKeys.GUI_CARD_POOL_RESULT_DONE))
                .setOnClick(event -> onClose());
        rebuildPage();
    }

    private void rebuildPage() {
        view.rewards().clearAllScrollViewChildren();
        int from = page * LDLib2GachaUiConstants.REWARDS_PER_PAGE;
        int to = Math.min(from + LDLib2GachaUiConstants.REWARDS_PER_PAGE, rewards.size());
        for (ItemStack reward : rewards.subList(from, to)) {
            view.rewards().addScrollViewChild(new LDLib2GachaRewardElement(reward));
        }
        view.previous().setActive(page > 0);
        view.next().setActive(to < rewards.size());
    }

    private void changePage(int offset) {
        int selected = page + offset;
        if (selected < 0 || selected * LDLib2GachaUiConstants.REWARDS_PER_PAGE >= rewards.size()) {
            return;
        }
        page = selected;
        rebuildPage();
    }

    @Override
    public void init() {
        try {
            super.init();
        } catch (RuntimeException | LinkageError failure) {
            nativeFallback(failure);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        try {
            super.render(graphics, mouseX, mouseY, partialTick);
        } catch (RuntimeException | LinkageError failure) {
            nativeFallback(failure);
        }
    }

    private void nativeFallback(Throwable failure) {
        if (fallbackStarted) {
            return;
        }
        fallbackStarted = true;
        GachaConstants.LOGGER.warn(LDLib2GachaUiConstants.FALLBACK_LOG, failure);
        Minecraft.getInstance().setScreen(new CardPoolResultScreen(transactionId, cardPoolId, packageId, rewards));
    }
}
