package xiaojin.gachaaddiction.execution;

import xiaojin.gachaaddiction.reward.number.GachaNumberContext;

@FunctionalInterface
public interface GachaParameterValue {
    Object resolve(GachaNumberContext context);
}
