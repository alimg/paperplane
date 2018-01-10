package bilkent.cs565.paper.physics.particle;

public interface Force {
    void step(double dt, int order);
}
