package com.example.itemrestrict;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

public final class ItemRestrictCommands {

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("itemrestrict")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("tag", StringArgumentType.string())
                                        .executes(ItemRestrictCommands::add)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("tag", StringArgumentType.string())
                                        .executes(ItemRestrictCommands::remove)))
                        .then(Commands.literal("clear")
                                .executes(ItemRestrictCommands::clear))
                        .then(Commands.literal("info")
                                .executes(ItemRestrictCommands::info))
                        .then(Commands.literal("check")
                                .executes(ItemRestrictCommands::check))
        );
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.empty_hand"));
            return 0;
        }

        String tag = StringArgumentType.getString(context, "tag").trim();
        if (tag.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.empty_tag"));
            return 0;
        }

        ItemRestriction old = ItemRestriction.get(stack);
        List<String> tags = old == null ? new ArrayList<>() : new ArrayList<>(old.allowedTags());
        if (!tags.contains(tag)) {
            tags.add(tag);
        }

        ItemRestriction.set(stack, new ItemRestriction(
                List.copyOf(tags),
                old == null ? "" : old.denyMessage()
        ));
        sync(player);
        context.getSource().sendSuccess(
                () -> Component.translatable("command.itemrestrict.added", tag), false);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.empty_hand"));
            return 0;
        }

        String tag = StringArgumentType.getString(context, "tag").trim();
        ItemRestriction old = ItemRestriction.get(stack);
        if (old == null) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.none"));
            return 0;
        }

        List<String> tags = new ArrayList<>(old.allowedTags());
        if (!tags.remove(tag)) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.tag_not_present", tag));
            return 0;
        }

        if (tags.isEmpty()) {
            ItemRestriction.remove(stack);
        } else {
            ItemRestriction.set(stack, new ItemRestriction(
                    List.copyOf(tags), old.denyMessage()));
        }
        sync(player);
        context.getSource().sendSuccess(
                () -> Component.translatable("command.itemrestrict.removed", tag), false);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.empty_hand"));
            return 0;
        }
        if (ItemRestriction.get(stack) == null) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.none"));
            return 0;
        }

        ItemRestriction.remove(stack);
        sync(player);
        context.getSource().sendSuccess(
                () -> Component.translatable("command.itemrestrict.cleared"), false);
        return 1;
    }

    private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.empty_hand"));
            return 0;
        }

        ItemRestriction restriction = ItemRestriction.get(stack);
        if (restriction == null) {
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.itemrestrict.none"), false);
        } else if (restriction.allowedTags().isEmpty()) {
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.itemrestrict.locked"), false);
        } else {
            context.getSource().sendSuccess(
                    () -> Component.translatable("command.itemrestrict.info",
                            String.join(", ", restriction.allowedTags())), false);
        }
        return 1;
    }

    private static int check(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            context.getSource().sendFailure(Component.translatable("command.itemrestrict.empty_hand"));
            return 0;
        }

        boolean denied = ItemRestrictEvents.isDenied(player, stack);
        context.getSource().sendSuccess(
                () -> Component.translatable(denied
                        ? "command.itemrestrict.check.denied"
                        : "command.itemrestrict.check.allowed"), false);
        return 1;
    }

    private static void sync(ServerPlayer player) {
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    private ItemRestrictCommands() {
    }
}
