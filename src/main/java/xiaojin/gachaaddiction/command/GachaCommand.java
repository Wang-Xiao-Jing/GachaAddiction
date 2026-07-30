package xiaojin.gachaaddiction.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.api.ItemStackEntry;
import xiaojin.gachaaddiction.init.GachaTypes;
import xiaojin.gachaaddiction.network.OpenGachaScreenPayload;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author wang_
 * @version 2024.3.4.1
 * @description
 * @date 2026/7/30
 */
public class GachaCommand {
    public static final String COMMANDS_GACHAADDICTION_GACHA = "commands.gachaaddiction.gacha.";

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("gacha")
                .requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("type", IdentifierArgument.id())
                                .suggests((context, builder) -> {
                                    for (GachaType type : GachaTypes.getLiat()) {
                                        if (!type.isEmpty()) {
                                            builder.suggest(type.getId().toString());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("lootTable", IdentifierArgument.id())
                                        .suggests((context, builder) -> {
                                            var server = context.getSource().getServer();
                                            var registry = server.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE);
                                            for (var holder : registry.listElements().toList()) {
                                                var table = holder.value();
                                                if (table != LootTable.EMPTY) {
                                                    builder.suggest(holder.key().identifier().toString());
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 10000))
                                                .executes(context -> executeGacha(
                                                        context.getSource(),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        IdentifierArgument.getId(context, "type"),
                                                        IdentifierArgument.getId(context, "lootTable"),
                                                        IntegerArgumentType.getInteger(context, "count")
                                                ))
                                        )
                                )
                        )
                );
    }

    private static int executeGacha(CommandSourceStack source, Collection<ServerPlayer> targets, Identifier typeId, Identifier lootTableLocation, int count) {
        ServerLevel level = source.getLevel();

        GachaType gachaType = GachaTypes.get(typeId);
        if (gachaType == null) {
            source.sendFailure(Component.translatable(COMMANDS_GACHAADDICTION_GACHA + "invalid_type", typeId.toString()));
            return 0;
        }

        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableLocation);
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableKey);
        if (lootTable == null || lootTable == LootTable.EMPTY) {
            source.sendFailure(Component.translatable(COMMANDS_GACHAADDICTION_GACHA + "invalid_loot_table", lootTableLocation.toString()));
            return 0;
        }

        List<ItemStackEntry> entries = ItemStackEntry.extract(lootTable);
        LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        List<ItemStack> rewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rewards.addAll(lootTable.getRandomItems(params));
        }

        for (ServerPlayer player : targets) {
            for (ItemStack stack : rewards) {
                ItemStack copy = stack.copy();
                boolean flag = player.getInventory().add(copy);
                if (flag && copy.isEmpty()) {
                    ItemEntity fakeDrop = player.drop(stack, false);
                    if (fakeDrop != null) {
                        fakeDrop.makeFakeItem();
                    }
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                            (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F);
                    player.containerMenu.broadcastChanges();
                } else {
                    ItemEntity drop = player.drop(copy, false);
                    if (drop != null) {
                        drop.setNoPickUpDelay();
                        drop.setTarget(player.getUUID());
                    }
                }
            }
            ServerPlayNetworking.send(player, new OpenGachaScreenPayload(
                    gachaType.isEmpty() ? Optional.empty() : Optional.of(gachaType),
                    List.of(lootTableLocation),
                    entries,
                    rewards
            ));
        }

        source.sendSuccess(() -> Component.translatable(COMMANDS_GACHAADDICTION_GACHA + "success", targets.size(), count, lootTableLocation.toString()), true);
        return count;
    }
}
