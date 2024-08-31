package dev.ultreon.quantum.text;

import dev.ultreon.quantum.entity.player.Player;
import dev.ultreon.quantum.registry.CustomKeyRegistry;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.player.ServerPlayer;
import dev.ultreon.quantum.text.icon.EmoteMap;
import dev.ultreon.quantum.text.icon.IconMap;
import dev.ultreon.quantum.util.NamespaceID;
import dev.ultreon.quantum.util.RgbColor;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharPredicate;
import org.apache.commons.lang3.CharUtils;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Formatter {
    private final boolean allowFormatting;
    private final boolean doPing;
    private final String message;
    private final TextObject prefix;
    private final TextObject textPrefix;
    private final @Nullable Player sender;
    private MutableText builder = MutableText.literal("");

    // Predicates
    private final CharPredicate emotePredicate = it -> CharUtils.isAsciiAlphanumeric(it) || "_-".contains(Character.toString(it));

    // Redirect
    private @MonotonicNonNull ParseResult redirectValue = null;
    private boolean redirect = false;

    // Reader
    private int offset = 0;

    // Colors
    private final RgbColor messageColor;

    // Formatting
    private StringBuilder currentBuilder = new StringBuilder();
    private RgbColor currentColor;
    private @Nullable ClickEvent currentClickEvent = null;
    private @Nullable HoverEvent<?> currentHoverEvent = null;
    private final @Nullable NamespaceID currentFont = null;
    private boolean bold = false;
    private boolean italic = false;
    private boolean underlined = false;
    private boolean strikethrough = false;

    // Flags
    private final @MonotonicNonNull ParseResult parsed = null;

    // Pings
    private final List<Player> pinged = new ArrayList<>();

    // Locks
    private final Object lock = new Object();

    public Formatter(boolean allowFormatting, boolean doPing, String message, TextObject prefix, TextObject textPrefix, @Nullable Player sender, RgbColor defaultMessageColor) {
        this.allowFormatting = allowFormatting;
        this.doPing = doPing;
        this.message = message;
        this.prefix = prefix;
        this.textPrefix = textPrefix;
        this.sender = sender;
        this.messageColor = defaultMessageColor;
        this.currentColor = this.messageColor;
    }

    public ParseResult parse() {
        try {
            return this.parse0();
        } catch (Exception e) {
            return ParseResult.error(e);
        }
    }

    private ParseResult parse0() {
        if (this.allowFormatting) {
            while (this.offset < this.message.length()) {
                switch (this.c()) {
                    case '&':
                        this.parseId();
                        continue;
                    case '@':
                        this.parseMention();
                        continue;
                    case '<':
                        this.parseColor();
                        continue;
                    case '{':
                        this.parseFunction();
                        continue;
                    case '%':
                        this.parseKey();
                        continue;

//                    case ':' -> {
//                        this.parseEmote();
//                        continue;
//                    }

                    case '!':
                        if (this.offset + 1 == this.message.length()) {
                            this.currentBuilder.append(this.c());
                            this.offset++;
                            continue;
                        }
                        this.offset++;
                        if (this.c() == '(') {
                            this.parseIcon();
                        } else {
                            currentBuilder.append("!");
                        }
                        continue;
                    default:
                        this.currentBuilder.append(this.c());
                        break;
                }
                if (this.redirect) {
                    this.redirect = false;
                    return this.redirectValue;
                }
                this.offset++;
            }
        } else {
            var offset = 0;
            while (offset < this.message.length()) {
                var c = this.message.charAt(offset);
                if (c == '@') {
                    offset++;
                    final var name = new StringBuilder();
                    while (true) {
                        c = this.message.charAt(offset);
                        if (!("_-".contains(Character.toString(c)) || CharUtils.isAsciiAlphanumeric(c))) break;
                        offset++;
                        if (offset >= this.message.length()) {
                            break;
                        }
                        name.append(c);
                    }
                    final var player = QuantumServer.get().getPlayer(name.toString());
                    if (player != null && !name.toString().isEmpty() && Objects.equals(player.getName(), name.toString()) && this.doPing) {
                        this.pushBuilder();
                        this.addPingText(player);
                    } else {
                        this.currentBuilder.append("@").append(name);
                    }
                    continue;
                } else {
                    this.currentBuilder.append(c);
                }
                if (this.redirect) {
                    this.redirect = false;
                    return this.redirectValue;
                }
                offset++;
            }
        }
        final var s = this.currentBuilder.toString();
        final var textObj =
                TextObject.literal(s).style(style -> style.color(this.currentColor).bold(this.bold)
                        .italic(this.italic).underline(this.underlined)
                        .strikethrough(this.strikethrough));
        this.builder.append(textObj);
        return new ParseResult(this.pinged, this.prefix, this.textPrefix, this.builder);
    }

    public static TextObject format(String message) {
        return Formatter.format(message, QuantumServer.get() != null);
    }

    public static TextObject format(String message, boolean doPing) {
        var formatter = new Formatter(true, doPing, message, TextObject.empty(), TextObject.empty(), null, RgbColor.WHITE);
        var parse = formatter.parse();
        return parse.getResult();
    }

    private void parseEmote() {
        // TODO: Add emote parsing system. (Currently broken)
        CharList characters = new CharArrayList();

        do {
            this.offset++;
            if (this.offset >= this.message.length()) {
                this.currentBuilder.append(':');
                this.pushBuilder();

                this.redirect(String.join("", new String(characters.toCharArray())));
                return;
            }
            characters.add(this.c());
        } while (this.c() != ':' && this.emotePredicate.test(this.c()));

        if (this.c() != ':') {
            this.currentBuilder.append(':');
            this.pushBuilder();

            var toFormat = new String(characters.toCharArray());
            this.redirect(toFormat);
            return;
        }

        this.offset++;

        var arg = new String(characters.toCharArray()).substring(0, characters.size() - 1);
        this.pushBuilder();

        if (EmoteMap.INSTANCE.get(arg) != null) {
            this.builder.append(new FontIconObject(arg, EmoteMap.INSTANCE));
        }
    }

    private void parseIcon() {
        CharList characters = new CharArrayList();

        do {
            this.offset++;
            if (this.offset >= this.message.length()) {
                return;
            }
            characters.add(this.c());
        } while (this.c() != ')');

        this.offset++;

        var arg = new String(characters.toCharArray()).substring(0, characters.size() - 1);
        this.pushBuilder();

        if (IconMap.INSTANCE.get(arg) != null) {
            this.builder.append(new FontIconObject(arg, IconMap.INSTANCE));
        }
    }

    public void parseId() {
        this.offset++;
        if (this.offset >= this.message.length()) {
            this.currentBuilder.append('&');
            return;
        }
        switch (this.c()) {
            case '#':
                if (this.offset + 6 > this.message.length()) {
                    this.currentBuilder.append('&');
                    this.currentBuilder.append(this.c());
                    this.offset++;
                    return;
                }
                this.pushBuilder();
                this.currentColor = RgbColor.rgb(Integer.parseInt(this.message.substring(this.offset + 1, this.offset + 7), 16));
                this.offset += 7;
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                this.pushBuilder();
                this.currentColor = RgbColor.of(ColorCode.getByChar(Character.toLowerCase(this.c())));
                this.offset++;
                break;
            case 'l':
                this.bold = true;
                this.offset++;
                break;
            case 'm':
                this.strikethrough = true;
                this.offset++;
                break;
            case 'n':
                this.underlined = true;
                this.offset++;
                break;
            case 'o':
                this.italic = true;
                this.offset++;
                break;
            case 'r':
                this.pushBuilder();
                this.currentColor = this.messageColor;
                this.bold = false;
                this.italic = false;
                this.underlined = false;
                this.strikethrough = false;
                this.currentClickEvent = null;
                this.currentHoverEvent = null;
                this.offset++;
                break;
            default:
                this.currentBuilder.append('&');
                this.currentBuilder.append(this.c());
                this.offset++;
                break;
        }
    }

    private void parseKey() {
        CharList characters = new CharArrayList();

        do {
            this.offset++;
            if (this.offset >= this.message.length()) {
                this.currentBuilder.append('%');
                this.currentBuilder.append(characters.toCharArray());
                return;
            }
            characters.add(this.c());
        } while (this.c() != '%');

        this.offset++;

        var key = String.valueOf(characters.toCharArray()).replace("%", "");
        this.pushBuilder();

        switch (key) {
            case "message-type":
            case "username":
                if (this.sender != null) {
                    this.currentBuilder.append(this.sender.getName());
                }
                break;
            case "player":
                if (this.sender != null) {
                    this.pushBuilder();

                    String hoverText = this.getPlayerHoverText(this.sender);
                    TextObject textObj2 = TextObject.literal(this.sender.getName()).style(style -> style
                            .color(this.currentColor)
                            .bold(this.bold)
                            .italic(this.italic)
                            .underline(this.underlined)
                            .strikethrough(this.strikethrough)
                            .hoverEvent(HoverEvent.text(new Formatter(true, false, hoverText, TextObject.empty(), TextObject.empty(), null, RgbColor.WHITE).parse().getResult()))
                            .clickEvent(this.currentClickEvent));
                    this.builder.append(textObj2);
                    this.currentBuilder.append(this.sender.getName());
                }
                break;
            case "console-sender":
                this.currentBuilder.append("Console");
                break;
            case "time":
                this.currentBuilder.append(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
                break;
            case "date":
                this.currentBuilder.append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                break;
            default:
                var identifier = NamespaceID.tryParse(key);
                if (identifier == null) {
                    this.currentBuilder.append('%');
                    this.currentBuilder.append(key);
                    this.currentBuilder.append('%');
                    break;
                }
                var textKey = CustomKeyRegistry.get(identifier);
                if (textKey == null) {
                    this.currentBuilder.append('%');
                    this.currentBuilder.append(key);
                    this.currentBuilder.append('%');
                    break;
                }
                this.currentBuilder.append(textKey.get(this.sender));
                break;
        }
    }

    private void parseFunction() {
        CharList characters = new CharArrayList();
        var type = "";

        var mode = Mode.TYPE;
        List<String> arguments = new ArrayList<>();
        var currentArg = new StringBuilder();
        var ignoreSpaces = true;
        while (this.c() != '}') {
            this.offset++;
            if (this.offset >= this.message.length()) {
                return;
            }
            if (ignoreSpaces) {
                if (this.c() == ' ') continue;
                ignoreSpaces = false;
            }
            if (mode == Mode.TYPE) {
                switch (this.c()) {
                    case ':':
                        type = new String(characters.toCharArray()).replaceAll("[:}]", "");
                        characters.clear();
                        mode = Mode.BODY;
                        ignoreSpaces = true;
                        continue;
                    case '}':
                        return;
                }
            }
            if (mode == Mode.BODY) {
                if (this.c() == '\\') {
                    this.offset++;
                    if (this.offset >= this.message.length()) {
                        return;
                    }
                } else if (this.c() == ';') {
                    arguments.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            }
            characters.add(this.c());
        }
        arguments.add(currentArg.toString());

        this.pushBuilder();

        switch (type) {
            case "click":
                var actionName = arguments.remove(0);
                ClickEvent event = null;
                switch (actionName) {
                    case "@":
                    case "web":
                    case "url":
                    case "open-url":
                        if (arguments.isEmpty()) return;
                        var url = arguments.remove(0);
                        try {
                            event = ClickEvent.openUri(new URI(url));
                        } catch (URISyntaxException ignored) {

                        }
                        break;
                    case "#":
                    case "clip":
                    case "clipboard":
                    case "copy":
                    case "cp":
                    case "copy-to-clipboard":
                        if (arguments.isEmpty()) return;
                        var text = arguments.remove(0);
                        event = ClickEvent.copyToClipboard(text);
                        break;
                    case "/":
                    case "cmd":
                    case "command":
                        if (arguments.isEmpty()) return;
                        var cmd = arguments.remove(0);
                        event = ClickEvent.runCommand(cmd);
                        break;
                    case ">":
                    case "suggest":
                    case "suggest-msg":
                    case "put-msg":
                    case "put":
                    case "example":
                    case "example-msg":
                        if (arguments.isEmpty()) return;
                        cmd = arguments.remove(0);
                        event = ClickEvent.suggestMessage(cmd);
                        break;
                    default:
                        if (actionName.startsWith("@")) {
                            url = actionName.substring(1);
                            try {
                                event = ClickEvent.openUri(new URI(url));
                            } catch (URISyntaxException ignored) {

                            }
                        }
                        if (actionName.startsWith("#")) {
                            text = actionName.substring(1);
                            event = ClickEvent.copyToClipboard(text);
                        }
                        if (actionName.startsWith("/")) {
                            cmd = actionName.substring(1);
                            event = ClickEvent.runCommand(cmd);
                        }
                        if (actionName.startsWith(">")) {
                            cmd = actionName.substring(1);
                            event = ClickEvent.suggestMessage(cmd);
                        }
                        break;
                }
                this.currentClickEvent = event;
                break;

            case "hover":
                this.currentHoverEvent = HoverEvent.text(
                        new Formatter(
                                true,
                                false,
                                arguments.remove(0),
                                TextObject.empty(),
                                this.textPrefix,
                                null,
                                this.messageColor
                        ).parse().getResult()
                );
                break;
        }
        this.offset++;
    }

    private void parseMention() {
        if (this.offset + 1 == this.message.length()) {
            this.offset++;
            return;
        }

        this.offset++;

        var name = new StringBuilder();
        while (true) {
            if (!("_-".contains(String.valueOf(this.c())) || Character.isLetterOrDigit(this.c())))
                break;

            name.append(this.c());
            this.offset++;
            if (this.offset + 1 >= this.message.length()) {
                break;
            }
        }

        QuantumServer quantumServer = QuantumServer.get();
        var player = quantumServer == null ? null : quantumServer.getPlayer(name.toString());

        if (player != null && !name.toString().isEmpty() && player.getName().contentEquals(name) && this.doPing) {
            this.pushBuilder();

            var hoverText = String.format(
                    "[light blue]%s\n[grey]Name [/]%s</i>\n[dark grey]%s".trim(),
                    ColorCode.stripColor(player.getPublicName()),
                    player.getName(),
                    player.getUuid()
            );

            TextObject textComponent1 = TextObject.literal(
                    "@" + player.getName()).style(style -> style.color(RgbColor.of(ColorCode.BLUE))
                    .bold(false)
                    .italic(false)
                    .underline(true)
                    .strikethrough(false)
                    .hoverEvent(HoverEvent.text(new Formatter(true, false, hoverText, TextObject.empty(), TextObject.empty(), null, RgbColor.WHITE).parse().getResult()))
                    .clickEvent(ClickEvent.suggestMessage("@" + player.getName())));

            this.builder.append(textComponent1);
            this.pinged.add(player);
        } else {
            this.currentBuilder.append("@").append(name);
        }
    }

    public void parseColor() {
        var characters = new CharArrayList();

        while (this.c() != '>') {
            this.offset++;
            if (this.offset >= this.message.length()) {
                this.currentBuilder.append('<');
                this.currentBuilder.append(characters.toCharArray());
                return;
            }
            characters.push(this.c());
        }

        var arg = new String(characters.toCharArray()).replace(">", "");

        this.pushBuilder();

        switch (arg) {
            case "/client/":
            case "/message/":
            case "/message-color/":
                this.currentColor = this.messageColor;
                break;
            case "b":
            case "bold":
            case "fat":
            case "%":
                this.bold = true;
                break;
            case "/b":
            case "/bold":
            case "/fat":
            case "/%":
                this.bold = false;
                break;
            case "i":
            case "italic":
            case "+":
                this.italic = true;
                break;
            case "/i":
            case "/italic":
            case "/+":
                this.italic = false;
                break;
            case "u":
            case "underlined":
            case "underline":
            case "_":
                this.underlined = true;
                break;
            case "/u":
            case "/underlined":
            case "/underline":
            case "/_":
                this.underlined = false;
                break;
            case "s":
            case "strikethrough":
            case "st":
            case "-":
                this.strikethrough = true;
                break;
            case "/s":
            case "/strikethrough":
            case "/st":
            case "/-":
                this.strikethrough = false;
                break;
            case "/":
            case "/*":
            case "r":
            case "reset":
            case "clear":
                this.bold = false;
                this.italic = false;
                this.underlined = false;
                this.strikethrough = false;
                this.currentColor = this.messageColor;
                this.currentClickEvent = null;
                this.currentHoverEvent = null;
                break;
            case "red":
                this.currentColor = RgbColor.of(ColorCode.RED);
                break;
            case "yellow":
                this.currentColor = RgbColor.of(ColorCode.YELLOW);
                break;
            case "lime":
            case "green":
                this.currentColor = RgbColor.of(ColorCode.GREEN);
                break;
            case "cyan":
            case "aqua":
                this.currentColor = RgbColor.of(ColorCode.AQUA);
                break;
            case "blue":
                this.currentColor = RgbColor.of(ColorCode.BLUE);
                break;
            case "magenta":
            case "light-purple":
                this.currentColor = RgbColor.of(ColorCode.LIGHT_PURPLE);
                break;
            case "dark-red":
                this.currentColor = RgbColor.of(ColorCode.DARK_RED);
                break;
            case "gold":
                this.currentColor = RgbColor.of(ColorCode.GOLD);
                break;
            case "dark-green":
                this.currentColor = RgbColor.of(ColorCode.DARK_GREEN);
                break;
            case "turquoise":
            case "dark-aqua":
                this.currentColor = RgbColor.of(ColorCode.DARK_AQUA);
                break;
            case "dark-blue":
                this.currentColor = RgbColor.of(ColorCode.DARK_BLUE);
                break;
            case "purple":
            case "dark-purple":
                this.currentColor = RgbColor.of(ColorCode.DARK_PURPLE);
                break;
            case "gray-16":
            case "white":
                this.currentColor = RgbColor.of(ColorCode.WHITE);
                break;
            case "gray-15":
                this.currentColor = RgbColor.rgb(0xf0f0f0);
                break;
            case "gray-14":
                this.currentColor = RgbColor.rgb(0xe0e0e0);
                break;
            case "gray-13":
                this.currentColor = RgbColor.rgb(0xd0d0d0);
                break;
            case "gray-12":
            case "light-gray":
                this.currentColor = RgbColor.rgb(0xc0c0c0);
                break;
            case "gray-11":
                this.currentColor = RgbColor.rgb(0xb0b0b0);
                break;
            case "gray-10":
                this.currentColor = RgbColor.rgb(0xa0a0a0);
                break;
            case "gray":
            case "silver":
                this.currentColor = RgbColor.of(ColorCode.GRAY);
                break;
            case "gray-9":
                this.currentColor = RgbColor.rgb(0x909090);
                break;
            case "gray-8":
                this.currentColor = RgbColor.rgb(0x808080);
                break;
            case "mid-gray":
            case "gray-7":
                this.currentColor = RgbColor.rgb(0x707070);
                break;
            case "gray-6":
                this.currentColor = RgbColor.rgb(0x606060);
                break;
            case "dark-gray":
                this.currentColor = RgbColor.of(ColorCode.DARK_GRAY);
                break;
            case "gray-5":
                this.currentColor = RgbColor.rgb(0x505050);
                break;
            case "gray-4":
                this.currentColor = RgbColor.rgb(0x404040);
                break;
            case "darker-gray":
            case "gray-3":
                this.currentColor = RgbColor.rgb(0x303030);
                break;
            case "gray-2":
                this.currentColor = RgbColor.rgb(0x202020);
                break;
            case "gray-1":
                this.currentColor = RgbColor.rgb(0x101010);
                break;
            case "gray-0":
                this.currentColor = RgbColor.rgb(0x000000);
                break;
            case "black":
                this.currentColor = RgbColor.of(ColorCode.BLACK);
                break;
            case "brown":
                this.currentColor = RgbColor.rgb(0x614E36);
                break;
            case "azure":
                this.currentColor = RgbColor.rgb(0x007FFF);
                break;
            case "mint":
                this.currentColor = RgbColor.rgb(0x00FF7F);
                break;
            case "orange":
                this.currentColor = RgbColor.rgb(0xFF7F00);
                break;
            case "pure-yellow":
                this.currentColor = RgbColor.rgb(0xFFFF00);
                break;
            case "yellow-gold":
                this.currentColor = RgbColor.rgb(0xFFD500);
                break;
            case "pure-gold":
                this.currentColor = RgbColor.rgb(0xFFC500);
                break;
            case "dark-yellow":
                this.currentColor = RgbColor.rgb(0x7F7F00);
                break;
            case "method":
                this.currentColor = RgbColor.rgb(0x61AFEF);
                break;
            case "string-escape":
                this.currentColor = RgbColor.rgb(0x2BBAC5);
                break;
            case "string":
                this.currentColor = RgbColor.rgb(0x89CA78);
                break;
            case "class":
                this.currentColor = RgbColor.rgb(0xE5C07B);
                break;
            case "number":
                this.currentColor = RgbColor.rgb(0xD19A66);
                break;
            case "enum-value":
                this.currentColor = RgbColor.rgb(0xEF596F);
                break;
            case "keyword":
                this.currentColor = RgbColor.rgb(0xD55FDE);
                break;
            default:
                this.currentBuilder.append('<');
                this.currentBuilder.append(arg);
                this.currentBuilder.append('>');
                break;
        }

        this.offset++;
    }

    private void redirect(String message) {
        this.redirect = true;
        var it = new Formatter(
                this.allowFormatting,
                this.doPing, message,
                this.builder,
                this.textPrefix,
                this.sender,
                this.messageColor
        );
        it.currentColor = this.currentColor;
        it.bold = this.bold;
        it.italic = this.italic;
        it.underlined = this.underlined;
        it.strikethrough = this.strikethrough;
        it.currentHoverEvent = this.currentHoverEvent;
        it.currentClickEvent = this.currentClickEvent;
        it.currentBuilder = this.currentBuilder;
        this.redirectValue = it.parse();
    }

    private void pushBuilder() {
        LiteralText obj = TextObject.literal(this.currentBuilder.toString()).style(style -> style
                .color(this.currentColor)
                .bold(this.bold)
                .italic(this.italic)
                .underline(this.underlined)
                .strikethrough(this.strikethrough)
                .hoverEvent(this.currentHoverEvent)
                .clickEvent(this.currentClickEvent));

        if (this.builder == null) this.builder = obj;
        this.builder.append(obj);
        this.currentBuilder = new StringBuilder();
    }

    public char c() {
        return this.message.charAt(this.offset);
    }

    private void addPingText(ServerPlayer player) {
        // Set hover text.
        final var hoverText = this.getPlayerHoverText(player);
        final var textComponent1 = TextObject.literal(
                "@" + player.getName()).style(style -> style
                .color(RgbColor.of(ColorCode.BLUE))
                .bold(false)
                .italic(false)
                .underline(true)
                .strikethrough(false)
                .hoverEvent(HoverEvent.text(Formatter.format(hoverText, false)))
                .clickEvent(null));

        // Add components and pinged person.
        this.builder.append(textComponent1);
        this.pinged.add(player);
    }


    private String getPlayerHoverText(@Nullable Player player) {
        if (player == null) {
            return "[light red]NULL\n[dark grey]<unknown-entity>";
        }
        // Set hover text.
        return String.format("[light blue]%s\n[grey]Name [/]%s</i>\n[dark grey]%s\n", ColorCode.stripColor(player.getPublicName()), player.getName(), player.getUuid());
    }

    public @Nullable NamespaceID getCurrentFont() {
        return currentFont;
    }

    private enum Mode {
        TYPE,
        BODY
    }
}