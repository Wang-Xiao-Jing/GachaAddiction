package xiaojin.gachaaddiction.util;

import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.registry.language.GachaTranslations;

public interface TranslationKeys {
    String SECONDARY_NONE = GachaTranslations.creates(GachaConstants.MODID, "gacha_secondary_type.gachaaddiction.none", "None", "无");
    String SECONDARY_SELECT_ONE = GachaTranslations.creates(GachaConstants.MODID, "gacha_secondary_type.gachaaddiction.select_one", "Choose one", "多选一");
    String SECONDARY_MULTIPLE = GachaTranslations.creates(GachaConstants.MODID, "gacha_secondary_type.gachaaddiction.multiple", "Multiple draws", "多连抽");
    String CONFIG_SECONDARY_TYPE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.gachaa_screen.secondary_type", "Totem/chest secondary type", "图腾／箱子副类型");
    String CONFIG_CHOICE_COUNT = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.gachaa_screen.choice_count", "Choose-one candidate count", "多选一候选数量");
    String GUI_SECONDARY_CHOOSE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.secondary.choose", "Click an object to continue", "点击一个对象继续");
    String GUI_SECONDARY_TRIGGER = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.secondary.trigger", "Click to activate", "点击触发");
    String GUI_SECONDARY_ANIMATING = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.secondary.animating", "Presenting…", "演出中…");
    String GUI_SECONDARY_PAGE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.secondary.page", "%s / %s · Scroll to change page", "%s / %s · 滚轮翻页");
    String CONFIG_PRESENTATION = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.presentation", "Gacha presentation", "抽奖演出");
    String CONFIG_FORCE_WATCH = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.presentation.force_watch", "Require complete presentations", "强制完整观看抽奖");
    String CONFIG_SERVER_PROTECT_LOOT = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.presentation.protect_loot", "Allow loot protection", "允许抽奖防抢");
    String CONFIG_CLIENT_PROTECT_LOOT = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.gachaa_screen.protect_loot", "Protect loot during presentations", "抽奖期间防抢");
    String CONFIG_FORCE_WATCH_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.presentation.force_watch.tooltip",
            "Server enforcement is combined with the source's force_watch flag. Disables fast skipping and early closing.",
            "服务端开关与来源的 force_watch 标记取或。启用后禁止快速跳过及提前关闭。");
    String CONFIG_SERVER_PROTECT_LOOT_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.presentation.protect_loot.tooltip",
            "Protection requires the opener's client option. Blocks other players and automated extraction until completion.",
            "还需开箱者开启客户端防抢。完成前阻止其他玩家操作和自动化取出，完成后释放。");
    String CONFIG_CLIENT_PROTECT_LOOT_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.gachaa_screen.protect_loot.tooltip",
            "Request exclusive source access during this presentation when the server allows it.",
            "在服务端允许时，本次抽奖期间独占来源；完成、取消、掉线或超时后释放。");
    String GUI_ANIMATION_FORCED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.animation.forced", "Complete presentation required · Continue with normal controls",
            "需完整观看 · 使用正常交互继续");
    String GUI_ANIMATION_CHEST_FORCED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.animation.chest_forced", "Swipe upward to open · Complete presentation required",
            "向上滑动打开 · 需完整观看");
    String GUI_ANIMATION_WHEEL_FORCED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.animation.wheel_forced", "Flick the ring to start · Wait for it to stop",
            "拨动圆环开始 · 等待自动停止");
    String GUI_ANIMATION_CLICK_FORCED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.animation.click_forced", "Click to start · Wait for it to stop",
            "点击开始 · 等待自动停止");
    String GUI_ANIMATION_RUNNING = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.animation.running", "Drawing · Click or Space to skip", "抽奖中 · 点击或空格快速跳过");
    String GUI_LEGACY_SLOT_SPIN_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.slot_machine.spin_hint",
            "Click or press Space to start stopping", "点击或按空格开始停轮");
    String GUI_LEGACY_SLOT_STOP_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.slot_machine.stop_hint",
            "Stopping · Click or press Space to finish quickly", "正在停轮 · 点击或按空格快速完成");
    String GACHA_TYPE_CHEST = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:chest",
            "Sealed Chest", "封印宝箱");
    String GACHA_TYPE_CSGO = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:csgo",
            "CSGO", "CSGO");
    String GACHA_TYPE_WHEEL = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:wheel",
            "Super Wheel", "超级大转盘");
    String GACHA_TYPE_TOTEM = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:totem",
            "Totem", "不死图腾");
    String ITEM_SEALED_CHEST = GachaTranslations.creates(GachaConstants.MODID, "item.gachaaddiction.sealed_chest",
            "Sealed Chest", "封印宝箱");
    String ITEM_GACHA_KEY = GachaTranslations.creates(GachaConstants.MODID, "item.gachaaddiction.gacha_key",
            "Gacha Key", "抽奖钥匙");
    String ITEM_WHEEL_TICKET = GachaTranslations.creates(GachaConstants.MODID, "item.gachaaddiction.wheel_ticket",
            "Wheel Ticket", "转盘券");
    String ITEM_SLOT_TOKEN = GachaTranslations.creates(GachaConstants.MODID, "item.gachaaddiction.slot_token",
            "Slot Token", "老虎机代币");
    String ITEM_WISH_TOTEM = GachaTranslations.creates(GachaConstants.MODID, "item.gachaaddiction.wish_totem",
            "Wish Totem", "祈愿图腾");
    String GUI_ANIMATION_CHEST = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.chest",
            "Swipe upward to open · Space to skip", "向上滑动打开 · 空格跳过");
    String GUI_ANIMATION_CSGO = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.csgo",
            "Click to start · Wait for it to stop · Space to skip", "点击开始 · 等待自动停止 · 空格跳过");
    String GUI_ANIMATION_WHEEL = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.wheel",
            "Flick the ring to start · Wait for it to stop · Space to skip", "拨动圆环开始 · 等待自动停止 · 空格跳过");
    String GUI_ANIMATION_SLOT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.slot",
            "Click to start the reels · Wait for them to stop · Space to skip", "点击开始转动 · 等待自动停止 · 空格跳过");
    String GUI_ANIMATION_TOTEM = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.totem",
            "Revealing · Space to skip", "正在揭晓 · 空格跳过");
    String GUI_ANIMATION_PREVIEW = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.preview",
            "Display icons · Rewards are confirmed by the server on completion", "演示图标 · 完成后由服务器确认奖励");
    String GUI_ANIMATION_WAITING = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.waiting",
            "Waiting for server settlement…", "等待服务器结算…");
    String GUI_ANIMATION_FAILED = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.animation.failed",
            "Draw failed or expired · Esc to close", "抽奖失败或已过期 · Esc 关闭");
    String GUI_DRAW_ITEM_UNBOUND = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.draw_item.unbound",
            "This item has no card pool binding. Contact the server owner.", "此道具尚未绑定卡池，请联系服主。");
    String GUI_DRAW_ITEM_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.draw_item.hint",
            "Use to draw · Consumes one item and pays the card pool cost on completion", "使用以抽奖 · 完成后消耗一个，并支付卡池所需消耗");
    String GUI_DRAW_ITEM_UNAVAILABLE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.draw_item.unavailable",
            "Cannot start this draw: unsupported client, busy session, or unavailable card pool.", "无法开始抽奖：客户端不支持、正在结算或卡池不可用。");

    //region 命令
    String COMMANDS_GACHA_UNKNOWN_DRAW_ITEM = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.unknown_draw_item", "Unknown draw item definition: %s", "未知的抽奖物品定义：%s");
    String COMMANDS_GACHA_DRAW_ITEM_GIVEN = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draw_item_given", "Gave %1$s %2$s to %3$s player(s)", "已给予 %3$s 名玩家各 %1$s 个%2$s");
    String COMMANDS_GACHA_INVALID_REQUEST_PRESET = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.invalid_request_preset",
            "Invalid gacha request preset: %s", "无效的抽奖请求预设：%s");
    String COMMANDS_GACHA_INVALID_REWARD_POOL = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.invalid_reward_pool",
            "Invalid reward pool: %s", "无效的奖励池：%s");
    String COMMANDS_GACHA_REWARD_POOL_TEST_SUCCESS = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.reward_pool_test_success",
            "Tested request preset %2$s for %1$s players", "已为 %1$s 名玩家测试请求预设 %2$s");
    String COMMANDS_GACHA_REWARD_POOL_TEST_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.reward_pool_test_failed",
            "Reward pool test failed: %s", "奖励池测试失败：%s");
    String COMMANDS_GACHA_REWARD_POOL_DIRECT_TEST_SUCCESS = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.reward_pool_direct_test_success",
            "Tested reward pool %2$s for %1$s players using %3$s rounds",
            "已为 %1$s 名玩家直接测试奖励池 %2$s，共 %3$s 轮");
    String COMMANDS_GACHA_REWARD_POOL_DIRECT_TEST_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.reward_pool_direct_test_failed",
            "Direct reward pool test failed: %s", "直接奖励池测试失败：%s");
    String COMMANDS_GACHA_INVALID_EXECUTION_PLAN = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.invalid_execution_plan",
            "Invalid execution plan: %s", "无效的执行计划：%s");
    String COMMANDS_GACHA_EXECUTION_PLAN_TEST_SUCCESS = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.execution_plan_test_success",
            "Simulated execution plan %2$s for %1$s players", "已为 %1$s 名玩家模拟执行计划 %2$s");
    String COMMANDS_GACHA_EXECUTION_PLAN_TEST_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.execution_plan_test_failed",
            "Execution plan simulation failed: %s", "执行计划模拟失败：%s");
    String COMMANDS_GACHA_LIST_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.list_header",
            "%1$s definitions: %2$s", "%1$s 定义：%2$s 个");
    String COMMANDS_GACHA_PROBABILITY_UNKNOWN_KIND = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.probability_unknown_kind",
            "Unknown probability definition kind: %s", "未知的概率定义类型：%s");
    String COMMANDS_GACHA_PROBABILITY_NOT_FOUND = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.probability_not_found",
            "Probability description not found: %s", "未找到概率结构说明：%s");
    String COMMANDS_GACHA_PROBABILITY_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.probability_header",
            "Probability %1$s | mode=%2$s | selection_count=%3$s",
            "概率 %1$s | 模式=%2$s | 抽取数量=%3$s");
    String COMMANDS_GACHA_PROBABILITY_CANDIDATE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.probability_candidate",
            "Candidate %1$s | weight=%2$s", "候选 %1$s | 权重=%2$s");
    String COMMANDS_GACHA_RUNTIME_UNAVAILABLE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.runtime_unavailable",
            "The gacha server runtime is unavailable", "抽奖服务端运行时当前不可用");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_SERVER_UNAVAILABLE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_server_unavailable",
            "The server does not have LDLib2 available", "服务端未安装或未加载 LDLib2");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_CLIENT_UNAVAILABLE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_client_unavailable",
            "Your client did not declare LDLib2 workbench support", "你的客户端未声明 LDLib2 工作台支持");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_ACTION_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_action_failed",
            "Administration workbench action failed: %s", "管理工作台操作失败：%s");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_DRAFT_CHANGED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_draft_changed",
            "The draft changed; the workbench was refreshed", "草稿已经变化，工作台已刷新");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_DOCUMENT_MISSING = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_document_missing",
            "Draft document does not exist: %1$s/%2$s", "草稿文档不存在：%1$s/%2$s");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_PUBLISHED_REVISION_MISSING = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_published_revision_missing",
            "Published revision does not exist: %s", "已发布修订不存在：%s");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_PUBLISHED_REVISION_TOO_LARGE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_published_revision_too_large",
            "Published revision exceeds the workbench document limit of %s",
            "已发布修订超过工作台的 %s 个文档限制");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_EDIT_SESSION_REQUIRED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_edit_session_required",
            "An owned edit session is required", "需要先取得属于你的编辑会话");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_DOCUMENT_EXISTS = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_document_exists",
            "Draft document already exists: %1$s/%2$s", "草稿文档已经存在：%1$s/%2$s");
    String COMMANDS_GACHA_ADMIN_WORKBENCH_DOCUMENT_TOO_LARGE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.admin_workbench_document_too_large",
            "Draft document exceeds the workbench limit of %s characters",
            "草稿文档超过工作台的 %s 字符限制");
    String COMMANDS_GACHA_INBOX_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.inbox_header",
            "%1$s has %2$s pending reward transactions", "%1$s 有 %2$s 个待领取奖励事务");
    String COMMANDS_GACHA_INBOX_ENTRY = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.inbox_entry",
            "%1$s: %2$s reward stacks", "%1$s：%2$s 组奖励");
    String COMMANDS_GACHA_INBOX_CLAIMED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.inbox_claimed",
            "Claimed %1$s reward stacks from transaction %2$s",
            "已领取事务 %2$s 的 %1$s 组奖励");
    String COMMANDS_GACHA_INBOX_CLAIM_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.inbox_claim_failed",
            "Reward inbox claim failed: %s", "奖励收件箱领取失败：%s");
    String COMMANDS_GACHA_EDIT_SESSION_AVAILABLE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_session_available",
            "No definition edit session is active", "当前没有定义编辑会话");
    String COMMANDS_GACHA_EDIT_SESSION_STATUS = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_session_status",
            "Editor %1$s, session %2$s, expires %3$s, revision %4$s",
            "编辑者 %1$s，会话 %2$s，到期时间 %3$s，修订 %4$s");
    String COMMANDS_GACHA_EDIT_SESSION_ACQUIRED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_session_acquired",
            "Edit session %1$s acquired until %2$s", "已取得编辑会话 %1$s，有效至 %2$s");
    String COMMANDS_GACHA_EDIT_SESSION_RENEWED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_session_renewed",
            "Edit session renewed until %1$s at revision %2$s", "编辑会话已续期至 %1$s，当前修订 %2$s");
    String COMMANDS_GACHA_EDIT_SESSION_RELEASED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_session_released",
            "Definition edit session released", "定义编辑会话已释放");
    String COMMANDS_GACHA_EDIT_FORCE_TAKEOVER_PREPARED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_force_takeover_prepared",
            "Confirm force takeover with token %1$s before %2$s",
            "请在 %2$s 前使用令牌 %1$s 确认强制接管");
    String COMMANDS_GACHA_EDIT_FORCE_TAKEOVER_COMPLETED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.edit_force_takeover_completed",
            "Edit session %1$s forcibly acquired until %2$s",
            "已强制取得编辑会话 %1$s，有效至 %2$s");
    String COMMANDS_GACHA_AUDIT_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.audit_header", "%s recent audit entries", "最近 %s 条审计记录");
    String COMMANDS_GACHA_AUDIT_ENTRY = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.audit_entry", "%1$s | %2$s | %3$s | %4$s",
            "%1$s | %2$s | %3$s | %4$s");
    String COMMANDS_GACHA_AUDIT_NO_TARGET = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.audit_no_target", "No target", "无目标");
    String COMMANDS_GACHA_DRAFT_STATUS = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_status",
            "Draft revision %1$s contains %2$s documents, saved at %3$s",
            "草稿修订 %1$s 包含 %2$s 个文档，保存时间 %3$s");
    String COMMANDS_GACHA_DRAFT_LIST_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_list_header",
            "Draft revision %1$s contains %2$s documents", "草稿修订 %1$s 包含 %2$s 个文档");
    String COMMANDS_GACHA_DRAFT_UNKNOWN_KIND = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_unknown_kind",
            "Unknown definition kind: %s", "未知的定义类型：%s");
    String COMMANDS_GACHA_DRAFT_INVALID = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_invalid",
            "Draft validation failed; a recovery copy was saved: %s",
            "草稿校验失败，已保存恢复副本：%s");
    String COMMANDS_GACHA_DRAFT_SAVED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_saved",
            "Saved draft revision %1$s with %2$s documents",
            "已保存草稿修订 %1$s，共 %2$s 个文档");
    String COMMANDS_GACHA_DRAFT_REMOVED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_removed",
            "Removed %1$s/%2$s; saved draft revision %3$s with %4$s documents",
            "已删除 %1$s/%2$s；保存为草稿修订 %3$s，共 %4$s 个文档");
    String COMMANDS_GACHA_DRAFT_REVISIONS_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_revisions_header",
            "%s retained draft revisions", "保留了 %s 个草稿修订");
    String COMMANDS_GACHA_DRAFT_REVISION_ENTRY = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.draft_revision_entry",
            "Revision %1$s | %2$s | %3$s | %4$s documents",
            "修订 %1$s | %2$s | %3$s | %4$s 个文档");
    String COMMANDS_GACHA_PUBLISHED_REVISIONS_HEADER = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.published_revisions_header",
            "%s retained published revisions", "保留了 %s 个已发布修订");
    String COMMANDS_GACHA_PUBLISH_PREPARED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.publish_prepared",
            "Confirm publishing with token %1$s before %2$s", "请在 %2$s 前使用令牌 %1$s 确认发布");
    String COMMANDS_GACHA_BACKUP_RESTORED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.backup_restored",
            "Loaded backup; editing revision: %s", "已读取备份，当前编辑版本：%s");
    String COMMANDS_GACHA_CARD_POOL_ENABLED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.card_pool_enabled",
            "Card pool %1$s set to %2$s; publish to apply", "卡池 %1$s 已设为 %2$s，请发布以生效");
    String COMMANDS_GACHA_PUBLISHED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.published",
            "Published revision %1$s using %2$s", "已使用 %2$s 发布修订 %1$s");
    String COMMANDS_GACHA_ROLLBACK_PREPARED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.rollback_prepared",
            "Confirm rollback with token %1$s before %2$s", "请在 %2$s 前使用令牌 %1$s 确认回滚");
    String COMMANDS_GACHA_ROLLED_BACK = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.rolled_back",
            "Rolled published revision %1$s into new revision %2$s",
            "已将发布修订 %1$s 回滚为新的修订 %2$s");
    String COMMANDS_GACHA_PUBLISH_PACK_UNAVAILABLE = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.publish_pack_unavailable",
            "The managed datapack was written but is unavailable to the pack repository",
            "受控数据包已写入，但数据包仓库无法读取它");
    String COMMANDS_GACHA_PUBLISH_RELOAD_COMPLETED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.publish_reload_completed",
            "Published definitions were reloaded", "已重载发布的定义");
    String COMMANDS_GACHA_PUBLISH_RELOAD_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.publish_reload_failed",
            "Published definitions were written but reload failed: %s",
            "发布定义已写入，但重载失败：%s");
    String COMMANDS_GACHA_PUBLISH_SCHEDULED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.publish_scheduled",
            "Published revision %s is waiting to be reloaded", "发布修订 %s 正等待重载");
    String COMMANDS_GACHA_PUBLISH_NOT_SCHEDULED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.publish_not_scheduled",
            "No published revision is waiting to be reloaded", "当前没有等待重载的发布修订");
    String COMMANDS_GACHA_RESOURCE_PACK_PREPARED = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.resource_pack_prepared",
            "Confirm resource pack sending with token %1$s before %2$s",
            "请在 %2$s 前使用令牌 %1$s 确认发送资源包");
    String COMMANDS_GACHA_RESOURCE_PACK_SENT = GachaTranslations.creates(GachaConstants.MODID,
            "commands.gachaaddiction.gacha.resource_pack_sent",
            "Sent resource pack request %1$s to %2$s players", "已向 %2$s 名玩家发送资源包请求 %1$s");
    String GUI_GACHA_DRAW_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.draw_failed",
            "The card pool draw failed. Please contact a server administrator.",
            "卡池抽取失败，请联系服务器管理员。");
    String GUI_CARD_POOL_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.title", "Card Pools", "卡池");
    String GUI_CARD_POOL_EMPTY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.empty", "No card pools are currently available",
            "当前没有可用卡池");
    String GUI_CARD_POOL_PREVIOUS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.previous", "Previous", "上一页");
    String GUI_CARD_POOL_NEXT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.next", "Next", "下一页");
    String GUI_CARD_POOL_PACKAGE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.package", "%s", "%s");
    String GUI_CARD_POOL_ROUNDS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.rounds", "Rounds", "轮数");
    String GUI_CARD_POOL_ROUNDS_INVALID = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.rounds.invalid", "Invalid rounds: %s", "轮数无效：%s");
    String GUI_CARD_POOL_CHOICE_DEFAULT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.choice.default", "Default", "默认");
    String GUI_CARD_POOL_CHOICE_VALUE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.choice.value", "%1$s: %2$s", "%1$s：%2$s");
    String GUI_CARD_POOL_CHOICE_INVALID = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.choice.invalid", "Invalid player choice: %s", "玩家选项无效：%s");
    String GUI_CARD_POOL_CONFIRM_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.title", "Confirm Draw", "确认抽取");
    String GUI_CARD_POOL_CONFIRM = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.confirm", "Confirm", "确认");
    String GUI_CARD_POOL_CANCEL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.cancel", "Cancel", "取消");
    String GUI_CARD_POOL_CONFIRM_PACKAGE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.package", "Package: %s", "抽取包：%s");
    String GUI_CARD_POOL_CONFIRM_CONSUMPTION = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.consumption", "Consumption: %s", "消耗：%s");
    String GUI_CARD_POOL_CONFIRM_NO_CONSUMPTION = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.no_consumption", "None", "无");
    String GUI_CARD_POOL_CONFIRM_STEP = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.step", "Step: %s", "步骤：%s");
    String GUI_CARD_POOL_CONFIRM_STEP_TARGET = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.step_target", "%1$s -> %2$s", "%1$s -> %2$s");
    String GUI_CARD_POOL_CONFIRM_STEP_ROUNDS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.step_rounds", "Rounds: %s", "轮数：%s");
    String GUI_CARD_POOL_CONFIRM_PROBABILITY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.probability",
            "Probability %1$s | mode=%2$s | count=%3$s | %4$s",
            "概率 %1$s | 模式=%2$s | 数量=%3$s | %4$s");
    String GUI_CARD_POOL_CONFIRM_MORE_PROBABILITIES = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.more_probabilities",
            "And %s more probability descriptions", "另有 %s 个概率结构说明");
    String GUI_CARD_POOL_CONFIRM_MORE_STEPS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.more_steps", "And %s more steps", "另有 %s 个步骤");
    String GUI_CARD_POOL_CONFIRM_PREVIEW_UNCERTAIN = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.preview_uncertain",
            "Some values depend on current state and may change", "部分数值取决于当前状态，确认时可能变化");
    String GUI_CARD_POOL_CONFIRM_PREVIEW_UNSUPPORTED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.preview_unsupported",
            "Some custom steps cannot be previewed", "部分自定义步骤无法预览");
    String GUI_CARD_POOL_CONFIRM_EXPIRES = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.confirm.expires", "Expires: %s", "过期时间：%s");
    String GUI_CARD_POOL_RESULT_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.result.title", "Draw Results", "抽取结果");
    String GUI_CARD_POOL_RESULT_CONTEXT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.result.context",
            "%1$s / %2$s / transaction %3$s", "%1$s / %2$s / 事务 %3$s");
    String GUI_CARD_POOL_RESULT_DONE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.card_pool.result.done", "Done", "完成");
    String GUI_REWARD_INBOX_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.reward_inbox.title", "Reward Inbox", "奖励收件箱");
    String GUI_REWARD_INBOX_EMPTY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.reward_inbox.empty", "The reward inbox is empty", "奖励收件箱为空");
    String GUI_REWARD_INBOX_ENTRY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.reward_inbox.entry", "%1$s: %2$s - %3$s stacks", "%1$s：%2$s - %3$s 组");
    String GUI_REWARD_INBOX_KIND_REWARD = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.reward_inbox.kind.reward", "Reward", "奖励");
    String GUI_REWARD_INBOX_KIND_RETURN = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.reward_inbox.kind.return", "Return", "返还");
    String GUI_REWARD_INBOX_CLAIM = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.reward_inbox.claim", "Claim selected rewards", "领取选中奖励");
    //endregion

    //region 外部约定键
    @SuppressWarnings("unused")
    String FTBQUESTS_REWARD_FTBQUESTS_LOOT_GACHA_TYPE = GachaTranslations.creates(GachaConstants.MODID, "ftbquests.reward.ftbquests.loot.gachaaddiction:gacha_type",
            "Gacha Style", "抽卡样式");
    //endregion

    //region 抽奖类型
    @SuppressWarnings("unused")
    String GACHA_TYPE_EMPTY = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:empty",
            "None", "无");
    @SuppressWarnings("unused")
    String GACHA_TYPE_SLOT_MACHINE = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:slot_machine",
            "Slot Machine", "老虎机");
    @SuppressWarnings("unused")
    String GACHA_TYPE_INTERACTIVE_SLOT_MACHINE = GachaTranslations.creates(GachaConstants.MODID,
            "gacha_type.gachaaddiction:interactive_slot_machine", "Interactive Slot Machine", "交互式老虎机");
    @SuppressWarnings("unused")
    String GACHA_TYPE_MODERN = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:modern",
            "Modern", "现代");
    @SuppressWarnings("unused")
    String GACHA_TYPE_MODERN_2 = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:modern_2",
            "Modern 2", "现代2");
    @SuppressWarnings("unused")
    String GACHA_TYPE_LDLIB2 = GachaTranslations.creates(GachaConstants.MODID, "gacha_type.gachaaddiction:ldlib2",
            "LDLib2 Gacha Interface (requires LDLib2)", "LDLib2 抽卡界面（需要 LDLib2）");
    //endregion

    String GUI_MODERN_REVEAL_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.reveal_hint",
            "Click to reveal · Space to skip reveals · Shift to view all", "点击揭晓 · 空格跳过揭晓 · Shift 查看全部");
    String GUI_MODERN_NEXT_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.next_hint",
            "Click for next · Space to skip reveals · Shift to view all", "点击下一件 · 空格跳过揭晓 · Shift 查看全部");
    String GUI_MODERN_COUNT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.count",
            "× %s", "× %s");
    String GUI_MODERN_SUMMARY = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.summary",
            "Rewards obtained", "本次获得");
    String GUI_MODERN_SUMMARY_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.summary_hint",
            "Click to continue", "点击继续");
    String GUI_MODERN_SUMMARY_REVEAL_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.summary_reveal_hint",
            "Click to speed up · Space to reveal all", "点击加速 · 空格显示全部");
    String GUI_MODERN_SUMMARY_SCROLL_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.modern.summary_scroll_hint",
            "Scroll to view rewards · Click to continue", "滚轮查看奖励 · 点击继续");

    //region 配置
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen"),
            "Gacha Screen", "抽奖屏幕");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_BLACKLIST_FILTER_LIST = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "blacklist_filter_list"),
            "Blacklist Filter", "黑名单过滤");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_BLACKLIST_FILTER_LIST_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "blacklist_filter_list", "tooltip"),
            "Items matching this list are filtered out. Use ; for compound conditions, #tag/!#tag for tags, rarity comparisons, and vanilla item syntax with optional ! exclusion.",
            "匹配此列表的物品将被过滤。使用 ; 组合条件，使用 #标签/!#标签匹配或排除标签，支持稀有度比较和带可选 ! 排除的原版物品格式。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_DEFAULT_GACHAAS_TYPE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "default_gachaas_type"),
            "Default Gacha Screen Type", "默认抽奖屏幕类型");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_DEFAULT_GACHAAS_TYPE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "default_gachaas_type", "tooltip"),
            "Used when the gacha data does not specify a screen type.", "当抽奖数据没有指定屏幕类型时，使用该类型。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_FTB_QUESTS_GACHAA = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ftb_quests_gachaa"),
            "Enable Gacha Screen for FTB Quest Rewards", "开启FTB任务抽奖的抽奖屏幕");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_FTB_QUESTS_GACHAA_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ftb_quests_gachaa", "tooltip"),
            "Open the gacha screen for FTB quest rewards even when the quest does not enable it.", "开启后，即使任务未开启也会为 FTB 任务奖励打开抽奖界面。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_LDLIB2 = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ldlib2"),
            "LDLib2 Gacha Interface (requires LDLib2)", "LDLib2 抽卡界面（需要 LDLib2）");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_LDLIB2_TEMPLATE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ldlib2", "template"),
            "UI Template", "界面模板");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_LDLIB2_TEMPLATE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ldlib2", "template", "tooltip"),
            "Client resource-pack template ID used only when the LDLib2 gacha interface is selected. Requires LDLib2.",
            "仅在选用 LDLib2 抽卡界面时使用的客户端资源包模板 ID。需要 LDLib2。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_LDLIB2_STYLE_SHEET = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ldlib2", "style_sheet"),
            "LSS Stylesheet", "LSS 样式表");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_LDLIB2_STYLE_SHEET_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "ldlib2", "style_sheet", "tooltip"),
            "Client resource-pack LSS stylesheet ID for the LDLib2 gacha interface. Requires LDLib2.",
            "LDLib2 抽卡界面使用的客户端资源包 LSS 样式表 ID。需要 LDLib2。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_MERGE_ITEM = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "merge_item"),
            "Merge Duplicate Rewards", "合并抽奖物品");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_MERGE_ITEM_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "merge_item", "tooltip"),
            "Display identical reward items together in the same slot.", "开启后，同种物品会合并显示在同一个槽位中。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_RARITY_SORTING = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "rarity_sorting"),
            "Sort by Rarity", "稀有度排序");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_RARITY_SORTING_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "rarity_sorting", "tooltip"),
            "Sort rewards by rarity from highest to lowest.", "按稀有度从高到低排序奖品。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "reward_sound_effects"),
            "Reward Sound Effects", "奖品音效");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "reward_sound_effects", "tooltip"),
            "Play a sound when a reward is drawn.", "抽到奖品时播放音效。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS_OPTIMIZE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "reward_sound_effects_optimize"),
            "Reward Sound Effect Optimization", "奖品音效优化");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_REWARD_SOUND_EFFECTS_OPTIMIZE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "reward_sound_effects_optimize", "tooltip"),
            "Avoid overlapping reward sounds by only playing the highest rarity sound.", "优化奖品音效播放，避免多个音效叠加，只播放最高稀有度的。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine"),
            "Slot Machine", "老虎机");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone"),
            "Deceleration", "减速");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone"),
            "Deceleration Zone Distance", "滚动减速区距离");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone", "tooltip"),
            "Start decelerating when this many items remain before the result.", "距离结果还剩几个物品时开始减速。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MAX = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_max"),
            "Max Random Deceleration Multiplier", "随机减速倍率范围最高");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MAX_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_max", "tooltip"),
            "Upper bound of the random deceleration multiplier.", "随机减速倍率范围上限，让减速效果带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MIN = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_min"),
            "Min Random Deceleration Multiplier", "随机减速倍率范围最低");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_DECEL_ZONE_VARIANCE_MIN_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "decel_zone_variance_min", "tooltip"),
            "Lower bound of the random deceleration multiplier.", "随机减速倍率范围下限，让减速效果带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_MIN_SPEED = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "min_speed"),
            "Minimum Speed Multiplier in Deceleration Phase", "减速阶段最低速度倍率");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_DECEL_ZONE_MIN_SPEED_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "decel_zone", "min_speed", "tooltip"),
            "Minimum speed multiplier during deceleration to prevent the reel from stopping completely.", "减速阶段最低速度倍率，防止转盘完全停住。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_MAX_SCALE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "exit_max_scale"),
            "Exit Animation Max Scale", "退出动画最大放大倍率");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_MAX_SCALE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "exit_max_scale", "tooltip"),
            "Maximum exit animation scale increase (actual scale is 1 plus this value).", "退出动画最大放大倍率（实际为 1 + 此值）。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_SPEED = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "exit_speed"),
            "Exit Animation Progress Per Tick", "退出动画每tick进度推进量");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_EXIT_SPEED_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "exit_speed", "tooltip"),
            "Exit animation progress per tick. Higher values exit faster.", "退出动画每 tick 的进度推进量，越大退出越快。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item"),
            "Item", "物品");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEM_SPACING = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "item_spacing"),
            "Item Vertical Spacing", "物品垂直间距");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEM_SPACING_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "item_spacing", "tooltip"),
            "Vertical spacing between items in pixels.", "物品之间的垂直间距（像素）。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEMS_PER_SECOND = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "items_per_second"),
            "Items Per Second", "每秒经过的物品数量");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_ITEMS_PER_SECOND_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "items_per_second", "tooltip"),
            "Number of items passing per second. Higher values scroll faster.", "转盘每秒经过的物品数量，越大滚动越快。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MAX_ALPHA = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "max_alpha"),
            "Max Item Opacity", "物品最大不透明度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MAX_ALPHA_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "max_alpha", "tooltip"),
            "Maximum opacity of items near the center. 0 is fully transparent; 1 is fully opaque.", "靠近中心的物品的最大不透明度。0 为完全透明，1 为完全不透明。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MIN_ALPHA = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "min_alpha"),
            "Min Item Opacity", "物品最小不透明度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_MIN_ALPHA_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "min_alpha", "tooltip"),
            "Minimum opacity of items far from the center. 0 is fully transparent; 1 is fully opaque.", "远离中心的物品的最小不透明度。0 为完全透明，1 为完全不透明。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_SLOT_SIZE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "slot_size"),
            "Slot Size", "转盘槽位大小");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_SLOT_SIZE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "slot_size", "tooltip"),
            "Reel slot size used to calculate the decoy count range and result insertion position.", "转盘槽位大小，是 decoy 生成数量范围和 result 插入位置的计算基准。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_VISIBLE_COUNT = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "visible_count"),
            "Visible Item Count", "可见物品数量");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_ITEM_VISIBLE_COUNT_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "item", "visible_count", "tooltip"),
            "Number of items visible on the reel at once.", "转盘上同时可见的物品数量。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects"),
            "Tick Sound Effects", "拨片音效");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects", "tooltip"),
            "Play a ticking sound as items scroll past.", "物品滚过时播放拨片音效。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS_OPTIMIZE = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects_optimize"),
            "Tick Sound Effect Optimization", "拨片音效优化");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_PASS_SOUND_EFFECTS_OPTIMIZE_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "pass_sound_effects_optimize", "tooltip"),
            "Only play the ticking sound from one reel at a time.", "开启后同时只出现一条滚动条的拨片音效。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed"),
            "Speed", "速度");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MAX = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_max"),
            "Max Random Minimum Speed Multiplier", "随机最小速度倍率范围最高");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MAX_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_max", "tooltip"),
            "Upper bound of the random minimum speed multiplier.", "随机最小速度倍率范围上限。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MIN = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_min"),
            "Min Random Minimum Speed Multiplier", "随机最小速度倍率范围最低");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_MIN_SPEED_VARIANCE_MIN_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "min_speed_variance_min", "tooltip"),
            "Lower bound of the random minimum speed multiplier.", "随机最小速度倍率范围下限。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MAX = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_max"),
            "Max Random Speed Multiplier", "随机速度倍率范围最高");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MAX_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_max", "tooltip"),
            "Upper bound of the random speed multiplier.", "随机速度倍率范围上限，让滚动速度带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MIN = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_min"),
            "Min Random Speed Multiplier", "随机速度倍率范围最低");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_SLOT_MACHINE_SPEED_SPEED_VARIANCE_MIN_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "slot_machine", "speed", "speed_variance_min", "tooltip"),
            "Lower bound of the random speed multiplier.", "随机速度倍率范围下限，让滚动速度带随机性。");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_WHITELIST_FILTER_LIST = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "whitelist_filter_list"),
            "Whitelist Filter", "白名单过滤");
    @SuppressWarnings("unused")
    String CONFIG_GACHAA_SCREEN_WHITELIST_FILTER_LIST_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "gachaa_screen", "whitelist_filter_list", "tooltip"),
            "Items matching this list take priority over the blacklist. Use ; for compound conditions, #tag/!#tag for tags, rarity comparisons, and vanilla item syntax with optional ! exclusion.",
            "匹配此列表的物品优先于黑名单。使用 ; 组合条件，使用 #标签/!#标签匹配或排除标签，支持稀有度比较和带可选 ! 排除的原版物品格式。");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin"),
            "Administration", "管理");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN_EDIT_LEASE_SECONDS = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin", "edit_lease_seconds"),
            "Edit Lease Duration (seconds)", "编辑锁租期（秒）");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN_EDIT_LEASE_SECONDS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin", "edit_lease_seconds", "tooltip"),
            "Duration in seconds of a renewable exclusive lease for editing definitions.", "独占编辑定义的可续期编辑锁租期，单位为秒。");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN_EDIT_DISCONNECT_GRACE_SECONDS = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin", "edit_disconnect_grace_seconds"),
            "Edit Disconnect Grace Period (seconds)", "编辑断线宽限期（秒）");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN_EDIT_DISCONNECT_GRACE_SECONDS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin", "edit_disconnect_grace_seconds", "tooltip"),
            "Maximum grace period in seconds for the edit lease after the editor disconnects.", "编辑者断线后编辑锁的最长宽限期，单位为秒。");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN_MAXIMUM_DRAFT_SNAPSHOT_BYTES = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin", "maximum_draft_snapshot_bytes"),
            "Maximum Draft Snapshot Size (bytes)", "草稿快照大小上限（字节）");
    @SuppressWarnings("unused")
    String CONFIG_ADMIN_MAXIMUM_DRAFT_SNAPSHOT_BYTES_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "admin", "maximum_draft_snapshot_bytes", "tooltip"),
            "Maximum accepted encoded size in bytes of one draft snapshot.", "单份草稿快照可接受的编码后大小上限，单位为字节。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction"),
            "Transactions", "事务");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_WAL_PAYLOAD_BYTES = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_wal_payload_bytes"),
            "Maximum WAL Payload Size (bytes)", "预写日志载荷大小上限（字节）");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_WAL_PAYLOAD_BYTES_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_wal_payload_bytes", "tooltip"),
            "Maximum accepted encoded payload size in bytes of one write-ahead log (WAL) frame.", "单帧预写日志（WAL）可接受的编码后载荷大小上限，单位为字节。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_SNAPSHOT_BYTES = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_snapshot_bytes"),
            "Maximum Archived Snapshot Size (bytes)", "存档卡池快照大小上限（字节）");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_SNAPSHOT_BYTES_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_snapshot_bytes", "tooltip"),
            "Maximum accepted encoded size in bytes of one archived card pool snapshot.", "单份存档卡池快照可接受的编码后大小上限，单位为字节。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_INBOX_ENTRY_BYTES = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_inbox_entry_bytes"),
            "Maximum Reward Inbox Entry Size (bytes)", "奖励收件箱条目大小上限（字节）");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_INBOX_ENTRY_BYTES_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_inbox_entry_bytes", "tooltip"),
            "Maximum accepted encoded size in bytes of one reward inbox entry.", "单条奖励收件箱条目可接受的编码后大小上限，单位为字节。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_PARAMETER_STATE_BYTES = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_parameter_state_bytes"),
            "Maximum Persistent Parameter State Size (bytes)", "持久化参数状态大小上限（字节）");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_MAXIMUM_PARAMETER_STATE_BYTES_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "maximum_parameter_state_bytes", "tooltip"),
            "Maximum accepted encoded size in bytes of persistent parameter state.", "持久化参数状态可接受的编码后大小上限，单位为字节。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_GROUP_COMMIT_DELAY_MILLIS = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "group_commit_delay_millis"),
            "Group Commit Delay (milliseconds)", "合并刷盘延迟（毫秒）");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_GROUP_COMMIT_DELAY_MILLIS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "group_commit_delay_millis", "tooltip"),
            "Maximum delay in milliseconds before a grouped write-ahead log (WAL) flush.", "预写日志（WAL）合并刷盘前的最长延迟，单位为毫秒。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_RECOVERY_DURABILITY = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "recovery_durability"),
            "Recovery Durability", "恢复流程持久化方式");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_RECOVERY_DURABILITY_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "recovery_durability", "tooltip"),
            "Durability used when recovery appends a terminal write-ahead log (WAL) frame.", "恢复流程追加事务终态预写日志（WAL）帧时采用的持久化方式。");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_PUBLIC_DRAW_DURABILITY = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "public_draw_durability"),
            "Public Draw Minimum Durability", "公开抽奖最低持久化级别");
    @SuppressWarnings("unused")
    String CONFIG_TRANSACTION_PUBLIC_DRAW_DURABILITY_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "transaction", "public_draw_durability", "tooltip"),
            "Minimum durability requested by the public card pool endpoint.", "公开卡池入口请求的最低持久化级别。");
    @SuppressWarnings("unused")
    String CONFIG_CARD_POOL = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "card_pool"),
            "Card Pools", "卡池");
    @SuppressWarnings("unused")
    String CONFIG_CARD_POOL_PUBLIC_DRAW_CONFIRMATION_LIFETIME_SECONDS = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "card_pool", "public_draw_confirmation_lifetime_seconds"),
            "Public Draw Confirmation Lifetime (seconds)", "公开抽奖确认有效期（秒）");
    @SuppressWarnings("unused")
    String CONFIG_CARD_POOL_PUBLIC_DRAW_CONFIRMATION_LIFETIME_SECONDS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "card_pool", "public_draw_confirmation_lifetime_seconds", "tooltip"),
            "Lifetime in seconds of a prepared public card pool draw before confirmation.", "已准备的公开卡池抽奖在确认前的有效期，单位为秒。");
    String CONFIG_DURABILITY_SYNC = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "durability", "sync"),
            "Synchronous Flush", "同步刷盘");
    String CONFIG_DURABILITY_GROUP_COMMIT = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "durability", "group_commit"),
            "Grouped Flush", "合并刷盘");
    String CONFIG_DURABILITY_ASYNC = GachaTranslations.creates(GachaConstants.MODID, GachaTranslations.getConfigTranslation(GachaConstants.MODID, "durability", "async"),
            "Asynchronous Flush", "异步刷盘");
    //endregion

    //region 配置界面
    String GUI_CONFIG_EMPTY = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.empty",
            "No configuration available", "暂无配置");
    String GUI_CONFIG_TITLE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.title",
            "GachaAddiction Configuration", "GachaAddiction 配置");
    String GUI_CONFIG_LDLIB2_TITLE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.ldlib2_title",
            "GachaAddiction Configuration (LDLib2)", "GachaAddiction 配置（LDLib2）");
    String GUI_CONFIG_PREVIEW = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.preview",
            "Gacha preview (LDLib2)", "抽卡预览（LDLib2）");
    String GUI_CONFIG_PREVIEW_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.preview_hint",
            "Local simulation only. No items, pity state or server data are changed.", "仅本地模拟，不消耗物品、不更新保底、不修改服务端数据。");
    String GUI_CONFIG_PREVIEW_REPLAY = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.preview_replay",
            "Replay", "重新播放");
    String GUI_CONFIG_FINISH = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.finish",
            "Done", "完成");
    String GUI_CONFIG_UNSUPPORTED = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.unsupported",
            "Unsupported configuration type: %s", "暂不支持此配置类型：%s");
    String GUI_CONFIG_CONFIRM = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.confirm",
            "Confirm", "确认");
    String GUI_CONFIG_CANCEL = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.cancel",
            "Cancel", "取消");
    String GUI_CONFIG_LIST_INVALID = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.list_invalid",
            "Complete or remove invalid entries before confirming", "确认前请补全或删除无效条目");
    String GUI_CONFIG_FILTER_RULE_TITLE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.filter_rule_title",
            "Filter Conditions", "过滤条件");
    String GUI_CONFIG_FILTER_RULE_EMPTY = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.filter_rule_empty",
            "Add at least one filter condition", "请至少添加一个过滤条件");
    String GUI_CONFIG_FILTER_CONDITION_EMPTY = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.filter_condition_empty",
            "Enter one filter condition. Item components use component=value or !component. Add compound conditions as separate entries in the same rule. Examples:\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs",
            "请输入一个过滤条件。物品组件使用 组件=值 或 !组件；同一规则的复合条件请分别添加为多个条目。格式示例：\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs");
    String GUI_CONFIG_FILTER_CONDITION_INVALID = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.config.filter_condition_invalid",
            "Invalid filter condition. Item components require component=value or !component. Add compound conditions as separate entries in the same rule. Examples:\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs",
            "过滤条件格式错误。物品组件必须使用 组件=值 或 !组件；同一规则的复合条件请分别添加为多个条目。示例：\nminecraft:diamond\nminecraft:flow_banner_pattern[minecraft:custom_data={adadada:{adda:1b}}]\n#minecraft:logs\nrarity>=2\n!minecraft:stone\n!#minecraft:logs");
    //endregion

    //region 抽卡布局设计器
    String GUI_GACHA_DESIGNER_TITLE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.title",
            "Gacha layout designer", "抽卡布局设计器");
    String GUI_GACHA_DESIGNER_REQUIRES_LDLIB = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.requires_ldlib",
            "Requires LDLib2; local visual editing only", "需要 LDLib2；仅本地视觉编辑");
    String GUI_GACHA_DESIGNER_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.hint",
            "Edit display components and styles. Fixed draw controls keep their safe runtime bindings.", "编辑展示组件与样式；固定抽卡控件保留安全业务绑定。");
    String GUI_GACHA_DESIGNER_OPEN = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.open",
            "Design gacha layout", "设计抽卡布局");
    String GUI_GACHA_DESIGNER_FILE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.file",
            "File name (.ui.nbt), inside ldlib2/assets/gachaaddiction/ui", "文件名（.ui.nbt），位于 ldlib2/assets/gachaaddiction/ui 内");
    String GUI_GACHA_DESIGNER_IMPORT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.import",
            "Import", "导入");
    String GUI_GACHA_DESIGNER_EXPORT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.export",
            "Export", "导出");
    String GUI_GACHA_DESIGNER_RESET = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.reset",
            "Restore default layout", "恢复默认布局");
    String GUI_GACHA_DESIGNER_ADD_PANEL = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.add_panel",
            "Add image/container", "添加图片/容器");
    String GUI_GACHA_DESIGNER_ADD_TEXT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.add_text",
            "Add text", "添加文本");
    String GUI_GACHA_DESIGNER_ADD_ITEM = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.add_item",
            "Add read-only item", "添加只读物品");
    String GUI_GACHA_DESIGNER_REMOVE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.remove",
            "Remove display component", "删除展示组件");
    String GUI_GACHA_DESIGNER_PREVIEW = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.preview",
            "Simulate preview", "模拟预览");
    String GUI_GACHA_DESIGNER_SIMULATED = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.simulated",
            "Local preview; no draw or reward delivery occurs", "本地预览；不会抽奖或发放奖励");
    String GUI_GACHA_DESIGNER_INVALID = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.invalid",
            "Layout could not be used: %s", "无法使用布局：%s");
    String GUI_GACHA_DESIGNER_LOADED = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.loaded",
            "Imported %s", "已导入 %s");
    String GUI_GACHA_DESIGNER_SAVED = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.saved",
            "Exported %s", "已导出 %s");
    String GUI_GACHA_DESIGNER_OVERWRITE = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.overwrite",
            "Overwrite this local layout file: %s?", "覆盖这个本地布局文件：%s？");
    String GUI_GACHA_DESIGNER_DISCARD = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.designer.discard",
            "Discard unsaved layout changes?", "放弃未保存的布局修改？");
    //endregion

    //region 管理工作台
    String GUI_ADMIN_WORKBENCH_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.title", "Gacha Administration", "抽奖管理工作台");
    String GUI_ADMIN_WORKBENCH_EDIT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.edit", "Edit", "编辑");
    String GUI_ADMIN_WORKBENCH_PUBLISH = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish", "Publish", "发布");
    String GUI_ADMIN_WORKBENCH_CARD_POOLS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pools", "Card pools", "卡池管理");
    String GUI_ADMIN_WORKBENCH_PUBLISH_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_hint",
            "Validate the edited definitions and write the datapack. Choose whether to reload it now.",
            "校验编辑内容并写入数据包，可选择是否立即重载。");
    String GUI_ADMIN_WORKBENCH_CARD_POOLS_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pools_hint",
            "Select a card pool to view its linked plan, public settings and enabled state.",
            "选择卡池，查看关联计划、公开配置和启用状态。");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_hint",
            "Send an external resource pack through Minecraft's resource pack request.",
            "填写外部资源包地址，通过原版资源包请求发送给玩家。");
    String GUI_ADMIN_WORKBENCH_RESTORE_BACKUP = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.restore_backup", "Load backup", "读取备份");
    String GUI_ADMIN_WORKBENCH_RESTORE_BACKUP_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.restore_backup_hint",
            "Replace the current editing content with the backup. Publish it when ready.",
            "使用备份替换当前编辑内容，检查完成后再发布。");
    String GUI_ADMIN_WORKBENCH_OVERVIEW = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.overview", "Overview", "总览");
    String GUI_ADMIN_WORKBENCH_READ_ONLY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.read_only", "Read-only overview", "只读总览");
    String GUI_ADMIN_WORKBENCH_CLOSE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.close", "Close", "关闭");
    String GUI_ADMIN_WORKBENCH_DRAFT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.draft", "Draft revision %1$s, %2$s documents",
            "草稿修订 %1$s，%2$s 个文档");
    String GUI_ADMIN_WORKBENCH_PUBLISHED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.published", "Published revision %1$s, %2$s documents",
            "已发布修订 %1$s，%2$s 个文档");
    String GUI_ADMIN_WORKBENCH_PUBLISHED_EMPTY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.published_empty", "No published revision", "暂无已发布修订");
    String GUI_ADMIN_WORKBENCH_RELOAD_PENDING = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.reload_pending", "Reload pending for revision %s",
            "修订 %s 正在等待重载");
    String GUI_ADMIN_WORKBENCH_RELOAD_NONE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.reload_none", "No reload is pending", "当前没有待执行重载");
    String GUI_ADMIN_WORKBENCH_APPLY_SCHEDULED_RELOAD = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.apply_scheduled_reload",
            "Apply scheduled reload", "执行计划重载");
    String GUI_ADMIN_WORKBENCH_EDITOR = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.editor",
            "Editor %1$s, expires %2$s",
            "编辑者 %1$s，有效至 %2$s");
    String GUI_ADMIN_WORKBENCH_EDITOR_NONE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.editor_none", "No active editor", "当前没有编辑者");
    String GUI_ADMIN_WORKBENCH_REFRESH = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.refresh", "Refresh", "刷新");
    String GUI_ADMIN_WORKBENCH_ACQUIRE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.acquire", "Acquire edit session", "取得编辑权");
    String GUI_ADMIN_WORKBENCH_HEARTBEAT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.heartbeat", "Renew edit session", "续期编辑权");
    String GUI_ADMIN_WORKBENCH_RELEASE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.release", "Release edit session", "释放编辑权");
    String GUI_ADMIN_WORKBENCH_YES = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.yes", "Yes", "是");
    String GUI_ADMIN_WORKBENCH_NO = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.no", "No", "否");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_LINK = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_link",
            "Linked %1$s: %2$s", "关联 %1$s：%2$s");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_title",
            "Public title: %s", "公开标题：%s");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_ICON = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_icon",
            "Icon: %s", "图标：%s");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_PACKAGES = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_packages",
            "Draw packages: %s", "抽取包：%s");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_ENABLED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_enabled", "Enabled", "已启用");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_DISABLED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_disabled", "Disabled", "已停用");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_ENABLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_enable", "Enable card pool", "启用卡池");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_DISABLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_disable", "Disable card pool", "停用卡池");
    String GUI_ADMIN_WORKBENCH_CARD_POOL_PUBLIC_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.card_pool_public_hint",
            "Edit the linked plan or reward pool and public presentation in the document, then save and publish.",
            "在文档中修改关联计划或奖励池和公开展示配置，保存后发布。");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_SAVE_FIRST = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_save_first",
            "Save the edited content before changing the enabled state.", "请先保存编辑内容，再修改启用状态。");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_UNSAVED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_unsaved", "Unsaved changes", "存在未保存的修改");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_LEAVE_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_leave_hint",
            "Save before continuing? Choose No to discard changes, or Cancel to keep editing.",
            "是否保存后继续？选择“否”放弃修改，选择“取消”继续编辑。");
    String GUI_ADMIN_WORKBENCH_DOCUMENT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document", "Draft document", "草稿文档");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_title", "Draft document: %s", "草稿文档：%s");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_READ_ONLY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_read_only", "Read-only document view", "只读文档查看");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_EDITABLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_editable", "Editing draft document", "正在编辑草稿文档");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_SAVE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_save", "Validate and save", "校验并保存");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_INVALID = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_invalid", "Validation failed: %s", "校验失败：%s");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_DELETE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_delete", "Delete document", "删除文档");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_DELETE_CONFIRM = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_delete_confirm",
            "Click again to confirm deletion", "再次点击以确认删除");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_DUPLICATE_ID = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_duplicate_id",
            "New resource location", "新资源位置");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_DUPLICATE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_duplicate", "Duplicate document", "复制文档");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_RENAME = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_rename", "Rename document and update references", "重命名文档并更新引用");
    String GUI_ADMIN_WORKBENCH_DOCUMENT_RENAMED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.document_renamed", "Renamed %1$s to %2$s", "已将 %1$s 重命名为 %2$s");
    String GUI_ADMIN_WORKBENCH_RENAME_UNAVAILABLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.rename_unavailable", "This connection does not support document renaming.", "当前连接不支持文档重命名。");
    String GUI_ADMIN_WORKBENCH_RENAME_UNSAFE_REFERENCE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.rename_unsafe_reference", "Cannot safely update an extension or ambiguous reference in %s.", "无法安全更新 %s 中的扩展或歧义引用。");
    String GUI_ADMIN_WORKBENCH_QUICK_CREATE_TABLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_create_table",
            "Quick-create custom loot table", "快速创建自定义战利品表");
    String GUI_ADMIN_WORKBENCH_QUICK_CREATE_REWARD_POOL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_create_reward_pool",
            "Quick-create reward pool", "快速创建奖励池");
    String GUI_ADMIN_WORKBENCH_QUICK_CREATE_EXECUTION_PLAN = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_create_execution_plan",
            "Quick-create execution plan", "快速创建执行计划");
    String GUI_ADMIN_WORKBENCH_QUICK_CREATE_CARD_POOL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_create_card_pool",
            "Quick-create public card pool", "快速创建公开卡池");
    String GUI_ADMIN_WORKBENCH_QUICK_CREATE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_create", "Create template", "创建模板");
    String GUI_ADMIN_WORKBENCH_QUICK_ID = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_id", "New resource location", "新资源位置");
    String GUI_ADMIN_WORKBENCH_QUICK_TARGET_TABLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_target_table", "Existing custom loot table",
            "现有自定义战利品表");
    String GUI_ADMIN_WORKBENCH_QUICK_PLAN = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_plan", "Execution plan resource location",
            "执行计划资源位置");
    String GUI_ADMIN_WORKBENCH_QUICK_ROUNDS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_rounds", "Test rounds", "测试轮数");
    String GUI_ADMIN_WORKBENCH_QUICK_COPY_TEST_COMMAND = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_copy_test_command",
            "Copy execution-plan test command", "复制执行计划测试命令");
    String GUI_ADMIN_WORKBENCH_QUICK_COPY_TEST_COMMAND_BUTTON = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_copy_test_command_button",
            "Copy command", "复制命令");
    String GUI_ADMIN_WORKBENCH_QUICK_COPY_PROBABILITY_COMMAND = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_copy_probability_command",
            "Copy probability description command", "复制概率结构说明命令");
    String GUI_ADMIN_WORKBENCH_QUICK_PROBABILITY_KIND = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_probability_kind",
            "Probability kind (reward_pool or table)", "概率类型（reward_pool 或 table）");
    String GUI_ADMIN_WORKBENCH_QUICK_COPY_PROBABILITY_COMMAND_BUTTON = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.quick_copy_probability_command_button",
            "Copy probability command", "复制概率命令");
    String GUI_ADMIN_WORKBENCH_PUBLISH_ONLY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_only", "Publish without reload", "发布但不重载");
    String GUI_ADMIN_WORKBENCH_PUBLISH_SCHEDULE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_schedule", "Publish and schedule reload", "发布并计划重载");
    String GUI_ADMIN_WORKBENCH_PUBLISH_RELOAD = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_reload", "Publish and reload now", "发布并立即重载");
    String GUI_ADMIN_WORKBENCH_PUBLISH_CONFIRM_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_confirm_title", "Confirm publication", "确认发布");
    String GUI_ADMIN_WORKBENCH_PUBLISH_REVISION = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_revision", "Draft revision: %s", "草稿修订：%s");
    String GUI_ADMIN_WORKBENCH_PUBLISH_STRATEGY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.publish_strategy", "Publication strategy: %s", "发布策略：%s");
    String GUI_ADMIN_WORKBENCH_CONFIRM_EXPIRES = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.confirm_expires", "Confirmation expires: %s", "确认令牌到期时间：%s");
    String GUI_ADMIN_WORKBENCH_CONFIRM = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.confirm", "Confirm", "确认");
    String GUI_ADMIN_WORKBENCH_CANCEL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.cancel", "Cancel", "取消");
    String GUI_ADMIN_WORKBENCH_PUBLISHED_HISTORY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.published_history", "Published history", "已发布历史");
    String GUI_ADMIN_WORKBENCH_REVISION_ENTRY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.revision_entry",
            "Revision %1$s | %2$s | %3$s | %4$s documents",
            "修订 %1$s | %2$s | %3$s | %4$s 个文档");
    String GUI_ADMIN_WORKBENCH_ROLLBACK_CONFIRM_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.rollback_confirm_title", "Confirm rollback", "确认回滚");
    String GUI_ADMIN_WORKBENCH_ROLLBACK_REVISION = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.rollback_revision",
            "Restore published revision: %s", "恢复已发布修订：%s");
    String GUI_ADMIN_WORKBENCH_ROLLBACK_EFFECT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.rollback_effect",
            "Rollback creates a new published revision and schedules a reload",
            "回滚会创建新的已发布修订并计划重载");
    String GUI_ADMIN_WORKBENCH_AUDIT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.audit", "Audit log", "审计记录");
    String GUI_ADMIN_WORKBENCH_AUDIT_ENTRY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.audit_entry",
            "%1$s | %2$s | %3$s | target=%4$s | %5$s",
            "%1$s | %2$s | %3$s | 目标=%4$s | %5$s");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack", "Send resource pack", "发送资源包");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_title",
            "External resource pack", "外部资源包");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_TARGETS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_targets",
            "Player name or Minecraft selector (for example @a)", "玩家名或原版选择器（例如 @a）");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_URL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_url", "Download URL", "下载地址");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_SHA1 = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_sha1", "SHA-1", "SHA-1");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_REQUIRED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_required", "Required", "必须接受");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_PREPARE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_prepare",
            "Review resource pack request", "检查资源包请求");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_TARGET_COUNT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_target_count",
            "Target players: %s", "目标玩家：%s");
    String GUI_ADMIN_WORKBENCH_RESOURCE_PACK_REQUIRED_VALUE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.resource_pack_required_value",
            "Required: %s", "必须接受：%s");
    String GUI_ADMIN_WORKBENCH_BACK = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin_workbench.back", "Back", "返回");
    String GUI_ADMIN_JSON_VISUAL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.visual", "Visual editor", "可视化编辑");
    String GUI_ADMIN_JSON_RAW = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.raw", "JSON", "JSON");
    String GUI_ADMIN_JSON_FORM = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.form", "Form", "业务表单");
    String GUI_ADMIN_JSON_TREE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.tree", "Structure tree", "结构树");
    String GUI_ADMIN_JSON_FIELD = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.field", "Field: %s", "字段：%s");
    String GUI_ADMIN_JSON_MISSING = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.missing", "Not present", "尚未设置");
    String GUI_ADMIN_JSON_EDIT_STRUCTURE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.edit_structure", "Edit structure", "编辑结构");
    String GUI_ADMIN_JSON_TYPE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.type", "Value type", "值类型");
    String GUI_ADMIN_JSON_OBJECT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.object", "Object", "对象");
    String GUI_ADMIN_JSON_ARRAY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.array", "Array", "数组");
    String GUI_ADMIN_JSON_STRING = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.string", "String", "字符串");
    String GUI_ADMIN_JSON_NUMBER = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.number", "Number", "数字");
    String GUI_ADMIN_JSON_BOOLEAN = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.boolean", "Boolean", "布尔值");
    String GUI_ADMIN_JSON_NULL = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.null", "Null", "空值");
    String GUI_ADMIN_JSON_KEY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.key", "Object key", "对象键名");
    String GUI_ADMIN_JSON_VALUE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.value", "Value", "值");
    String GUI_ADMIN_JSON_ADD = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.add", "Add", "添加");
    String GUI_ADMIN_JSON_REMOVE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.remove", "Remove", "删除");
    String GUI_ADMIN_JSON_RENAME = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.rename", "Rename", "重命名");
    String GUI_ADMIN_JSON_APPLY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.apply", "Apply value", "应用值");
    String GUI_ADMIN_JSON_ROOT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.root", "Document", "文档");
    String GUI_ADMIN_JSON_INVALID = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.invalid", "Cannot edit this JSON visually: %s", "此 JSON 暂时无法可视化编辑：%s");
    String GUI_ADMIN_JSON_LIMIT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.limit", "Visual editing limit exceeded (depth %s, nodes %s); use raw JSON.", "超过可视化编辑限制（深度 %s，节点 %s），请使用原始 JSON。");
    String GUI_ADMIN_JSON_INVALID_VALUE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.invalid_value", "Invalid value, key, or JSON type.", "值、键名或 JSON 类型无效。");
    String GUI_ADMIN_JSON_DUPLICATE_KEY = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.duplicate_key", "This object key already exists.", "此对象键名已存在。");
    String GUI_ADMIN_JSON_REFERENCE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.admin.json.reference", "Reference suggestions", "引用建议");
    //endregion

    //region 抽奖界面
    String GUI_GACHA_SKIP = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.gacha.skip",
            "Skip", "跳过");
    String GUI_SLOT_MACHINES_KIP_HINT = GachaTranslations.creates(GachaConstants.MODID, "gui.gachaaddiction.slot_machines.kip_hint",
            "Press SPACE to skip or ESC to open the container GUI directly", "按 空格 键进行跳过或按 ESC 键直接打开容器GUI");
    //endregion

    //region 资源包
    @SuppressWarnings("unused")
    String PACK_DESCRIPTION = GachaTranslations.creates(GachaConstants.MODID, "pack.gachaaddiction.description",
            "Gacha Addiction", "抽卡瘾");
    //endregion

    String GUI_LOCAL_PACK_TITLE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.title",
            "Local datapack editor", "本地数据包编辑器");
    String GUI_LOCAL_HINT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.hint",
            "Edits are saved only on this computer. Export the datapack, place it on the server manually, then let the owner run /gacha reload or use Update server. UI layouts remain client resource-pack files.", "编辑只保存在本机。导出数据包后由服主手动放入服务器，再执行 /gacha reload 或点击更新服务器。界面布局仍属于客户端资源包。");
    String GUI_LOCAL_REFRESH = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.refresh",
            "Reload local files", "重新读取本地文件");
    String GUI_LOCAL_OPEN_FOLDER = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.open_folder",
            "Open local folder", "打开本地目录");
    String GUI_LOCAL_EXPORT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.export",
            "Export datapack", "导出数据包");
    String GUI_LOCAL_RELOAD = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.reload",
            "Update server", "更新服务器");
    String GUI_LOCAL_EXPORTED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.exported",
            "Exported to %s; place this file on the server manually", "已导出至 %s，请手动放置到服务器");
    String GUI_LOCAL_OPERATION_FAILED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.operation_failed",
            "Local file operation failed: %s", "本地文件操作失败：%s");
    String GUI_LOCAL_SAVED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.saved",
            "Saved locally; server definitions are unchanged", "已保存到本地，服务器定义尚未更新");
    String GUI_LOCAL_FILE_EXISTS = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.file_exists",
            "The local target file already exists", "本地目标文件已存在");
    String GUI_LOCAL_FILE_CHANGED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.file_changed",
            "The local file changed externally; reload it before saving", "本地文件已被外部修改，请重新读取后再保存");
    String GUI_LOCAL_INVALID_PATH = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.invalid_path",
            "The file must stay inside the local datapack directory", "文件必须位于本地数据包目录内");
    String GUI_LOCAL_FILE_TOO_LARGE = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.local_editor.file_too_large",
            "The local document exceeds the size limit", "本地文档超过大小限制");
    String CONFIG_AUTO_TAKE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.auto_take",
            "Automatic reward collection", "自动拿取");
    String CONFIG_AUTO_TAKE_ENABLED = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.auto_take.enabled",
            "Collect matching rewards on this client", "本地客户端自动拿取匹配物品");
    String CONFIG_AUTO_TAKE_ENABLED_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.auto_take.enabled.tooltip",
            "Local preference only. The server independently decides whether automatic collection is allowed.", "只影响本地客户端，服务端使用自身配置决定是否允许自动拿取。");
    String CONFIG_AUTO_TAKE_ALLOWED = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.auto_take.allowed",
            "Allow automatic collection on this server", "逻辑服务端允许自动拿取");
    String CONFIG_AUTO_TAKE_ALLOWED_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.auto_take.allowed.tooltip",
            "Only read by the logical server. This COMMON value is not synchronized to clients.", "只由逻辑服务端读取。此 COMMON 配置不向客户端同步。");
    String CONFIG_MINIMUM_REWARD_RARITY = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.gachaa_screen.minimum_reward_rarity",
            "Minimum automatic reward rarity", "自动处理的最低稀有度");
    String CONFIG_MINIMUM_REWARD_RARITY_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.gachaa_screen.minimum_reward_rarity.tooltip",
            "Use the existing rarity integrations; defaults to RARE (2). A client whitelist match also qualifies.", "复用现有稀有度兼容，默认 RARE（2）。命中客户端白名单时也满足条件。");

    String STRUCTURE_NAME_PREFIX = "structure";
    String STRUCTURE_FALLBACK_NAME_PREFIX = "gachaaddiction.structure_fallback";
    String DOUBLE_CHEST_NAME = "container.chestDouble";
    String RARITY_NAME_PREFIX = "gachaaddiction.rarity.";
    String ANNOUNCEMENT_FORMAT = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.announcement.format",
            "%1$s found %4$s item %5$s in %3$s in %2$s", "%1$s在%2$s中的%3$s中开出%4$s物品%5$s");
    String ANNOUNCEMENT_RENAMED = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.announcement.renamed",
            "%1$s (%2$s)", "%1$s（%2$s）");
    String ANNOUNCEMENT_CONTAINER = GachaTranslations.creates(GachaConstants.MODID,
            "gui.gachaaddiction.announcement.container",
            "Container", "箱子");
    String CONFIG_ANNOUNCEMENT = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement",
            "Loot announcements", "开箱播报");
    String CONFIG_ANNOUNCEMENT_AUTOMATIC = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.automatic",
            "Automatically announce matching rewards", "自动播报匹配的开箱物品");
    String CONFIG_ANNOUNCEMENT_AUTOMATIC_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.automatic.tooltip",
            "The opener selects up to 3 rarest items using their rarity threshold or whitelist. Alt announces a hovered revealed reward regardless of these filters.", "由开箱者使用本地稀有度阈值或白名单选出最稀有的最多3个物品。悬浮已揭晓奖励按Alt可手动播报，不受此筛选限制。");
    String CONFIG_ANNOUNCEMENT_CHAT = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.chat",
            "Show announcements in chat", "在聊天栏显示播报");
    String CONFIG_ANNOUNCEMENT_TOAST = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.toast",
            "Show top-right announcement hints", "在右上角显示播报提示");
    String CONFIG_ANNOUNCEMENT_BANNER = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.banner",
            "Show upper-center scrolling announcements", "在屏幕中上方显示滚动播报");
    String CONFIG_ANNOUNCEMENT_TOOLTIPS = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.tooltips",
            "Show full item information on hover", "悬浮播报物品显示完整信息");
    String CONFIG_ANNOUNCEMENT_TOOLTIPS_TOOLTIP = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.tooltips.tooltip",
            "Uses the receiver's item tooltip settings. Overlay tooltips are available when a screen such as chat provides a cursor.", "使用接收者的物品提示设置。打开聊天等可使用鼠标的界面时，可悬浮右上角提示和滚动条中的物品名称查看。");
    String CONFIG_ANNOUNCEMENT_SECONDS = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.configuration.announcement.seconds",
            "Announcement display time (seconds)", "提示及滚动条显示时长（秒）");
    String ANNOUNCEMENT_RARITY_COMMON = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.common",
            "Common", "普通");
    String ANNOUNCEMENT_RARITY_UNCOMMON = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.uncommon",
            "Uncommon", "罕见");
    String ANNOUNCEMENT_RARITY_RARE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.rare",
            "Rare", "稀有");
    String ANNOUNCEMENT_RARITY_EPIC = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.epic",
            "Epic", "史诗");
    String ANNOUNCEMENT_RARITY_GRAY = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.gray",
            "Gray", "灰色");
    String ANNOUNCEMENT_RARITY_WHITE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.white",
            "White", "白色");
    String ANNOUNCEMENT_RARITY_BLUE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.blue",
            "Blue", "蓝色");
    String ANNOUNCEMENT_RARITY_GREEN = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.green",
            "Green", "绿色");
    String ANNOUNCEMENT_RARITY_ORANGE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.orange",
            "Orange", "橙色");
    String ANNOUNCEMENT_RARITY_LIGHT_RED = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.light_red",
            "Light Red", "浅红色");
    String ANNOUNCEMENT_RARITY_PINK = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.pink",
            "Pink", "粉色");
    String ANNOUNCEMENT_RARITY_LIGHT_PURPLE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.light_purple",
            "Light Purple", "浅紫色");
    String ANNOUNCEMENT_RARITY_LIME = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.lime",
            "Lime", "黄绿色");
    String ANNOUNCEMENT_RARITY_YELLOW = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.yellow",
            "Yellow", "黄色");
    String ANNOUNCEMENT_RARITY_CYAN = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.cyan",
            "Cyan", "青色");
    String ANNOUNCEMENT_RARITY_RED = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.red",
            "Red", "红色");
    String ANNOUNCEMENT_RARITY_PURPLE = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.purple",
            "Purple", "紫色");
    String ANNOUNCEMENT_RARITY_EXPERT = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.expert",
            "Expert", "专家");
    String ANNOUNCEMENT_RARITY_MASTER = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.master",
            "Master", "大师");
    String ANNOUNCEMENT_RARITY_QUEST = GachaTranslations.creates(GachaConstants.MODID,
            "gachaaddiction.rarity.quest",
            "Quest", "任务");

    String STRUCTURE_FALLBACK_PILLAGER_OUTPOST = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.pillager_outpost",
            "Pillager Outpost", "掠夺者前哨站");
    String STRUCTURE_FALLBACK_MINESHAFT = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.mineshaft",
            "Mineshaft", "废弃矿井");
    String STRUCTURE_FALLBACK_MINESHAFT_MESA = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.mineshaft_mesa",
            "Badlands Mineshaft", "恶地废弃矿井");
    String STRUCTURE_FALLBACK_MANSION = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.mansion",
            "Woodland Mansion", "林地府邸");
    String STRUCTURE_FALLBACK_JUNGLE_PYRAMID = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.jungle_pyramid",
            "Jungle Temple", "丛林神庙");
    String STRUCTURE_FALLBACK_DESERT_PYRAMID = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.desert_pyramid",
            "Desert Pyramid", "沙漠神殿");
    String STRUCTURE_FALLBACK_IGLOO = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.igloo",
            "Igloo", "雪屋");
    String STRUCTURE_FALLBACK_SHIPWRECK = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.shipwreck",
            "Shipwreck", "沉船");
    String STRUCTURE_FALLBACK_SHIPWRECK_BEACHED = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.shipwreck_beached",
            "Beached Shipwreck", "搁浅沉船");
    String STRUCTURE_FALLBACK_SWAMP_HUT = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.swamp_hut",
            "Swamp Hut", "沼泽小屋");
    String STRUCTURE_FALLBACK_STRONGHOLD = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.stronghold",
            "Stronghold", "要塞");
    String STRUCTURE_FALLBACK_MONUMENT = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.monument",
            "Ocean Monument", "海底神殿");
    String STRUCTURE_FALLBACK_OCEAN_RUIN_COLD = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ocean_ruin_cold",
            "Cold Ocean Ruins", "冷水海底废墟");
    String STRUCTURE_FALLBACK_OCEAN_RUIN_WARM = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ocean_ruin_warm",
            "Warm Ocean Ruins", "暖水海底废墟");
    String STRUCTURE_FALLBACK_FORTRESS = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.fortress",
            "Nether Fortress", "下界要塞");
    String STRUCTURE_FALLBACK_NETHER_FOSSIL = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.nether_fossil",
            "Nether Fossil", "下界化石");
    String STRUCTURE_FALLBACK_END_CITY = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.end_city",
            "End City", "末地城");
    String STRUCTURE_FALLBACK_BURIED_TREASURE = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.buried_treasure",
            "Buried Treasure", "埋藏的宝藏");
    String STRUCTURE_FALLBACK_BASTION_REMNANT = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.bastion_remnant",
            "Bastion Remnant", "堡垒遗迹");
    String STRUCTURE_FALLBACK_VILLAGE_PLAINS = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.village_plains",
            "Plains Village", "平原村庄");
    String STRUCTURE_FALLBACK_VILLAGE_DESERT = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.village_desert",
            "Desert Village", "沙漠村庄");
    String STRUCTURE_FALLBACK_VILLAGE_SAVANNA = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.village_savanna",
            "Savanna Village", "热带草原村庄");
    String STRUCTURE_FALLBACK_VILLAGE_SNOWY = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.village_snowy",
            "Snowy Village", "雪原村庄");
    String STRUCTURE_FALLBACK_VILLAGE_TAIGA = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.village_taiga",
            "Taiga Village", "针叶林村庄");
    String STRUCTURE_FALLBACK_RUINED_PORTAL = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal",
            "Ruined Portal", "废弃传送门");
    String STRUCTURE_FALLBACK_RUINED_PORTAL_DESERT = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal_desert",
            "Desert Ruined Portal", "沙漠废弃传送门");
    String STRUCTURE_FALLBACK_RUINED_PORTAL_JUNGLE = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal_jungle",
            "Jungle Ruined Portal", "丛林废弃传送门");
    String STRUCTURE_FALLBACK_RUINED_PORTAL_SWAMP = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal_swamp",
            "Swamp Ruined Portal", "沼泽废弃传送门");
    String STRUCTURE_FALLBACK_RUINED_PORTAL_MOUNTAIN = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal_mountain",
            "Mountain Ruined Portal", "山地废弃传送门");
    String STRUCTURE_FALLBACK_RUINED_PORTAL_OCEAN = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal_ocean",
            "Ocean Ruined Portal", "海洋废弃传送门");
    String STRUCTURE_FALLBACK_RUINED_PORTAL_NETHER = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ruined_portal_nether",
            "Nether Ruined Portal", "下界废弃传送门");
    String STRUCTURE_FALLBACK_ANCIENT_CITY = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.ancient_city",
            "Ancient City", "远古城市");
    String STRUCTURE_FALLBACK_TRAIL_RUINS = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.trail_ruins",
            "Trail Ruins", "古迹废墟");
    String STRUCTURE_FALLBACK_TRIAL_CHAMBERS = GachaTranslations.creates(GachaConstants.MODID,
            STRUCTURE_FALLBACK_NAME_PREFIX + ".minecraft.trial_chambers",
            "Trial Chambers", "试炼密室");

    String DIMENSION_OVERWORLD = GachaTranslations.creates(GachaConstants.MODID,
            "dimension.minecraft.overworld",
            "Overworld", "主世界");
    String DIMENSION_THE_NETHER = GachaTranslations.creates(GachaConstants.MODID,
            "dimension.minecraft.the_nether",
            "The Nether", "下界");
    String DIMENSION_THE_END = GachaTranslations.creates(GachaConstants.MODID,
            "dimension.minecraft.the_end",
            "The End", "末地");

    static void init() {
    }
}
