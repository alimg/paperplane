package bilkent.cs565.paper.integration;

import bilkent.cs565.paper.physics.Paper;
import bilkent.cs565.paper.physics.Wall;
import bilkent.cs565.paper.physics.particle.Force;
import bilkent.cs565.paper.physics.particle.Particle;
import glm.vec._3.Vec3;

import java.util.List;

public class PaperPhysics {

    private final List<Wall> walls;
    public Paper paper;
    private Vec3 gravity;

    public PaperPhysics(Paper paper, List<Wall> walls, Vec3 gravity) {
        this.paper = paper;
        this.gravity = gravity;
        this.walls = walls;
    }

    public void step(double dt, int order) {
        // apply gravity
        for (Particle p : paper.getParticles()) {
            //Vec3 x = p.pos.plus(p.dxdt[order-1].times(dt));
            Vec3 v = p.vel.plus(p.dvdt[order-1].times(dt));
            p.dxdt[order] = v;
            p.dvdt[order] = gravity;
        }

        // apply spring and aero forces
        for (Force s : paper.getForces()) {
            s.step(dt, order);
        }

        // apply collision forces
        for (Particle p: paper.getParticles()) {
            walls.forEach(wall -> wall.step((float) dt, order, p));
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
            p.pos = p.pos.plus(dxdt.times(dt));
            for (int i = 0; i < 5; i++) {
                p.dxdt[i] = new Vec3();
                p.dvdt[i] = new Vec3();
            }
        }
    }
}
