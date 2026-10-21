package xiaojin.gachaaddiction.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import xiaojin.gachaaddiction.cardpool.GachaRuntimeSettings;
import xiaojin.gachaaddiction.cardpool.transaction.GachaDurability;
import xiaojin.gachaaddiction.config.entry.BooleanConfigEntry;
import xiaojin.gachaaddiction.config.entry.IntConfigEntry;
import xiaojin.gachaaddiction.config.entry.SelectorConfigEntry;

import java.time.Duration;
import java.util.List;

@SuppressWarnings("unused")
public class ServerConfig extends BasicConfig {
    private static final int DEFAULT_MAXIMUM_WAL_PAYLOAD_BYTES = 16 * 1024 * 1024;
    private static final int DEFAULT_MAXIMUM_SNAPSHOT_BYTES = 64 * 1024 * 1024;
    private static final int DEFAULT_MAXIMUM_INBOX_ENTRY_BYTES = 16 * 1024 * 1024;
    private static final int DEFAULT_MAXIMUM_PARAMETER_STATE_BYTES = 64 * 1024 * 1024;
    private static final int DEFAULT_GROUP_COMMIT_DELAY_MILLIS = 50;
    private static final int DEFAULT_PUBLIC_DRAW_CONFIRMATION_LIFETIME_SECONDS = 60;

    public final IntConfigEntry maximumWalPayloadBytes;
    public final BooleanConfigEntry forceWatch;
    public final BooleanConfigEntry protectLoot;
    public final IntConfigEntry maximumSnapshotBytes;
    public final IntConfigEntry maximumInboxEntryBytes;
    public final IntConfigEntry maximumParameterStateBytes;
    public final IntConfigEntry groupCommitDelayMillis;
    public final IntConfigEntry publicDrawConfirmationLifetimeSeconds;
    public final SelectorConfigEntry<GachaDurability> recoveryDurability;
    public final SelectorConfigEntry<GachaDurability> publicDrawDurability;

    public ServerConfig(ModConfigSpec.Builder builder) {
        super(builder);
        push("presentation");
        forceWatch = define(false, "force_watch", "Require players to finish gacha presentations before closing or skipping.");
        protectLoot = define(true, "protect_loot", "Allow clients to lock gacha sources against other players and automation during presentations.");
        pop();
        push("transaction");
        maximumWalPayloadBytes = define(DEFAULT_MAXIMUM_WAL_PAYLOAD_BYTES, 1024, Integer.MAX_VALUE,
                "maximum_wal_payload_bytes", "Maximum encoded payload size accepted for one WAL frame.");
        maximumSnapshotBytes = define(DEFAULT_MAXIMUM_SNAPSHOT_BYTES, 1024, Integer.MAX_VALUE,
                "maximum_snapshot_bytes", "Maximum encoded size accepted for one archived card pool snapshot.");
        maximumInboxEntryBytes = define(DEFAULT_MAXIMUM_INBOX_ENTRY_BYTES, 1024, Integer.MAX_VALUE,
                "maximum_inbox_entry_bytes", "Maximum encoded size accepted for one reward inbox entry.");
        maximumParameterStateBytes = define(DEFAULT_MAXIMUM_PARAMETER_STATE_BYTES, 1024, Integer.MAX_VALUE,
                "maximum_parameter_state_bytes", "Maximum encoded size accepted for persistent parameter state.");
        groupCommitDelayMillis = define(DEFAULT_GROUP_COMMIT_DELAY_MILLIS, 0, 60_000,
                "group_commit_delay_millis", "Maximum delay before a grouped WAL flush.");
        recoveryDurability = selectorDefine(GachaDurability.SYNC, List.of(GachaDurability.values()),
                "recovery_durability", GachaDurability::valueOf, GachaDurability::name,
                "Durability used when recovery appends a terminal WAL frame.");
        publicDrawDurability = selectorDefine(GachaDurability.GROUP_COMMIT, List.of(GachaDurability.values()),
                "public_draw_durability", GachaDurability::valueOf, GachaDurability::name,
                "Minimum durability requested by the public card pool endpoint.");
        pop();
        push("card_pool");
        publicDrawConfirmationLifetimeSeconds = define(DEFAULT_PUBLIC_DRAW_CONFIRMATION_LIFETIME_SECONDS, 1,
                Integer.MAX_VALUE, "public_draw_confirmation_lifetime_seconds",
                "Lifetime of a prepared public card pool draw before confirmation.");
        pop();
    }

    public GachaRuntimeSettings runtimeSettings() {
        return new GachaRuntimeSettings(maximumWalPayloadBytes.get(), maximumSnapshotBytes.get(),
                maximumInboxEntryBytes.get(), maximumParameterStateBytes.get(),
                Duration.ofMillis(groupCommitDelayMillis.get()), recoveryDurability.get(),
                Duration.ofSeconds(publicDrawConfirmationLifetimeSeconds.get()));
    }
}
