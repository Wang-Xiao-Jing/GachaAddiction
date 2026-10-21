package xiaojin.gachaaddiction.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.admin.GachaAdminCapability;
import xiaojin.gachaaddiction.data.drawitem.GachaDrawItemDefinitions;
import xiaojin.gachaaddiction.util.TranslationKeys;
import xiaojin.gachaaddiction.world.item.GachaDrawItemFactory;

import java.util.Collection;

public class GachaDrawItemCommand {
    private static final int MAXIMUM_COUNT = 64;
    private static final DynamicCommandExceptionType UNKNOWN_DEFINITION = new DynamicCommandExceptionType(id ->
            Component.translatable(TranslationKeys.COMMANDS_GACHA_UNKNOWN_DRAW_ITEM, id));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("item")
                .requires(GachaAdminCapability.MANAGE_ADMIN::allows)
                .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("definition", ResourceLocationArgument.id())
                                        .suggests((context, builder) -> {
                                            GachaDrawItemDefinitions.ids().forEach(id -> builder.suggest(id.toString()));
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> give(context.getSource(), EntityArgument.getPlayers(context, "targets"),
                                                ResourceLocationArgument.getId(context, "definition"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, MAXIMUM_COUNT))
                                                .executes(context -> give(context.getSource(), EntityArgument.getPlayers(context, "targets"),
                                                        ResourceLocationArgument.getId(context, "definition"), IntegerArgumentType.getInteger(context, "count")))))));
    }

    private static int give(CommandSourceStack source, Collection<ServerPlayer> players, ResourceLocation id,
                            int count) throws CommandSyntaxException {
        if (!GachaDrawItemDefinitions.ids().contains(id)) {
            throw UNKNOWN_DEFINITION.create(id);
        }

        ItemStack template = GachaDrawItemFactory.create(id.toString());
        for (ServerPlayer player : players) {
            int remaining = count;
            while (remaining > 0) {
                int amount = Math.min(remaining, template.getMaxStackSize());
                ItemStack stack = template.copyWithCount(amount);
                player.getInventory().add(stack);
                if (!stack.isEmpty()) {
                    var dropped = player.drop(stack, false);
                    if (dropped != null) {
                        dropped.setNoPickUpDelay();
                        dropped.setTarget(player.getUUID());
                    }
                }
                remaining -= amount;
            }
            player.containerMenu.broadcastChanges();
        }
        source.sendSuccess(() -> Component.translatable(TranslationKeys.COMMANDS_GACHA_DRAW_ITEM_GIVEN,
                count, template.getHoverName(), players.size()), true);
        return players.size();
    }
}
