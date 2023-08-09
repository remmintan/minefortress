package org.minefortress.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.minefortress.interfaces.FortressGameRenderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class CameraTools {

    private static Vec3d mouseBasedViewVector;
    private static double oldMouseX, oldMouseY;
    private static float oldPlayerXRot;
    private static float oldPlayerYRot;

    public static Vec3d getMouseBasedViewVector(MinecraftClient minecraft, float xRot, float yRot) {
        double xpos = minecraft.mouse.getX();
        double ypos = minecraft.mouse.getY();
        if(Math.abs(xpos - oldMouseX) + Math.abs(ypos - oldMouseY) > 0.01 || xRot != oldPlayerXRot || yRot != oldPlayerYRot) {
            mouseBasedViewVector = getMouseBasedViewVector(minecraft,  xpos, ypos);
            oldMouseX = xpos;
            oldMouseY = ypos;
            oldPlayerXRot = xRot;
            oldPlayerYRot = yRot;
        }

        return mouseBasedViewVector;
    }

    public static Vec3d getMouseBasedViewVector(MinecraftClient minecraft, double xpos, double ypos) {
        int winWidth = minecraft.getWindow().getWidth();
        int winHeight = minecraft.getWindow().getHeight();

        FloatBuffer resultingViewBuffer = FloatBuffer.allocate(3);
        FloatBuffer modelViewBuffer = getModelViewMatrix(minecraft);
        FloatBuffer projectionBuffer = getProjectionMatrix(minecraft);
        IntBuffer viewport = getViewport(winWidth, winHeight);

        GLU.gluUnProject((float) xpos, (float)(winHeight - ypos) , 1.0f, modelViewBuffer, projectionBuffer, viewport, resultingViewBuffer);

        return calculateResultingVector(resultingViewBuffer);
    }

    private static IntBuffer getViewport(int winWidth, int winHeight) {
        IntBuffer viewport = IntBuffer.allocate(4);
        viewport.put(0);
        viewport.put(0);
        viewport.put(winWidth);
        viewport.put(winHeight);
        viewport.rewind();
        return viewport;
    }

    private static Vec3d calculateResultingVector(FloatBuffer resultingViewBuffer) {
        Vector3f resultingViewVector = new Vector3f(resultingViewBuffer.get(0), resultingViewBuffer.get(1), resultingViewBuffer.get(2));
        resultingViewVector.normalize();
        return new Vec3d(resultingViewVector);
    }

    private static FloatBuffer getModelViewMatrix(MinecraftClient minecraft) {
        Matrix4f modelViewMatrix = RenderSystem.getModelViewMatrix().copy();
        ClientPlayerEntity player = minecraft.player;
        if(player != null) {
            modelViewMatrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(player.getRotationClient().x));
            modelViewMatrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(player.getRotationClient().y + 180f));
        }
        FloatBuffer modelViewBuffer = FloatBuffer.allocate(16);
        modelViewMatrix.writeColumnMajor(modelViewBuffer);
        modelViewBuffer.rewind();
        return modelViewBuffer;
    }

    private  static FloatBuffer getProjectionMatrix(MinecraftClient minecraft) {
        Matrix4f projectionMatrix = getProjectionMatrix4f(minecraft);
        FloatBuffer projectionBuffer = FloatBuffer.allocate(16);
        projectionMatrix.writeColumnMajor(projectionBuffer);
        projectionBuffer.rewind();
        return projectionBuffer;
    }

    @NotNull
    public static Matrix4f getProjectionMatrix4f(MinecraftClient minecraft) {
        final GameRenderer gameRenderer = minecraft.gameRenderer;
        double fov = ((FortressGameRenderer)gameRenderer).getFov(1.0f, true);
        return getProjectionMatrix4f(minecraft, fov);
    }

    @NotNull
    private static Matrix4f getProjectionMatrix4f(MinecraftClient minecraft, double fov) {
        final GameRenderer gameRenderer = minecraft.gameRenderer;
        return gameRenderer.getBasicProjectionMatrix(fov).copy();
    }

}
