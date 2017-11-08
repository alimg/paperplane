package bilkent.cs565.paper.newton;

public class Stepper {
    public PaperPhysics paper;

    public Stepper(PaperPhysics paperP) {
        this.paper = paperP;
    }

    public void step(double dt) {
        paper.step(dt);
    }
}
