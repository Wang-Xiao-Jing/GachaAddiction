package xiaojin.gachaaddiction.cardpool;

import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public record GachaCardPoolAvailability(boolean enabled, int minimumPermissionLevel,
                                        Optional<Instant> opensAt, Optional<Instant> closesAt) {
    public GachaCardPoolAvailability {
        if (minimumPermissionLevel < 0 || minimumPermissionLevel > 4) {
            throw new IllegalArgumentException("Card pool permission level must be between 0 and 4");
        }
        opensAt = Objects.requireNonNull(opensAt, "opensAt");
        closesAt = Objects.requireNonNull(closesAt, "closesAt");
        if (opensAt.isPresent() && closesAt.isPresent() && !opensAt.get().isBefore(closesAt.get())) {
            throw new IllegalArgumentException("Card pool opening time must be before its closing time");
        }
    }

    public boolean allows(ServerPlayer player, Instant now) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(now, "now");
        return enabled && player.hasPermissions(minimumPermissionLevel)
                && opensAt.map(opening -> !now.isBefore(opening)).orElse(true)
                && closesAt.map(closing -> now.isBefore(closing)).orElse(true);
    }
}
