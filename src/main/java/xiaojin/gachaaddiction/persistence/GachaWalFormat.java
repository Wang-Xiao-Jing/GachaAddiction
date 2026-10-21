package xiaojin.gachaaddiction.persistence;

public final class GachaWalFormat {
    public static final int MAGIC = 0x47414348;
    public static final int FRAME_VERSION = 1;
    public static final String IO_THREAD_NAME = "GachaAddiction-WAL";

    private GachaWalFormat() {
    }
}
