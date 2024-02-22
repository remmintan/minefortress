package org.minefortress.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.dtos.combat.MousePos;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.minefortress.interfaces.FortressGameRenderer;
import org.minefortress.utils.ModUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class CameraTools {

    private static Vec3d mouseBasedViewVector;
    private static double oldMouseX, oldMouseY;
    private static float oldPlayerXRot;
    private static float oldPlayerYRot;

    public static Vec3d getMouseBasedViewVector(MinecraftClient minecraft, double xpos, double ypos) {
        final var xRot = ModUtils.getClientPlayer().getPitch();
        final var yRot = ModUtils.getClientPlayer().getYaw();

        if(Math.abs(xpos - oldMouseX) + Math.abs(ypos - oldMouseY) > 0.01 || xRot != oldPlayerXRot || yRot != oldPlayerYRot) {
            mouseBasedViewVector = getMouseBasedViewVector(xpos, ypos, minecraft);
            oldMouseX = xpos;
            oldMouseY = ypos;
            oldPlayerXRot = xRot;
            oldPlayerYRot = yRot;
        }

        return mouseBasedViewVector;
    }

    // project list of all Vec3d to screen space
    public static List<Vec2f> projectToScreenSpace(List<Vec3d> positions, MinecraftClient minecraft) {
        final var winWidth = minecraft.getWindow().getWidth();
        final var winHeight = minecraft.getWindow().getHeight();
        final var modelViewBuffer = getModelViewMatrix(minecraft);
        final var projectionBuffer = getProjectionMatrix(minecraft);
        final var viewport = getViewport(winWidth, winHeight);
        final var resultingViewBuffer = MemoryUtil.memAllocFloat(3);

        List<Vec2f> screenPositions = new ArrayList<>();

        for (Vec3d position : positions) {
            resultingViewBuffer.position(0);
            GLU.gluProject((float) position.x, (float) position.y, (float) position.z, modelViewBuffer, projectionBuffer, viewport, resultingViewBuffer);
            screenPositions.add(new Vec2f(resultingViewBuffer.get(0), resultingViewBuffer.get(1)));
        }

        return screenPositions;
    }

    private static Vec3d getMouseBasedViewVector(double xpos, double ypos, MinecraftClient minecraft) {
        int winWidth = minecraft.getWindow().getWidth();
        int winHeight = minecraft.getWindow().getHeight();

        FloatBuffer resultingViewBuffer = MemoryUtil.memAllocFloat(3);
        resultingViewBuffer.position(0);
        FloatBuffer modelViewBuffer = getModelViewMatrix(minecraft);
        FloatBuffer projectionBuffer = getProjectionMatrix(minecraft);
        IntBuffer viewport = getViewport(winWidth, winHeight);

        GLU.gluUnProject((float) xpos, (float)(winHeight - ypos) , 1.0f, modelViewBuffer, projectionBuffer, viewport, resultingViewBuffer);

        return calculateResultingVector(resultingViewBuffer);
    }

    private static IntBuffer getViewport(int winWidth, int winHeight) {
        IntBuffer viewport = MemoryUtil.memAllocInt(4);
        viewport.position(0);
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
        final var modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
        final var player = minecraft.player;
        if(player != null) {
            final var xRads = (float) Math.toRadians(player.getRotationClient().x);
            modelViewMatrix.rotate(xRads, new Vector3f(1, 0,0));
            final var yRads = (float) Math.toRadians(player.getRotationClient().y + 180f);
            modelViewMatrix.rotate(yRads, new Vector3f(0, 1,0));
        }
        FloatBuffer modelViewBuffer = MemoryUtil.memAllocFloat(16);
        modelViewBuffer.position(0);
        modelViewMatrix.get(modelViewBuffer);

        modelViewBuffer.rewind();
        return modelViewBuffer;
    }

    private  static FloatBuffer getProjectionMatrix(MinecraftClient minecraft) {
        Matrix4f projectionMatrix = getProjectionMatrix4f(minecraft);
        FloatBuffer projectionBuffer = MemoryUtil.memAllocFloat(16);
        projectionBuffer.position(0);
        projectionMatrix.get(projectionBuffer);
        projectionBuffer.rewind();
        return projectionBuffer;
    }

    @NotNull
    public static Matrix4f getProjectionMatrix4f(MinecraftClient minecraft) {
        final GameRenderer gameRenderer = minecraft.gameRenderer;
        double fov = ((FortressGameRenderer)gameRenderer).get_Fov(1.0f, true);
        return getProjectionMatrix4f(minecraft, fov);
    }

    @NotNull
    private static Matrix4f getProjectionMatrix4f(MinecraftClient minecraft, double fov) {
        final GameRenderer gameRenderer = minecraft.gameRenderer;
        return new Matrix4f(gameRenderer.getBasicProjectionMatrix(fov));
    }

}
