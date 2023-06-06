package org.minefortress.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.*;

public class GuiUtils {

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String formatSlotCount(long value) {
        if (value == Long.MIN_VALUE) return formatSlotCount(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatSlotCount(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        var e = suffixes.floorEntry(value);
        var divideBy = e.getKey();
        var suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        return (truncated / 10) + suffix;
    }

    public static List<Text> splitTextInWordsForLength(String text) {
        return splitTextInWordsForLength(text, 150);
    }

    public static List<Text> splitTextInWordsForLength(String text, int maxWidth) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        final List<Text> preparedTextParts = new ArrayList<>();
        for (String line : text.split("\n")) {
            final StringBuilder stringBuilder = new StringBuilder();
            for (String part : line.split(" ")) {
                stringBuilder.append(part).append(" ");
                final int lastPartLength = part.length();
                if (textRenderer.getWidth(stringBuilder.toString()) > (maxWidth)) {
                    stringBuilder.delete(stringBuilder.length() - lastPartLength - 1, stringBuilder.length());
                    preparedTextParts.add(new LiteralText(stringBuilder.toString()));
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append(part).append(" ");
                }
            }
            preparedTextParts.add(new LiteralText(stringBuilder.toString()));
        }

        return Collections.unmodifiableList(preparedTextParts);
    }

}
