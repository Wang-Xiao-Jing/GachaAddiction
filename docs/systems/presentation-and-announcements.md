# 展示、播报与客户端会话

## 状态所有者

| 入口 | 状态与职责 |
| --- | --- |
| core presentation/GachaPresentations | 展示会话、玩家偏好、来源对象锁；开始、完成、关闭、超时与登出清理 |
| core presentation/lock/GachaLockedItemHandler | 包装既有物品处理器，在来源被锁时拒绝抽取；其余操作继续委托 |
| core announcement/GachaAnnouncements | 播报 token、来源位置、奖励候选、自动播报标记和手动冷却 |
| client client/presentation/GachaPresentationClient | 待接收展示规则、活动界面类型、卡池标题与偏好发送缓存 |
| client client/presentation/GachaItemPresentationClient | 道具抽奖待完成会话及当前、返回界面 |
| client client/announcement/GachaAnnouncementClient | 播报队列、手动选择、提示与每帧渲染状态 |
| client client/announcement/GachaAnnouncementTextures | 播报纹理资源及重载生命周期 |
| client client/workspace | 本地工作区、文档编辑状态与工作台入口 |

这些类从通用 util 或 client 根包迁出后继续拥有原有状态，不另建会话表、锁表或工作区缓存。旧包路径按 D01=B 移除，调用方通过 IDEA 语义迁移更新。

## 展示生命周期

来源锁仍使用 IdentityHashMap 和对象身份；双箱通过 CompoundContainerAccessor 展开到两个实际容器。访问权限、物品处理器、Hopper/Dispenser 及玩家容器操作仍查询同一状态所有者。

开始会话先释放该玩家之前的展示。频道可用性、来源规则、服务端强制观看与玩家偏好共同决定是否发送展示规则；token、menuId 和原有副类型选择规则保持不变。道具展示仍使用 ITEM_MENU=-1。

完成、关闭、登出、死亡、旁观、换维度、来源移除或超过 300000ms 时，沿用原有释放规则。需要通知客户端时仍使用原 GachaPresentationPayload，不新增消息或更改编码。

## 播报生命周期

来源会话保留最多三种展示物品的限制，真实奖励仍由服务端核对。手动请求保留 20 tick 冷却；自动播报标记与玩家会话绑定。来源结构查询只读已加载区块，不因迁移触发额外区块加载。

客户端继续从原有消息和真实奖励同步中更新展示。多轮小总览、返回交互及最后总总览没有因包迁移而合并或重新抽奖。

## 本批验证边界

12 个类通过 IDEA 语义迁移，44 个相关调用文件均完成错误诊断，未发现错误。旧类全限定名在 main Java 源码中的搜索结果为空。此证据不包含游戏运行、视觉、网络交互和本地文件操作验收。

