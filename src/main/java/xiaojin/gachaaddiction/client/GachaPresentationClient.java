package xiaojin.gachaaddiction.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.cardpool.GachaCardPoolCatalogEntry;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2GachaResultScreen;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2GachaScreen;
import xiaojin.gachaaddiction.client.compat.ldlib2.LDLib2GachaUiConstants;
import xiaojin.gachaaddiction.client.gui.screen.*;
import xiaojin.gachaaddiction.client.render.GachaSceneRenderer;
import xiaojin.gachaaddiction.config.GachaAddictionConfig;
import xiaojin.gachaaddiction.network.stc.GachaPresentationPayload;
import xiaojin.gachaaddiction.network.cts.GachaPresentationPreferencesPayload;
import xiaojin.gachaaddiction.registry.GachaTypes;

import java.util.*;

public class GachaPresentationClient {
    private static final Map<Integer, GachaPresentationPayload> PENDING = new HashMap<>();
    private static final Map<BasicGachaScreen, GachaType> ACTIVE_TYPES = new WeakHashMap<>();
    private static final Map<ResourceLocation, String> CARD_POOL_TITLES = new HashMap<>();
    private static @Nullable GachaPresentationPreferencesPayload sentPreferences;

    private GachaPresentationClient() {
    }

    public static void rememberCardPools(List<GachaCardPoolCatalogEntry> entries) {
        CARD_POOL_TITLES.clear();
        entries.forEach(entry -> rememberCardPool(entry.id(), entry.titleTranslationKey()));
    }

    public static void rememberCardPool(ResourceLocation id, String translationKey) {
        CARD_POOL_TITLES.put(id, translationKey);
    }

    public static Component cardPoolTitle(ResourceLocation id) {
        String key = CARD_POOL_TITLES.get(id);
        return key == null ? Component.literal(id.toString()) : Component.translatable(key);
    }

    public static void receive(GachaPresentationPayload payload) {
        if (payload.ended()) {
            PENDING.computeIfPresent(payload.menuId(), (id, rules) -> rules.token().equals(payload.token()) ? null : rules);
            if (Minecraft.getInstance().screen instanceof BasicGachaScreen screen) {
                screen.releasePresentation(payload.token());
            }
        } else {
            if (Minecraft.getInstance().screen instanceof BasicGachaScreen screen
                    && screen.acceptsPresentationMenu(payload.menuId()) && screen.acceptsPresentationToken(payload.token())) {
                GachaType current = ACTIVE_TYPES.getOrDefault(screen, GachaAddictionConfig.CLIENT.getDefaultGachaaType());
                GachaType requested = payload.animation().orElse(current);
                boolean interaction = requested == GachaTypes.TOTEM || requested == GachaTypes.CHEST
                        && payload.secondaryType() != GachaSecondaryType.NONE;
                if (payload.menuId() >= 0 && !requested.isEmpty() && (requested != current
                        || (requested == GachaTypes.CHEST || requested == GachaTypes.TOTEM)
                        && interaction != (screen instanceof GachaSourceInteractionScreen)
                        || screen instanceof GachaSourceInteractionScreen source && !source.matchesPresentationOptions(payload))) {
                    PENDING.put(payload.menuId(), payload);
                    screen.preparePresentationReplacement();
                    BasicGachaScreen replacement;
                    try {
                        replacement = requested.getClientData().create(screen.getOriginalScreen(),
                                screen.getLootTableId(), screen.getItemStackEntries());
                    } finally {
                        GachaSceneRenderer.clearSource();
                    }
                    screen.transferPresentationTo(replacement);
                    apply(replacement, payload.menuId(), requested);
                    Minecraft.getInstance().setScreen(replacement);
                    screen.replayRewardsTo(replacement);
                    GachaAnnouncementClient.INSTANCE.presentation(replacement);
                    return;
                }
                screen.setPresentation(payload);
            } else {
                PENDING.put(payload.menuId(), payload);
            }
        }
    }

    public static GachaType animation(int menuId, GachaType fallback) {
        GachaPresentationPayload rules = PENDING.get(menuId);
        GachaType result = rules == null ? fallback : rules.animation().orElse(fallback);
        return rules != null && rules.forceWatch() && result.isEmpty() ? GachaTypes.MODERN : result;
    }

    public static GachaSecondaryType secondary(int menuId) {
        GachaPresentationPayload rules = PENDING.get(menuId);
        return rules == null ? GachaAddictionConfig.CLIENT.secondaryType.get() : rules.secondaryType();
    }

    public static BasicGachaScreen createSource(@Nullable Screen parent, List<@Nullable ResourceLocation> tables,
                                                List<ItemStackEntry> entries, GachaType animation) {
        int menuId = parent instanceof MenuAccess<?> menu ? menu.getMenu().containerId : -1;
        if (animation == GachaTypes.TOTEM || secondary(menuId) != GachaSecondaryType.NONE) {
            return new GachaSourceInteractionScreen(parent, tables, entries, animation);
        }
        return new GachaAnimationScreen(parent, tables, entries, animation);
    }

    public static <T extends BasicGachaScreen> T apply(T screen, int menuId) {
        return apply(screen, menuId, GachaAddictionConfig.CLIENT.getDefaultGachaaType());
    }

    public static <T extends BasicGachaScreen> T apply(T screen, int menuId, GachaType animation) {
        screen.bindPresentationMenu(menuId);
        GachaPresentationPayload rules = PENDING.remove(menuId);
        ACTIVE_TYPES.put(screen, rules == null ? animation : rules.animation().orElse(animation));
        if (rules != null) {
            screen.setPresentation(rules);
        }
        return screen;
    }

    public static void tick() {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null || !connection.hasChannel(GachaPresentationPreferencesPayload.TYPE)) {
            return;
        }
        GachaPresentationPreferencesPayload preferences = new GachaPresentationPreferencesPayload(
                GachaAddictionConfig.CLIENT.protectLoot.get(), GachaAddictionConfig.CLIENT.getDefaultGachaaType(),
                GachaAddictionConfig.CLIENT.secondaryType.get(), GachaAddictionConfig.CLIENT.choiceCount.get());
        if (!preferences.equals(sentPreferences)) {
            sentPreferences = preferences;
            PacketDistributor.sendToServer(preferences);
        }
    }

    public static void clear() {
        PENDING.clear();
        ACTIVE_TYPES.clear();
        CARD_POOL_TITLES.clear();
        sentPreferences = null;
    }

    public static BasicGachaScreen createLegacy(@Nullable Screen parent,
                                                List<@Nullable ResourceLocation> lootTableIds,
                                                List<ItemStackEntry> entries) {
        if (GachaAddiction.LDLIB2_LOADED) {
            try {
                return new LDLib2GachaScreen(parent, lootTableIds, entries);
            } catch (RuntimeException | LinkageError failure) {
                GachaAddiction.LOGGER.warn(LDLib2GachaUiConstants.FALLBACK_LOG, failure);
            }
        }
        return new SlotMachineScreen(parent, lootTableIds, entries);
    }

    public static Screen createCardPoolResult(UUID transactionId, ResourceLocation cardPoolId,
                                              ResourceLocation packageId, List<ItemStack> rewards) {
        return createCardPoolResult(transactionId, cardPoolId, packageId, rewards, List.of());
    }

    public static Screen createCardPoolResult(UUID transactionId, ResourceLocation cardPoolId,
                                              ResourceLocation packageId, List<ItemStack> rewards, List<List<ItemStack>> rounds) {
        GachaType animation = animation(-1, GachaAddictionConfig.CLIENT.getDefaultGachaaType());
        if (animation == GachaTypes.TOTEM || animation == GachaTypes.CHEST) {
            GachaSourceInteractionScreen screen = apply(new GachaSourceInteractionScreen(Minecraft.getInstance().screen,
                    List.of(), List.of(), animation), -1, animation);
            screen.setSourceTitle(cardPoolTitle(cardPoolId));
            screen.loadCommitted(rewards, rounds);
            return screen;
        }
        if (GachaAddiction.LDLIB2_LOADED
                && GachaAddictionConfig.CLIENT.getDefaultGachaaType() == GachaTypes.LDLIB2) {
            try {
                return new LDLib2GachaResultScreen(transactionId, cardPoolId, packageId, rewards);
            } catch (RuntimeException | LinkageError failure) {
                GachaAddiction.LOGGER.warn(LDLib2GachaUiConstants.FALLBACK_LOG, failure);
            }
        }
        return new CardPoolResultScreen(transactionId, cardPoolId, packageId, rewards);
    }
}
