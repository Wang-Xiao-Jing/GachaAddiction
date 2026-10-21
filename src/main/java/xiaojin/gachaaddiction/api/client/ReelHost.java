package xiaojin.gachaaddiction.api.client;

import net.minecraft.client.Minecraft;
import xiaojin.gachaaddiction.api.RewardData;

/**
 * 老虎机转轮（{@link xiaojin.gachaaddiction.client.gui.widget.VerticalReelWidget}）的宿主抽象。
 * <p>
 * 真实屏幕（{@link xiaojin.gachaaddiction.client.gui.screen.BasicGachaScreen}）与配置界面的
 * 预览面板都实现本接口，从而让转轮控件不再强依赖具体 Screen 类，便于在配置界面中复用渲染。
 */
public interface ReelHost {
    /**
     * 抢占"拨片经过"音效的播放权。预览场景返回 false 以静音。
     */
    boolean tryClaimPassSound();

    /**
     * 当前帧可播放音效的最高稀有度级别（用于只播最高稀有度奖品音效的优化）。
     */
    int getCurrentRenderHighestNewCompleteLevel();

    /**
     * 抢占"获得奖品"音效的播放权。预览场景返回 false 以静音。
     */
    boolean tryClaimCompleteSound();

    /**
     * 宿主所在的 Minecraft 实例。
     */
    Minecraft getMinecraft();

    /**
     * 真实动画在揭晓时通知；本地预览沿用空实现，不产生拿取请求。
     */
    default void onRewardRevealed(RewardData reward) {
    }

    default boolean canAnnounceReward(RewardData reward) {
        return false;
    }
}
