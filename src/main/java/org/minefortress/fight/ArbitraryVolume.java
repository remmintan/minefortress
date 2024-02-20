package org.minefortress.fight;

import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ArbitraryVolume {

    private final List<Vec3d> vertices;

    public ArbitraryVolume(List<Vec3d> vertices) {
        this.vertices = vertices;
    }

    public boolean isInside(Vec3d point) {
        // Step 1: Compute Convex Hull (You might need an external library for this)
        List<Vec3d> convexHullVertices = calculateConvexHull(vertices);

        // Step 2: Check if the point is inside the convex hull
        return isPointInsideConvexHull(convexHullVertices, point);
    }

    private List<Vec3d> calculateConvexHull(List<Vec3d> points) {
        List<Face> hull = findInitialTetrahedron(points);

        for (Vec3d p : points) {
            if (!isPointInsideHull(hull, p)) {
                List<Face> visibleFaces = findVisibleFaces(hull, p);
                List<Edge> boundaryEdges = findBoundaryEdges(visibleFaces);

                // Update hull (remove visible, add new formed with 'p')
                hull.removeAll(visibleFaces);
                for (Edge edge : boundaryEdges) {
                    hull.add(new Face(edge.v1, edge.v2, p));
                }
            }
        }

        return extractVertices(hull);
    }

    private List<Face> findInitialTetrahedron(List<Vec3d> points) {
        // Simple (not always robust) approach:

        // Find a point, and the point furthest from it
        Vec3d p1 = points.get(0);
        Vec3d p2 = findFurthestPoint(points, p1);

        // Find a point furthest away from the line formed by p1 and p2
        Vec3d p3 = findFurthestFromLine(points, p1, p2);

        // Find a point that doesn't lie in the plane formed by p1, p2, p3
        Vec3d p4 = findPointOutsidePlane(points, p1, p2, p3);

        // Create faces to form the tetrahedron
        return Arrays.asList(
                new Face(p1, p2, p3),
                new Face(p1, p3, p4),
                new Face(p1, p4, p2),
                new Face(p2, p4, p3)
        );
    }

    private Vec3d findFurthestPoint(List<Vec3d> points, Vec3d basePoint) {
        Vec3d furthestPoint = null;
        double maxDistanceSq = 0; // Squared distance for efficiency

        for (Vec3d p : points) {
            if (p != basePoint) { // Exclude the base point itself
                double distanceSq = p.subtract(basePoint).lengthSquared();
                if (distanceSq > maxDistanceSq) {
                    maxDistanceSq = distanceSq;
                    furthestPoint = p;
                }
            }
        }

        return furthestPoint;
    }

    private Vec3d findFurthestFromLine(List<Vec3d> points, Vec3d p1, Vec3d p2) {
        Vec3d lineDirection = p2.subtract(p1);

        Vec3d furthestPoint = null;
        double maxDistanceSq = 0;

        for (Vec3d p : points) {
            if (p != p1 && p != p2) { // Exclude points on the line
                Vec3d vectorToLine = p.subtract(p1);
                Vec3d projection = lineDirection.normalize().multiply(vectorToLine.dotProduct(lineDirection));

                Vec3d perpVector = vectorToLine.subtract(projection); // Vector from point to the line
                double distanceSq = perpVector.lengthSquared();

                if (distanceSq > maxDistanceSq) {
                    maxDistanceSq = distanceSq;
                    furthestPoint = p;
                }
            }
        }

        return furthestPoint;
    }

    private Vec3d findPointOutsidePlane(List<Vec3d> points, Vec3d p1, Vec3d p2, Vec3d p3) {
        Vec3d normal = p2.subtract(p1).crossProduct(p3.subtract(p1)); // Plane normal

        for (Vec3d p : points) {
            if (p != p1 && p != p2 && p != p3) {
                if (normal.dotProduct(p.subtract(p1)) != 0) {
                    return p; // Found a point not on the plane
                }
            }
        }

        return null; // This should theoretically not happen if you have enough points
    }

    private List<Edge> findBoundaryEdges(List<Face> faces) {
        Set<Edge> boundaryEdges = new HashSet<>();
        for (Face face : faces) {
            boundaryEdges.add(new Edge(face.v1, face.v2));
            boundaryEdges.add(new Edge(face.v2, face.v3));
            boundaryEdges.add(new Edge(face.v3, face.v1));
        }

        // Remove internal edges (that appear in two faces)
        boundaryEdges.removeIf(edge -> Collections.frequency(faces, edge) > 1);
        return new ArrayList<>(boundaryEdges);
    }

    private List<Vec3d> extractVertices(List<Face> hull) {
        Set<Vec3d> vertices = new HashSet<>();
        for (Face face : hull) {
            vertices.add(face.v1);
            vertices.add(face.v2);
            vertices.add(face.v3);
        }
        return new ArrayList<>(vertices);
    }

    private boolean isPointInsideHull(List<Face> hull, Vec3d point) {
        for (Face face : hull) {
            // Using plane equation (normal of face dotted with point-on-face should be negative)
            Vec3d normal = face.v2.subtract(face.v1).crossProduct(face.v3.subtract(face.v1));
            if (normal.dotProduct(point.subtract(face.v1)) >= 0) {
                return false; // Outside if on the same side of the face's plane or on the plane
            }
        }
        return true;
    }

    private List<Face> findVisibleFaces(List<Face> hull, Vec3d point) {
        List<Face> visibleFaces = new ArrayList<>();
        for (Face face : hull) {
            Vec3d normal = face.v2.subtract(face.v1).crossProduct(face.v3.subtract(face.v1));
            if (normal.dotProduct(point.subtract(face.v1)) >= 0) {
                visibleFaces.add(face); // Facing towards the point
            }
        }
        return visibleFaces;
    }

    private boolean isPointInsideConvexHull(List<Vec3d> hullVertices, Vec3d point) {
        // For each face of the convex hull:
        for (int i = 0; i < hullVertices.size(); i++) {
            Vec3d v1 = hullVertices.get(i);
            Vec3d v2 = hullVertices.get((i + 1) % hullVertices.size()); // Next vertex
            Vec3d v3 = hullVertices.get((i + 2) % hullVertices.size()); // Third vertex to form a face

            // Calculate the normal of the face
            Vec3d faceNormal = v2.subtract(v1).crossProduct(v3.subtract(v1));

            // If the point is on the "wrong" side of the face's plane, it's outside
            if (point.subtract(v1).dotProduct(faceNormal) < 0) {
                return false;
            }
        }
        return true; // If it hasn't failed for any face, it's inside
    }

    private static class Face {
        public Vec3d v1;
        public Vec3d v2;
        public Vec3d v3;

        public Face(Vec3d v1, Vec3d v2, Vec3d v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }
    }

    private static class Edge {
        public Vec3d v1;
        public Vec3d v2;

        public Edge(Vec3d v1, Vec3d v2) {
            this.v1 = v1;
            this.v2 = v2;
        }
    }
}
