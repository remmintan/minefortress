package net.remmintan.mods.minefortress.core.utils.camera;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.remmintan.mods.minefortress.core.interfaces.renderers.FortressGameRenderer;
import net.remmintan.mods.minefortress.core.utils.GlobalProjectionCache;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CameraTools {

    private static Vec3d mouseBasedViewVector;

    public static Vec3d getMouseBasedViewVector(MinecraftClient minecraft, double xpos, double ypos) {
        if(GlobalProjectionCache.shouldUpdateValues("mouseBasedViewVector")) {
            mouseBasedViewVector = getMouseBasedViewVector(xpos, ypos, minecraft);
        }

        return mouseBasedViewVector;
    }

    public static Vec2f projectToScreenSpace(Vec3d position, MinecraftClient minecraft) {
        final var modelViewBuffer = getModelViewMatrix(minecraft, true);
        final var projectionBuffer = getProjectionMatrix(minecraft);
        final var winWidth = minecraft.getWindow().getWidth();
        final var winHeight = minecraft.getWindow().getHeight();
        final var viewport = getViewport(winWidth, winHeight);
        final var resultingViewBuffer = MemoryUtil.memAllocFloat(3);

        resultingViewBuffer.position(0);
        GLU.gluProject((float) position.x, (float) position.y, (float) position.z, modelViewBuffer, projectionBuffer, viewport, resultingViewBuffer);
        return new Vec2f(resultingViewBuffer.get(0), winHeight - resultingViewBuffer.get(1));
    }

    public static Map<Vec2f, Vec3d> projectToScreenSpace(Set<Vec3d> positions, MinecraftClient minecraft) {
        final var winWidth = minecraft.getWindow().getWidth();
        final var winHeight = minecraft.getWindow().getHeight();
        final var modelViewBuffer = getModelViewMatrix(minecraft, true);
        final var projectionBuffer = getProjectionMatrix(minecraft);
        final var viewport = getViewport(winWidth, winHeight);
        final var resultingViewBuffer = MemoryUtil.memAllocFloat(3);

        Map<Vec2f, Vec3d> screenPositions = new HashMap<>();

        for (Vec3d position : positions) {
            resultingViewBuffer.position(0);
            GLU.gluProject((float) position.x, (float) position.y, (float) position.z, modelViewBuffer, projectionBuffer, viewport, resultingViewBuffer);
            screenPositions.put(new Vec2f(resultingViewBuffer.get(0) , winHeight-resultingViewBuffer.get(1)), position);
        }

        return screenPositions;
    }

    private static Vec3d getMouseBasedViewVector(double xpos, double ypos, MinecraftClient minecraft) {
        int winWidth = minecraft.getWindow().getWidth();
        int winHeight = minecraft.getWindow().getHeight();

        FloatBuffer resultingViewBuffer = MemoryUtil.memAllocFloat(3);
        resultingViewBuffer.position(0);
        FloatBuffer modelViewBuffer = getModelViewMatrix(minecraft, false);
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

    private static FloatBuffer getModelViewMatrix(MinecraftClient minecraft, boolean translateToPlayer) {
        final var modelViewMatrix = new Matrix4f(RenderSystem.getModelViewMatrix());
        final var player = minecraft.player;
        if(player != null) {
            final var xRads = (float) Math.toRadians(player.getPitch());
            modelViewMatrix.rotate(xRads, new Vector3f(1, 0,0));
            final float yRads = (float) Math.toRadians(player.getYaw() + 180f);
            modelViewMatrix.rotate(yRads, new Vector3f(0, 1,0));

            if(translateToPlayer) {
                final var offset = new Vector3f(
                        (float) -player.getX(),
                        (float) -player.getY(),
                        (float) -player.getZ()
                );
                modelViewMatrix.translate(offset);
            }
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
