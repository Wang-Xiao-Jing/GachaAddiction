package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

@FunctionalInterface
public interface IGachaParameterValue {
    Object resolve(GachaNumberContext context);
}
