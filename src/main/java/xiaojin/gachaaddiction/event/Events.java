package xiaojin.gachaaddiction.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.cardpool.GachaServerRuntimeManager;
import xiaojin.gachaaddiction.command.GachaCommand;
import xiaojin.gachaaddiction.data.GachaDefinitionReloadListener;
import xiaojin.gachaaddiction.data.GachaDefinitionRepository;
import xiaojin.gachaaddiction.data.GachaRuntimeRegistries;
import xiaojin.gachaaddiction.item.GachaItemDraws;
import xiaojin.gachaaddiction.item.GachaDrawItemDefinitions;
import xiaojin.gachaaddiction.registry.Payloads;
import xiaojin.gachaaddiction.registry.item.GachaDrawItems;
import xiaojin.gachaaddiction.util.GachaAnnouncements;
import xiaojin.gachaaddiction.util.GachaPresentations;
import xiaojin.gachaaddiction.util.LootDisplayCache;

@EventBusSubscriber(modid = GachaAddiction.MODID)
public class Events {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        GachaRuntimeRegistries.registerAndFreeze();
        event.addListener(new GachaDefinitionReloadListener(event.getRegistryAccess(),
                GachaRuntimeRegistries.PARAMETER_TYPES, GachaDefinitionRepository.INSTANCE));
        event.addListener(new GachaDrawItemDefinitions(event.getRegistryAccess()));
    }

    @SubscribeEvent
    public static void creativeContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            event.accept(GachaDrawItems.SEALED_CHEST);
            event.accept(GachaDrawItems.GACHA_KEY);
            event.accept(GachaDrawItems.WHEEL_TICKET);
            event.accept(GachaDrawItems.SLOT_TOKEN);
            event.accept(GachaDrawItems.WISH_TOTEM);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        LootDisplayCache.reload(level);
        GachaServerRuntimeManager.start(event.getServer()).exceptionally(failure -> {
            GachaAddiction.LOGGER.error("Failed to recover gacha transactions", failure);
            return null;
        });
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        GachaPresentations.clear();
        GachaItemDraws.clear();
        GachaDrawItemDefinitions.clear();
        GachaAnnouncements.clear();
        GachaServerRuntimeManager.stop();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        GachaPresentations.forget(event.getEntity().getUUID());
        GachaItemDraws.forget(event.getEntity().getUUID());
        GachaAnnouncements.forget(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onContainerClosed(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            GachaPresentations.closeMenu(player, event.getContainer().containerId);
            GachaAnnouncements.closeMenu(player, event.getContainer().containerId);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        GachaServerRuntimeManager.current().ifPresent(runtime -> {
            try {
                runtime.reconcilePlayerDeliveries(player);
            } catch (RuntimeException failure) {
                GachaAddiction.LOGGER.error("Failed to reconcile gacha reward delivery for player {}",
                        player.getUUID(), failure);
            }
            runtime.recover(context -> context.playerId().equals(player.getUUID())).exceptionally(failure -> {
                GachaAddiction.LOGGER.error("Failed to recover gacha transactions for player {}",
                        player.getUUID(), failure);
                return null;
            });
        });
    }

    @SubscribeEvent
    public static void onPlayerSaved(PlayerEvent.SaveToFile event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        GachaServerRuntimeManager.current().ifPresent(runtime -> {
            try {
                runtime.acknowledgePlayerSave(player);
            } catch (RuntimeException failure) {
                GachaAddiction.LOGGER.error("Failed to acknowledge gacha reward delivery for player {}",
                        player.getUUID(), failure);
            }
        });
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerLevel level = event.getPlayerList().getServer().overworld();
        LootDisplayCache.reload(level);
    }


    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(GachaCommand.register());
    }

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        Payloads.registrar(event);
    }

    @SubscribeEvent
    public static void onPresentationTick(ServerTickEvent.Post event) {
        GachaPresentations.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onProtectedBlockBroken(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            var holder = event.getLevel().getBlockEntity(event.getPos());
            if (holder != null && !GachaPresentations.mayAccess(player, holder)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onProtectedEntityAttacked(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && !GachaPresentations.mayAccess(player, event.getTarget())) {
            event.setCanceled(true);
        }
    }
}
