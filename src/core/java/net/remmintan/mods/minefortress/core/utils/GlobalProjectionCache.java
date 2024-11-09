package net.remmintan.mods.minefortress.core.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalProjectionCache {

    public static final int UPDATE_DELAY = 100;
    private static double cachedMouseX = Double.MIN_VALUE;
    private static double cachedMouseY = Double.MIN_VALUE;
    // head rotation and player position
    private static float cachedPlayerXRot = Float.MIN_VALUE;
    private static float cachedPlayerYRot = Float.MIN_VALUE;
    private static Vec3d cachedPlayerPos = Vec3d.ZERO;

    // time
    private static final Map<String, Long> lastUpdateTimes = new ConcurrentHashMap<>();

    public static boolean shouldUpdateValues(String timeKey) {
        final var minecraft = MinecraftClient.getInstance();
        if(minecraft == null) return false;

        final var mouseX = minecraft.mouse.getX();
        final var mouseY = minecraft.mouse.getY();

        final var player = minecraft.player;
        if(player == null) return false;

        final var playerXRot = player.getPitch();
        final var playerYRot = player.getYaw();
        final var playerPos = player.getPos();
        if(playerPos == null) return false;

        if(mouseX != cachedMouseX ||
                mouseY != cachedMouseY ||
                playerXRot != cachedPlayerXRot ||
                playerYRot != cachedPlayerYRot ||
                !playerPos.equals(cachedPlayerPos) ||
                System.currentTimeMillis() - lastUpdateTimes.computeIfAbsent(timeKey, it -> 0L) > UPDATE_DELAY
        ) {
            cachedMouseX = mouseX;
            cachedMouseY = mouseY;
            cachedPlayerXRot = playerXRot;
            cachedPlayerYRot = playerYRot;
            cachedPlayerPos = new Vec3d(playerPos.x, playerPos.y, playerPos.z);
            lastUpdateTimes.put(timeKey, System.currentTimeMillis());
            return true;
        }

        return false;
    }

}
