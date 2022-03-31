package org.minefortress.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiUtils {

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
