package xiaojin.gachaaddiction.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import xiaojin.gachaaddiction.cardpool.delivery.GachaRewardDeliveryReceipt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

public class GachaPlayerSaveCheckpoint {
    private static final String PLAYER_FILE_EXTENSION = ".dat";
    private static final String WORLD_DATA_KEY = "Data";
    private static final String WORLD_PLAYER_KEY = "Player";

    public static void saveAndVerify(ServerPlayer player, GachaRewardDeliveryReceipt receipt,
                                     ListTag expectedInventory) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(receipt, "receipt");
        ListTag inventorySnapshot = Objects.requireNonNull(expectedInventory, "expectedInventory").copy();
        MinecraftServer server = Objects.requireNonNull(player.getServer(), "server");
        if (!server.isSameThread()) {
            throw new IllegalStateException("Reward delivery checkpoint requires the server thread");
        }

        if (!player.getUUID().equals(receipt.playerId()) || !receipt.isPresentIn(player.getPersistentData())
                || !player.getInventory().save(new ListTag()).equals(inventorySnapshot)) {
            throw new IllegalStateException("Live player state does not match the reward delivery receipt");
        }

        boolean owner = server.isSingleplayerOwner(player.getGameProfile());
        if (owner) {
            server.saveEverything(true, true, true);
        } else {
            server.getPlayerList().saveAll();
        }

        verifyAfterSave(player, receipt, inventorySnapshot);
    }

    public static void verifyAfterSave(ServerPlayer player, GachaRewardDeliveryReceipt receipt,
                                       ListTag expectedInventory) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(receipt, "receipt");
        ListTag inventorySnapshot = Objects.requireNonNull(expectedInventory, "expectedInventory").copy();
        MinecraftServer server = Objects.requireNonNull(player.getServer(), "server");
        if (!server.isSameThread() || !player.getUUID().equals(receipt.playerId())
                || !receipt.isPresentIn(player.getPersistentData())) {
            throw new IllegalStateException("Player checkpoint identity or thread does not match");
        }

        boolean owner = server.isSingleplayerOwner(player.getGameProfile());
        Path playerFile = server.getWorldPath(LevelResource.PLAYER_DATA_DIR)
                .resolve(player.getStringUUID() + PLAYER_FILE_EXTENSION);
        try {
            if (owner) {
                verifyOwnerFiles(playerFile, server.getWorldPath(LevelResource.LEVEL_DATA_FILE),
                        receipt, inventorySnapshot);
            } else {
                verifyPlayerFile(playerFile, receipt, inventorySnapshot);
            }
        } catch (IOException failure) {
            throw new UncheckedIOException("Unable to verify reward delivery player checkpoint", failure);
        }
    }

    public static void verifyPlayerFile(Path playerFile, GachaRewardDeliveryReceipt receipt,
                                         ListTag expectedInventory) throws IOException {
        Objects.requireNonNull(playerFile, "playerFile");
        Objects.requireNonNull(receipt, "receipt");
        Objects.requireNonNull(expectedInventory, "expectedInventory");
        CompoundTag savedPlayer = NbtIo.readCompressed(playerFile, NbtAccounter.unlimitedHeap());
        requireMatching(savedPlayer, receipt, expectedInventory, "player data");
    }

    public static void verifyOwnerFiles(Path playerFile, Path levelFile, GachaRewardDeliveryReceipt receipt,
                                         ListTag expectedInventory) throws IOException {
        verifyPlayerFile(playerFile, receipt, expectedInventory);
        CompoundTag savedLevel = NbtIo.readCompressed(Objects.requireNonNull(levelFile, "levelFile"),
                NbtAccounter.unlimitedHeap());
        CompoundTag savedOwner = savedLevel.getCompound(WORLD_DATA_KEY).getCompound(WORLD_PLAYER_KEY);
        requireMatching(savedOwner, receipt, expectedInventory, "singleplayer level data");
    }

    private static void requireMatching(CompoundTag playerSnapshot, GachaRewardDeliveryReceipt receipt,
                                          ListTag expectedInventory, String source) throws IOException {
        if (!receipt.matchesPlayerSnapshot(playerSnapshot, expectedInventory)) {
            throw new IOException("Saved " + source + " does not match the reward delivery receipt");
        }
    }
}
