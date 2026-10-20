package com.example.itemrestrict;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-only restriction data.
 *
 * <p>It is stored inside the vanilla {@code minecraft:custom_data} component, not in a
 * custom data component. Data component types are a synchronized registry; adding a
 * custom one would require every client to install this mod. {@code custom_data} is a
 * vanilla component, so a vanilla/NeoForge client without this mod can still decode it.</p>
 */
public record ItemRestriction(List<String> allowedTags, String denyMessage) {

    private static final String ROOT_KEY = "itemrestrict:restriction";
    private static final String TAGS_KEY = "allowed_tags";
    private static final String MESSAGE_KEY = "deny_message";

    /**
     * @return the restriction, or {@code null} if the item is unrestricted
     */
    public static ItemRestriction get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }

        CompoundTag root = data.copyTag();
        if (!root.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag restriction = root.getCompound(ROOT_KEY);
        ListTag list = restriction.getList(TAGS_KEY, Tag.TAG_STRING);
        List<String> tags = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            tags.add(list.getString(i));
        }
        return new ItemRestriction(List.copyOf(tags), restriction.getString(MESSAGE_KEY));
    }

    public static void set(ItemStack stack, ItemRestriction restriction) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> {
            CompoundTag tag = new CompoundTag();
            ListTag list = new ListTag();
            for (String allowedTag : restriction.allowedTags()) {
                list.add(StringTag.valueOf(allowedTag));
            }
            tag.put(TAGS_KEY, list);
            if (!restriction.denyMessage().isEmpty()) {
                tag.putString(MESSAGE_KEY, restriction.denyMessage());
            }
            root.put(ROOT_KEY, tag);
        });
    }

    public static void remove(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> root.remove(ROOT_KEY));
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null && data.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        }
    }

    public boolean allows(Player player) {
        for (String tag : allowedTags) {
            if (player.getTags().contains(tag)) {
                return true;
            }
        }
        return false;
    }
}
