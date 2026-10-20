package com.example.itemrestrict;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ItemRestrictEvents {

    private static final Map<UUID, Long> LAST_DENY = new HashMap<>();

    public static boolean isDenied(ServerPlayer player, ItemStack stack) {
        ItemRestriction restriction = ItemRestriction.get(stack);
        return restriction != null && !restriction.allows(player);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isDenied(player, event.getItemStack())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        deny(player, event.getItemStack());
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (!isDenied(player, event.getItemStack())) {
            return;
        }
        event.cancelWithResult(ItemInteractionResult.FAIL);
        deny(player, event.getItemStack());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isDenied(player, event.getItemStack())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        deny(player, event.getItemStack());
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!isDenied(player, event.getItemStack())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
        deny(player, event.getItemStack());
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemRestriction restriction = ItemRestriction.get(event.getItemStack());
        if (restriction == null) {
            return;
        }
        if (restriction.allowedTags().isEmpty()) {
            event.getToolTip().add(Component.translatable("tooltip.itemrestrict.locked")
                    .withStyle(ChatFormatting.DARK_RED));
        } else {
            event.getToolTip().add(Component.translatable("tooltip.itemrestrict.allowed_tags",
                            String.join(", ", restriction.allowedTags()))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_DENY.remove(event.getEntity().getUUID());
    }

    private static void deny(ServerPlayer player, ItemStack stack) {
        long now = player.serverLevel().getGameTime();
        Long last = LAST_DENY.get(player.getUUID());
        if (last != null && now - last < 10L) {
            return;
        }
        LAST_DENY.put(player.getUUID(), now);

        ItemRestriction restriction = ItemRestriction.get(stack);
        Component message = restriction != null && !restriction.denyMessage().isEmpty()
                ? Component.literal(restriction.denyMessage())
                : Component.translatable("message.itemrestrict.restricted");

        player.displayClientMessage(message.copy().withStyle(ChatFormatting.RED), true);
        player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    private ItemRestrictEvents() {
    }
}
