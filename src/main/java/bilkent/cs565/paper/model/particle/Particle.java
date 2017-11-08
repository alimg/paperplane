package bilkent.cs565.paper.model.particle;

import glm.vec._3.Vec3;
import glm.mat.Mat4x4;

public class Particle {
    public final int id;

    public Mat4x4 orientation = new Mat4x4();
    public Vec3 pos = new Vec3();
    public Vec3 norm = new Vec3();
    public Vec3 dir = new Vec3();
    public Vec3 vel = new Vec3();
    public Vec3 acc = new Vec3();

    public Particle(int id) {
        this.id = id;
    }
}
