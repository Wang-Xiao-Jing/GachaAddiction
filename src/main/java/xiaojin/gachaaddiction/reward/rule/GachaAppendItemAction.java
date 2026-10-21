package xiaojin.gachaaddiction.reward.rule;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiaojin.gachaaddiction.reward.model.GachaLogicalReward;
import xiaojin.gachaaddiction.reward.model.GachaResultEditor;
import xiaojin.gachaaddiction.reward.number.IGachaLongProvider;
import xiaojin.gachaaddiction.reward.number.GachaNumberContext;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterEditor;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOrigin;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardOriginTypes;
import xiaojin.gachaaddiction.reward.provenance.GachaRewardProvenance;

import java.util.Map;
import java.util.Objects;

public final class GachaAppendItemAction implements IGachaRuleAction {
    private final ResourceLocation id;
    private final ItemStack template;
    private final IGachaLongProvider quantity;

    public GachaAppendItemAction(ResourceLocation id, ItemStack template, IGachaLongProvider quantity) {
        this.id = Objects.requireNonNull(id, "id");
        Objects.requireNonNull(template, "template");
        if (template.isEmpty()) {
            throw new IllegalArgumentException("Append item action cannot use an empty item");
        }
        this.template = template.copyWithCount(1);
        this.quantity = Objects.requireNonNull(quantity, "quantity");
    }

    @Override
    public void apply(GachaRuleContext context, GachaResultEditor results, GachaParameterEditor parameters) {
        GachaNumberContext numbers = new GachaNumberContext(context.request().parameters(),
                parameters.currentValues(), context.request().random());
        long count = quantity.get(numbers);
        if (count <= 0) {
            return;
        }
        GachaRewardOrigin origin = new GachaRewardOrigin(GachaRewardOriginTypes.RULE_ACTION, id);
        results.append(new GachaLogicalReward(template, count, GachaRewardProvenance.generated(origin), Map.of()));
    }
}
