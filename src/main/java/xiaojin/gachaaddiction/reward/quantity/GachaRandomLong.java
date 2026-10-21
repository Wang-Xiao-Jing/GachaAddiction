package xiaojin.gachaaddiction.reward.quantity;

import net.minecraft.util.RandomSource;

public final class GachaRandomLong {
    private GachaRandomLong() {
    }

    public static long inclusive(RandomSource random, long minimum, long maximum) {
        if (minimum > maximum) {
            throw new IllegalArgumentException("Random quantity minimum cannot exceed maximum");
        }
        long bound = maximum - minimum + 1;
        if (bound > 0) {
            long mask = bound - 1;
            long value = random.nextLong();
            if ((bound & mask) == 0) {
                return minimum + (value & mask);
            }
            long unsigned = value >>> 1;
            long remainder;
            while (unsigned + mask - (remainder = unsigned % bound) < 0) {
                unsigned = random.nextLong() >>> 1;
            }
            return minimum + remainder;
        }
        long value;
        do {
            value = random.nextLong();
        } while (value < minimum || value > maximum);
        return value;
    }
}
