package net.remmintan.mods.minefortress.core.dtos.combat;

public record MousePos(double x, double y) {
    public int getX() {
        return (int) x;
    }
    public int getY() {
        return (int) y;
    }
}