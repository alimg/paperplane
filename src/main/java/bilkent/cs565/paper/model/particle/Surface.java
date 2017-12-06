package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;


public class Surface implements Force {

    private static final float LIFT_FACTOR = -90.0f;
    private final float mag;

    public final Particle[] particles;

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
            p1.mass += 1;
        }
        mag = particles[0].pos.minus(center).length();
    }

    @Override
    public void step(double dt, int order) {
        Vec3 normal = new Vec3();
        Vec3 vel = new Vec3();
        Vec3 center = new Vec3();

        for (Particle p: particles) {
            vel = vel.plus(p.dxdt[order]);
            normal = normal.plus(p.norm);
            center = center.plus(p.pos);
        }

        vel = vel.div(particles.length);
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

        if (GLM.dot(n1,n2)<0) {
            System.out.println("n1n2");
        }

        particles[0].normSum = particles[0].normSum.plus(n1.normalize());
        particles[1].normSum = particles[1].normSum.plus(n1.normalize());
        particles[2].normSum = particles[2].normSum.plus(n2.normalize());
        particles[3].normSum = particles[3].normSum.plus(n2.normalize());
        particles[0].normCount += 1;
        particles[1].normCount += 1;
        particles[2].normCount += 1;
        particles[3].normCount += 1;

        //Vec3 vel2 = vel.times(vel.length()*1000);
        //Vec3 liftForce = normal.times(0.5f * GLM.dot(normal, vel2) * area * LIFT_FACTOR * dt);
        // apply lift
        for (Particle p: particles) {
            Vec3 vel2 = p.dxdt[order].times(p.dxdt[order].length()*1000);
            Vec3 liftForce = p.norm.times(0.5f * GLM.dot(p.norm, vel2) * area * LIFT_FACTOR / p.mass * dt);
            p.dxdt[order] = p.dxdt[order].plus(liftForce);
            //p.dxdt[order] = p.dxdt[order].plus(liftForce.div(p.mass));
        }
    }

    public static Surface create(Particle... p) {
        return new Surface(p);
    }
}
