package xiaojin.gachaaddiction.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;
import xiaojin.gachaaddiction.GachaConstants;
import xiaojin.gachaaddiction.api.GachaRandomType;
import xiaojin.gachaaddiction.api.GachaSecondaryType;
import xiaojin.gachaaddiction.api.GachaSourceSettings;
import xiaojin.gachaaddiction.api.IGachaSource;
import xiaojin.gachaaddiction.mixed.IMenuProviderLootExtension;
import xiaojin.gachaaddiction.world.item.GachaDrawItemFactory;

import java.util.Optional;

public class GachaSources {
    private static final ResourceLocation SOURCE_ID = ResourceLocation.fromNamespaceAndPath(GachaConstants.MODID, "gacha_source");
    public static final BlockCapability<IGachaSource, Void> BLOCK = BlockCapability.createVoid(SOURCE_ID, IGachaSource.class);
    public static final EntityCapability<IGachaSource, Void> ENTITY = EntityCapability.createVoid(SOURCE_ID, IGachaSource.class);
    public static final ItemCapability<IGachaSource, Void> ITEM = ItemCapability.createVoid(SOURCE_ID, IGachaSource.class);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, GachaConstants.MODID);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GachaSourceSettings>> SETTINGS =
            ATTACHMENTS.register("gacha_source", () -> AttachmentType.builder(() -> GachaSourceSettings.DEFAULT)
                    .serialize(GachaSourceSettings.CODEC, value -> !value.equals(GachaSourceSettings.DEFAULT)).build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
        bus.addListener(EventPriority.LOWEST, GachaSources::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            registerBlock(event, type);
        }
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            registerEntity(event, type);
        }
        event.registerItem(ITEM, (stack, context) -> new ItemSource(stack),
                BuiltInRegistries.ITEM.stream().toArray(Item[]::new));
    }

    private static <T extends BlockEntity> void registerBlock(RegisterCapabilitiesEvent event, BlockEntityType<T> type) {
        event.registerBlockEntity(BLOCK, type, (holder, context) -> new HolderSource(holder, holder,
                new ItemStack(holder.getBlockState().getBlock())));
    }

    private static <T extends Entity> void registerEntity(RegisterCapabilitiesEvent event, EntityType<T> type) {
        event.registerEntity(ENTITY, type, (holder, context) -> new HolderSource(holder, holder,
                holder.getPickResult() == null ? ItemStack.EMPTY : holder.getPickResult()));
    }

    public static @Nullable IGachaSource find(MenuProvider provider) {
        if (provider instanceof BlockEntity holder && holder.getLevel() != null) {
            return holder.getLevel().getCapability(BLOCK, holder.getBlockPos(), holder.getBlockState(), holder);
        }
        if (provider instanceof Entity holder) {
            return holder.getCapability(ENTITY);
        }
        if (provider instanceof IMenuProviderLootExtension extension) {
            return extension.gachaaddiction$getGachaSource();
        }
        return null;
    }

    private record HolderSource(IAttachmentHolder holder, Object target, ItemStack display) implements IGachaSource {
        @Override
        public GachaSourceSettings getSettings() {
            return holder.getData(SETTINGS);
        }

        @Override
        public void setSettings(GachaSourceSettings settings) {
            holder.setData(SETTINGS, settings);
            if (target instanceof BlockEntity blockEntity) {
                blockEntity.setChanged();
            }
        }

        @Override
        public ItemStack getDisplayItem() {
            return display.copyWithCount(1);
        }

        @Override
        public Object getLockTarget() {
            return target;
        }
    }

    private record ItemSource(ItemStack stack) implements IGachaSource {
        @Override
        public GachaSourceSettings getSettings() {
            var definition = GachaDrawItemFactory.find(stack);
            if (definition.isPresent()) {
                return definition.get().source();
            }
            CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
                    .getCompound(GachaConstants.MODID);
            return new GachaSourceSettings(Optional.ofNullable(ResourceLocation.tryParse(
                    data.getString(GachaSourceSettings.TYPE_KEY))), data.getBoolean(GachaSourceSettings.FORCE_WATCH_KEY),
                    data.contains(GachaSourceSettings.SECONDARY_TYPE_KEY)
                            ? Optional.of(GachaSecondaryType.byName(data.getString(GachaSourceSettings.SECONDARY_TYPE_KEY))) : Optional.empty(),
                    data.contains(GachaSourceSettings.CHOICE_COUNT_KEY)
                            ? Math.clamp(data.getInt(GachaSourceSettings.CHOICE_COUNT_KEY), 2, GachaSecondaryType.MAXIMUM_CHOICES)
                            : GachaSecondaryType.DEFAULT_CHOICES,
                    GachaRandomType.byName(data.getString(GachaSourceSettings.RANDOM_TYPE_KEY)));
        }

        @Override
        public void setSettings(GachaSourceSettings settings) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
                CompoundTag data = root.getCompound(GachaConstants.MODID);
                settings.gachaType().ifPresentOrElse(id -> data.putString(GachaSourceSettings.TYPE_KEY, id.toString()),
                        () -> data.remove(GachaSourceSettings.TYPE_KEY));
                data.putBoolean(GachaSourceSettings.FORCE_WATCH_KEY, settings.forceWatch());
                settings.secondaryType().ifPresentOrElse(type -> data.putString(GachaSourceSettings.SECONDARY_TYPE_KEY,
                        type.getSerializedName()), () -> data.remove(GachaSourceSettings.SECONDARY_TYPE_KEY));
                data.putInt(GachaSourceSettings.CHOICE_COUNT_KEY, settings.choiceCount());
                data.putString(GachaSourceSettings.RANDOM_TYPE_KEY, settings.randomType().getSerializedName());
                root.put(GachaConstants.MODID, data);
            });
        }

        @Override
        public ItemStack getDisplayItem() {
            return stack.copyWithCount(1);
        }

        @Override
        public Object getLockTarget() {
            return stack;
        }
    }
}
