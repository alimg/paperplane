package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;


public class Surface implements Force {

    private static final float LIFT_FACTOR = -0.35f;
    private final float mag;

    private final Particle[] particles;

    public Surface(Particle[] p) {
        particles = p;


        Vec3 c1 = particles[1].pos.minus(particles[0].pos);
        Vec3 c2 = particles[3].pos.minus(particles[0].pos);
        Vec3 n1 = c1.cross(c2);
        Vec3 c3 = particles[3].pos.minus(particles[2].pos);
        Vec3 c4 = particles[1].pos.minus(particles[2].pos);
        Vec3 n2 = c3.cross(c4);
        particles[0].norm = n1.normalize();
        particles[1].norm = n1.normalize();
        particles[2].norm = n2.normalize();
        particles[3].norm = n2.normalize();
        Vec3 center = new Vec3();
        for (Particle p1: particles) {
            center = center.plus(p1.pos);
        }
        mag = particles[0].pos.minus(center).length();
    }

    @Override
    public void step(double dt) {
        Vec3 normal = new Vec3();
        Vec3 vel = new Vec3();
        Vec3 center = new Vec3();

        for (Particle p: particles) {
            vel = vel.plus(p.vel);
            normal = normal.plus(p.norm);
            center = center.plus(p.pos);
        }
        normal = normal.normalize();
        center = center.times(1.0/particles.length);

        // compute surface area
        Vec3 c1 = particles[1].pos.minus(particles[0].pos);
        Vec3 c2 = particles[3].pos.minus(particles[0].pos);
        Vec3 n1 = c1.cross(c2);
        float a1 = n1.length() / 2;
        Vec3 c3 = particles[3].pos.minus(particles[2].pos);
        Vec3 c4 = particles[1].pos.minus(particles[2].pos);
        Vec3 n2 = c3.cross(c4);
        float a2 = n2.length() / 2;
        float area = a1 + a2;

        particles[0].norm = particles[0].norm.plus(n1.normalize()).normalize();
        particles[1].norm = particles[1].norm.plus(n1.normalize()).normalize();
        particles[2].norm = particles[2].norm.plus(n2.normalize()).normalize();
        particles[3].norm = particles[3].norm.plus(n2.normalize()).normalize();

        vel = vel.div(particles.length);
        float v2 = vel.length() * vel.length();
        vel = vel.normalize();

        Vec3 liftForce = new Vec3();
        if (v2 > 0) {
            liftForce = normal.times(0.5f * GLM.dot(normal, vel) * v2 * area * LIFT_FACTOR * dt);
        }
        // apply lift
        for (Particle p: particles) {
            //liftForce = p.norm.times(glm.dot(normal, vel) * area * LIFT_FACTOR * dt);
            Vec3 rvel = p.vel.minus(vel);
            Vec3 dp = p.pos.minus(center);
            dp.minus(dp.normalize().times(mag));
            if (dp.length()>0.00001) {
                p.vel = p.vel
                    .plus(dp.times(dt * -3.0))
                    .plus(dp.times(GLM.dot(dp.normalize(), rvel) * dt * -0.2f / dp.length()));
            }
            //dp = dp.times(dp.length());
            p.vel = p.vel
                .plus(liftForce);
        }
    }

    public static Surface create(Particle... p) {
        return new Surface(p);
    }
}
