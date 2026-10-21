# 事件入口与运行时注册

当前版本：Minecraft 1.21.1、NeoForge 21.1.249、FML 4.0.44。当前 EventBusSubscriber 根据事件是否实现 IModBusEvent 自动选择总线，bus 参数已弃用且被忽略，因此不配置 bus。

## 自动订阅入口

| 入口 | 发行侧 | 总线与职责 |
| --- | --- | --- |
| events/GachaServerEvents | 两个物理端 | 游戏总线；服务器生命周期、玩家、容器、展示 tick、来源保护、命令和数据重载 |
| events/GachaModEvents | 两个物理端 | 模组总线；创造栏内容、网络消息注册 |
| events/client/GachaClientEvents | 仅客户端 | 客户端资源重载走模组总线，其余界面与交互事件走游戏总线 |
| events/GachaDataEvents | 两个物理端 | 模组总线；GatherData 转交 generator/Datagen 注册 Provider |

通用订阅入口保留双方物理端注册，使客户端中的集成服务器仍能接收服务器事件；逻辑端判断保持原方法规则。客户端入口保留 Dist.CLIENT，服务端不会加载它的渲染和屏幕类型。

| 事件类型 | 唯一订阅方法 |
| --- | --- |
| AddReloadListenerEvent | GachaServerEvents.onAddReloadListeners |
| ServerStartedEvent | GachaServerEvents.onServerStarted |
| ServerStoppingEvent | GachaServerEvents.onServerStopping |
| PlayerEvent.PlayerLoggedOutEvent | GachaServerEvents.onPlayerLoggedOut |
| PlayerContainerEvent.Close | GachaServerEvents.onContainerClosed |
| PlayerEvent.PlayerLoggedInEvent | GachaServerEvents.onPlayerLoggedIn |
| EntityJoinLevelEvent | GachaServerEvents.onPendingRewardDropJoined |
| OnDatapackSyncEvent | GachaServerEvents.onDatapackSync |
| RegisterCommandsEvent | GachaServerEvents.onRegisterCommands |
| ServerTickEvent.Post | GachaServerEvents.onPresentationTick |
| BlockEvent.BreakEvent | GachaServerEvents.onProtectedBlockBroken |
| AttackEntityEvent | GachaServerEvents.onProtectedEntityAttacked |
| BuildCreativeModeTabContentsEvent | GachaModEvents.creativeContents |
| RegisterPayloadHandlersEvent | GachaModEvents.onRegisterPayloadHandlers |
| RegisterClientReloadListenersEvent | GachaClientEvents.register |
| PlayerInteractEvent.RightClickBlock | GachaClientEvents.onRightClickBlock |
| PlayerInteractEvent.RightClickItem | GachaClientEvents.onRightClickItem |
| PlayerInteractEvent.EntityInteract | GachaClientEvents.onRightClickEntity |
| ScreenEvent.Opening | GachaClientEvents.onScreenOpening |
| ScreenEvent.Render.Pre | GachaClientEvents.onScreenRenderPre |
| ScreenEvent.Render.Post | GachaClientEvents.onScreenRenderPost |
| RenderGuiEvent.Post | GachaClientEvents.onGuiRender |
| RenderTooltipEvent.Pre | GachaClientEvents.onItemTooltip |
| ContainerScreenEvent.Render.Background | GachaClientEvents.onContainerBackground |
| ScreenEvent.KeyPressed.Pre | GachaClientEvents.onKeyPressed |
| ScreenEvent.KeyReleased.Pre | GachaClientEvents.onKeyReleased |
| ClientPlayerNetworkEvent.LoggingOut | GachaClientEvents.onLoggedOut |
| ClientTickEvent.Post | GachaClientEvents.onClientTick |
| GatherDataEvent | GachaDataEvents.gatherData |

同一入口内部的调用顺序保持迁移前顺序。服务器停止先清理展示、道具、定义和播报，再关闭运行时；登出和关闭容器保持原有会话清理顺序。资源重载先注册音效和播报纹理，再注册三种渲染资源修订监听。

## 运行时扩展注册

registry/runtime/GachaRuntimeRegistries 在 AddReloadListenerEvent 中调用 registerAndFreeze。首次调用通过 NeoForge.EVENT_BUS 发布 event/RegisterGachaRuntimeTypesEvent，随后依次冻结参数、消费、投递和执行步骤注册容器，并设置 frozen。后续调用保持无操作，不重新发布事件。

扩展事件仍提供原有四个注册容器访问方法，不额外发布旧包事件，也不新增第二组类型注册表。旧的 api/event 包路径已按本次 D01=B 策略移除；订阅方需要更新 import。

数据生成的 Provider 集合、语言初始化顺序、资源路径和输出目录未因监听入口移动而改变。Datagen.registerProviders 是普通生成装配方法，不再带自动订阅注解，避免重复生成。

## 本批证据

IDEA 检查本批 9 个 Java 文件：全部 analyzed，0 错误、5 个警告。订阅入口清单共 29 项，按当前事件类型名称检查无重复。没有执行本批编译、Data、游戏或测试；事件运行加载和生成输出仍待获批执行验证。

上述为事件迁移批次证据。后续投递修复移除 SaveToFile 确认入口，增加待确认掉落实体入世界对账；当前订阅表已同步。单工程编译、五项检查、Data 与 JAR 装配结果见 single-project-verification-20261021.md，不代表实际玩家／实体事件场景已运行。
