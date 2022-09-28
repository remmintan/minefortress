package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import org.minefortress.utils.ModOSUtils;
import org.minefortress.utils.ModUtils;

import java.util.Optional;

public class ImportExportBlueprintsScreen extends Screen {

    private ScreenState state = ScreenState.DEFAULT;

    protected ImportExportBlueprintsScreen() {
        super(new LiteralText("Import/Export Blueprints"));
    }

    @Override
    protected void init() {
        if(!ModUtils.isClientInFortressGamemode()){
            closeMenu();
            return;
        }

        final var backButton = new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 24 - 16,
                204,
                20,
                new LiteralText("Back"),
                button -> MinecraftClient.getInstance().setScreen(new BlueprintsScreen())
        );
        this.addDrawableChild(backButton);
        this.addDrawableChild(
                new ButtonWidget(
                        this.width / 2 - 102,
                        this.height / 4 + 48 - 16,
                        204,
                        20,
                        new LiteralText("Export blueprints"),
                        button -> {
                            setState(ScreenState.EXPORTING);
                            final var path = ModOSUtils.showSaveDialog("zip", "blueprints.zip");
                            if(path.isPresent()){

                            } else {
                                setState(ScreenState.DEFAULT);
                            }
                        }
                )
        );
        this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 72 - 16, 204, 20, new LiteralText("Discard Blueprint"), button -> {
            sendSave(false);
            closeMenu();
        }));
    }

    private void closeMenu() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void tick() {
        switch (state) {

        }
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(new BlueprintsScreen());
    }

    private void setState(ScreenState state) {
        this.state = state;
    }

    private enum ScreenState {
        DEFAULT,
        EXPORTING,
        IMPORTING,
        EXPORT_SUCCESS,
        EXPORT_FAILURE,
        IMPORT_SUCCESS,
        IMPORT_FAILURE
    }

}
