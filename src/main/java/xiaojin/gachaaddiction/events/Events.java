package xiaojin.gachaaddiction.events;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.command.GachaCommand;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;
import xiaojin.gachaaddiction.util.LootDisplayCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EventBusSubscriber(modid = GachaAddiction.MODID)
public class Events {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        LootDisplayCache.reload(level);
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        ServerLevel level = event.getPlayerList().getServer().overworld();
        LootDisplayCache.reload(level);
    }

    @SubscribeEvent
    public static void onPlayerContainerOpen(PlayerContainerEvent.Open event) {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(GachaCommand.register());
    }
}
