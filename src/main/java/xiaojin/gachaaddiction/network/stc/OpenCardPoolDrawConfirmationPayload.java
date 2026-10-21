package xiaojin.gachaaddiction.network.stc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xiaojin.gachaaddiction.GachaAddiction;
import xiaojin.gachaaddiction.client.GachaPresentationClient;
import xiaojin.gachaaddiction.client.gui.screen.CardPoolDrawConfirmationScreen;
import xiaojin.gachaaddiction.execution.GachaExecutionPreviewIssue;
import xiaojin.gachaaddiction.execution.GachaExecutionStepPreview;
import xiaojin.gachaaddiction.network.IToClientPayload;

import java.time.Instant;
import java.util.*;

public record OpenCardPoolDrawConfirmationPayload(
        UUID token,
        ResourceLocation cardPoolId,
        ResourceLocation packageId,
        String titleTranslationKey,
        List<GachaExecutionStepPreview> previewSteps,
        List<GachaExecutionPreviewIssue> previewIssues,
        long expiresAtEpochMillis
) implements IToClientPayload {
    public static final Type<OpenCardPoolDrawConfirmationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(GachaAddiction.MODID, "open_card_pool_draw_confirmation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenCardPoolDrawConfirmationPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public OpenCardPoolDrawConfirmationPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new OpenCardPoolDrawConfirmationPayload(UUIDUtil.STREAM_CODEC.decode(buffer),
                            ResourceLocation.STREAM_CODEC.decode(buffer), ResourceLocation.STREAM_CODEC.decode(buffer),
                            ByteBufCodecs.STRING_UTF8.decode(buffer),
                            decodeSteps(buffer), decodeIssues(buffer),
                            ByteBufCodecs.VAR_LONG.decode(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, OpenCardPoolDrawConfirmationPayload value) {
                    UUIDUtil.STREAM_CODEC.encode(buffer, value.token());
                    ResourceLocation.STREAM_CODEC.encode(buffer, value.cardPoolId());
                    ResourceLocation.STREAM_CODEC.encode(buffer, value.packageId());
                    ByteBufCodecs.STRING_UTF8.encode(buffer, value.titleTranslationKey());
                    encodeSteps(buffer, value.previewSteps());
                    encodeIssues(buffer, value.previewIssues());
                    ByteBufCodecs.VAR_LONG.encode(buffer, value.expiresAtEpochMillis());
                }
            };
    private static final int MAXIMUM_PREVIEW_STEPS = 1024;
    private static final int MAXIMUM_CONSUMPTION_TYPES_PER_STEP = 256;
    private static final int MAXIMUM_PREVIEW_ISSUES = 32;

    public OpenCardPoolDrawConfirmationPayload {
        previewSteps = List.copyOf(previewSteps);
        previewIssues = List.copyOf(previewIssues);
    }

    private static List<GachaExecutionStepPreview> decodeSteps(RegistryFriendlyByteBuf buffer) {
        int size = readBoundedSize(buffer, MAXIMUM_PREVIEW_STEPS, "preview steps");
        List<GachaExecutionStepPreview> steps = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            ResourceLocation type = ResourceLocation.STREAM_CODEC.decode(buffer);
            Optional<ResourceLocation> target = buffer.readBoolean()
                    ? Optional.of(ResourceLocation.STREAM_CODEC.decode(buffer)) : Optional.empty();
            OptionalLong rounds = buffer.readBoolean() ? OptionalLong.of(buffer.readVarLong()) : OptionalLong.empty();
            int consumptionSize = readBoundedSize(buffer, MAXIMUM_CONSUMPTION_TYPES_PER_STEP,
                    "preview consumption types");
            List<ResourceLocation> consumptionTypes = new ArrayList<>(consumptionSize);
            for (int consumptionIndex = 0; consumptionIndex < consumptionSize; consumptionIndex++) {
                consumptionTypes.add(ResourceLocation.STREAM_CODEC.decode(buffer));
            }
            steps.add(new GachaExecutionStepPreview(type, target, rounds, consumptionTypes));
        }
        return List.copyOf(steps);
    }

    private static void encodeSteps(RegistryFriendlyByteBuf buffer, List<GachaExecutionStepPreview> steps) {
        buffer.writeVarInt(steps.size());
        for (GachaExecutionStepPreview step : steps) {
            ResourceLocation.STREAM_CODEC.encode(buffer, step.type());
            buffer.writeBoolean(step.target().isPresent());
            step.target().ifPresent(target -> ResourceLocation.STREAM_CODEC.encode(buffer, target));
            buffer.writeBoolean(step.rounds().isPresent());
            step.rounds().ifPresent(buffer::writeVarLong);
            buffer.writeVarInt(step.consumptionTypes().size());
            step.consumptionTypes().forEach(type -> ResourceLocation.STREAM_CODEC.encode(buffer, type));
        }
    }

    private static List<GachaExecutionPreviewIssue> decodeIssues(RegistryFriendlyByteBuf buffer) {
        int size = readBoundedSize(buffer, MAXIMUM_PREVIEW_ISSUES, "preview issues");
        List<GachaExecutionPreviewIssue> issues = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            issues.add(GachaExecutionPreviewIssue.valueOf(ByteBufCodecs.STRING_UTF8.decode(buffer)));
        }
        return List.copyOf(issues);
    }

    private static void encodeIssues(RegistryFriendlyByteBuf buffer, List<GachaExecutionPreviewIssue> issues) {
        buffer.writeVarInt(issues.size());
        issues.forEach(issue -> ByteBufCodecs.STRING_UTF8.encode(buffer, issue.name()));
    }

    private static int readBoundedSize(RegistryFriendlyByteBuf buffer, int maximum, String description) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) {
            throw new IllegalArgumentException("Invalid " + description + " size: " + size);
        }
        return size;
    }

    public Instant expiresAt() {
        return Instant.ofEpochMilli(expiresAtEpochMillis);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void work(IPayloadContext context, AbstractClientPlayer player) {
        GachaPresentationClient.rememberCardPool(cardPoolId, titleTranslationKey);
        Minecraft.getInstance().setScreen(new CardPoolDrawConfirmationScreen(this));
    }
}
