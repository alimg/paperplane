package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import com.jogamp.opengl.GL;
import glm.vec._3.Vec3;

public class HingeForce implements Force
{
    private static final float STIFFNESS = 20.1f;
    private static final float DAMPING = 0.1f;
    private final Particle edgeA;
    private final Particle edgeB;
    private final float med;
    private final Particle pa;
    private final Particle pb;
    private final float rest;

    public HingeForce(final Edge edge, final Particle pa, final Particle pb)
    {
        edgeA = edge.a;
        edgeB = edge.b;

        this.pa = pa;
        this.pb = pb;

        // http://morroworks.com/Content/Docs/Rays%20closest%20point.pdf
        final Vec3 c = pa.pos.minus(edgeA.pos);
        final Vec3 d = pb.pos.minus(pa.pos).normalize();
        final Vec3 e = edgeB.pos.minus(edgeA.pos).normalize();
        final float ed = (-GLM.dot(e, d) * GLM.dot(d, c) + GLM.dot(e, c) * GLM.dot(d, d)) /
                (GLM.dot(e, e) * GLM.dot(d, d) - GLM.dot(e, d) * GLM.dot(e, d));
        //final float ed = GLM.dot(edgeA.pos.minus(pa.pos), e.cross(d))/e.cross(d).length();
        med = ed;
        final Vec3 pivot = edgeA.pos.plus(e.times(ed));
        final Vec3 f = pa.pos.minus(pivot);
        final Vec3 g = pb.pos.minus(pivot);

        int dir = (GLM.dot(e, f.cross(g))>0) ? 1 : -1;
        float cos = GLM.dot(f.normalize(), g.normalize());
        if (cos>1) {
            cos = 1f;
        } else if (cos<-1) {
            cos = -1f;
        }
        rest = (float) (Math.PI - Math.acos(cos)) * dir;
        if (Float.isNaN(rest)) {
            System.out.println(this);
            throw new RuntimeException("hinge angle is NaN");
        }
    }

    @Override
    public void step(final double dt, final int order)
    {
        Vec3 posEA = edgeA.pos.plus(edgeA.dxdt[order-1].times(dt));
        Vec3 velEA = edgeA.vel.plus(edgeA.dvdt[order-1].times(dt));
        Vec3 posEB = edgeB.pos.plus(edgeB.dxdt[order-1].times(dt));
        Vec3 velEB = edgeB.vel.plus(edgeB.dvdt[order-1].times(dt));
        Vec3 posPA = pa.pos.plus(pa.dxdt[order-1].times(dt));
        Vec3 velPA = pa.vel.plus(pa.dvdt[order-1].times(dt));
        Vec3 posPB = pb.pos.plus(pb.dxdt[order-1].times(dt));
        Vec3 velPB = pb.vel.plus(pb.dvdt[order-1].times(dt));


        final Vec3 c = posPA.minus(posEA);
        final Vec3 d = posPB.minus(posPA).normalize();
        Vec3 e = posEB.minus(posEA);
        final float elen = e.length();
        e = e.normalize();

        float ed = (-GLM.dot(e, d) * GLM.dot(d, c) + GLM.dot(e, c) * GLM.dot(d, d)) /
                (GLM.dot(e, e) * GLM.dot(d, d) - GLM.dot(e, d) * GLM.dot(e, d));
        if (Float.isNaN(ed)) {
            ed = 0;
        }

        final Vec3 pivot = posEA.plus(e.times(ed));
        final Vec3 f = posPA.minus(pivot);
        final Vec3 g = posPB.minus(pivot);

        int dir = (GLM.dot(e, f.cross(g))>0) ? 1 : -1;
        float cos = GLM.dot(f.normalize(), g.normalize());
        if (cos>1) {
            cos = 1f;
        } else if (cos<-1) {
            cos = -1f;
        }
        float angle = (float) (Math.PI - Math.acos(cos)) * dir;
        if (Float.isNaN(angle)) {
            angle = 0;
        }
        angle = rest - angle;
        if (Math.abs(angle)> 0.1) {
            System.out.println(angle);
        }

        float torque = angle * -STIFFNESS;

        final Vec3 normPA = f.cross(e).normalize();
        final Vec3 normPB = e.cross(g).normalize();

        float fl = f.length();
        float gl = g.length();
        if (fl < 0.00001f || gl < 0.00001f) {
            return;
        }
        Vec3 vpivot = velEA.times((1.f - ed/elen)).plus(velEB.times(ed));
        Vec3 vlina = normPA.times(GLM.dot(normPA, velPA.minus(vpivot)));
        Vec3 vlinb = normPB.times(GLM.dot(normPB, velPB.minus(vpivot)));

        Vec3 fa = normPA.times(torque/fl).plus(vlina.times(-DAMPING));
        Vec3 fb = normPB.times(torque/gl).plus(vlinb.times(-DAMPING));
        if (pa.id == 0) {
            System.out.println(String.format("%f %f %f", vlina.x, vlina.y, vlina.z));
        }


        pa.dvdt[order] = pa.dvdt[order].plus(fa.div(pa.mass));
        pb.dvdt[order] = pb.dvdt[order].plus(fb.div(pb.mass));

        Vec3 ft = fa.plus(fb);
        edgeA.dvdt[order] = edgeA.dvdt[order].minus(ft.times((1.f - ed/elen) / edgeA.mass));
        edgeB.dvdt[order] = edgeB.dvdt[order].minus(ft.times(ed/elen / edgeB.mass));
    }
}
