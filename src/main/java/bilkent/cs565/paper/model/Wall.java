package bilkent.cs565.paper.model;

import bilkent.cs565.paper.gl.GLM;
import bilkent.cs565.paper.model.particle.Particle;
import glm.vec._3.Vec3;

public class Wall {
    private static final float BOUNCE = 10;
    private static final float DAMP = 10;
    private final Vec3 pos;
    private final Vec3 norm;

    public Wall(Vec3 pos, Vec3 norm) {
        this.pos = pos;
        this.norm = norm;
    }

    public void step(float dt, int order, Particle particle) {
        Vec3 x = particle.pos.plus(particle.dxdt[order-1].times(dt)).minus(pos);
        Vec3 v = particle.vel.plus(particle.dvdt[order-1].times(dt));
        float d = GLM.dot(x, norm);
        if (d<0) {
            Vec3 f = norm.times(d * -BOUNCE).plus(v.times(-DAMP));
            particle.dvdt[order] = particle.dvdt[order].plus(f);
        }
    }
}
