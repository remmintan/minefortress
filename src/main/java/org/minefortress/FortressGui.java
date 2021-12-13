package org.minefortress;

import com.chocohead.mm.api.ClassTinkerers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.GameMode;
import org.minefortress.interfaces.FortressMinecraftClient;
import org.minefortress.selections.SelectionManager;

public class FortressGui {

    private final MinecraftClient minecraft;
    private final SelectionManager selectionManager;

   private final static int MOD_GUI_COLOR = 0xf0f0f0;
   private final static boolean SHOW_WATER_MARKS = false;
   private final static boolean REDDIT_WATERMARKS_ENABLED = false;

    public FortressGui(MinecraftClient minecraft) {
        this.minecraft = minecraft;
        this.selectionManager = ((FortressMinecraftClient)minecraft).getSelectionManager();
    }

    public void render(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight) {
        renderWatermarks(p, font, screenWidth, screenHeight);
        if(this.minecraft.interactionManager != null && this.minecraft.interactionManager.getCurrentGameMode() == ClassTinkerers.getEnum(GameMode.class, "FORTRESS")) {
            renderSelectTypeName(p, font);
        }
    }

    private void renderWatermarks(MatrixStack p, TextRenderer font, int screenWidth, int screenHeight) {
        int y = screenHeight - font.fontHeight - 5;

        if(SHOW_WATER_MARKS) {
            if(REDDIT_WATERMARKS_ENABLED) {
                DrawableHelper.drawCenteredText(p, font, "/u/remmintan", 5, y, MOD_GUI_COLOR);
            } else {
                DrawableHelper.drawCenteredText(p, font, "Minecraft Fortress Mod", 5, y, MOD_GUI_COLOR);
                DrawableHelper.drawCenteredText(p, font, "minecraftfortress.org", screenWidth - font.getWidth("minecraftfortress.org") - 5, y, MOD_GUI_COLOR);
            }
        }
    }

    private void renderSelectTypeName(MatrixStack p, TextRenderer font) {
        String name = this.selectionManager.getCurrentSelectionType().getName();
        String selectionText = "Selection type: " + name;
        DrawableHelper.drawCenteredText(p, font, selectionText, 5, 5, MOD_GUI_COLOR);
    }

}
