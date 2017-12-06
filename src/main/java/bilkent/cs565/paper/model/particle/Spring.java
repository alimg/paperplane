package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;

public class Spring implements Force {
    private static final float K = 300.f;
    private static final float K_V = 3.2f;
    private static final float K_H = 12;
    private static final float B = 20.4f;

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
        //Vec3 damp = displacement.normalize().times(GLM.dot(displacement.normalize(), vrel) * B);
        Vec3 damp = vrel.times(B);
        Vec3 springF = tension.plus(damp);

        Vec3 dp1 = p1.norm.cross(displacement.cross(p1.norm)).normalize().times(displacement.length());
        Vec3 dp2 = p2.norm.cross(displacement.cross(p2.norm)).normalize().times(displacement.length());

        Vec3 dn = displacement.cross(p1.norm.cross(displacement)).normalize();
        //Vec3 dn2 = displacement.cross(p2.norm.cross(displacement)).normalize();
        //Vec3 rot2F = dp1.minus(displacement).times(K_V);
        //Vec3 rot1F = dp2.minus(displacement).times(K_V);

        Vec3 rot2F = dn.times(size*Math.asin(GLM.dot(displacement.normalize(), p1.norm)) * -K_V);
        Vec3 rot1F = dn.times(size*Math.asin(GLM.dot(displacement.normalize(), p2.norm)) * K_V);

        //if (rot1F.length()>0) {
        //    Vec3 dn1 = rot1F.normalize();
        //    Vec3 rot1Fd = dn1.times(GLM.dot(dn1, vrel) * K_H);
        //    rot1F = rot1F.plus(rot1Fd);
        //}
        //if (rot2F.length()>0) {
        //    Vec3 dn2 = rot2F.normalize();
        //    Vec3 rot2Fd = dn2.times(GLM.dot(dn2, vrel) * -K_H);
        //    rot2F = rot2F.plus(rot2Fd);
        //}

        //Vec3 rot1F = dn2.times(GLM.dot(p2.norm, displacement) * K_V).plus(rot1Fd);
        //Vec3 rot2F = dn1.times(GLM.dot(p1.norm, displacement) * -K_V).plus(rot2Fd);

        p1.dxdt[order] = p1.dxdt[order].plus(springF.plus(rot1F).times(dt / p1.mass));
        p2.dxdt[order] = p2.dxdt[order].minus(springF.minus(rot2F).times(dt / p2.mass));
    }
}
