package xiaojin.gachaaddiction.client.compat.ldlib2;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import xiaojin.gachaaddiction.config.GachaPresentationDefaults;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;
import java.util.regex.Pattern;

public final class LDLib2GachaDesignerFiles {
    private static final String LOCAL_ASSETS = "ldlib2/assets";
    private static final String UI_DIRECTORY = "ui";
    private static final String TEMPORARY_PREFIX = ".gacha-layout-";
    private static final String TEMPORARY_SUFFIX = ".tmp";
    private static final Pattern FILE_NAME = Pattern.compile("[a-z0-9_-][a-z0-9_.-]*\\.ui\\.nbt");

    private LDLib2GachaDesignerFiles() {
    }

    public static String defaultFileName() {
        return Path.of(GachaPresentationDefaults.DEFAULT_TEMPLATE_ID.getPath()).getFileName().toString();
    }

    public static Path resolve(String name) throws IOException {
        if (name == null || !FILE_NAME.matcher(name).matches() || name.contains("..")) {
            throw invalid(FileFailure.NAME);
        }
        Path game = Platform.getGamePath().toRealPath();
        Path directory = game.resolve(LOCAL_ASSETS)
                .resolve(GachaPresentationDefaults.DEFAULT_TEMPLATE_ID.getNamespace()).resolve(UI_DIRECTORY);
        Path target = directory.resolve(name).normalize();
        for (Path current = target; current != null && !current.equals(game); current = current.getParent()) {
            if (Files.isSymbolicLink(current)) {
                throw invalid(FileFailure.PATH);
            }
        }
        if (!target.getParent().equals(directory) || !target.startsWith(game)) {
            throw invalid(FileFailure.PATH);
        }
        return target;
    }

    public static UITemplate read(String name) throws IOException {
        Path target = resolve(name);
        if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)
                || Files.size(target) > LDLib2GachaUiConstants.MAXIMUM_TEMPLATE_BYTES) {
            throw invalid(FileFailure.SIZE);
        }
        CompoundTag resource;
        try (var input = new DataInputStream(Files.newInputStream(target))) {
            resource = NbtIo.read(input, NbtAccounter.create(LDLib2GachaUiConstants.MAXIMUM_TEMPLATE_BYTES));
        }
        if (!LDLib2GachaUiConstants.RESOURCE_UI_TYPE.equals(
                resource.getString(LDLib2GachaUiConstants.RESOURCE_TYPE_KEY))
                || !resource.contains(LDLib2GachaUiConstants.RESOURCE_DATA_KEY, Tag.TAG_COMPOUND)) {
            throw invalid(FileFailure.FORMAT);
        }
        CompoundTag encoded = resource.getCompound(LDLib2GachaUiConstants.RESOURCE_DATA_KEY);
        LDLib2GachaDesignerTemplates.validateEncoded(encoded);
        UITemplate template = Objects.requireNonNull(
                UIResource.INSTANCE.deserializeResource(encoded, Platform.getFrozenRegistry()));
        LDLib2GachaDesignerTemplates.validate(template);
        return template;
    }

    public static void write(String name, UITemplate template, boolean overwrite) throws IOException {
        Path target = resolve(name);
        LDLib2GachaDesignerTemplates.validate(template);
        Tag encoded = UIResource.INSTANCE.serializeResource(template, Platform.getFrozenRegistry());
        if (!(encoded instanceof CompoundTag data)) {
            throw invalid(FileFailure.FORMAT);
        }
        LDLib2GachaDesignerTemplates.validateEncoded(data);
        var resource = new CompoundTag();
        resource.putString(LDLib2GachaUiConstants.RESOURCE_TYPE_KEY, LDLib2GachaUiConstants.RESOURCE_UI_TYPE);
        resource.put(LDLib2GachaUiConstants.RESOURCE_DATA_KEY, data);
        var buffer = new ByteArrayOutputStream();
        try (var output = new DataOutputStream(buffer)) {
            NbtIo.write(resource, output);
        }
        if (buffer.size() > LDLib2GachaUiConstants.MAXIMUM_TEMPLATE_BYTES) {
            throw invalid(FileFailure.SIZE);
        }
        Files.createDirectories(target.getParent());
        resolve(name);
        Path temporary = Files.createTempFile(target.getParent(), TEMPORARY_PREFIX, TEMPORARY_SUFFIX);
        try {
            Files.write(temporary, buffer.toByteArray());
            if (overwrite) {
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException unsupported) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.move(temporary, target);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static IOException invalid(FileFailure failure) {
        return new IOException(failure.name());
    }

    private enum FileFailure {
        NAME, PATH, SIZE, FORMAT
    }
}
