package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.minefortress.network.ServerboundBlueprintsImportExport;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.utils.ModOSUtils;
import org.minefortress.utils.ModUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class ImportExportBlueprintsScreen extends Screen {

    private static final LiteralText DEFAULT_LABEL = new LiteralText("Import/Export Blueprints");
    private static final LiteralText IMPORT_LABEL = new LiteralText("Importing...");
    private static final LiteralText EXPORT_LABEL = new LiteralText("Exporting...");
    private static final LiteralText IMPORT_SUCCESS = new LiteralText("Imported successfully!");
    private static final LiteralText EXPORT_SUCCESS = new LiteralText("Exported successfully!");
    private static final LiteralText IMPORT_FAILURE = new LiteralText("Import failed!");
    private static final LiteralText EXPORT_FAILURE = new LiteralText("Export failed!");

    private ScreenState state = ScreenState.DEFAULT;

    private ButtonWidget backButton;
    private ButtonWidget importButton;
    private ButtonWidget exportButton;

    private LiteralText label;

    public ImportExportBlueprintsScreen() {
        super(new LiteralText("Import/Export Blueprints"));
    }

    @Override
    protected void init() {
        if(!ModUtils.isClientInFortressGamemode()){
            closeMenu();
            return;
        }

        backButton = new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 24 - 16,
                204,
                20,
                new LiteralText("Back"),
                button -> MinecraftClient.getInstance().setScreen(new BlueprintsScreen())
        );

        exportButton = new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 48 - 16,
                204,
                20,
                new LiteralText("Export blueprints"),
                button -> {
                    setState(ScreenState.EXPORTING);
                    final var path = ModOSUtils.showSaveDialog("zip", "blueprints.zip");
                    if (path.isPresent()) {
                        final var packet = new ServerboundBlueprintsImportExport(path.get());
                        FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, packet);
                    } else {
                        setState(ScreenState.DEFAULT);
                    }
                }
        );

        importButton = new ButtonWidget(
                this.width / 2 - 102,
                this.height / 4 + 72 - 16,
                204,
                20,
                new LiteralText("Discard Blueprint"),
                button -> {
                    setState(ScreenState.IMPORTING);
                    final var path = ModOSUtils.showOpenDialog("zip", null);
                    if (path.isPresent()) {
                        final var bytesOpt = readFile(path.get());
                        if(bytesOpt.isPresent()) {
                            final var packet = new ServerboundBlueprintsImportExport(bytesOpt.get());
                            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, packet);
                        } else {
                            setState(ScreenState.IMPORT_FAILURE);
                        }
                    } else {
                        setState(ScreenState.DEFAULT);
                    }
                }
        );

        this.addDrawableChild(backButton);
        this.addDrawableChild(exportButton);
        this.addDrawableChild(importButton);
    }


    private Optional<byte[]> readFile(String pathStr) {
        final var path = Paths.get(pathStr);
        if(!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void tick() {
        switch (state) {
            case DEFAULT -> setDefaultState();
            case IMPORTING -> setImportingState();
            case EXPORTING -> setExportingState();
            case IMPORT_SUCCESS -> setImportSuccessState();
            case EXPORT_SUCCESS -> setExportSuccessState();
            case IMPORT_FAILURE -> setImportFailureState();
            case EXPORT_FAILURE -> setExportFailureState();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        GameMenuScreen.drawCenteredText(matrices, this.textRenderer, this.label, this.width / 2, 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if(state == ScreenState.IMPORTING || state == ScreenState.EXPORTING) return;

        MinecraftClient.getInstance().setScreen(new BlueprintsScreen());
    }

    public void fail() {
        switch (state) {
            case IMPORTING -> setState(ScreenState.IMPORT_FAILURE);
            case EXPORTING -> setState(ScreenState.EXPORT_FAILURE);
            default -> setState(ScreenState.DEFAULT);
        }
    }

    public void success() {
        switch (state) {
            case IMPORTING -> setState(ScreenState.IMPORT_SUCCESS);
            case EXPORTING -> setState(ScreenState.EXPORT_SUCCESS);
            default -> setState(ScreenState.DEFAULT);
        }
    }

    private void closeMenu() {
        MinecraftClient.getInstance().setScreen(new BlueprintsScreen());
    }

    private void setDefaultState() {
        backButton.visible = true;
        exportButton.visible = true;
        importButton.visible = true;

        label = DEFAULT_LABEL;
    }

    private void setExportingState() {
        backButton.visible = false;
        exportButton.visible = false;
        importButton.visible = false;

        label = EXPORT_LABEL;
    }

    private void setImportingState() {
        backButton.visible = false;
        exportButton.visible = false;
        importButton.visible = false;

        label = IMPORT_LABEL;
    }

    private void setImportSuccessState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        label = IMPORT_SUCCESS;
    }

    private void setExportSuccessState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        label = EXPORT_SUCCESS;
    }

    private void setImportFailureState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        label = IMPORT_FAILURE;
    }

    private void setExportFailureState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        label = EXPORT_FAILURE;
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
