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
        // apply gravity
        for (Particle p : paper.getParticles()) {
            //Vec3 x = p.pos.plus(p.dxdt[order-1].times(dt));
            Vec3 v = p.vel.plus(p.dvdt[order-1].times(dt));
            p.dxdt[order] = v;
            p.dvdt[order] = gravity;
        }

        // apply spring forces
        for (Force s : paper.getForces()) {
            s.step(dt, order);
        }


        float D = -5f;
        // apply collision forces
        for (Particle p: paper.getParticles()) {
            Vec3 pos = p.pos.plus(p.dxdt[order-1].times(dt));
            if (pos.x < 0.5f && pos.z < D)
            {
                if (pos.z > D-.2) {
                    Vec3 v = p.vel.plus(p.dvdt[order-1].times(dt));
                    Vec3 f = new Vec3(0,0, ((D - pos.z) * 500.02f))
                            .plus(v.times(-1.5f));
                    //p.dxdt[order] = p.dxdt[order].plus(v);
                    p.dvdt[order] = p.dvdt[order].plus(f);
                }
            }
            p.norm = p.normSum.div(p.normCount);
            p.normSum = new Vec3();
            p.normCount = 0;
        }
    }

    public void integrate(float dt) {
        for (Particle p : paper.getParticles()) {
            Vec3 dvdt = p.dvdt[1].plus(p.dvdt[2].plus(p.dvdt[3]).times(2)).plus(p.dvdt[4]).div(6);
            Vec3 dxdt = p.dxdt[1].plus(p.dxdt[2].plus(p.dxdt[3]).times(2)).plus(p.dxdt[4]).div(6);
            p.vel = p.vel.plus(dvdt.times(dt));
            //if (p.id <= 16 )
            //{
            //    p.vel = new Vec3();
            //    dxdt = new Vec3();
            //}
            //paper.getParticles().get(0).vel = new Vec3();
            p.pos = p.pos.plus(dxdt.times(dt));
            for (int i = 0; i < 5; i++) {
                p.dxdt[i] = new Vec3();
                p.dvdt[i] = new Vec3();
            }
        }
    }
}
