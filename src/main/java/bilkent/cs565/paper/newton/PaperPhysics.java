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

    public void step(double dt, int order) {
        // apply spring forces
        for (Force s: paper.getForces()) {
            s.step(dt, order);
        }

        // apply gravity
        for (Particle p: paper.getParticles()) {
            p.dxdt[order] = p.dxdt[order].plus(gravity.times(dt));
        }

        // apply collision forces
        for (Particle p: paper.getParticles()) {

            if (p.pos.x < 5.5 && p.pos.z < -10)
            {
                if (p.pos.z > -10.3) {
                    p.dxdt[order].z += (-10 - p.pos.z) * 2;
                }
                p.dxdt[order] = p.dxdt[order].times(0.98);
            }
        }
    }

    public void integrate(double dt) {
        for (Particle p: paper.getParticles()) {
            p.vel = p.dxdt[0].plus(p.dxdt[1].times(2)).plus(p.dxdt[2].times(2)).plus(p.dxdt[3]).div(6);
            p.pos = p.pos.plus(p.vel);
            for (int i=0;i<4;i++) {
                p.dxdt[i] = p.vel;
            }
        }
    }
}
