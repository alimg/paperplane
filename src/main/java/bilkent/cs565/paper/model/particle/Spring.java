package bilkent.cs565.paper.model.particle;

import glm.vec._3.Vec3;

public class Spring implements Force {
    private static final float K = 30f;
    private static final float B = 0.3f;

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
        Vec3 newDir = displacement.normalize().times(size);
        //displacement.normalize().cross(dir.normalize());
        //displacement = displacement.minus(dir);
        Vec3 dnorm = newDir.normalize().cross(p1.norm.normalize()).normalize();
        newDir = p1.norm.cross(dnorm).normalize().times(size);
        Vec3 dnorm2 = newDir.normalize().cross(p2.norm.normalize()).normalize();
        Vec3 newDir2 = p2.norm.cross(dnorm2).normalize().times(size);
        newDir = newDir.plus(newDir2).div(2);
        //newDir = newDir.plus(dir.times(0.1)).div(1.5);
        //displacement = displacement.minus(newDir) .plus( displacement.minus(dir));
        displacement = displacement.minus(newDir);
        Vec3 tension = displacement.times(K);
        Vec3 damp = p2.vel.minus(p1.vel).times(B);

        Vec3 f = tension.plus(damp).times(dt);
        p1.vel = p1.vel.plus(f);
        p2.vel = p2.vel.minus(f);
        dir = dir.plus(newDir.times(10)).div(11);
        //dir = newDir;
    }
}
