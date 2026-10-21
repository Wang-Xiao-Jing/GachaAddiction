package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

public final class GachaFilePoolSnapshotArchive implements IGachaPoolSnapshotArchive {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String FILE_EXTENSION = ".nbt";
    private static final String TEMP_FILE_PREFIX = "snapshot-";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final String HASH_PATTERN = "[0-9a-f]{64}";

    private final Path root;
    private final int maxSnapshotBytes;

    public GachaFilePoolSnapshotArchive(Path root, int maxSnapshotBytes) throws IOException {
        this.root = Objects.requireNonNull(root, "root").toAbsolutePath().normalize();
        if (maxSnapshotBytes <= 0) {
            throw new IllegalArgumentException("Maximum snapshot size must be positive");
        }
        this.maxSnapshotBytes = maxSnapshotBytes;
        Files.createDirectories(this.root);
    }

    private static String hash(byte[] encoded) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(HASH_ALGORITHM).digest(encoded));
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException("Missing SHA-256 message digest", failure);
        }
    }

    @Override
    public String store(CompoundTag snapshot) throws IOException {
        byte[] encoded = encode(Objects.requireNonNull(snapshot, "snapshot"));
        requireSize(encoded.length);
        String contentHash = hash(encoded);
        Path target = resolve(contentHash);
        if (Files.exists(target)) {
            verifyExisting(target, contentHash);
            return contentHash;
        }

        Path temporary = Files.createTempFile(root, TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                ByteBuffer bytes = ByteBuffer.wrap(encoded);
                while (bytes.hasRemaining()) {
                    int written = channel.write(bytes);
                    if (written <= 0) {
                        throw new IOException("Unable to write pool snapshot");
                    }
                }
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target);
            } catch (FileAlreadyExistsException raced) {
                verifyExisting(target, contentHash);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return contentHash;
    }

    @Override
    public Optional<CompoundTag> load(String contentHash) throws IOException {
        Path target = resolve(contentHash);
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        requireSize(Files.size(target));
        byte[] encoded = Files.readAllBytes(target);
        if (!hash(encoded).equals(contentHash)) {
            throw new IOException("Card pool snapshot content hash mismatch");
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded))) {
            return Optional.of(NbtIo.read(input, NbtAccounter.create(maxSnapshotBytes)));
        }
    }

    private void verifyExisting(Path target, String contentHash) throws IOException {
        requireSize(Files.size(target));
        byte[] encoded = Files.readAllBytes(target);
        if (!hash(encoded).equals(contentHash)) {
            throw new IOException("Existing card pool snapshot content does not match its file name");
        }
    }

    private byte[] encode(CompoundTag snapshot) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            NbtIo.write(snapshot, output);
        }
        return bytes.toByteArray();
    }

    private Path resolve(String contentHash) {
        Objects.requireNonNull(contentHash, "contentHash");
        if (!contentHash.matches(HASH_PATTERN)) {
            throw new IllegalArgumentException("Invalid card pool snapshot hash");
        }
        Path resolved = root.resolve(contentHash + FILE_EXTENSION).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("Card pool snapshot path escapes its archive root");
        }
        return resolved;
    }

    private void requireSize(long size) throws IOException {
        if (size > maxSnapshotBytes) {
            throw new IOException("Card pool snapshot exceeds configured maximum size");
        }
    }
}
