package bilkent.cs565.paper.model.particle;

import bilkent.cs565.paper.gl.GLM;
import glm.vec._3.Vec3;

public class HingeForce implements Force
{
    private static final float STIFFNESS = 500.f;
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

        final Vec3 eapa = edgeA.pos.minus(pa.pos).times(-1);
        final Vec3 papb = pb.pos.minus(pa.pos).normalize();
        final Vec3 e = edgeB.pos.minus(edgeA.pos).normalize();
        // a/sina = b/sinb = c/sinc
        final float cosa = GLM.dot(e, papb);
        final float sina = (float) Math.sqrt(1-cosa*cosa);
        final float cosb = GLM.dot(eapa.normalize(), papb);
        final float sinb = (float) Math.sqrt(1-cosb*cosb);
        final float ed = eapa.length() * sinb / sina;
        med = ed;
        final Vec3 pivot = edgeA.pos.plus(e.times(ed));
        final Vec3 f = pa.pos.minus(pivot);
        final Vec3 g = pb.pos.minus(pivot);

        int dir = (GLM.dot(e, f.cross(g))>0) ? 1 : -1;
        float cos = GLM.dot(f, g)/(f.length() + g.length());
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

        final Vec3 eapa = posEA.minus(posPA);
        final Vec3 papb = posPB.minus(posPA).normalize();
        Vec3 e = posEB.minus(posEA);
        final float elen = e.length();
        e = e.normalize();
        // a/sina = b/sinb = c/sinc

        final float cosa = GLM.dot(e, papb);
        final float sina = (float) Math.sqrt(1-cosa*cosa);
        final float cosb = GLM.dot(eapa.normalize(), papb);
        final float sinb = (float) Math.sqrt(1-cosb*cosb);
        final float ed = eapa.length() * sinb / sina;
        final Vec3 pivot = posEA.plus(e.times(ed));
        final Vec3 f = posPA.minus(pivot);
        final Vec3 g = posPB.minus(pivot);

        int dir = (GLM.dot(e, f.cross(g))>0) ? 1 : -1;
        double cos = GLM.dot(f, g)/Math.sqrt(f.length() * g.length());
        float angle = (float) (Math.PI - Math.acos(cos)) * dir;
        if (Float.isNaN(angle)) {
            angle = 0;
        }
        angle = rest - angle;

        final Vec3 normPA = f.cross(e);
        final Vec3 normPB = e.cross(g);

        Vec3 fa = normPA.times(angle * -STIFFNESS);
        Vec3 fb = normPB.times(angle * -STIFFNESS);

        pa.dvdt[order] = pa.dvdt[order].plus(fa.div(pa.mass));
        pb.dvdt[order] = pb.dvdt[order].plus(fb.div(pb.mass));

        Vec3 ft = fa.plus(fb);
        edgeA.dvdt[order] = edgeA.dvdt[order].minus(ft.times((1.f - ed/elen) / edgeA.mass));
        edgeB.dvdt[order] = edgeB.dvdt[order].minus(ft.times(ed/elen / edgeB.mass));
    }
}
