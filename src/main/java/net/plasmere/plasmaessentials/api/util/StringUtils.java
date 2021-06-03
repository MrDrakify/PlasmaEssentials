package net.plasmere.plasmaessentials.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.plasmere.plasmaessentials.PlasmaEssentials;
import net.plasmere.plasmaessentials.api.text.TextFormat;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final String EMPTY_STRING = "";
    private static final Pattern INVALID_FILE_CHARS = Pattern.compile("[^a-z0-9-]");
    public static final Pattern USERNAME_PATTERN = Pattern.compile("/[a-zA-Z][a-zA-Z0-9-_]/gi");
    public static final Pattern UUID_PATTERN = Pattern.compile("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    public static String sanitizeFileName(final String name) {
        return INVALID_FILE_CHARS.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
    }

    public static String stringToUsername(final String string) {
        return string.replaceAll("[^a-zA-Z0-9_]", "");
    }

    public static String normalizeCapitalization(@NotNull final String string) {
        StringBuilder builder = new StringBuilder();
        String[] strings = string.split(" ");
        int index = 0;
        for (String str : strings) {
            builder.append(
                    String.valueOf(str.charAt(0)).toUpperCase(Locale.ROOT)
            ).append(
                    str.substring(1)
            );

            index++;
            if (index != strings.length) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static String socketAddressToIp(@NotNull final String address) {
        String string = address;
        string = string.substring(string.indexOf("/") + 1);
        string = string.substring(0, string.indexOf(":"));
        return string;
    }

    public static String socketAddressToPort(@NotNull final String address) {
        String string = address;
        string = string.substring(string.indexOf("/") + 1);
        string = string.substring(string.indexOf(":") + 1);
        return string;
    }

    public static class Calculator implements Comparable<Double> {
        private final String input;
        private double output;

        public Calculator(@NotNull final String input) {
            this.input = input;
        }

        public void calculate() throws Exception {
            String[] strings = this.input.split("(?<=[-+*^%/])|(?=[-+*^%/])");

            for (int i = 0; i < strings.length - 2; i+=2) {
                double x = Double.parseDouble(strings[i]);
                double y = Double.parseDouble(strings[i + 2]);

                Operator operator = Operator.byIcon(strings[i + 1]);
                if (operator == null) {
                    throw new Exception("Invalid Operation!");
                }

                if (i == 0) {
                    this.output = operator.operate(x, y);
                } else {
                    this.output = operator.operate(this.output, y);
                }
            }
        }

        public String getInput() {
            String[] strings = this.input.split("(?<=[-+*^%/])|(?=[-+*^%/])");
            StringBuilder builder = new StringBuilder();

            for (String string : strings) {
                builder.append(string);
            }

            return builder.toString();
        }

        public double result() {
            return this.output;
        }

        @Override
        public int compareTo(@NotNull Double o) {
            return o.compareTo(this.output);
        }

        public static String[] operations() {
            String[] strings = new String[Operator.values().length];
            for (int i = 0; i < Operator.values().length; i++) {
                strings[i] = Operator.values()[i].i;
            }

            return strings;
        }

        private enum Operator {
            SUM("addition", "+"),
            MINUS("subtraction", "-"),
            OBELUS("division", "/"),
            TIMES("multiplication", "*"),
            REMAINDER("modules", "%"),
            POWER_OF("power", "^");

            private final String name;
            private final String i;
            Operator(final String name, final String i) {
                this.name = name;
                this.i = i;
            }

            public double operate(final double x, final double y) throws Exception {
                switch (this) {
                    case SUM:
                        return x + y;
                    case MINUS:
                        return x - y;
                    case TIMES:
                        return x * y;
                    case OBELUS:
                        return x / y;
                    case REMAINDER:
                        return x % y;
                    case POWER_OF:
                        return Math.pow(x, y);
                    default:
                        throw new Exception("Invalid Operation!");
                }
            }

            public String getName() {
                return this.name;
            }

            @Nullable
            public static Operator byIcon(final String i) {
                for (Operator value : values()) {
                    if (value.i.equals(i)) {
                        return value;
                    }
                }

                return null;
            }
        }
    }

    public static String resize(String text, int digits) {
        try {
            digits = getDigits(digits, text.length());
            return text.substring(0, digits);
        } catch (Exception e) {
            return text;
        }
    }

    public static String truncate(String text, int digits) {
        try {
            digits = getDigits(text.indexOf(".") + digits + 1, text.length());
            return text.substring(0, digits);
        } catch (Exception e) {
            return text;
        }
    }

    public static int getDigits(int start, int otherSize){
        if (start <= otherSize) {
            return start;
        } else {
            return otherSize;
        }
    }

    public static String newLined(String text){
        int count = 0;

        File[] f = PlasmaEssentials.getInstance().getPlDir().listFiles();

        if (f != null) count = f.length;

        return text.replace("%newline%", "\n").replace("%uniques%", String.valueOf(count));
    }

    public static boolean isCommand(String msg){
        return msg.startsWith("/");
    }

    public static String codedString(String text){
        return translateAlternateColorCodes('&', text).replace("%nl%", "\n").replace("%newline%", "\n");
    }

    public static final char COLOR_CHAR = '\u00A7';

    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        Validate.notNull(textToTranslate, "Cannot translate null text");
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }

        return new String(b);
    }

    public static MutableText newText() {
        return new LiteralText("");
    }

    public static MutableText newText(@Nullable final String... strings) {
        MutableText text = newText();
        for (String string : strings) {
            text.append(string);
        }
        return text;
    }

    @NotNull
    public static String translate(String string) {
        return translateAlternateColorCodes('&', string);
    }

    public static MutableText newText(final String str) {
        return new LiteralText(translate(str));
    }

    public static MutableText hexedText(String text){
        text = codedString(text);

        try {
            //String ntext = text.replace(ConfigUtils.linkPre, "").replace(ConfigUtils.linkSuff, "");

            Pattern pattern = Pattern.compile("([<][#][1-9a-f][1-9a-f][1-9a-f][1-9a-f][1-9a-f][1-9a-f][>])+", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String found = "";

            String textLeft = text;

            MutableText mt = new LiteralText("");

            int i = 0;
            boolean find = false;

            while (matcher.find()) {
                find = true;
                found = matcher.group(0);
                String colorHex = found.substring(1, found.indexOf('>'));
                String[] split = textLeft.split(Pattern.quote(found));

                if (i == 0) {
                    mt.append(codedString(split[0]));
                }

                //BaseComponent[] bc = new ComponentBuilder(split[1]).color(ChatColor.of(Color.decode(colorHex))).create();
                mt.append(new LiteralText(split[1]).styled(style -> style.withColor(Integer.decode(colorHex))));

                i ++;
            }
            if (! find) return new LiteralText(text);

            return mt;
        } catch (Exception e) {
            e.printStackTrace();
            return new LiteralText(text);
        }
    }

    public static MutableText codedText(String text){
        MutableText mt = hexedText(codedString(text));

        try {
            Pattern pattern = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String foundUrl = "";

            while (matcher.find()) {
                foundUrl = matcher.group(0);

                return makeLinked(codedString(text), foundUrl);
            }
        } catch (Exception e) {
            return mt;
        }
        return mt;
    }

    public static MutableText codedCHText(String text){
        MutableText mt = hexedText(codedString(text));

        try {
            Pattern pattern = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String foundUrl = "";

            while (matcher.find()) {
                foundUrl = matcher.group(0);

                String hover = "&4&n" + foundUrl;

                return makeCHLinked(codedString(text), foundUrl, hover);
            }
        } catch (Exception e) {
            return mt;
        }
        return mt;
    }

    public static MutableText codedCHNamedText(String text, String name){
        LiteralText mt = new LiteralText(codedString(text));

        try {
            Pattern pattern = Pattern.compile("(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            String foundUrl = "";

            while (matcher.find()) {
                foundUrl = matcher.group(0);

                String hover = "&4&n" + foundUrl;

                text = text.replace(name, "&e" + name + "&r");

                return makeCHLinked(codedString(text), foundUrl, hover);
            }
        } catch (Exception e) {
            return mt;
        }
        return mt;
    }

    public static String chattedString(ServerPlayerEntity playerEntity){
        return "&r<" + getDisplayName(playerEntity) + "&r> ";
    }

    public static String argsToStringMinus(String[] args, int... toRemove){
        TreeMap<Integer, String> argsSet = new TreeMap<>();

        for (int i = 0; i < args.length; i++) {
            argsSet.put(i, args[i]);
        }

        for (int remove : toRemove) {
            argsSet.remove(remove);
        }

        return normalize(argsSet);
    }

    public static String getDisplayName(ServerPlayerEntity playerEntity){
//        Team team = playerEntity.getScoreboard().getTeam(Objects.requireNonNull(playerEntity.getScoreboardTeam()).getName());
//        return team.getPrefix().asString() + team.getColor().toString() + playerEntity.getName().asString() + team.getSuffix().asString();
        String dis = Team.decorateName(playerEntity.getScoreboardTeam(), playerEntity.getName()).asString();
        String dis2 = playerEntity.getDisplayName().asString();
        return dis.equals("") ? dis2.equals("") ? playerEntity.getName().asString() : dis2 : dis;
    }

    public static String getDisplayName(Entity entity){
//        Team team = playerEntity.getScoreboard().getTeam(Objects.requireNonNull(playerEntity.getScoreboardTeam()).getName());
//        return team.getPrefix().asString() + team.getColor().toString() + playerEntity.getName().asString() + team.getSuffix().asString();
        String dis = Team.decorateName(entity.getScoreboardTeam(), entity.getName()).asString();
        String dis2 = entity.getDisplayName().asString();
        String dis3 = entity.getName().asString();
        return dis.equals("") ? dis2.equals("") ? dis3.equals("") ? entity.getEntityName() : dis3 : dis2 : dis;
    }

    public static MutableText getPlayerDisplayNameText(ServerPlayerEntity playerEntity){
        MutableText mutableText = Team.decorateName(playerEntity.getScoreboardTeam(), playerEntity.getName());
        return addTellClickEvent(mutableText, playerEntity);
    }

    public static MutableText addTellClickEvent(MutableText component, ServerPlayerEntity playerEntity) {
        String string = playerEntity.getGameProfile().getName();
        return component.styled(style ->
                style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + string + " ")).withHoverEvent(getPlayerHoverEvent(playerEntity)).withInsertion(string)
        );
    }

    public static HoverEvent getPlayerHoverEvent(ServerPlayerEntity playerEntity) {
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new HoverEvent.EntityContent(playerEntity.getType(), playerEntity.getUuid(), playerEntity.getName()));
    }

    public static MutableText makeLinked(String text, String url){
        MutableText mt = new LiteralText(text);
        mt.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        return mt;
    }

    public static MutableText makeCHLinked(String text, String url, String hover){
        MutableText mt = new LiteralText(text);
        mt.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        mt.styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, codedText(hover))));
        return mt;
    }

    public static String normalize(String[] strings){
        StringBuilder thing = new StringBuilder();
        int i = 0;
        for (String t : strings){
            i ++;

            if (i < strings.length) {
                thing.append(t).append(" ");
            } else {
                thing.append(t);
            }
        }

        return thing.toString();
    }

    public static String normalize(List<String> strings){
        StringBuilder thing = new StringBuilder();
        int i = 0;
        for (String t : strings){
            i ++;

            if (i < strings.size()) {
                thing.append(t).append(" ");
            } else {
                thing.append(t);
            }
        }

        return thing.toString();
    }

    public static String normalize(TreeMap<Integer, String> splitMsg) {
        int i = 0;
        StringBuilder text = new StringBuilder();

        for (Integer split : splitMsg.keySet()){
            i++;
            if (splitMsg.get(split).equals("")) continue;

            if (i < splitMsg.size())
                text.append(splitMsg.get(split)).append(" ");
            else
                text.append(splitMsg.get(split));
        }

        return text.toString();
    }
}
