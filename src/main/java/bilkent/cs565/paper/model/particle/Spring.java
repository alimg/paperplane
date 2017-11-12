package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;

public class Spring implements Force {
    private static final float K = 30.1f;
    private static final float K_V = 6.8f;
    private static final float K_H = 40.1f;
    private static final float B = 2.61f;

    public Particle p1;
    public Particle p2;
    public Vec3 normal = new Vec3();
    public Vec3 dir = new Vec3();
    private float size;

    public void reset() {
        dir = p2.pos.minus(p1.pos).times(1);
        size = dir.length();
        normal = p1.norm;
        p1.dir = new Vec3(dir);
        p2.dir = dir.times(-1);
    }

    @Override
    public void step(double dt) {
        Vec3 displacement = p2.pos.minus(p1.pos);

        Vec3 pull = displacement.minus(displacement.normalize().times(size));
        final Vec3 vrel = p2.vel.minus(p1.vel);
        Vec3 tension = pull.times(K);
        Vec3 damp = displacement.normalize().times(GLM.dot(displacement.normalize(), vrel) * B);
        Vec3 f = tension.plus(damp).times(dt);
        p1.vel = p1.vel.plus(f);
        p2.vel = p2.vel.minus(f);

        //p1.vel = p1.vel.plus(p1.norm.times(p1.norm.cross(p2.norm).length() * -1.1 * dt));
        p2.vel = p2.vel.plus(p2.norm.times(Math.pow(GLM.dot(p1.norm, displacement), 3) * -K_V * dt));
        p1.vel = p1.vel.plus(p1.norm.times(Math.pow(GLM.dot(p2.norm, displacement), 3) * K_V * dt));
    }
}
