package org.minefortress.renderer.gui;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;

import java.util.Arrays;
import java.util.List;

public class FortressBookContents implements BookScreen.Contents {

    public static final List<String> HELP_BOOK = Arrays.asList(
            """
            This mod adds strategy-view to the game. You can dig and build using only your mouse pointer and group of Pawns.\s

            Keybindings:
            left-click - start dig selection
            right-click - start build selection
            """,
            """
            middle mouse button press - rotate the camera with the mouse
            ctrl + R - change the build type (Squares, Walls, Ladder)
            ctrl + E - move current selection Up
            ctrl + Q - move current selection Down
            """,
            """
            ctrl + W - rotate camera down
            ctrl + S - rotate camera up
            ctrl + A - rotate camera left
            ctrl + D - rotate camera right
            """,
            """
            Z - cancel latest task
            ctrl + Z - cancel all tasks
            """
    );

    private final List<String> pages;

    public FortressBookContents(List<String> pages) {
        this.pages = pages;
    }

    @Override
    public int getPageCount() {
        return pages.size();
    }

    @Override
    public StringVisitable getPageUnchecked(int index) {
        return new LiteralText(pages.get(index));
    }

}
