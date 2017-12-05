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

        float D = -20f;
        // apply collision forces
        for (Particle p: paper.getParticles()) {
            Vec3 pos = p.pos(order);
            if (pos.x < 8.5f && pos.z < D)
            {
                if (pos.z > D-.4) {
                    p.dxdt[order].z += (float)dt*(Math.max(D - pos.z, 0.01f) * 1000.02f - p.dxdt[order].z * 2.9f);
                    p.dxdt[order] = p.dxdt[order].minus(p.dxdt[order].times(dt * 22.9f));
                }
            }
        }
    }

    public void integrate(double dt) {
        for (Particle p: paper.getParticles()) {
            p.vel = p.dxdt[0].plus(p.dxdt[1].times(2)).plus(p.dxdt[2].times(2)).plus(p.dxdt[3]).div(6);
            //paper.getParticles().get(0).vel = new Vec3();
            p.pos = p.pos.plus(p.vel);
            for (int i=0;i<4;i++) {
                p.dxdt[i] = p.vel;
            }
            p.norm = p.normSum.div(p.normCount);
            p.normSum = new Vec3();
            p.normCount = 0;
        }
    }
}
