package xiaojin.gachaaddiction.resourcepack;

import net.minecraft.network.chat.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public record GachaResourcePackDescriptor(URI downloadUri, String sha1, boolean required,
                                          Optional<Component> prompt) {
    private static final int SHA1_LENGTH = 40;

    public GachaResourcePackDescriptor {
        downloadUri = normalize(Objects.requireNonNull(downloadUri, "downloadUri"));
        sha1 = requireSha1(sha1);
        Objects.requireNonNull(prompt, "prompt");
    }

    public static GachaResourcePackDescriptor external(String url, String sha1, boolean required) {
        try {
            return new GachaResourcePackDescriptor(new URI(Objects.requireNonNull(url, "url")), sha1, required,
                    Optional.empty());
        } catch (URISyntaxException failure) {
            throw new IllegalArgumentException("Resource pack URL is invalid", failure);
        }
    }

    private static URI normalize(URI uri) {
        if (!uri.isAbsolute() || uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("Resource pack URL must be an absolute URL without credentials or fragment");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("Resource pack URL must use HTTP or HTTPS");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("Resource pack URL must include a host");
        }
        return uri.normalize();
    }

    private static String requireSha1(String value) {
        String normalized = Objects.requireNonNull(value, "sha1").toLowerCase(Locale.ROOT);
        if (normalized.length() != SHA1_LENGTH || !normalized.matches("[0-9a-f]{40}")) {
            throw new IllegalArgumentException("Resource pack SHA-1 must contain 40 hexadecimal characters");
        }
        return normalized;
    }

    public String url() {
        return downloadUri.toASCIIString();
    }
}
