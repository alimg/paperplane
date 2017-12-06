package bilkent.cs565.paper.model;

import bilkent.cs565.paper.model.particle.Force;
import bilkent.cs565.paper.model.particle.Particle;
import bilkent.cs565.paper.model.particle.Spring;
import bilkent.cs565.paper.model.particle.Surface;
import glm.vec._3.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Paper {

    private final List<Force> forces;
    private final List<Particle> particles;
    private final List<Spring> springForces = new ArrayList<>();
    private final List<Surface> surfaces = new ArrayList<>();

    public Paper(ArrayList<Particle> particles, ArrayList<Force> forces) {
        this.particles = particles;
        this.forces = forces;
        forces.forEach((f)-> {
            if (f instanceof Spring) {
                springForces.add((Spring) f);
            } else if (f instanceof Surface){
                surfaces.add((Surface) f);
            }
        });
    }

    public static Paper createFlat(int n, int m, float X, float Y) {
        ArrayList<Particle> particles = new ArrayList<>();
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                Particle p = new Particle(particles.size());
                p.pos = new Vec3(x*X/n, y*Y/m, Math.random()*0.0021 + x*0.01*X/n+ 4);
                p.norm = new Vec3(0, 0, 1);
                p.dir = new Vec3(0, -1, 0);
                p.vel = new Vec3(0.2f,0.1f,0);
                particles.add(p);
            }
        }

        ArrayList<Force> forces = new ArrayList<>();

        for (int x = 1; x < n; x++) {
            for (int y = 1; y < m; y++) {
                if (x == 1) {
                    Spring s = new Spring();
                    s.p1 = particles.get(n * (x - 1) + y - 1);
                    s.p2 = particles.get(n * (x - 1) + y);
                    s.reset();
                    forces.add(s);
                }
                if (y == 1) {
                    Spring s = new Spring();
                    s.p1 = particles.get(n * (x - 1) + y - 1);
                    s.p2 = particles.get(n * x + y - 1);
                    s.reset();
                    forces.add(s);
                }
                Spring s1 = new Spring();
                s1.p1 = particles.get(n * (x - 1) + y - 1);
                s1.p2 = particles.get(n * x + y);
                s1.reset();
                forces.add(s1);
                Spring s1c = new Spring();
                s1c.p1 = particles.get(n * x + y - 1);
                s1c.p2 = particles.get(n * (x - 1) + y);
                s1c.reset();
                forces.add(s1c);
                Spring s2 = new Spring();
                s2.p1 = particles.get(n * (x - 1) + y);
                s2.p2 = particles.get(n * x + y);
                s2.reset();
                forces.add(s2);
                Spring s3 = new Spring();
                s3.p1 = particles.get(n * x + y - 1);
                s3.p2 = particles.get(n * x + y);
                s3.reset();
                forces.add(s3);
                Surface f = Surface.create(
                        particles.get(n * (x - 1) + y - 1),
                        particles.get(n * (x - 1) + y),
                        particles.get(n * x + y),
                        particles.get(n * x + y - 1));
                forces.add(f);
            }
        }

        return new Paper(particles, forces);
    }

    public List<Force> getForces() {
        return forces;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public List<Spring> getSpringForces() {
        return springForces;
    }

    public List<Surface> getSurfaces() {
        return surfaces;
    }
}
