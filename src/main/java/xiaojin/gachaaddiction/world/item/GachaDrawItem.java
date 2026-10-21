package xiaojin.gachaaddiction.world.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import xiaojin.gachaaddiction.api.GachaType;
import xiaojin.gachaaddiction.registry.GachaSources;
import xiaojin.gachaaddiction.util.TranslationKeys;

import java.util.List;

public class GachaDrawItem extends Item {
    private final GachaType animation;

    public GachaDrawItem(Properties properties, GachaType animation) {
        super(properties);
        this.animation = animation;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            var source = stack.getCapability(GachaSources.ITEM);
            if (!(source == null ? GachaItemDraws.begin(serverPlayer, hand, animation)
                    : source.beginDraw(serverPlayer, hand, animation))) {
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(TranslationKeys.GUI_DRAW_ITEM_HINT));
    }
}
