package org.minefortress.renderer;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.minefortress.selections.SelectionManager;

public class MineFortressEntityRenderer {

    private final TextRenderer font;
    private final SelectionManager manager;
    private World level;
    private Camera camera;
    private Quaternion cameraOrientation;

    public MineFortressEntityRenderer(TextRenderer font, SelectionManager manager) {
        this.font = font;
        this.manager = manager;
    }

    public void prepare(World level, Camera camera) {
        this.level = level;
        this.camera = camera;
        this.cameraOrientation = camera.getRotation();
    }

    public void render(double x, double y, double z, MatrixStack poseStack, VertexConsumerProvider source, int packLightCoords) {

        try {
            for(Pair<Vec3d, String> pair : manager.getLabels()) {
                Vec3d pos = pair.getFirst();
                String stringToDisplay = pair.getSecond();
                poseStack.push();
                poseStack.translate(pos.x - x, pos.y + 2 - y, pos.z - z);
                poseStack.multiply(this.cameraOrientation);
                float verticalDistanceMultiplier = (float) (Math.abs(this.camera.getPos().y - pos.y) / 10);
                poseStack.scale(-0.075F * verticalDistanceMultiplier, -0.075F * verticalDistanceMultiplier, 0.075F * verticalDistanceMultiplier);
                final Matrix4f matrix = poseStack.peek().getModel();

                float backgroundOpacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
                int backgroundAlpha = (int)(backgroundOpacity * 255.0F) << 24;

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

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.create(throwable, "Rendering entity in world");
            CrashReportSection crashreportcategory1 = crashreport.addElement("Renderer details");
            crashreportcategory1.add("Assigned renderer", this);
            crashreportcategory1.add("Location", CrashReportSection.createPositionString(this.level, x, y, z));
            throw new CrashException(crashreport);
        }
    }

    public void setLevel(World level) {
        this.level = level;
    }
}
