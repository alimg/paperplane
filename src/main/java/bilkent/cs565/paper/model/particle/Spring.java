package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.World;
import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;

public class Spring implements Force {
    private static final float STIFFNESS = 0.15f;
    private static final float DAMPING = 0.001f;

    private static final float K_V = .00015f;
    private static final float K_H = 12;

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
        Vec3 pos1 = p1.pos.plus(p1.dxdt[order-1].times(dt));
        Vec3 vel1 = p1.vel.plus(p1.dvdt[order-1].times(dt));
        Vec3 pos2 = p2.pos.plus(p2.dxdt[order-1].times(dt));
        Vec3 vel2 = p2.vel.plus(p2.dvdt[order-1].times(dt));

        float reducedMass = 1f/(1f/p1.mass + 1f/p2.mass);

        Vec3 displacement = pos2.minus(pos1);
        Vec3 rest = displacement.normalize().times(size);
        Vec3 pull = displacement.minus(rest);

        float kMax = reducedMass / (World.STEP_SIZE * World.STEP_SIZE);
        float dMax = reducedMass / (World.STEP_SIZE);

        final Vec3 vrel = vel2.minus(vel1);

        Vec3 tension = pull.times(STIFFNESS * kMax);
        Vec3 damp = vrel.times(DAMPING * dMax);
        Vec3 springF = tension.plus(damp);

        // angular spring
        Vec3 dn = displacement.cross(p1.norm.cross(displacement)).normalize();
        Vec3 dnorm = displacement.normalize();
        int d1 = GLM.dot(dn, p1.norm) > 0 ? 1 : -1;
        int d2 = GLM.dot(dn, p2.norm) > 0 ? 1 : -1;
        Vec3 rot2F = dn.times(size*size*Math.acos(GLM.dot(dnorm, p1.norm)) * kMax * d1 * K_V);
        Vec3 rot1F = dn.times(size*size*Math.acos(GLM.dot(dnorm, p2.norm)) * kMax * d2 * -K_V);

        p1.dvdt[order] = p1.dvdt[order].plus (springF.plus(rot1F.minus(rot2F).div(p1.mass)));
        p2.dvdt[order] = p2.dvdt[order].minus(springF.plus(rot2F.minus(rot1F).div(p2.mass)));
    }
}
