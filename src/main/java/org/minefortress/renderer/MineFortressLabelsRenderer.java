package org.minefortress.renderer;


import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.dtos.buildings.BuildingHealthRenderInfo;
import net.remmintan.mods.minefortress.core.interfaces.selections.ISelectionManager;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MineFortressLabelsRenderer {

    public static final int GREEN = 0xff008450;
    public static final int YELLOW = 0xffefb700;
    public static final int RED = 0xffb81d13;
    public static final int BLACK = 0xff000000;
    private final TextRenderer font;
    private final Supplier<ISelectionManager> selectionManagerSupplier;
    private final Supplier<List<BuildingHealthRenderInfo>> buildingsHealthSupplier;
    private Camera camera;
    private Quaternionf cameraOrientation;

    public MineFortressLabelsRenderer(TextRenderer font,
                                      Supplier<ISelectionManager> selectionManagerSupplier,
                                      Supplier<List<BuildingHealthRenderInfo>> buildingsHealthSupplier) {
        this.font = font;
        this.selectionManagerSupplier = selectionManagerSupplier;
        this.buildingsHealthSupplier = buildingsHealthSupplier;
    }

    public void prepare(Camera camera) {
        this.camera = camera;
        this.cameraOrientation = camera.getRotation();
    }

    public void render(double x, double y, double z, MatrixStack matrixStack, VertexConsumerProvider source, int packLightCoords) {
        renderSelectionLabels(x, y, z, matrixStack, source, packLightCoords);
        renderBuildingHealth(x, y, z, matrixStack, source, packLightCoords);
    }

    private void renderBuildingHealth(double x, double y, double z, MatrixStack matrixStack, VertexConsumerProvider source, int packLightCoords) {
        final var totalHealthLength = 30;
        final var healthbarBackground = StringUtils.repeat("|", totalHealthLength);
        final var healthbarWidth = this.font.getWidth(healthbarBackground);
        final var healthbarVerticalOffset = -4f;

        for (BuildingHealthRenderInfo renderInfo : buildingsHealthSupplier.get()) {
            final var pos = renderInfo.pos();
            final var health = renderInfo.health();
            final var healthLength = (int) (health / 100f * totalHealthLength);
            final var healthbar = StringUtils.repeat("|", healthLength);


            int color;
            if (health > 66) {
                color = GREEN;
            } else if (health > 33) {
                color = YELLOW;
            } else {
                color = RED;
            }

            final Consumer<Matrix4f> renderAction = matrix -> {

                this.font.draw(
                        healthbarBackground,
                        -healthbarWidth/2f,
                        healthbarVerticalOffset,
                        BLACK,
                        false,
                        matrix,
                        source,
                        TextRenderer.TextLayerType.NORMAL,
                        BLACK,
                        packLightCoords
                );

                matrixStack.push();
                matrixStack.translate(0, 0, -0.05f);
                final var newMatrix = matrixStack.peek().getPositionMatrix();
                this.font.draw(
                        healthbar,
                        -healthbarWidth/2f,
                        healthbarVerticalOffset,
                        color,
                        false,
                        newMatrix,
                        source,
                        TextRenderer.TextLayerType.NORMAL,
                        color,
                        packLightCoords
                );
                matrixStack.pop();
            };

            this.renderTranslated(x, y, z, matrixStack, pos, renderAction, 0.5f);
        }
    }

    private void renderSelectionLabels(double x, double y, double z, MatrixStack matrixStack, VertexConsumerProvider source, int packLightCoords) {
        for(Pair<Vec3d, String> pair : getSelectionManager().getLabels()) {
            final var pos = pair.getFirst();
            final var stringToDisplay = pair.getSecond();
            final int backgroundAlpha = getBackgroundAlpha();

            final Consumer<Matrix4f> renderAction = matrix ->
                    this.font.draw(
                        stringToDisplay,
                        0f,
                        0f,
                        0xffffffff,
                        false,
                        matrix,
                        source,
                        TextRenderer.TextLayerType.NORMAL,
                        backgroundAlpha,
                        packLightCoords
                );
            this.renderTranslated(x, y, z, matrixStack, pos, renderAction, 1f);
        }
    }

    private static int getBackgroundAlpha() {
        final var backgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        return (int) (backgroundOpacity * 255.0F) << 24;
    }

    private void renderTranslated(double x, double y, double z, MatrixStack matrixStack, Vec3d pos, Consumer<Matrix4f> renderAction, float size) {
        matrixStack.push();
        matrixStack.translate(pos.x - x, pos.y + 2 - y, pos.z - z);
        matrixStack.multiply(this.cameraOrientation);
        final double a =  camera.getPos().distanceTo(pos);
        float verticalDistanceMultiplier = (float)a/10;
        matrixStack.scale(-0.075F * verticalDistanceMultiplier * size, -0.075F * verticalDistanceMultiplier * size, 0.075F * verticalDistanceMultiplier * size);
        final var matrix = matrixStack.peek().getPositionMatrix();

        renderAction.accept(matrix);

        matrixStack.pop();
    }

    public ISelectionManager getSelectionManager() {
        return selectionManagerSupplier.get();
    }

}
