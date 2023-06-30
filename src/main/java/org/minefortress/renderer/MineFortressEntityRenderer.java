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
import org.minefortress.selections.SelectionManager;

import java.util.function.Supplier;

public class MineFortressEntityRenderer {

    private final TextRenderer font;
    private final Supplier<SelectionManager> selectionManagerSupplier;
    private Camera camera;
    private Quaternion cameraOrientation;

    public MineFortressEntityRenderer(TextRenderer font, Supplier<SelectionManager> selectionManagerSupplier) {
        this.font = font;
        this.selectionManagerSupplier = selectionManagerSupplier;
    }

    public void prepare(Camera camera) {
        this.camera = camera;
        this.cameraOrientation = camera.getRotation();
    }

    public void render(double x, double y, double z, MatrixStack poseStack, VertexConsumerProvider source, int packLightCoords) {
        for(Pair<Vec3d, String> pair : getSelectionManager().getLabels()) {
            Vec3d pos = pair.getFirst();
            String stringToDisplay = pair.getSecond();
            poseStack.push();
            poseStack.translate(pos.x - x, pos.y + 2 - y, pos.z - z);
            poseStack.multiply(this.cameraOrientation);
            final double a =  camera.getPos().distanceTo(pos);
            float verticalDistanceMultiplier = (float)a/10;
            poseStack.scale(-0.075F * verticalDistanceMultiplier, -0.075F * verticalDistanceMultiplier, 0.075F * verticalDistanceMultiplier);
            final Matrix4f matrix = poseStack.peek().getPositionMatrix();

            float backgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
            int backgroundAlpha = (int) (backgroundOpacity * 255.0F) << 24;

            float f2 = 0f;
//                float f2 = (float)(-font.width(stringToDisplay) / 2);
            this.font.draw(
                    stringToDisplay,
                    f2,
                    0f,
                    0xffffffff,
                    false,
                    matrix,
                    source,
                    false,
                    backgroundAlpha,
                    packLightCoords
            );

            poseStack.pop();
        }
    }
    public SelectionManager getSelectionManager() {
        return selectionManagerSupplier.get();
    }

}
