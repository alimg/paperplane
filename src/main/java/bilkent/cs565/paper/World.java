package bilkent.cs565.paper;

import bilkent.cs565.paper.gl.Constants;
import bilkent.cs565.paper.model.Paper;
import bilkent.cs565.paper.model.Wall;
import bilkent.cs565.paper.model.particle.Surface;
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
import java.util.ArrayList;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static uno.gl.GlErrorKt.checkError;

public class World {
    public static final float STEP_SIZE = 1f/120f;
    private final ArrayList<Wall> walls = new ArrayList<>();
    private int elementCount;
    private ShortBuffer surfaceBuffer;
    private int frames;
    private int framesD;

    public interface Buffer {
        int VERTEX = 0;
        int ELEMENT = 1;
        int ELEMENT2 = 2;
        int GLOBAL_MATRICES = 3;
        int MAX = 4;
    }

    private final Paper paper;
    private final Vec3 gravity = new Vec3(0, 0, -2.9);
    private final Stepper stepper;
    private long time;
    private FloatBuffer vertexBuffer;
    private ShortBuffer elementBuffer;
    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(2);
    private FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);


    public World() {
        //paper = Paper.createFlat(15,15, 5, 5);
        //paper = Paper.createFlat(16, 16, 5, 5);
        //paper = Paper.createFlat(24, 24, 6, 6);
        //paper = Paper.createFlat(12, 12, 1.5f, 1.5f);
        paper = Paper.createFromModel();
        //paper = Paper.createFlat(8, 8, 6, 6);
        walls.add(new Wall(new Vec3(0, -40, 0), new Vec3(0, 1, 0), new Vec3(10, 0, 0)));
        walls.add(new Wall(new Vec3(0, 0, -40), new Vec3(0, 0, 1), new Vec3(10, 0, 0)));
        PaperPhysics paperP = new PaperPhysics(paper, walls, gravity);
        stepper = new Stepper(paperP);

        time = System.nanoTime();
    }

    public void update() {
        long newTime = System.nanoTime();
        float dt = (newTime - time) / 1000000000.0f;
        if (((long)time/1000000000) < (newTime/1000000000) ) {
            System.out.println(String.format("%d %d", framesD, frames));
            framesD = 0;
        }
        if (dt > 0.1) {
            // limit time step to 100msec
            dt = 0.1f;
        }
        synchronized(stepper)
        {
            stepper.step(STEP_SIZE);
        }
        frames += 1;
        framesD += 1;
        time = newTime;
    }

    public void initBuffers(GL3 gl) {
        int vertexCount = paper.getParticles().size() * 9 * 2;
        elementCount = paper.getSpringForces().size() * 2 + paper.getParticles().size() * 2;
        vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexCount);
        elementBuffer = GLBuffers.newDirectShortBuffer(elementCount);
        surfaceBuffer = GLBuffers.newDirectShortBuffer(paper.getSurfaces().size() * 3);

        int i = 0;
        for (Spring f: paper.getSpringForces()) {
            elementBuffer.put(i++, (short) (f.p1.id*2));
            elementBuffer.put(i++, (short) (f.p2.id*2));
        }
        for (Particle p: paper.getParticles()) {
            elementBuffer.put(i++, (short) (p.id*2));
            elementBuffer.put(i++, (short) (p.id*2+1));
        }
        i=0;
        for (Surface f: paper.getSurfaces()) {
            surfaceBuffer.put(i++, (short) (f.particles[0].id*2));
            surfaceBuffer.put(i++, (short) (f.particles[1].id*2));
            surfaceBuffer.put(i++, (short) (f.particles[2].id*2));
        }


        gl.glGenBuffers(Buffer.MAX, bufferName);

        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl.glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Short.BYTES, elementBuffer, GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT2));
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, surfaceBuffer.capacity() * Short.BYTES, surfaceBuffer, GL_DYNAMIC_DRAW);
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

        for (Wall w: walls) {
            w.initVertexArray(gl);
        }

        gl.glCullFace(GL_FRONT_AND_BACK);
        checkError(gl, "initVao");
    }

    public void draw(GL3 gl, Program program, Program programLine) {
        synchronized(stepper) {
            for (Particle p : paper.getParticles())
            {
                vertexBuffer.put(p.id * 9 * 2, p.pos.x);
                vertexBuffer.put(p.id * 9 * 2 + 1, p.pos.y);
                vertexBuffer.put(p.id * 9 * 2 + 2, p.pos.z);
                vertexBuffer.put(p.id * 9 * 2 + 3, 0.8f);
                vertexBuffer.put(p.id * 9 * 2 + 4, 0.7f);
                vertexBuffer.put(p.id * 9 * 2 + 5, 0.7f);
                vertexBuffer.put(p.id * 9 * 2 + 6, p.norm.x);
                vertexBuffer.put(p.id * 9 * 2 + 7, p.norm.y);
                vertexBuffer.put(p.id * 9 * 2 + 8, p.norm.z);

                vertexBuffer.put(p.id * 9 * 2 + 9, p.pos.x + p.norm.x*0.1f);
                vertexBuffer.put(p.id * 9 * 2 + 9 + 1, p.pos.y + p.norm.y*0.1f);
                vertexBuffer.put(p.id * 9 * 2 + 9 + 2, p.pos.z + p.norm.z*0.1f);
                vertexBuffer.put(p.id * 9 * 2 + 9 + 5, 1);
            }
        }

        gl.glUseProgram(program.name);
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * Float.BYTES, vertexBuffer);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

        Mat4x4 model = new Mat4x4();
        model.to(matBuffer);
        gl.glUniformMatrix4fv(program.get("model"), 1, false, matBuffer);


        gl.glUseProgram(programLine.name);
        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl.glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * Float.BYTES, vertexBuffer);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
        gl.glUniformMatrix4fv(program.get("model"), 1, false, matBuffer);

        gl.glBindVertexArray(vertexArrayName.get(0));
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl.glDrawElements(GL_LINES, elementBuffer.capacity(), GL_UNSIGNED_SHORT, 0);
        gl.glUseProgram(program.name);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT2));
        gl.glDrawElements(GL_TRIANGLES, surfaceBuffer.capacity(), GL_UNSIGNED_SHORT, 0);


        model = new Mat4x4();
        model.to(matBuffer);
        gl.glUniformMatrix4fv(program.get("model"), 1, false, matBuffer);
        for (Wall wall: walls) {
            wall.draw(gl);
        }

        gl.glBindVertexArray(0);
        gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void destroy(final GL3 gl)
    {
        gl.glDeleteVertexArrays(1, vertexArrayName);
        gl.glDeleteBuffers(Buffer.MAX, bufferName);
    }

}
