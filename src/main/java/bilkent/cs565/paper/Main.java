package bilkent.cs565.paper;

import bilkent.cs565.paper.gl.Constants;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import glm.mat.Mat4x4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import uno.glsl.Program;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES3.*;
import static glm.GlmKt.glm;
import static uno.buffer.UtilKt.destroyBuffers;
import static uno.gl.GlErrorKt.checkError;


public class Main implements GLEventListener, KeyListener {

    private static GLWindow window;
    private static Animator animator;
    private final World world;
    private Thread thread;
    private AtomicBoolean running = new AtomicBoolean(true);
    private Vec3 cam = new Vec3();
    private Vec3 camV = new Vec3();
    private Vec3 camDir = new Vec3(0,0,1);
    private Vec3 camNorm = new Vec3(0,1,0);
    private float camRx = 0;
    private float camRz = 0;

    public Main() {
        world = new World();
    }

    public static void main(String[] args) {
        new Main().setup();
    }

    private interface Buffer {
        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MAX = 3;
    }

    private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);

    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(4);
    private FloatBuffer clearDepth = GLBuffers.newDirectFloatBuffer(1);

    private FloatBuffer matBuffer = GLBuffers.newDirectFloatBuffer(16);

    private Program program;

    private long start;

    private void setup() {
        GLProfile glProfile = GLProfile.get(GLProfile.GL3);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        window = GLWindow.create(glCapabilities);

        window.setTitle("Paper");
        window.setSize(1024, 768);

        window.setVisible(true);

        window.addGLEventListener(this);
        window.addKeyListener(this);

        animator = new Animator(window);
        animator.start();

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyed(WindowEvent e) {
                animator.stop();
                System.exit(1);
            }
        });
    }

    @Override
    public void init(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();

        initBuffers(gl);

        initProgram(gl);

        gl.glEnable(GL_DEPTH_TEST);

        world.initBuffers(gl);
        world.initVertexArray(gl);
        start = System.currentTimeMillis();

        thread = new Thread(() -> {
            while (running.get()) {
                world.update();
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void initBuffers(GL3 gl) {
        gl.glGenBuffers(Buffer.MAX, bufferName);
        gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES));
        gl.glBufferData(GL_UNIFORM_BUFFER, Mat4x4.SIZE * 2, null, GL_STREAM_DRAW);
        gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl.glBindBufferBase(GL_UNIFORM_BUFFER, Constants.Uniform.GLOBAL_MATRICES, bufferName.get(Buffer.GLOBAL_MATRICES));
        checkError(gl, "initBuffers");
    }


    private void initProgram(GL3 gl) {

        program = new Program(gl, getClass(), "gl3/shaders", "shader.vert", "shader.frag", "model");

        int globalMatricesBI = gl.glGetUniformBlockIndex(program.name, "GlobalMatrices");

        if (globalMatricesBI == -1) {
            System.err.println("block index 'GlobalMatrices' not found!");
        }
        gl.glUniformBlockBinding(program.name, globalMatricesBI, Constants.Uniform.GLOBAL_MATRICES);

        checkError(gl, "initProgram");
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        cam = cam.plus(camV.times(-0.01f));
        GL3 gl = drawable.getGL().getGL3();
        gl.glBindBufferBase(GL_UNIFORM_BUFFER, Constants.Uniform.GLOBAL_MATRICES, bufferName.get(Buffer.GLOBAL_MATRICES));

        long now = System.currentTimeMillis();
        float diff = (float) (now - start) / 1_000f;
        // view matrix
        {
            diff = Math.max(0,diff-4);
            Mat4x4 view = new Mat4x4();
            view = view.scale((float)(0.15f + 0.025f*(1/(1+diff) -1)));
            view = view.scale(1,1,0.1f);
            view = view.rotate((float) (Math.PI/4.0f) - camRx, -1,0, -camRz);
            //view = view.translate(0,0,-1f);
            view = view.translate(-2.5f, 15+ diff*0.1f,-10);
            view = view.translate(cam);
            view.to(matBuffer);

            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES));
            gl.glBufferSubData(GL_UNIFORM_BUFFER, Mat4x4.SIZE, Mat4x4.SIZE, matBuffer);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0.66f).put(1, .66f).put(2, 0.66f).put(3, 1f));
        gl.glClearBufferfv(GL_DEPTH, 0, clearDepth.put(0, 1f));

        gl.glUseProgram(program.name);
        world.draw(gl, program);

        gl.glBindVertexArray(0);
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


        gl.glUseProgram(0);

        checkError(gl, "display");
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL3 gl = drawable.getGL().getGL3();

        glm.ortho(-1f, 1f, -1f, 1f, 1f, -1f).to(matBuffer);

        gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES));
        gl.glBufferSubData(GL_UNIFORM_BUFFER, 0, Mat4x4.SIZE, matBuffer);
        gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        gl.glViewport(x, y, width, height);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

        GL3 gl = drawable.getGL().getGL3();

        gl.glDeleteProgram(program.name);
        gl.glDeleteVertexArrays(1, vertexArrayName);
        gl.glDeleteBuffers(Buffer.MAX, bufferName);

        world.destroy(gl);

        destroyBuffers(vertexArrayName, bufferName, matBuffer, clearColor, clearDepth);
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            running.set(false);
            new Thread(() -> window.destroy()).start();
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                camRx+=0.1;
                break;
            case KeyEvent.VK_DOWN:
                camRx-=0.1;
                break;
            case KeyEvent.VK_LEFT:
                camRz+=0.1;
                break;
            case KeyEvent.VK_RIGHT:
                camRz-=0.1;
                break;
            case KeyEvent.VK_R:
                camV = camV.plus(camNorm);
                break;
            case KeyEvent.VK_H:
                camV = camV.minus(camNorm);
                break;
            case KeyEvent.VK_W:
                camV = camV.plus(camDir);
                break;
            case KeyEvent.VK_S:
                camV = camV.minus(camDir);
                break;
            case KeyEvent.VK_A:
                camV = camV.minus(camDir.cross(camNorm));
                break;
            case KeyEvent.VK_D:
                camV = camV.plus(camDir.cross(camNorm));
                break;
        };
    }

    @Override
    public void keyReleased(KeyEvent e) {
        cam = cam.plus(camV.times(0.1));
        if( !e.isPrintableKey() || e.isAutoRepeat()  ) {
            return;
        }
        System.out.println(e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                camV = camV.minus(camDir);
                break;
            case KeyEvent.VK_S:
                camV = camV.plus(camDir);
                break;
            case KeyEvent.VK_A:
                camV = camV.plus(camDir.cross(camNorm));
                break;
            case KeyEvent.VK_D:
                camV = camV.minus(camDir.cross(camNorm));
                break;
        }
    }
}