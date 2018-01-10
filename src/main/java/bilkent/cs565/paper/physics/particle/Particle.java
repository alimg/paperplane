package bilkent.cs565.paper.physics.particle;

import glm.vec._3.Vec3;

public class Particle {
    public final int id;
    public float mass = 0;
    public Vec3 pos = new Vec3();
    public Vec3 norm = new Vec3();
    public Vec3 normSum = new Vec3();
    public int normCount = 0;
    public Vec3 dir = new Vec3();
    public Vec3 vel = new Vec3(0,-12,2);

    public Vec3 dxdt[] = new Vec3[5];
    public Vec3 dvdt[] = new Vec3[5];

    public Particle(int id) {
        this.id = id;
        for (int i=0;i<dxdt.length;i++) {
            dxdt[i] = new Vec3();
            dvdt[i] = new Vec3();
        }
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(id);
    }
}
