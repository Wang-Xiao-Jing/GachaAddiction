package xiaojin.gachaaddiction.cardpool;

import java.util.Locale;

public enum GachaCardPoolResultStyle {
    SUMMARY;

    public static GachaCardPoolResultStyle parse(String value) {
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}
