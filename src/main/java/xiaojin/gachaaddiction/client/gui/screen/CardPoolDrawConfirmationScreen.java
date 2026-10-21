package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.execution.GachaExecutionPreviewIssue;
import xiaojin.gachaaddiction.execution.GachaExecutionStepPreview;
import xiaojin.gachaaddiction.network.cts.ConfirmCardPoolDrawPayload;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolDrawConfirmationPayload;
import xiaojin.gachaaddiction.network.stc.OpenCardPoolProbabilityPreviewPayload;
import xiaojin.gachaaddiction.network.cts.RequestCardPoolProbabilityPreviewPayload;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CardPoolDrawConfirmationScreen extends Screen {
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 8;
    private static final int MAXIMUM_VISIBLE_PREVIEW_STEPS = 4;
    private static final int MAXIMUM_VISIBLE_PROBABILITIES = 2;
    private static final int MAXIMUM_VISIBLE_CANDIDATES = 3;
    private static final int PREVIEW_LINE_HEIGHT = 12;

    private final OpenCardPoolDrawConfirmationPayload preparation;
    private List<OpenCardPoolProbabilityPreviewPayload.Probability> probabilityDescriptions = List.of();
    private boolean submitted;

    public CardPoolDrawConfirmationScreen(OpenCardPoolDrawConfirmationPayload preparation) {
        super(Component.translatable(preparation.titleTranslationKey()));
        this.preparation = preparation;
    }

    private static Component describe(GachaExecutionStepPreview step) {
        String name = step.target().map(target -> Component.translatable(
                                TranslationKeys.GUI_CARD_POOL_CONFIRM_STEP_TARGET, step.type().toString(), target.toString())
                        .getString())
                .orElse(step.type().toString());
        if (step.rounds().isPresent()) {
            name = name + " | " + Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_STEP_ROUNDS,
                    step.rounds().getAsLong()).getString();
        }
        if (!step.consumptionTypes().isEmpty()) {
            name = name + " | " + Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_CONSUMPTION,
                    step.consumptionTypes().stream().map(Object::toString).sorted().toList()).getString();
        }
        return Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_STEP, name);
    }

    private static Component describeProbability(OpenCardPoolProbabilityPreviewPayload.Probability probability) {
        String candidates = probability.candidates().stream()
                .limit(MAXIMUM_VISIBLE_CANDIDATES)
                .map(candidate -> candidate.id() + "=" + candidate.weight())
                .collect(Collectors.joining(", "));
        return Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_PROBABILITY,
                probability.id().toString(), probability.selectionMode(), probability.selectionCount(), candidates);
    }

    @Override
    protected void init() {
        int y = height / 2 + 70;
        Button confirm = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_CONFIRM), ignored -> confirm())
                .bounds(width / 2 - BUTTON_WIDTH - BUTTON_GAP / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        confirm.active = !submitted && Instant.now().isBefore(preparation.expiresAt());
        addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_CANCEL), ignored -> onClose())
                .bounds(width / 2 + BUTTON_GAP / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        PacketDistributor.sendToServer(new RequestCardPoolProbabilityPreviewPayload(preparation.token()));
    }

    public void setProbabilityPreview(UUID token,
                                      List<OpenCardPoolProbabilityPreviewPayload.Probability> probabilities) {
        if (!preparation.token().equals(token)) {
            return;
        }
        probabilityDescriptions = List.copyOf(probabilities);
    }

    private void confirm() {
        if (submitted || !Instant.now().isBefore(preparation.expiresAt())) {
            return;
        }
        submitted = true;
        PacketDistributor.sendToServer(new ConfirmCardPoolDrawPayload(preparation.token()));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int center = width / 2;
        int y = height / 2 - 86;
        graphics.drawCenteredString(font, title, center, y, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_TITLE), center,
                y + 18, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_PACKAGE,
                preparation.packageId().toString()), center, y + 34, 0xA0A0A0);
        int previewY = y + 50;
        int visibleSteps = Math.min(preparation.previewSteps().size(), MAXIMUM_VISIBLE_PREVIEW_STEPS);
        for (int index = 0; index < visibleSteps; index++) {
            graphics.drawCenteredString(font, describe(preparation.previewSteps().get(index)), center,
                    previewY, 0xD0D0D0);
            previewY += PREVIEW_LINE_HEIGHT;
        }
        int remainingSteps = preparation.previewSteps().size() - visibleSteps;
        if (remainingSteps > 0) {
            graphics.drawCenteredString(font, Component.translatable(
                    TranslationKeys.GUI_CARD_POOL_CONFIRM_MORE_STEPS, remainingSteps), center, previewY, 0xA0A0A0);
            previewY += PREVIEW_LINE_HEIGHT;
        }
        int visibleProbabilities = Math.min(probabilityDescriptions.size(), MAXIMUM_VISIBLE_PROBABILITIES);
        for (int index = 0; index < visibleProbabilities; index++) {
            graphics.drawCenteredString(font, describeProbability(probabilityDescriptions.get(index)), center,
                    previewY, 0xB0D0FF);
            previewY += PREVIEW_LINE_HEIGHT;
        }
        int remainingProbabilities = probabilityDescriptions.size() - visibleProbabilities;
        if (remainingProbabilities > 0) {
            graphics.drawCenteredString(font, Component.translatable(
                            TranslationKeys.GUI_CARD_POOL_CONFIRM_MORE_PROBABILITIES, remainingProbabilities), center,
                    previewY, 0xA0A0A0);
            previewY += PREVIEW_LINE_HEIGHT;
        }
        if (preparation.previewIssues().contains(GachaExecutionPreviewIssue.STATE_DEPENDENT_OUTPUTS)) {
            graphics.drawCenteredString(font, Component.translatable(
                    TranslationKeys.GUI_CARD_POOL_CONFIRM_PREVIEW_UNCERTAIN), center, previewY, 0xFFD060);
            previewY += PREVIEW_LINE_HEIGHT;
        }
        if (preparation.previewIssues().contains(GachaExecutionPreviewIssue.UNSUPPORTED_CUSTOM_STEP)) {
            graphics.drawCenteredString(font, Component.translatable(
                    TranslationKeys.GUI_CARD_POOL_CONFIRM_PREVIEW_UNSUPPORTED), center, previewY, 0xFFD060);
            previewY += PREVIEW_LINE_HEIGHT;
        }
        graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_CARD_POOL_CONFIRM_EXPIRES,
                preparation.expiresAt().toString()), center, previewY, 0xA0A0A0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
