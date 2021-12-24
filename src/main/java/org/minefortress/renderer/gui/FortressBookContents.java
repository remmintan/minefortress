package org.minefortress.renderer.gui;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;

public class FortressBookContents implements BookScreen.Contents {

    private final String text;

    public FortressBookContents(String text) {
        this.text = text;
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public StringVisitable getPageUnchecked(int index) {
        return new LiteralText(text);
    }

}
