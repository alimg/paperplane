package bilkent.cs565.paper.newton;

public class Stepper {
    public PaperPhysics paper;

    public Stepper(PaperPhysics paperP) {
        this.paper = paperP;
    }

    public void step(float dt) {
        paper.step(0, 1);
        paper.step(dt*0.5, 2);
        paper.step(dt*0.5, 3);
        paper.step(dt, 4);
        paper.integrate(dt);
        //System.out.println(paper.paper.getParticles().get(0).vel.length());
    }
}
