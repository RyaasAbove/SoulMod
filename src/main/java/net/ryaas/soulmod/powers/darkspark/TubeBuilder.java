package net.ryaas.soulmod.powers.darkspark;

import net.minecraft.world.phys.Vec3;

public class TubeBuilder {

    /**
     * Builds a cylindrical tube from point a->b with the given radius and ringCount.
     * Returns a TubeSegment containing startRing and endRing positions in order.
     */
    public static TubeSegment buildTube(Vec3 a, Vec3 b, float radius, int ringCount) {
        TubeSegment seg = new TubeSegment();

        Vec3 axis = b.subtract(a);
        double length = axis.length();
        if (length < 1e-6) {
            return seg; // degenerate
        }
        Vec3 dir = axis.normalize();

        // choose some "up" vector not parallel to dir
        Vec3 up = new Vec3(0,1,0);
        Vec3 right = dir.cross(up);
        if (right.lengthSqr() < 1e-6) {
            right = dir.cross(new Vec3(1,0,0)).normalize();
        } else {
            right = right.normalize();
        }
        Vec3 forward = dir.cross(right).normalize();

        for (int i = 0; i < ringCount; i++) {
            double theta = 2.0 * Math.PI * i / ringCount;
            double cosT = Math.cos(theta);
            double sinT = Math.sin(theta);

            Vec3 offset = right.scale(cosT * radius).add(forward.scale(sinT * radius));

            seg.startRing.add(a.add(offset));
            seg.endRing.add(b.add(offset));
        }

        return seg;
    }
}