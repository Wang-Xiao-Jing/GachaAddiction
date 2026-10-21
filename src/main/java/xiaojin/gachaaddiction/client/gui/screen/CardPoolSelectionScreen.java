package xiaojin.gachaaddiction.client.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolCatalogEntry;
import xiaojin.gachaaddiction.cardpool.GachaDrawPackageSummary;
import xiaojin.gachaaddiction.cardpool.GachaPlayerChoiceKind;
import xiaojin.gachaaddiction.cardpool.GachaPlayerChoiceSummary;
import xiaojin.gachaaddiction.network.cts.RequestCardPoolDrawWithRoundsPayload;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterTypes;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CardPoolSelectionScreen extends Screen {
    private static final int CARD_POOLS_PER_PAGE = 6;
    private static final int PACKAGES_PER_PAGE = 5;
    private static final int PLAYER_CHOICES_PER_PAGE = 3;
    private static final int PANEL_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;

    private final List<GachaCardPoolCatalogEntry> cardPools;
    private final Map<String, String> selectedChoices = new HashMap<>();
    private int page;
    private int selectedIndex;
    private int packagePage;
    private int choicePage;
    private boolean requestSent;
    private String roundsValue = "1";

    public CardPoolSelectionScreen(List<GachaCardPoolCatalogEntry> cardPools) {
        super(Component.translatable(TranslationKeys.GUI_CARD_POOL_TITLE));
        this.cardPools = List.copyOf(cardPools);
        this.selectedIndex = this.cardPools.isEmpty() ? -1 : 0;
    }

    private static void validateChoice(GachaPlayerChoiceSummary choice, String value) {
        if (choice.kind() == GachaPlayerChoiceKind.ENUMERATED && !choice.values().contains(value)) {
            throw new IllegalArgumentException(choice.name());
        }
        if (choice.kind() != GachaPlayerChoiceKind.NUMBER_RANGE) {
            return;
        }
        BigDecimal decimal = new BigDecimal(value);
        BigDecimal minimum = new BigDecimal(choice.minimum().orElseThrow());
        BigDecimal maximum = new BigDecimal(choice.maximum().orElseThrow());
        BigDecimal step = new BigDecimal(choice.step().orElseThrow());
        if (decimal.compareTo(minimum) < 0 || decimal.compareTo(maximum) > 0
                || decimal.subtract(minimum).remainder(step).signum() != 0) {
            throw new IllegalArgumentException(choice.name());
        }
    }

    private static void putValue(CompoundTag entry, GachaPlayerChoiceSummary choice, String value) {
        if (choice.parameterType().equals(GachaParameterTypes.BOOLEAN.id())) {
            entry.putBoolean("value", Boolean.parseBoolean(value));
        } else if (choice.parameterType().equals(GachaParameterTypes.INTEGER.id())) {
            entry.putInt("value", Integer.parseInt(value));
        } else if (choice.parameterType().equals(GachaParameterTypes.LONG.id())) {
            entry.putLong("value", Long.parseLong(value));
        } else if (choice.parameterType().equals(GachaParameterTypes.DOUBLE.id())) {
            entry.putDouble("value", Double.parseDouble(value));
        } else if (choice.parameterType().equals(GachaParameterTypes.STRING.id())
                || choice.parameterType().equals(GachaParameterTypes.RESOURCE_LOCATION.id())) {
            entry.putString("value", value);
        } else {
            throw new IllegalArgumentException(choice.name());
        }
    }

    private static String choiceKey(GachaPlayerChoiceSummary choice) {
        return choice.owner() + "/" + choice.name();
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        int left = width / 2 - PANEL_WIDTH - BUTTON_GAP;
        int top = 42;
        int firstIndex = page * CARD_POOLS_PER_PAGE;
        int lastIndex = Math.min(firstIndex + CARD_POOLS_PER_PAGE, cardPools.size());
        for (int index = firstIndex; index < lastIndex; index++) {
            GachaCardPoolCatalogEntry entry = cardPools.get(index);
            int buttonY = top + (index - firstIndex) * (BUTTON_HEIGHT + BUTTON_GAP);
            int selected = index;
            addRenderableWidget(Button.builder(Component.translatable(entry.titleTranslationKey()),
                            ignored -> select(selected))
                    .bounds(left, buttonY, PANEL_WIDTH, BUTTON_HEIGHT).build());
        }
        int navigationY = top + CARD_POOLS_PER_PAGE * (BUTTON_HEIGHT + BUTTON_GAP);
        Button previous = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_PREVIOUS), ignored -> changePage(-1))
                .bounds(left, navigationY, 72, BUTTON_HEIGHT).build());
        Button next = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_NEXT), ignored -> changePage(1))
                .bounds(left + PANEL_WIDTH - 72, navigationY, 72, BUTTON_HEIGHT).build());
        previous.active = page > 0;
        next.active = (page + 1) * CARD_POOLS_PER_PAGE < cardPools.size();
        if (selectedIndex >= 0) {
            GachaCardPoolCatalogEntry selected = cardPools.get(selectedIndex);
            int right = width / 2 + BUTTON_GAP;
            EditBox rounds = new EditBox(font, right, 20, PANEL_WIDTH, BUTTON_HEIGHT,
                    Component.translatable(TranslationKeys.GUI_CARD_POOL_ROUNDS));
            rounds.setValue(roundsValue);
            rounds.setHint(Component.translatable(TranslationKeys.GUI_CARD_POOL_ROUNDS));
            rounds.setResponder(value -> roundsValue = value);
            addRenderableWidget(rounds);
            addPackageButtons(selected, right, top);
            addPlayerChoiceWidgets(selected, right,
                    top + (PACKAGES_PER_PAGE + 1) * (BUTTON_HEIGHT + BUTTON_GAP) + 12);
        }
    }

    private void addPlayerChoiceWidgets(GachaCardPoolCatalogEntry cardPool, int left, int top) {
        int firstIndex = choicePage * PLAYER_CHOICES_PER_PAGE;
        int lastIndex = Math.min(firstIndex + PLAYER_CHOICES_PER_PAGE, cardPool.playerChoices().size());
        for (int index = firstIndex; index < lastIndex; index++) {
            GachaPlayerChoiceSummary choice = cardPool.playerChoices().get(index);
            int y = top + (index - firstIndex) * (BUTTON_HEIGHT + BUTTON_GAP);
            if (choice.kind() == GachaPlayerChoiceKind.NUMBER_RANGE) {
                EditBox input = new EditBox(font, left, y, PANEL_WIDTH, BUTTON_HEIGHT,
                        Component.translatable(choice.labelTranslationKey()));
                input.setValue(selectedChoices.getOrDefault(choiceKey(choice), ""));
                input.setHint(Component.translatable(TranslationKeys.GUI_CARD_POOL_CHOICE_VALUE,
                        Component.translatable(choice.labelTranslationKey()),
                        choice.minimum().orElseThrow() + "–" + choice.maximum().orElseThrow()));
                input.setResponder(value -> updateChoice(choice, value));
                addRenderableWidget(input);
            } else {
                addRenderableWidget(Button.builder(choiceLabel(choice), ignored -> cycleChoice(choice))
                        .bounds(left, y, PANEL_WIDTH, BUTTON_HEIGHT).build());
            }
        }
        if (cardPool.playerChoices().size() > PLAYER_CHOICES_PER_PAGE) {
            int navigationY = top + PLAYER_CHOICES_PER_PAGE * (BUTTON_HEIGHT + BUTTON_GAP);
            Button previous = addRenderableWidget(Button.builder(Component.translatable(
                            TranslationKeys.GUI_CARD_POOL_PREVIOUS), ignored -> changeChoicePage(-1))
                    .bounds(left, navigationY, 72, BUTTON_HEIGHT).build());
            Button next = addRenderableWidget(Button.builder(Component.translatable(
                            TranslationKeys.GUI_CARD_POOL_NEXT), ignored -> changeChoicePage(1))
                    .bounds(left + PANEL_WIDTH - 72, navigationY, 72, BUTTON_HEIGHT).build());
            previous.active = choicePage > 0;
            next.active = (choicePage + 1) * PLAYER_CHOICES_PER_PAGE < cardPool.playerChoices().size();
        }
    }

    private void addPackageButtons(GachaCardPoolCatalogEntry cardPool, int left, int top) {
        int firstIndex = packagePage * PACKAGES_PER_PAGE;
        int lastIndex = Math.min(firstIndex + PACKAGES_PER_PAGE, cardPool.packages().size());
        for (int index = firstIndex; index < lastIndex; index++) {
            GachaDrawPackageSummary drawPackage = cardPool.packages().get(index);
            Button button = addRenderableWidget(Button.builder(Component.translatable(
                                    TranslationKeys.GUI_CARD_POOL_PACKAGE, drawPackage.id().toString()),
                            ignored -> requestDraw(cardPool, drawPackage))
                    .bounds(left, top + (index - firstIndex) * (BUTTON_HEIGHT + BUTTON_GAP),
                            PANEL_WIDTH, BUTTON_HEIGHT).build());
            button.active = !requestSent;
        }
        int navigationY = top + PACKAGES_PER_PAGE * (BUTTON_HEIGHT + BUTTON_GAP);
        Button previous = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_PREVIOUS), ignored -> changePackagePage(-1))
                .bounds(left, navigationY, 72, BUTTON_HEIGHT).build());
        Button next = addRenderableWidget(Button.builder(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_NEXT), ignored -> changePackagePage(1))
                .bounds(left + PANEL_WIDTH - 72, navigationY, 72, BUTTON_HEIGHT).build());
        previous.active = packagePage > 0;
        next.active = (packagePage + 1) * PACKAGES_PER_PAGE < cardPool.packages().size();
    }

    private void select(int index) {
        selectedIndex = index;
        packagePage = 0;
        choicePage = 0;
        rebuildWidgets();
    }

    private void changePage(int offset) {
        page += offset;
        selectedIndex = Math.min(page * CARD_POOLS_PER_PAGE, cardPools.size() - 1);
        packagePage = 0;
        choicePage = 0;
        rebuildWidgets();
    }

    private void changePackagePage(int offset) {
        packagePage += offset;
        rebuildWidgets();
    }

    private void changeChoicePage(int offset) {
        choicePage += offset;
        rebuildWidgets();
    }

    private void cycleChoice(GachaPlayerChoiceSummary choice) {
        String key = choiceKey(choice);
        String current = selectedChoices.getOrDefault(key, "");
        String next;
        if (choice.kind() == GachaPlayerChoiceKind.BOOLEAN) {
            next = current.isEmpty() ? "false" : current.equals("false") ? "true" : "";
        } else {
            int index = choice.values().indexOf(current);
            next = index < 0 ? choice.values().getFirst()
                    : index + 1 < choice.values().size() ? choice.values().get(index + 1) : "";
        }
        updateChoice(choice, next);
        rebuildWidgets();
    }

    private void updateChoice(GachaPlayerChoiceSummary choice, String value) {
        if (value.isBlank()) {
            selectedChoices.remove(choiceKey(choice));
        } else {
            selectedChoices.put(choiceKey(choice), value);
        }
    }

    private Component choiceLabel(GachaPlayerChoiceSummary choice) {
        String selected = selectedChoices.get(choiceKey(choice));
        Component value = selected == null ? Component.translatable(TranslationKeys.GUI_CARD_POOL_CHOICE_DEFAULT)
                : Component.literal(selected);
        return Component.translatable(TranslationKeys.GUI_CARD_POOL_CHOICE_VALUE,
                Component.translatable(choice.labelTranslationKey()), value);
    }

    private void requestDraw(GachaCardPoolCatalogEntry cardPool, GachaDrawPackageSummary drawPackage) {
        if (requestSent) {
            return;
        }
        CompoundTag encodedChoices;
        int rounds;
        try {
            encodedChoices = encodeChoices(cardPool);
        } catch (RuntimeException failure) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_CHOICE_INVALID, failure.getMessage()));
            }
            return;
        }
        try {
            rounds = Integer.parseInt(roundsValue);
            if (rounds <= 0) {
                throw new IllegalArgumentException(roundsValue);
            }
        } catch (RuntimeException failure) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.translatable(
                        TranslationKeys.GUI_CARD_POOL_ROUNDS_INVALID, failure.getMessage()));
            }
            return;
        }
        requestSent = true;
        rebuildWidgets();
        PacketDistributor.sendToServer(new RequestCardPoolDrawWithRoundsPayload(cardPool.id(), drawPackage.id(),
                encodedChoices, rounds));
        onClose();
    }

    private CompoundTag encodeChoices(GachaCardPoolCatalogEntry cardPool) {
        CompoundTag encoded = new CompoundTag();
        ListTag values = new ListTag();
        for (GachaPlayerChoiceSummary choice : cardPool.playerChoices()) {
            String selected = selectedChoices.get(choiceKey(choice));
            if (selected == null) {
                continue;
            }
            validateChoice(choice, selected);
            CompoundTag entry = new CompoundTag();
            entry.putString("owner", choice.owner().toString());
            entry.putString("name", choice.name());
            entry.putString("type", choice.parameterType().toString());
            putValue(entry, choice, selected);
            values.add(entry);
        }
        encoded.put("values", values);
        return encoded;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        if (cardPools.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(TranslationKeys.GUI_CARD_POOL_EMPTY),
                    width / 2, height / 2, 0xA0A0A0);
            return;
        }
        GachaCardPoolCatalogEntry selected = cardPools.get(selectedIndex);
        graphics.renderItem(selected.icon(), width / 2 + BUTTON_GAP, 20);
        selected.descriptionTranslationKey().ifPresent(description -> graphics.drawString(font,
                Component.translatable(description), width / 2 + BUTTON_GAP + 20, 24, 0xA0A0A0));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
