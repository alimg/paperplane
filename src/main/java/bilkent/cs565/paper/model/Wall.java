package bilkent.cs565.paper.model;

import bilkent.cs565.paper.World;
import bilkent.cs565.paper.gl.Constants;
import bilkent.cs565.paper.gl.GLM;
import bilkent.cs565.paper.model.particle.Particle;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import glm.vec._3.Vec3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_DYNAMIC_DRAW;

public class Wall {
    private static final float BOUNCE = 0.2f;
    private static final float DAMP = 0.9f;
    private static final float FRICTION = 10.f;
    private final Vec3 pos;
    private final Vec3 norm;
    private final Vec3 right;

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(World.Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(2);
    private FloatBuffer vertexBuffer;
    private ShortBuffer elementBuffer;
    private FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);

    public Wall(Vec3 pos, Vec3 norm, Vec3 right) {
        this.pos = pos;
        this.norm = norm;
        this.right = right;
    }

    public void step(float dt, int order, Particle particle) {
        Vec3 x = particle.pos.plus(particle.dxdt[order-1].times(dt)).minus(pos);
        Vec3 v = particle.vel.plus(particle.dvdt[order-1].times(dt));
        float kMax = particle.mass / (World.STEP_SIZE * World.STEP_SIZE);
        float dMax = particle.mass / (World.STEP_SIZE);

        float d = GLM.dot(x, norm);
        if (d<0) {
            Vec3 f = norm.times(d * -kMax * BOUNCE - GLM.dot(norm, v) * dMax * DAMP)
                    //.plus(norm.cross(v).times())
                    .plus(norm.cross(v).cross(norm).times(-FRICTION));
            particle.dvdt[order] = particle.dvdt[order].plus(f.div(particle.mass));
        }
    }

    public void initVertexArray(GL3 gl) {
        vertexBuffer = GLBuffers.newDirectFloatBuffer(4 * 9);
        elementBuffer = GLBuffers.newDirectShortBuffer(4);

        float f[] = new float[]{0,0,0, 0.5f, 0.5f, 0.5f, norm.x, norm.y, norm.z};
        Vec3 u = norm.cross(right);
        Vec3 r = u.cross(norm).times(right.length());
        u=u.times(right.length());
        Vec3 p = pos.minus(norm.times(0.01));
        p.plus(r.plus(u)).to(f);
        vertexBuffer.put(f,0,9);
        p.plus(r.minus(u)).to(f);
        vertexBuffer.put(f,0,9);
        p.minus(r.plus(u)).to(f);
        vertexBuffer.put(f,0,9);
        p.minus(r.minus(u)).to(f);
        vertexBuffer.put(f,0,9);

        float[] buf = new float[vertexBuffer.capacity()];
        vertexBuffer.position(0);
        vertexBuffer.get(buf);
        vertexBuffer.position(0);

        elementBuffer.put(0, (short) 0);
        elementBuffer.put(1, (short) 1);
        elementBuffer.put(2, (short) 3);
        elementBuffer.put(3, (short) 2);

        gl.glGenBuffers(bufferName.capacity(), bufferName);
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(World.Buffer.VERTEX));
        gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(World.Buffer.ELEMENT));
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Short.BYTES, elementBuffer, GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


        gl.glGenVertexArrays(1, vertexArrayName);
        gl.glBindVertexArray(vertexArrayName.get(0));
        {
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(World.Buffer.VERTEX));
            {
                int stride = Vec3.SIZE * 3;
                int offset = 0;

                gl.glEnableVertexAttribArray(Constants.Attr.POSITION);
                gl.glVertexAttribPointer(Constants.Attr.POSITION, Vec3.length, GL_FLOAT, false, stride, offset);

                offset = Vec3.SIZE;
                gl.glEnableVertexAttribArray(Constants.Attr.COLOR);
                gl.glVertexAttribPointer(Constants.Attr.COLOR, Vec3.length, GL_FLOAT, false, stride, offset);

                offset = Vec3.SIZE * 2;
                gl.glEnableVertexAttribArray(Constants.Attr.NORMAL);
                gl.glVertexAttribPointer(Constants.Attr.NORMAL, Vec3.length, GL_FLOAT, false, stride, offset);
            }
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        gl.glBindVertexArray(0);
    }

    public void draw(GL3 gl) {
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(World.Buffer.VERTEX));
        gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * Float.BYTES, vertexBuffer);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(World.Buffer.ELEMENT));
        gl.glDrawElements(GL_TRIANGLE_STRIP, elementBuffer.capacity(), GL_UNSIGNED_SHORT, 0);
        gl.glBindVertexArray(0);
    }
}
