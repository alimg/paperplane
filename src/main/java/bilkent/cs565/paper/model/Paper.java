package bilkent.cs565.paper.model;

import bilkent.cs565.paper.model.particle.Edge;
import bilkent.cs565.paper.model.particle.Force;
import bilkent.cs565.paper.model.particle.HingeForce;
import bilkent.cs565.paper.model.particle.Particle;
import bilkent.cs565.paper.model.particle.Spring;
import bilkent.cs565.paper.model.particle.Surface;
import glm.vec._3.Vec3;
import org.smurn.jply.*;
import org.smurn.jply.util.NormalMode;
import org.smurn.jply.util.NormalizingPlyReader;
import org.smurn.jply.util.TesselationMode;
import org.smurn.jply.util.TextureMode;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Paper createFromModel() {
        ArrayList<Particle> particles = new ArrayList<>();
        ArrayList<Force> forces = new ArrayList<>();
        HashMap<Edge, Particle> hingeEdges = new HashMap<>();
        try {
            NormalizingPlyReader ply = new NormalizingPlyReader(new PlyReaderFile(
                    ClassLoader.getSystemResourceAsStream("model/plane.ply")),
                    TesselationMode.TRIANGLES,
                    NormalMode.ADD_NORMALS_CCW,
                    TextureMode.XY
            );

            int vertexCount = ply.getElementCount("vertex");
            int triangleCount = ply.getElementCount("face");

            ElementReader reader = ply.nextElementReader();
            while (reader != null) {
                ElementType type = reader.getElementType();
                if (type.getName().equals("vertex")) {
                    Element element = reader.readElement();
                    while (element != null) {
                        Particle p = new Particle(particles.size());
                        p.pos = new Vec3(element.getDouble("x"),
                                element.getDouble("y"),
                                element.getDouble("z"));
                        particles.add(p);
                        element = reader.readElement();
                    }
                } else if (type.getName().equals("face")) {
                    Element triangle = reader.readElement();
                    while (triangle != null) {
                        int[] indices = triangle.getIntList("vertex_index");
                        assert indices.length == 3;
                        Spring s1 = new Spring();
                        s1.p1 = particles.get(indices[0]);
                        s1.p2 = particles.get(indices[1]);
                        s1.reset();
                        forces.add(s1);
                        Spring s2 = new Spring();
                        s2.p1 = particles.get(indices[1]);
                        s2.p2 = particles.get(indices[2]);
                        s2.reset();
                        forces.add(s2);
                        Spring s3 = new Spring();
                        s3.p1 = particles.get(indices[2]);
                        s3.p2 = particles.get(indices[0]);
                        s3.reset();
                        forces.add(s3);

                        Surface f = Surface.create(
                                particles.get(indices[0]),
                                particles.get(indices[1]),
                                particles.get(indices[2]));
                        forces.add(f);
                        addHinges(f, hingeEdges, forces);
                        triangle = reader.readElement();
                    }
                }

                reader.close();
                reader = ply.nextElementReader();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Paper(particles, forces);
    }

    public static Paper createFlat(int n, int m, float X, float Y) {
        ArrayList<Particle> particles = new ArrayList<>();
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < m; y++) {
                Particle p = new Particle(particles.size());
                p.pos = new Vec3(x*X/n, y*Y/m, Math.random()*0.11 + x*0.02*X/n+ 4);
                p.norm = new Vec3(0, 0, 1);
                p.dir = new Vec3(0, -1, 0);
                p.vel = new Vec3(0.2f,0.1f,0);
                particles.add(p);
            }
        }

        ArrayList<Force> forces = new ArrayList<>();
        HashMap<Edge, Particle> hingeEdges = new HashMap<>();

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
                //Spring s1c = new Spring();
                //s1c.p1 = particles.get(n * x + y - 1);
                //s1c.p2 = particles.get(n * (x - 1) + y);
                //s1c.reset();
                //forces.add(s1c);
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
                Surface f1 = Surface.create(
                        particles.get(n * (x - 1) + y - 1),
                        particles.get(n * (x - 1) + y),
                        particles.get(n * x + y));
                Surface f2 = Surface.create(
                        particles.get(n * (x - 1) + y - 1),
                        particles.get(n * x + y),
                        particles.get(n * x + y - 1));
                addHinges(f1, hingeEdges, forces);
                addHinges(f2, hingeEdges, forces);
                forces.add(f1);
                forces.add(f2);
            }
        }

        return new Paper(particles, forces);
    }

    private static void addHinges(
        final Surface f, final HashMap<Edge, Particle> hingeEdges, final ArrayList<Force> forces) {
        for (int i = 0; i<3; i++) {
            int j = (i+1) % 3;
            int k = (i+2) % 3;

            final Edge edge = new Edge(f.particles[i], f.particles[j]);
            final Particle h = hingeEdges.get(edge);
            if (h != null) {
                forces.add(new HingeForce(edge, h, f.particles[k]));
            } else {
                hingeEdges.put(edge, f.particles[k]);
            }
        }
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
