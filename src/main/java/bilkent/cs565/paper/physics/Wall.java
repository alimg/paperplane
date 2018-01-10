package bilkent.cs565.paper.physics;

import bilkent.cs565.paper.World;
import bilkent.cs565.paper.gl.GLM;
import bilkent.cs565.paper.physics.particle.Particle;
import glm.vec._3.Vec3;

public class Wall {
    private static final float BOUNCE = 0.2f;
    private static final float DAMP = 0.9f;
    private static final float FRICTION = 10.f;
    public final Vec3 pos;
    public final Vec3 norm;
    public final Vec3 right;

    public Wall(Vec3 pos, Vec3 norm, Vec3 right) {
        this.pos = pos;
        this.norm = norm;
        this.right = right;
    }

    public void step(float dt, int order, Particle particle) {
        Vec3 x = particle.pos.plus(particle.dxdt[order-1].times(dt)).minus(pos);
        Vec3 v = particle.vel.plus(particle.dvdt[order-1].times(dt));
        float kMax = particle.mass / (World.STEP_SIZE * World.STEP_SIZE);
        float dMax = particle.mass / (World.STEP_SIZE);

        float d = GLM.dot(x, norm);
        if (d<0) {
            Vec3 f = norm.times(d * -kMax * BOUNCE - GLM.dot(norm, v) * dMax * DAMP)
                    .plus(norm.cross(v).cross(norm).times(-FRICTION));
            particle.dvdt[order] = particle.dvdt[order].plus(f.div(particle.mass));
        }
    }

}
