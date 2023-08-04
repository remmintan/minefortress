package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.util.Util;
import org.minefortress.MineFortressMod;
import org.minefortress.network.c2s.ServerboundBlueprintsImportExportPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressClientNetworkHelper;
import org.minefortress.renderer.gui.blueprints.list.BlueprintListEntry;
import org.minefortress.renderer.gui.blueprints.list.BlueprintsListWidget;
import org.minefortress.utils.ModUtils;
import ;
import I;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ImportExportBlueprintsScreen extends Screen {

    private static final LiteralTextContent DEFAULT_LABEL = new LiteralTextContent("Import/Export Blueprints");
    private static final LiteralTextContent IMPORT_LABEL = new LiteralTextContent("Importing...");
    private static final LiteralTextContent IMPORT_PROMPT_LABEL = new LiteralTextContent("Select blueprints to import");
    private static final LiteralTextContent EXPORT_LABEL = new LiteralTextContent("Exporting...");
    private static final LiteralTextContent EXPORT_PROMPT_LABEL = new LiteralTextContent("Enter file name:");
    private static final LiteralTextContent IMPORT_SUCCESS = new LiteralTextContent("Imported successfully!");
    private static final LiteralTextContent EXPORT_SUCCESS = new LiteralTextContent("Exported successfully!");
    private static final LiteralTextContent IMPORT_FAILURE = new LiteralTextContent("Import failed!");
    private static final LiteralTextContent EXPORT_FAILURE = new LiteralTextContent("Export failed!");

    private ScreenState state = ScreenState.DEFAULT;

    private ButtonWidget backButton;
    private ButtonWidget importButton;
    private ButtonWidget exportButton;

    private TextFieldWidget exportName;
    private ButtonWidget exportConfirm;
    private ButtonWidget exportCancel;

    private BlueprintsListWidget importsList;
    private ButtonWidget importConfirm;
    private ButtonWidget importOpenFolder;
    private ButtonWidget importRefresh;
    private ButtonWidget importCancel;

    private LiteralTextContent label = DEFAULT_LABEL;

    public ImportExportBlueprintsScreen() {
        super(new LiteralTextContent("Import/Export Blueprints"));
    }

    @Override
    protected void init() {
        if(!ModUtils.isClientInFortressGamemode()){
            closeMenu();
            return;
        }

        final var x = this.width / 2 - 102;
        final var y = this.height / 4 + 8;
        final var width = 204;
        final var height = 20;
        final var step = height + 4;
        initDefaultButtons(x, y, width, height, step);
        initExportPrompt(x, y, width, height, step);
        initImportPrompt(x, y, width, height, step);
    }

    private void initImportPrompt(int x, int y, int width, int height, int step) {
        final var listHeight = 100;
        final var listInternalPadding = 5;
        importsList = new BlueprintsListWidget(
                this.client,
                width,
                this.height,
                y - 10 - listInternalPadding,
                y + listHeight - listInternalPadding,
                height
        );
        importsList.setLeftPos(-1000);

        importConfirm = new ButtonWidget(
                x,
                y + listHeight,
                width,
                height,
                new LiteralTextContent("Import"),
                (button) -> {
                    setState(ScreenState.IMPORTING);
                    final var selected = importsList.getSelectedOrNull();
                    if(selected != null){
                        try {
                            final var file = ModUtils.getBlueprintsFolder()
                                    .resolve(selected.getValue());

                            final var bytes = Files.readAllBytes(file);
                            final var packet = new ServerboundBlueprintsImportExportPacket(bytes);
                            FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                            setState(ScreenState.IMPORT_FAILURE);
                        }
                    }
                }
        );
        importConfirm.visible = false;

        importOpenFolder = new ButtonWidget(
                x,
                y + listHeight + step,
                width/2 - 1,
                height,
                new LiteralTextContent("Open Folder"),
                (button) -> openBlueprintsFolder()
        );
        importOpenFolder.visible = false;

        importRefresh = new ButtonWidget(
                x + width/2 + 2,
                y + listHeight + step,
                width/2 - 1,
                height,
                new LiteralTextContent("Refresh list"),
                (button) -> refreshImportsList()
        );
        importRefresh.visible = false;

        importCancel = new ButtonWidget(
                x,
                y + listHeight + step * 2,
                width,
                height,
                new LiteralTextContent("Cancel"),
                (button) -> setState(ScreenState.DEFAULT)
        );
        importCancel.visible = false;

        this.addDrawableChild(importsList);
        this.addDrawableChild(importConfirm);
        this.addDrawableChild(importOpenFolder);
        this.addDrawableChild(importRefresh);
        this.addDrawableChild(importCancel);
    }

    private void openBlueprintsFolder() {
        try {
            final var blueprintsFolder = ModUtils.getBlueprintsFolder().toFile();
            if(!blueprintsFolder.exists()){
                blueprintsFolder.mkdirs();
            }
            Util.getOperatingSystem().open(blueprintsFolder);
        } catch (Exception e) {
            e.printStackTrace();
            setState(ScreenState.IMPORT_FAILURE);
        }
    }

    private void initExportPrompt(int x, int y, int width, int height, int step) {
        exportName = new TextFieldWidget(
                textRenderer,
                x,
                y,
                width,
                height,
                new LiteralTextContent("")
        );
        exportName.visible = false;

        exportConfirm = new ButtonWidget(
                x,
                y + step,
                width,
                height,
                new LiteralTextContent("Export"),
                button -> {
                    setState(ScreenState.EXPORTING);
                    final var text = exportName.getText();

                    // add .zip if not present
                    final var fileName = text.endsWith(MineFortressMod.BLUEPRINTS_EXTENSION) ? text : text + MineFortressMod.BLUEPRINTS_EXTENSION;
                    final var packet = new ServerboundBlueprintsImportExportPacket(fileName);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, packet);
                }
        );
        exportConfirm.visible = false;

        exportCancel = new ButtonWidget(
                x,
                y +(step *2),
                width,
                height,
                new LiteralTextContent("Cancel"),
                button -> setState(ScreenState.DEFAULT)
        );
        exportCancel.visible = false;

        this.addDrawableChild(exportName);
        this.addDrawableChild(exportConfirm);
        this.addDrawableChild(exportCancel);
    }

    private void initDefaultButtons(int x, int y, int width, int height, int step) {
        backButton = new ButtonWidget(
                x,
                y,
                width,
                height,
                new LiteralTextContent("Back"),
                button -> MinecraftClient.getInstance().setScreen(new BlueprintsScreen())
        );

        exportButton = new ButtonWidget(
                x,
                y + step,
                width,
                height,
                new LiteralTextContent("Export blueprints"),
                button -> setState(ScreenState.EXPORT_PROMPT)
        );

        importButton = new ButtonWidget(
                x,
                y + (step * 2),
                width,
                height,
                new LiteralTextContent("Import blueprints"),
                button -> {
                    setState(ScreenState.IMPORT_PROMPT);
                    refreshImportsList();
                }
        );

        this.addDrawableChild(backButton);
        this.addDrawableChild(exportButton);
        this.addDrawableChild(importButton);
    }

    @Override
    public void tick() {
        switch (state) {
            case DEFAULT -> setDefaultState();
            case IMPORTING -> setImportingState();
            case IMPORT_PROMPT -> setImportPromptState();
            case EXPORTING -> setExportingState();
            case EXPORT_PROMPT -> setExportPromptState();
            case IMPORT_SUCCESS -> setImportSuccessState();
            case EXPORT_SUCCESS -> setExportSuccessState();
            case IMPORT_FAILURE -> setImportFailureState();
            case EXPORT_FAILURE -> setExportFailureState();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        GameMenuScreen.drawCenteredTextWithShadow(matrices, this.textRenderer, this.label, this.width / 2, 40, 0xFFFFFF);
    }

    @Override
    public void close() {
        if(state == ScreenState.IMPORTING || state == ScreenState.EXPORTING) return;

        MinecraftClient.getInstance().setScreen(new BlueprintsScreen());
    }

    public void fail() {
        switch (state) {
            case IMPORTING, IMPORT_PROMPT -> setState(ScreenState.IMPORT_FAILURE);
            case EXPORTING, EXPORT_PROMPT -> setState(ScreenState.EXPORT_FAILURE);
            default -> setState(ScreenState.DEFAULT);
        }
    }

    public void success() {
        switch (state) {
            case IMPORTING, IMPORT_PROMPT -> setState(ScreenState.IMPORT_SUCCESS);
            case EXPORTING, EXPORT_PROMPT -> {
                openBlueprintsFolder();
                setState(ScreenState.EXPORT_SUCCESS);
            }
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

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = DEFAULT_LABEL;
    }

    private void setExportPromptState() {
        backButton.visible = false;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.visible = true;
        exportConfirm.visible = true;
        exportCancel.visible = true;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = EXPORT_PROMPT_LABEL;
    }

    private void setExportingState() {
        backButton.visible = false;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = EXPORT_LABEL;
    }

    private void setImportPromptState() {
        backButton.visible = false;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(this.width / 2 - 102);
        importConfirm.visible = true;
        importOpenFolder.visible = true;
        importRefresh.visible = true;
        importCancel.visible = true;

        label = IMPORT_PROMPT_LABEL;

        importConfirm.active = importsList.getSelectedOrNull() != null;
    }

    private void setImportingState() {
        backButton.visible = false;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = IMPORT_LABEL;
    }

    private void setImportSuccessState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = IMPORT_SUCCESS;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void setExportSuccessState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = EXPORT_SUCCESS;
    }

    private void setImportFailureState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = IMPORT_FAILURE;
    }

    private void setExportFailureState() {
        backButton.visible = true;
        exportButton.visible = false;
        importButton.visible = false;

        exportName.setText("");
        exportName.visible = false;
        exportConfirm.visible = false;
        exportCancel.visible = false;

        importsList.setLeftPos(-1000);
        importConfirm.visible = false;
        importOpenFolder.visible = false;
        importRefresh.visible = false;
        importCancel.visible = false;

        label = EXPORT_FAILURE;
    }

    private void setState(ScreenState state) {
        this.state = state;
    }

    private void refreshImportsList() {
        try {
            final var blueprintsFolder = ModUtils.getBlueprintsFolder();
            if(Files.notExists(blueprintsFolder)) {
                Files.createDirectories(blueprintsFolder);
            }
            final var fileNamesList = Files.list(blueprintsFolder)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(MineFortressMod.BLUEPRINTS_EXTENSION))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(it -> new BlueprintListEntry(it, this.textRenderer, importsList))
                    .toList();

            importsList.children().clear();
            importsList.children().addAll(fileNamesList);

            if (!importsList.children().isEmpty()) {
                importsList.setSelected(importsList.children().get(0));
            } else {
                importsList.setSelected(null);
            }

        }catch (Exception e) {

            fail();
        }
    }

    private enum ScreenState {
        DEFAULT,
        EXPORT_PROMPT,
        EXPORTING,
        IMPORT_PROMPT,
        IMPORTING,
        EXPORT_SUCCESS,
        EXPORT_FAILURE,
        IMPORT_SUCCESS,
        IMPORT_FAILURE
    }

}
