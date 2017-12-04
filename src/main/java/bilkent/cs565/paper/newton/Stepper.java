package bilkent.cs565.paper.newton;

public class Stepper {
    public PaperPhysics paper;

    public Stepper(PaperPhysics paperP) {
        this.paper = paperP;
    }

    public void step(double dt) {
        paper.step(0, 0);
        paper.step(dt*0.5, 1);
        paper.step(dt*0.5, 2);
        paper.step(dt, 3);
        paper.integrate(dt);

    }
}
