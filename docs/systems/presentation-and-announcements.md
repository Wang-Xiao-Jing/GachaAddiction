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

## 首帧遮罩与发奖请求

所有 `BasicGachaScreen` 演出共用入场遮罩，覆盖现代、现代2、老虎机、交互式老虎机、CSGO、转盘、宝箱、图腾及 LDLib2 模板。通过现有 `ScreenEvent.Render.Post` 在首帧绘制不透明深色遮罩，180ms 淡出到各自演出，不重置各动画的状态。

客户端收到道具开始消息后先打开界面，等遮罩完成至少一次绘制，再在后续客户端 tick 发送原有确认发奖请求。服务端确认、消耗、事务及发奖规则保持不变，不等待揭晓完成。关闭或切换界面会清除未发送请求并取消该道具待确认会话；无动画模式仍直接确认。

容器自动拿取也经过相同首帧门槛，原来的 OPEN／COMPLETE 配置阶段保持不变。界面替换时沿用已有奖励重放和请求去重，旧界面的待执行动作不在新界面继续执行。

本次仅读取 IDEA 诊断；未执行构建、测试、游戏或重载，首帧视觉与联机时序仍需游戏内验收。

## 现代演出的随机剪影

来源设置 `GachaSourceSettings.randomType` 使用 `random_type` 保存，默认 `none`；仅 `modern`、`modern_2` 生效，真实抽奖和发奖不变。

| 值 | 剪影候选 |
| --- | --- |
| `none`（无） | 真实奖励，保持原演出 |
| `random`（随机） | 卡池执行计划可达的全部物品候选 |
| `full_random`（全随机） | 全部卡池候选与全部已注册物品的并集，排除空物品 |

候选按物品及组件去重，等概率抽取，每个奖励在同一界面中固定一个剪影。容器复用现有 `ItemStackEntry` 展示数据；抽奖道具由服务端从卡池套餐的执行计划递归收集奖励池、来源组、自定义表、嵌套表、物品标签和原版战利品表，并纳入规则追加、失败回退和保底奖励。`preview_items` 与卡池图标不参与候选收集。

收集不执行抽奖，不消费随机数或修改玩家状态；包含条件分支的候选并集，不按本次命中、数量、条件或概率缩减。显式零权重、零数量的直接条目排除。原版函数引用、替换物品、书本附魔、熔炼及探险地图的物品类型变化也纳入候选。引用按种类及 ID 去重以终止循环。动态战利品条目和未知扩展执行步骤无法静态枚举时明确报错，不用预览图标冒充完整候选。

独立的 `GachaItemPresentationPayload.possibleItems` 字段同步全部候选，不受 24 个预览图标的限制；候选上限 65536，超限报错而非截断。候选为空时仅显示问号，不退回真实奖励。两端须使用配套网络版本。

两种随机模式在揭晓前使用中性色背景、光效和卡片边框，剪影中央显示“？”，开始揭晓后恢复真实物品及稀有度。着色器不可用时的备用绘制遵循相同规则。

抽奖道具定义可在 `source` 中设置 `"random_type": "random"` 或 `"random_type": "full_random"`；普通物品的 `minecraft:custom_data.gachaaddiction` 和容器／实体的 `gacha_source` 附件使用同名字段。双箱优先采用左侧非 `none` 设置，否则采用右侧。展示消息在原有字段末尾追加随机类型，客户端与服务端应使用配套版本。

## 随机剪影手动验收

内置测试卡池 `gacha_test:silhouette_demo` 使用独立执行计划、来源组、规则组和两张自定义表；实际候选包括稀有钻石斧、罕见金锭、末影珍珠、`minecraft:logs` 标签全部物品，以及规则追加的下界之星。每次三轮，追加的下界之星保证现代演出存在可揭晓奖励。基岩只作为卡池图标和伪装预览，随机模式不应出现基岩剪影或基岩奖励；全随机模式允许从注册表选到基岩。

使用包含新增资源的版本进入世界，需权限等级 2。先执行 `/gacha admin list card_pools` 确认 `gacha_test:silhouette_demo` 已加载，再执行命令。手持获得的道具右键；多次开启观察候选，标签与嵌套表候选不应受单次真实奖励或预览图标限制。视觉抽样不等于穷举覆盖。

现代，随机：

```mcfunction
/give @s gachaaddiction:wish_totem[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:silhouette_demo",package:"gacha_test:triple",gacha_type:"gachaaddiction:modern",random_type:"random",preview_items:["minecraft:bedrock"]}}] 16
```

现代2，随机：

```mcfunction
/give @s gachaaddiction:wish_totem[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:silhouette_demo",package:"gacha_test:triple",gacha_type:"gachaaddiction:modern_2",random_type:"random",preview_items:["minecraft:bedrock"]}}] 16
```

现代2，全随机：

```mcfunction
/give @s gachaaddiction:wish_totem[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:silhouette_demo",package:"gacha_test:triple",gacha_type:"gachaaddiction:modern_2",random_type:"full_random",preview_items:["minecraft:bedrock"]}}] 16
```

现代2，无随机：

```mcfunction
/give @s gachaaddiction:wish_totem[minecraft:custom_data={gachaaddiction:{card_pool:"gacha_test:silhouette_demo",package:"gacha_test:triple",gacha_type:"gachaaddiction:modern_2",random_type:"none",preview_items:["minecraft:bedrock"]}}] 16
```

前两种随机模式在揭晓前应显示中央“？”及中性色背景、光效和边框；揭晓后恢复真实物品与稀有度。`none` 保持真实剪影和原稀有度效果。以上命令仅按源码核对，未运行构建、测试、游戏或重载。

## 播报生命周期

来源会话保留最多三种展示物品的限制，真实奖励仍由服务端核对。手动请求保留 20 tick 冷却；自动播报标记与玩家会话绑定。来源结构查询只读已加载区块，不因迁移触发额外区块加载。

客户端继续从原有消息和真实奖励同步中更新展示。多轮小总览、返回交互及最后总总览没有因包迁移而合并或重新抽奖。

## 本批验证边界

12 个类通过 IDEA 语义迁移，44 个相关调用文件均完成错误诊断，未发现错误。旧类全限定名在 main Java 源码中的搜索结果为空。此证据不包含游戏运行、视觉、网络交互和本地文件操作验收。
