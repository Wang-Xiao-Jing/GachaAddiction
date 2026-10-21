package xiaojin.gachaaddiction.persistence;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtIo;
import xiaojin.gachaaddiction.cardpool.parameter.IGachaParameterFingerprint;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSchema;
import xiaojin.gachaaddiction.reward.parameter.GachaParameterSet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

public final class GachaSha256ParameterFingerprint implements IGachaParameterFingerprint {
    private static final String ALGORITHM = "SHA-256";

    private final HolderLookup.Provider registries;

    public GachaSha256ParameterFingerprint(HolderLookup.Provider registries) {
        this.registries = Objects.requireNonNull(registries, "registries");
    }

    @Override
    public String create(GachaParameterSchema schema, GachaParameterSet parameters) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                NbtIo.write(new GachaParameterNbtEncoder(schema, registries).encode(parameters), output);
            }
            return HexFormat.of().formatHex(MessageDigest.getInstance(ALGORITHM).digest(bytes.toByteArray()));
        } catch (IOException | NoSuchAlgorithmException failure) {
            throw new IllegalStateException("Unable to fingerprint gacha parameters", failure);
        }
    }
}
