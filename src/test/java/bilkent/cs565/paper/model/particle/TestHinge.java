package bilkent.cs565.paper.model.particle;

import glm.vec._3.Vec3;
import org.junit.Test;

public class TestHinge {
    @Test
    public void testAngle() {
        Vec3 e = new Vec3(0, 0, 1).normalize();
        Vec3 f = new Vec3(1, 1, 0).normalize();
        Vec3 g = new Vec3(1, 1, 0).normalize();
        System.out.println(HingeForce.getAngle(e, f, g));

        g = new Vec3(0, 1, 0);
        System.out.println(HingeForce.getAngle(e, f, g));
        g = new Vec3(-1, 0, 0);
        System.out.println(HingeForce.getAngle(e, f, g));
        g = new Vec3(-1, -1, 0);
        System.out.println(HingeForce.getAngle(e, f, g));
        g = new Vec3(1, -1, 0);
        System.out.println(HingeForce.getAngle(e, f, g));


        System.out.println(new Vec3(1, 1, 0).normalize().cross(new Vec3(-1,0,0  ).normalize()).z);
        System.out.println(new Vec3(1, 1, 0).normalize().cross(new Vec3(-1,0.1,0).normalize()).z);
        System.out.println(new Vec3(1, 1, 0).normalize().cross(new Vec3(-1,0.2,0).normalize()).z);
        System.out.println(new Vec3(1, 1, 0).normalize().cross(new Vec3(0,1,0).normalize()).z);
        System.out.println(new Vec3(1, 1, 0).normalize().cross(new Vec3(0.1,1,0).normalize()).z);
    }
}
