package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;

public class Spring implements Force {
    private static final float K = 100.f;
    private static final float K_V = 10.f;
    private static final float K_H = 40.1f;
    private static final float B = 2.61f;

    public Particle p1;
    public Particle p2;
    public Vec3 normal = new Vec3();
    public Vec3 dir1 = new Vec3();
    public Vec3 dir2 = new Vec3();
    private float size;

    public void reset() {
        dir1 = p2.pos.minus(p1.pos);
        dir2 = p2.pos.minus(p1.pos);

        size = dir1.length();
        normal = p1.norm;
        p1.dir = new Vec3(dir1);
        p2.dir = dir1.times(-1);
    }

    @Override
    public void step(double dt, int order) {
        Vec3 pos1 = p1.pos(order);
        Vec3 pos2 = p2.pos(order);

        Vec3 displacement = pos2.minus(pos1);
        Vec3 pull = displacement.minus(displacement.normalize().times(size));

        final Vec3 vrel = p2.vel.minus(p1.vel);

        Vec3 tension = pull.times(K);
        Vec3 damp = displacement.normalize().times(GLM.dot(displacement.normalize(), vrel) * B);
        Vec3 springF = tension.plus(damp);
        Vec3 rot1F = p1.norm.times(Math.pow(GLM.dot(p2.norm, displacement), 1) * -K_V);
        Vec3 rot2F = p2.norm.times(Math.pow(GLM.dot(p1.norm, displacement), 1) * K_V);

        p1.dxdt[order] = p1.dxdt[order].plus(springF.times(dt));
        p2.dxdt[order] = p2.dxdt[order].minus(springF.times(dt));
    }
}
