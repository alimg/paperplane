package bilkent.cs565.paper;

import bilkent.cs565.paper.gl.Constants;
import bilkent.cs565.paper.model.Paper;
import bilkent.cs565.paper.newton.PaperPhysics;
import bilkent.cs565.paper.newton.Stepper;
import bilkent.cs565.paper.model.particle.Particle;
import bilkent.cs565.paper.model.particle.Spring;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import glm.mat.Mat4x4;
import glm.vec._3.Vec3;
import uno.glsl.Program;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static uno.gl.GlErrorKt.checkError;

public class World {
    private int elementCount;

    private interface Buffer {
        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MAX = 3;
    }

    private final Paper paper;
    private final Vec3 gravity = new Vec3(0, 0, -3.9);
    private final Stepper stepper;
    private long time;
    private FloatBuffer vertexBuffer;
    private ShortBuffer elementBuffer;
    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);
    private FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);


    public World() {
        //paper = Paper.createFlat(15,15, 15, 15);
        paper = Paper.createFlat(10, 10, 10, 10);
        PaperPhysics paperP = new PaperPhysics(paper, gravity);
        stepper = new Stepper(paperP);

        time = System.nanoTime();
    }

    public void update() {
        long newTime = System.nanoTime();
        float dt = (newTime - time) / 1000000000.0f;
        if (dt > 0.1) {
            // limit time step to 100msec
            dt = 0.1f;
        }
        synchronized(stepper)
        {
            stepper.step(0.001);
        }
        time = newTime;
    }

    public void initBuffers(GL3 gl) {
        int vertexCount = paper.getParticles().size() * 6 * 2;
        elementCount = paper.getSpringForces().size() * 2 + paper.getParticles().size() * 2;
        vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexCount);
        elementBuffer = GLBuffers.newDirectShortBuffer(elementCount);

        int i = 0;
        for (Spring f: paper.getSpringForces()) {
            elementBuffer.put(i++, (short) (f.p1.id*2));
            elementBuffer.put(i++, (short) (f.p2.id*2));
        }
        for (Particle p: paper.getParticles()) {
            elementBuffer.put(i++, (short) (p.id*2));
            elementBuffer.put(i++, (short) (p.id*2+1));
        }

        gl.glGenBuffers(Buffer.MAX, bufferName);

        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Short.BYTES, elementBuffer, GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


        gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES));
        gl.glBufferData(GL_UNIFORM_BUFFER, Mat4x4.SIZE * 2, null, GL_STREAM_DRAW);
        gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        checkError(gl, "initBuffers");
    }

    public void initVertexArray(GL3 gl) {

        gl.glGenVertexArrays(1, vertexArrayName);
        gl.glBindVertexArray(vertexArrayName.get(0));
        {
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            {
                int stride = Vec3.SIZE + Vec3.SIZE;
                int offset = 0;

                gl.glEnableVertexAttribArray(Constants.Attr.POSITION);
                gl.glVertexAttribPointer(Constants.Attr.POSITION, Vec3.length, GL_FLOAT, false, stride, offset);

                offset = Vec3.SIZE;
                gl.glEnableVertexAttribArray(Constants.Attr.COLOR);
                gl.glVertexAttribPointer(Constants.Attr.COLOR, Vec3.length, GL_FLOAT, false, stride, offset);
            }
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl.glBindVertexArray(0);
        checkError(gl, "initVao");
    }

    public void draw(GL3 gl, Program program) {
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        synchronized(stepper) {
            for (Particle p : paper.getParticles())
            {
                vertexBuffer.put(p.id * 6 * 2, p.pos.x);
                vertexBuffer.put(p.id * 6 * 2 + 1, p.pos.y);
                vertexBuffer.put(p.id * 6 * 2 + 2, p.pos.z);
                vertexBuffer.put(p.id * 6 * 2 + 3, Math.abs(p.pos.z / 10));


                vertexBuffer.put(p.id * 6 * 2 + 6, p.pos.x + p.norm.x);
                vertexBuffer.put(p.id * 6 * 2 + 6 + 1, p.pos.y + p.norm.y);
                vertexBuffer.put(p.id * 6 * 2 + 6 + 2, p.pos.z + p.norm.z);
                vertexBuffer.put(p.id * 6 * 2 + 6 + 5, 1);
            }
        }
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * Float.BYTES, vertexBuffer);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);


        Mat4x4 model = new Mat4x4();
        model
                .scale(0.50f)
                .to(matBuffer);

        gl.glUniformMatrix4fv(program.get("model"), 1, false, matBuffer);

        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glDrawElements(GL_LINES, elementBuffer.capacity(), GL_UNSIGNED_SHORT, 0);
        gl.glBindVertexArray(0);

        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void destroy(final GL3 gl)
    {
        gl.glDeleteVertexArrays(1, vertexArrayName);
        gl.glDeleteBuffers(Buffer.MAX, bufferName);
    }

}
