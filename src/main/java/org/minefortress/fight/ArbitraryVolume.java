package org.minefortress.fight;

import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ArbitraryVolume {

    private final List<Vec3d> vertices;

    public ArbitraryVolume(List<Vec3d> vertices) {
        if (vertices.contains(null)) throw new IllegalArgumentException("Vertices cannot contain null");
        if (vertices.size() != 5) throw new IllegalArgumentException("5 vertices are required");
        this.vertices = vertices;
    }

    public boolean isInside(Vec3d point) {
        // Create the faces (triangles) of the volume
        List<Face> faces = createFaces();

        // Check if the point lies on the same side of each face (formed by triangles)
        for (Face face : faces) {
            if (isSameSide(point, face.a, face.b, face.c)) {
                return false; // Point is outside
            }
        }

        return true; // Point is inside
    }

    private List<Face> createFaces() {
        List<Face> faces = new ArrayList<>();
        faces.add(new Face(vertices.get(0), vertices.get(1), vertices.get(2)));
        faces.add(new Face(vertices.get(0), vertices.get(2), vertices.get(3)));
        faces.add(new Face(vertices.get(0), vertices.get(3), vertices.get(4)));
        faces.add(new Face(vertices.get(1), vertices.get(4), vertices.get(3)));
        faces.add(new Face(vertices.get(1), vertices.get(2), vertices.get(4)));
        return faces;
    }

    // Determines if a point 'p' is on the same side of a plane as point 'a' (plane defined by a, b, c)
    private boolean isSameSide(Vec3d p, Vec3d a, Vec3d b, Vec3d c) {
        Vec3d ba = b.subtract(a);
        Vec3d ca = c.subtract(a);
        Vec3d pa = p.subtract(a);

        Vec3d normal = ba.crossProduct(ca); // Normal to the plane

        double dot1 = normal.dotProduct(pa);
        double dot2 = normal.dotProduct(ba); // Can use any non-zero vector on the plane

        return dot1 * dot2 > 0; // Same sign means same side
    }

    // Inner class to represent a triangular face
    private static class Face {
        Vec3d a;
        Vec3d b;
        Vec3d c;

        public Face(Vec3d a, Vec3d b, Vec3d c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }
}
