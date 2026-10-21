package xiaojin.gachaaddiction.data;

import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.rule.GachaRuleDefinition;

import java.util.List;

@FunctionalInterface
public interface GachaRuleDefinitionCompiler {
    List<GachaRuleDefinition> compile(ResourceLocation rewardPoolId, JsonArray rules,
                                      GachaParameterSchema parameterSchema);
}
