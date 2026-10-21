# 2026-10-20 重构验证记录

## 授权核对

已通过当前对话的原始记录核对 D02：助手给出 A=允许 IDEA 编译、Data 和针对性测试，用户随后回复 a。该选择早于 D07，仍不允许运行游戏或故障注入。之前自动审批误按更早的限制拒绝编译；取得原始选择证据后，同一 IDEA 编译入口接受并执行，不需要重复询问用户。

此授权仅记录本次重构，不改变 AGENTS 默认验证边界。

## 本次实际结果

| 检查 | 结果 | 覆盖 |
| --- | --- | --- |
| IDEA build_project，增量编译 | isSuccess=true | 当前多模块源码；仅 Rarity.color() 既有弃用警告 |
| WAL Regression Checks | exitCode=0 | IO 回调快照、可变 NBT 提交副本、三种 durability 和重新打开后的顺序 |
| Transaction Queue Regression Checks | exitCode=0 | 内联重入、恢复与 draw 整段串行、失败后继续、取消／完成观察 Future 不释放真实任务 |
| Player Save Checkpoint Regression Checks | exitCode=0 | 收据身份／格式／清理隔离、原始背包列表匹配、临时 player.dat 和单人 level.dat 的确认条件；普通背包投递已接入，真实世界场景未运行 |
| Persistence Format Regression Checks | exitCode=0 | 当前事务／空奖励邮箱／账本 4 元数据往返、计划深复制、账本 3 无收据读取和降版拒绝；新增原始实体快照的存在／唯一性／位置／物品／收据核对；不含真实实体存储和完整当前卡池往返 |
| Card Pool Fixture Regression Checks | exitCode=0 | 三个真实夹具的计划编译、1/3 轮参数、请求覆盖保护和旧根字段拒绝 |
| 标准 Data | exitCode=0 | zh_cn/en_us/ru_ru、物品模型、声音提供器完成；首次日志 written=0 |

标准 Data 日志：C:/Users/wang_/AppData/Local/JetBrains/IntelliJIdea2026.2/tmp/ij_run__Data_14874856843843087842.log。

Data 明确把根、api、core、client、compat、data 输出目录组合为一个 gachaaddiction 模组。根 out/production/classes 下核对只剩两个构造入口，未保留原来全部领域类作为影子副本。这不等于最终 JAR 装配验收。

## Data 日志保留问题

- RarityCore 自身的旧 KJS 插件尝试加载 dev.latvian.mods.kubejs.KubeJSPlugin 失败；错误来源为 org.yanbwe.raritycore.kubejs.RarityCoreKubeJSPlugin，不是本模组插件。
- 还有第三方 refmap、可选 RefinedStorage 类缺失、union URL 和源资源 Example resource package.zip 命名空间警告。
- 未为掩盖日志更换依赖、添加旧 KJS 包名兼容类或修改第三方 JAR；不能把退出码 0 解释为整份日志没有错误。

## 隔离 KJS 探针

尝试在 patches/kjs-binding-check 的 startup_scripts 写入仅检查 define/create 全局的探针，并用 Data 的临时工作目录覆盖运行。Data 退出 0，但没有通过标记。

已检查当前 KubeJS 源码：生成数据时不会调用 startupScriptManager.reload，因此这个探针没有执行，不能记为绑定通过，也不强行调用不支持的启动流程。日志为 C:/Users/wang_/AppData/Local/JetBrains/IntelliJIdea2026.2/tmp/ij_run__Data_1549864151838254886.log。

该尝试还触发 BasicLangGenerator 从工作目录父目录寻找手动 lang 的问题，改变了生成条目。随后用标准 Data 配置重新生成，退出 0；与修改前备份逐项比较，所有非缓存生成文件均恢复，只有 Data 管理的缓存记录变化。恢复日志为 C:/Users/wang_/AppData/Local/JetBrains/IntelliJIdea2026.2/tmp/ij_run__Data_13318318205576617277.log。

临时探针与日志位于忽略目录，没有部署到现有 run；KJS 脚本执行与资源生成场景仍待相应支持的验证。

## 未完成的验证

普通背包投递已接入新检查点，邮箱领取和直接掉落尚未完成；不以格式／临时玩家快照检查替代资产中断恢复。游戏、GL、可选依赖缺失矩阵、真实 KJS 调用、最终 JAR／发布元数据和 Git 差异隔离仍须分别核对。生成文件均通过 Data 管理，没有手工补写。
