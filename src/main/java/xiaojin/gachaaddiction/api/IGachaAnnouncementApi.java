package xiaojin.gachaaddiction.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 客户端入口由 GachaAnnouncementClient.INSTANCE 实现，服务端入口使用 GachaAnnouncements。
 */
public interface IGachaAnnouncementApi {
    /**
     * 开出物品的客户端按本地规则请求；Alt 手动播报限于当前结果界面，退出清空快照。
     */
    void request(ItemStack item, Trigger trigger);

    /**
     * 服务端从连接上下文确定玩家、核实实际开出记录后，再向接收客户端转发。
     */
    void broadcast(ServerPlayer player, ItemStack item, Trigger trigger);

    /**
     * 接收端展示服务端已经确认的播报。
     */
    void receive(GachaAnnouncement announcement);

    enum Trigger {
        AUTOMATIC, MANUAL
    }
}
