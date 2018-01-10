package bilkent.cs565.paper.physics.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;


public class Surface implements Force {

    private static final float DENSITY = 2.1f;
    private static final float LIFT_FACTOR = -3.9f;
    private final float mag;

    public final Particle[] particles;
    private Vec3 norm;

    public Surface(Particle[] p) {
        particles = p;

        Vec3 c1 = particles[1].pos.minus(particles[0].pos);
        Vec3 c2 = particles[2].pos.minus(particles[0].pos);
        norm = c1.cross(c2).normalize();
        particles[0].norm = norm;
        particles[1].norm = norm;
        particles[2].norm = norm;
        Vec3 center = new Vec3();
        for (Particle p1: particles) {
            center = center.plus(p1.pos);
            //p1.mass += 1;
            p1.mass += c1.cross(c2).length()/2 * DENSITY;
        }
        mag = particles[0].pos.minus(center).length();
    }

    @Override
    public void step(double dt, int order) {
        Vec3 normal = new Vec3();
        Vec3 vel = new Vec3();
        Vec3 center = new Vec3();

        for (Particle p: particles) {
            vel = vel.plus(p.vel.plus(p.dvdt[order-1].times(dt)));
            normal = normal.plus(p.norm);
            center = center.plus(p.pos.plus(p.dxdt[order-1].times(dt)));
        }

        vel = vel.div(particles.length);
        norm = normal.normalize();
        center = center.times(1.0/particles.length);

        // compute surface area
        Vec3 c1 = particles[1].pos.minus(particles[0].pos);
        Vec3 c2 = particles[2].pos.minus(particles[0].pos);
        Vec3 n1 = c1.cross(c2);
        float area = n1.length() / 2;
        n1 = n1.normalize();

        particles[0].normSum = particles[0].normSum.plus(n1);
        particles[1].normSum = particles[1].normSum.plus(n1);
        particles[2].normSum = particles[2].normSum.plus(n1);
        particles[0].normCount += 1;
        particles[1].normCount += 1;
        particles[2].normCount += 1;

        // apply lift
        for (Particle p: particles) {
            Vec3 vel2 = p.vel.plus(p.dvdt[order-1].times(dt));
            vel2 = vel2.times(vel2.length());
            Vec3 liftImpulse = n1.times(GLM.dot(n1, vel2) * area * LIFT_FACTOR / p.mass);
            p.dvdt[order] = p.dvdt[order].plus(liftImpulse);
        }
    }

    public static Surface create(Particle... p) {
        return new Surface(p);
    }
}
