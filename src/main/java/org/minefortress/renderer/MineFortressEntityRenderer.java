package org.minefortress.renderer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import org.minefortress.fortress.buildings.BuildingHealthRenderInfo;
import org.minefortress.selections.SelectionManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MineFortressEntityRenderer {

    private final TextRenderer font;
    private final Supplier<SelectionManager> selectionManagerSupplier;
    private final Supplier<List<BuildingHealthRenderInfo>> buildingsHealthSupplier;
    private Camera camera;
    private Quaternion cameraOrientation;

    public MineFortressEntityRenderer(TextRenderer font,
                                      Supplier<SelectionManager> selectionManagerSupplier,
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

        for (BuildingHealthRenderInfo renderInfo : buildingsHealthSupplier.get()) {
            final var pos = renderInfo.pos();
            final var health = renderInfo.health();

            final int backgroundAlpha = getBackgroundAlpha();

            final Consumer<Matrix4f> renderAction = matrix -> {

                this.font.draw(
                        String.valueOf(health),
                        0f,
                        0f,
                        0xffffffff,
                        false,
                        matrix,
                        source,
                        false,
                        backgroundAlpha,
                        packLightCoords
                );
            };

            this.renderTranslated(x, y, z, matrixStack, pos, renderAction);
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
                        false,
                        backgroundAlpha,
                        packLightCoords
                );
            this.renderTranslated(x, y, z, matrixStack, pos, renderAction);
        }
    }

    private static int getBackgroundAlpha() {
        final var backgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        return (int) (backgroundOpacity * 255.0F) << 24;
    }

    private void renderTranslated(double x, double y, double z, MatrixStack matrixStack, Vec3d pos, Consumer<Matrix4f> renderAction) {
        matrixStack.push();
        matrixStack.translate(pos.x - x, pos.y + 2 - y, pos.z - z);
        matrixStack.multiply(this.cameraOrientation);
        final double a =  camera.getPos().distanceTo(pos);
        float verticalDistanceMultiplier = (float)a/10;
        matrixStack.scale(-0.075F * verticalDistanceMultiplier, -0.075F * verticalDistanceMultiplier, 0.075F * verticalDistanceMultiplier);
        final Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        renderAction.accept(matrix);

        matrixStack.pop();
    }

    public SelectionManager getSelectionManager() {
        return selectionManagerSupplier.get();
    }

}
