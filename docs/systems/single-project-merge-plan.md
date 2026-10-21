# 单一 Gradle 工程合并计划

2026-10-21。用户撤回原 D06=B 多项目方案，明确要求将 api、core、client、compat、data 全部合并为一个工程。本文件取代原 P03M 多项目实施方向。

五个子项目的 539 个源码／测试文件已迁入根 src，Gradle 配置与运行配置已改为单工程。530 个文件迁移后字节完全一致；另外 9 个文件的 399 个换行字节已获用户授权恢复，脚本逐文件核对恢复结果与迁移前备份完全一致，退出码为 0。修复前备份位于 patches/line-ending-repair-20261021-083330-591。IDEA 当前仅识别根及 main/test 模块，本轮未执行刷新。合并后 541 个 Java 文件诊断 0 错误，IDEA 编译、五项针对性检查、标准 Data 及后续获准的离线 JAR/POM/module 验收均完成，准确结果见[单工程验证](single-project-verification-20261021.md)。用户已许可原有工作独立基线再分批提交；基线 2bf06e9、代码 138047e，文档独立收尾，见[证据对照](refactor-completion.md)。

## 目标结构

~~~text
gachaaddiction_1.21.1/
├── build.gradle
├── settings.gradle
├── src/main/java/xiaojin/gachaaddiction/
│   ├── api/
│   ├── cardpool/
│   ├── client/
│   ├── compat/
│   ├── data/
│   ├── events/
│   ├── persistence/
│   ├── registry/
│   └── 其他现有职责包
├── src/main/resources/
├── src/main/templates/
├── src/generated/resources/
├── src/test/java/xiaojin/gachaaddiction/
├── .run/
└── docs/
~~~

只保留根 Gradle project。Java 的 api、client、compat 等包继续表示职责；已有类的包名、注册 ID、资源路径、网络协议和数据格式不因工程合并改变。

## 合并映射

| 现有位置 | 合并后位置 |
| --- | --- |
| api/src/main/java | src/main/java，保留包路径 |
| core/src/main/java | src/main/java，保留包路径 |
| client/src/main/java | src/main/java，保留包路径 |
| compat/src/main/java | src/main/java，保留包路径 |
| data/src/main/java | src/main/java，保留包路径 |
| core/src/test/java | src/test/java，保留测试包路径 |
| 其他子项目中的实际源码或资源 | 逐项并入根对应 sourceSet，有冲突先合并内容，禁止覆盖 |
| 根 resources、templates、generated | 保持现有路径与内容 |

已有 GachaData、接口 I 前缀、事件入口、公共／客户端隔离、展示／播报／道具拆分、汇总视图、场景资源所有权、矿车修复、事务队列及投递收据实现均保留。

## 实施顺序

1. 保存当前工作树、Git 索引和迁移前路径清单；保护原有暂存、未暂存及未跟踪内容。
2. 通过 IDEA 将五个子项目的 main Java 源码迁入根源码目录，将测试迁入根 test。逐文件保留内容及包名，冲突不得覆盖。
3. 将 compat 的必要 compileOnly 声明并入根 dependencies，删除 project(...) 内部依赖；保留依赖版本、运行开关、JarJar 和可选模组语义。
4. settings.gradle 删除五项 include。根 build.gradle 删除 gachaModules、evaluationDependsOn、embeddedModules、多 sourceSet 的 modFolders 和手动聚合子项目输出的 jar 配置，仅使用根 sourceSet。
5. 将测试的 NeoForge modding dependencies 配置移到根。Data 仍使用原生成源、输出目录和既有生成器。
6. 更新五项回归检查及 Data／开发运行配置使用根工程模块，清除对旧子项目路径和模块名的引用。工程模型更新按当前授权边界执行，不能用旧模块输出冒充合并后的验证。
7. 备份后移除仅服务于多项目的子 build.gradle 和 gradle/gacha-module.gradle，清理空源码目录。build、out、.gradle、run 和存档继续按原保护规则保留，不递归删除项目缓存。
8. 在编辑阶段仅格式化新增代码和修改行，不额外优化导入或重排成员。全部代码修改完成后，统一检查类型／引用、物理端隔离、Mixin、资源和运行配置。
9. 修正并处理格式后，再执行已授权的 IDEA 编译、五项针对性检查和标准 Data。游戏／故障注入仍不执行，JAR／POM 任务不因本次重规划自动获准。
10. 更新当前系统文档和历史迁移说明，按原 D03 要求准备仅包含本次重构差异的提交；不把用户原有修改合并提交。

## 合并完成标准

- Gradle 只声明根项目，源码不再依赖任何子项目 sourceSet 或 project(...)。
- 所有生产源码和测试位于根 src；兼容和客户端边界由包职责、加载条件及事件发行侧保持。
- 只有一套根依赖、Mod 元数据、Mixin 清单、资源入口和数据生成入口；没有重复类或旧模块输出遮蔽。
- 五项检查的 IDEA 配置指向根测试模块；Data 使用根实际编译输出。
- 按“全部代码完成 → 统一正确性检查 → 已授权验证”的顺序留存证据，不沿用多项目阶段的通过结果冒充合并后的结果。
- 多项目方案的历史记录保留并标明被本计划取代；缓存和用户资产未被当作子项目源码删除。
