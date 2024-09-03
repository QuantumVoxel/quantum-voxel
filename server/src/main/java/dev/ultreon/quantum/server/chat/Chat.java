package dev.ultreon.quantum.server.chat;

import dev.ultreon.quantum.api.commands.CommandSender;
import dev.ultreon.quantum.api.commands.MessageCode;
import dev.ultreon.quantum.text.ColorCode;
import dev.ultreon.quantum.text.Formatter;
import dev.ultreon.quantum.text.MutableText;
import dev.ultreon.quantum.text.TextObject;
import dev.ultreon.quantum.util.RgbColor;

public class Chat {
    public static void sendServerMessage(CommandSender sender, String message) {
        sender.sendMessage("[light blue][[white]SERVER[light blue]] [grey]" + message);
    }

    public static void sendFatal(CommandSender sender, String message) {
        sender.sendMessage("[light red][ERROR] [white]" + message);
    }

    public static void sendFatal(CommandSender sender, MessageCode code, String message) {
        sender.sendMessage(Chat.formatFatal(sender, message, code.getId()));
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage("[light red][ERROR] [white]" + message);
    }

    public static void sendError(CommandSender sender, String message, String name) {
        sender.sendMessage(Chat.formatError(sender, message, name));
    }

    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage("[gold][WARNING] [white]" + message);
    }

    public static void sendInfo(CommandSender sender, String message) {
        sender.sendMessage("[light blue][INFO] [white]" + message);
    }

    public static void sendDebug(CommandSender sender, String message) {
        sender.sendMessage("[grey][DEBUG] [white]" + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage("[light green][SUCCESS] [white]" + message);
    }

    public static void sendDenied(CommandSender sender, String message) {
        sender.sendMessage("[gold][[light red]DENIED[gold]] [grey]" + message);
    }

    public static MutableText formatServerMessage(CommandSender formater, String message) {
        return new Formatter(true, false, "[light blue][[white]SERVER[light blue]] [grey]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.YELLOW)).parse().getResult();
    }

    public static MutableText formatFatal(CommandSender formater, String message) {
        return new Formatter(true, false, "[dark red][FATAL] [grey]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.GRAY)).parse().getResult();
    }

    public static MutableText formatError(CommandSender formater, String message) {
        return new Formatter(true, false, "[light red][ERROR] [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.WHITE)).parse().getResult();
    }

    public static MutableText formatWarning(CommandSender formater, String message) {
        return new Formatter(true, false, "[gold][WARNING] [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.WHITE)).parse().getResult();
    }

    public static MutableText formatInfo(CommandSender formater, String message) {
        return new Formatter(true, false, "[light blue][INFO] [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.WHITE)).parse().getResult();
    }

    public static MutableText formatDebug(CommandSender formater, String message) {
        return new Formatter(true, false, "[grey][DEBUG] [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.WHITE)).parse().getResult();
    }

    public static MutableText formatSuccess(CommandSender formater, String message) {
        return new Formatter(true, false, "[light green][SUCCESS] [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.WHITE)).parse().getResult();
    }

    public static MutableText formatDenied(CommandSender formater, String message) {
        return new Formatter(true, false, "[gold][[light red]DENIED[gold]] [grey]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.GRAY)).parse().getResult();
    }

    public static void sendVoidObject(CommandSender sender) {

    }

    public static void sendObject(CommandSender sender, Object object) {

    }

    public static MutableText formatError(CommandSender sender, String message, String name) {
        return new Formatter(true, false, "[light red][ERROR] [white]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.RED)).parse().getResult();
    }

    public static MutableText formatFatal(CommandSender sender, String message, String name) {
        return new Formatter(true, false, "[dark red][FATAL] [grey]" + message, TextObject.empty(), TextObject.empty(), null, RgbColor.of(ColorCode.DARK_RED)).parse().getResult();
    }
}
