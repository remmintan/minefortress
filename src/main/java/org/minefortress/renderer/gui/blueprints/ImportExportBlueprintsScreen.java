package org.minefortress.renderer.gui.blueprints;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.remmintan.mods.minefortress.networking.c2s.ServerboundBlueprintsImportExportPacket;
import net.remmintan.mods.minefortress.networking.helpers.FortressChannelNames;
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper;
import org.minefortress.MineFortressMod;
import org.minefortress.renderer.gui.blueprints.list.BlueprintListEntry;
import org.minefortress.renderer.gui.blueprints.list.BlueprintsListWidget;
import org.minefortress.utils.ModUtils;

import java.nio.file.Files;
import java.nio.file.Path;

;

public class ImportExportBlueprintsScreen extends Screen {

    private static final Text DEFAULT_LABEL = Text.literal("Import/Export Blueprints");
    private static final Text IMPORT_LABEL = Text.literal("Importing...");
    private static final Text IMPORT_PROMPT_LABEL = Text.literal("Select blueprints to import");
    private static final Text EXPORT_LABEL = Text.literal("Exporting...");
    private static final Text EXPORT_PROMPT_LABEL = Text.literal("Enter file name:");
    private static final Text IMPORT_SUCCESS = Text.literal("Imported successfully!");
    private static final Text EXPORT_SUCCESS = Text.literal("Exported successfully!");
    private static final Text IMPORT_FAILURE = Text.literal("Import failed!");
    private static final Text EXPORT_FAILURE = Text.literal("Export failed!");

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

    private Text label = DEFAULT_LABEL;

    public ImportExportBlueprintsScreen() {
        super(Text.literal("Import/Export Blueprints"));
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

        importConfirm = ButtonWidget
            .builder(
                Text.literal("Import"),
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
            )
            .dimensions(x, y + listHeight,width, height)
            .build();
        importConfirm.visible = false;

        importOpenFolder = ButtonWidget
            .builder(
                Text.literal("Open Folder"),
                (button) -> openBlueprintsFolder()
            )
            .dimensions(x, y + listHeight + step,width/2 - 1, height)
            .build();
        importOpenFolder.visible = false;

        importRefresh = ButtonWidget
            .builder(
                Text.literal("Refresh list"),
                (button) -> refreshImportsList()
            )
            .dimensions(x + width/2 + 2, y + listHeight + step,width/2 - 1, height)
            .build();
        importRefresh.visible = false;

        importCancel = ButtonWidget
            .builder(
                Text.literal("Cancel"),
                (button) -> setState(ScreenState.DEFAULT)
            )
            .dimensions(x, y + listHeight + step * 2,width, height)
            .build();
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
                Text.literal("")
        );
        exportName.visible = false;

        exportConfirm = ButtonWidget
            .builder(
                Text.literal("Export"),
                (button) -> {
                    setState(ScreenState.EXPORTING);
                    final var text = exportName.getText();

                    // add .zip if not present
                    final var fileName = text.endsWith(MineFortressMod.BLUEPRINTS_EXTENSION) ? text : text + MineFortressMod.BLUEPRINTS_EXTENSION;
                    final var packet = new ServerboundBlueprintsImportExportPacket(fileName);
                    FortressClientNetworkHelper.send(FortressChannelNames.FORTRESS_BLUEPRINTS_IMPORT_EXPORT, packet);
                }
            )
            .dimensions(x, y + step,width, height)
            .build();
        exportConfirm.visible = false;

        exportCancel = ButtonWidget
            .builder(
                Text.literal("Cancel"),
                (button) -> setState(ScreenState.DEFAULT)
            )
            .dimensions(x, y +(step *2),width, height)
            .build();
        exportCancel.visible = false;

        this.addDrawableChild(exportName);
        this.addDrawableChild(exportConfirm);
        this.addDrawableChild(exportCancel);
    }

    private void initDefaultButtons(int x, int y, int width, int height, int step) {
        backButton = ButtonWidget.builder(
                Text.literal("Back"),
                (button) -> MinecraftClient.getInstance().setScreen(new BlueprintsScreen())
            )
            .dimensions(x, y, width, height)
            .build();

        exportButton = ButtonWidget.builder(
                Text.literal("Export blueprints"),
                (button) -> setState(ScreenState.EXPORT_PROMPT)
            )
            .dimensions(x, y + step, width, height)
            .build();

        importButton = ButtonWidget.builder(
                Text.literal("Import blueprints"),
                (button) -> {
                    setState(ScreenState.IMPORT_PROMPT);
                    refreshImportsList();
                }
            )
            .dimensions(x, y + (step * 2), width, height)
            .build();

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
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        super.renderBackground(drawContext, mouseX, mouseY, delta);
        super.render(drawContext, mouseX, mouseY, delta);

        drawContext.drawCenteredTextWithShadow(this.textRenderer, this.label, this.width / 2, 40, 0xFFFFFF);
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
