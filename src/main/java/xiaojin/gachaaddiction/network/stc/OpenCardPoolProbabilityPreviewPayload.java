package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.gui.screen.CardPoolDrawConfirmationScreen;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record OpenCardPoolProbabilityPreviewPayload(UUID token, List<Probability> probabilities)
        implements IToClientPayload {
    public static final Type<OpenCardPoolProbabilityPreviewPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "open_card_pool_probability_preview"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCardPoolProbabilityPreviewPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenCardPoolProbabilityPreviewPayload decode(RegistryFriendlyByteBuf buffer) {
                    UUID token = UUIDUtil.STREAM_CODEC.decode(buffer);
                    int probabilityCount = readBoundedSize(buffer, MAXIMUM_PROBABILITIES, "probability descriptions");
                    List<Probability> probabilities = new ArrayList<>(probabilityCount);
                    for (int index = 0; index < probabilityCount; index++) {
                        ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buffer);
                        String selectionMode = buffer.readUtf(MAXIMUM_TEXT_LENGTH);
                        String selectionCount = buffer.readUtf(MAXIMUM_TEXT_LENGTH);
                        int candidateCount = readBoundedSize(buffer, MAXIMUM_CANDIDATES, "probability candidates");
                        List<Candidate> candidates = new ArrayList<>(candidateCount);
                        for (int candidateIndex = 0; candidateIndex < candidateCount; candidateIndex++) {
                            candidates.add(new Candidate(ResourceLocation.STREAM_CODEC.decode(buffer),
                                    buffer.readUtf(MAXIMUM_TEXT_LENGTH)));
                        }
                        probabilities.add(new Probability(id, selectionMode, selectionCount, candidates));
                    }
                    return new OpenCardPoolProbabilityPreviewPayload(token, probabilities);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, OpenCardPoolProbabilityPreviewPayload value) {
                    UUIDUtil.STREAM_CODEC.encode(buffer, value.token());
                    buffer.writeVarInt(value.probabilities().size());
                    for (Probability probability : value.probabilities()) {
                        ResourceLocation.STREAM_CODEC.encode(buffer, probability.id());
                        buffer.writeUtf(probability.selectionMode(), MAXIMUM_TEXT_LENGTH);
                        buffer.writeUtf(probability.selectionCount(), MAXIMUM_TEXT_LENGTH);
                        buffer.writeVarInt(probability.candidates().size());
                        for (Candidate candidate : probability.candidates()) {
                            ResourceLocation.STREAM_CODEC.encode(buffer, candidate.id());
                            buffer.writeUtf(candidate.weight(), MAXIMUM_TEXT_LENGTH);
                        }
                    }
                }
            };
    private static final int MAXIMUM_PROBABILITIES = 1024;
    private static final int MAXIMUM_CANDIDATES = 1024;
    private static final int MAXIMUM_TEXT_LENGTH = 512;

    public OpenCardPoolProbabilityPreviewPayload {
        Objects.requireNonNull(token, "token");
        probabilities = List.copyOf(Objects.requireNonNull(probabilities, "probabilities"));
        if (probabilities.size() > MAXIMUM_PROBABILITIES) {
            throw new IllegalArgumentException("Too many probability descriptions");
        }
    }

    private static int readBoundedSize(RegistryFriendlyByteBuf buffer, int maximum, String description) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) {
            throw new IllegalArgumentException("Invalid " + description + " size: " + size);
        }
        return size;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        if (Minecraft.getInstance().screen instanceof CardPoolDrawConfirmationScreen screen) {
            screen.setProbabilityPreview(token, probabilities);
        }
    }

    public record Probability(ResourceLocation id, String selectionMode, String selectionCount,
                              List<Candidate> candidates) {
        public Probability {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(selectionMode, "selectionMode");
            Objects.requireNonNull(selectionCount, "selectionCount");
            candidates = List.copyOf(Objects.requireNonNull(candidates, "candidates"));
            if (candidates.size() > MAXIMUM_CANDIDATES) {
                throw new IllegalArgumentException("Too many probability candidates");
            }
        }
    }

    public record Candidate(ResourceLocation id, String weight) {
        public Candidate {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(weight, "weight");
        }
    }
}
