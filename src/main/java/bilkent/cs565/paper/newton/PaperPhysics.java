package bilkent.cs565.paper.newton;

import bilkent.cs565.paper.model.Paper;
import bilkent.cs565.paper.model.particle.Force;
import bilkent.cs565.paper.model.particle.Particle;
import glm.vec._3.Vec3;

public class PaperPhysics {

    public Paper paper;
    private Vec3 gravity;

    public PaperPhysics(Paper paper, Vec3 gravity) {
        this.paper = paper;
        this.gravity = gravity;
    }

    public void step(double dt) {
        // apply spring forces
        for (Force s: paper.getForces()) {
            s.step(dt);
        }
        // apply collision forces
        for (Particle p: paper.getParticles()) {

            if (p.pos.x < 5 && p.pos.z < -10)
            {
                //p.pos.z = -10f;
                if (p.pos.z > -10.3)
                    p.vel.z += (-10 - p.pos.z)*2;

                p.vel = p.vel.times(0.99);
                //p.vel = p.vel.times(0.99f, 0.99f, 0.2f);
            }
        }

        // apply gravity
        for (Particle p: paper.getParticles()) {
            p.vel = p.vel.plus(gravity.times(dt));
        }

        // integrate
        for (Particle p: paper.getParticles()) {
            p.pos = p.pos.plus(p.vel.times(dt));
        }
    }
}
