package xiaojin.gachaaddiction.admin;

import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;

public enum GachaAdminCapability {
    TEST_DRAW(1),
    EDIT_DEFINITIONS(2),
    VIEW_ADMIN_DATA(2),
    MANAGE_ADMIN(4);

    private final int defaultPermissionLevel;

    GachaAdminCapability(int defaultPermissionLevel) {
        this.defaultPermissionLevel = defaultPermissionLevel;
    }

    public int defaultPermissionLevel() {
        return defaultPermissionLevel;
    }

    public boolean allows(CommandSourceStack source) {
        return Objects.requireNonNull(source, "source").hasPermission(defaultPermissionLevel);
    }
}
