package bilkent.cs565.paper;

import bilkent.cs565.paper.gl.Constants;
import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import glm.mat.Mat3x3;
import glm.mat.Mat4x4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import glm_.mat4x4.Mat4;
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

    private static final float CAM_SPEED = 0.1f;
    private static GLWindow window;
    private static Animator animator;
    private final World world;
    private Thread thread;
    private AtomicBoolean running = new AtomicBoolean(true);
    private Vec4 cam = new Vec4(10, -5, -30, 0);
    private Vec3 camV = new Vec3();
    private Mat4x4 camProj = new Mat4x4();
    private Vec3 camDir = new Vec3(0,0,1);
    private Vec3 camNorm = new Vec3(0,1,0);
    private float camRx = 0;
    private float camRz = 0;
    private Vec3 rotate = new Vec3();
    private MouseListener mouseListener = new MouseListener() {
        public Vec2 prevCoord;

        @Override
        public void mouseClicked(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {
            camV = new Vec3();
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (e.isButtonDown(MouseEvent.BUTTON1) && prevCoord != null) {
                float dx = e.getX() - prevCoord.x;
                float dy = e.getY() - prevCoord.y;
                rotate.y += dx;
                rotate.x += dy;
            }
            prevCoord = new Vec2(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.isButtonDown(MouseEvent.BUTTON1) && prevCoord != null) {
                float dx = e.getX() - prevCoord.x;
                float dy = e.getY() - prevCoord.y;
                rotate.x -= dx/window.getWidth();
                rotate.y += dy/window.getHeight();
                Mat4x4 proj = new Mat4x4();
                proj = proj.rotate(rotate.y, new Vec3(-1.0f, 0.0f, 0.0f));
                camProj = proj.rotate(rotate.x, new Vec3(0.0f, 1.0f, 0.0f));
            }
            prevCoord = new Vec2(e.getX(), e.getY());
        }

        @Override
        public void mouseWheelMoved(MouseEvent e) {

        }
    };

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
    private FloatBuffer matBufferProj = GLBuffers.newDirectFloatBuffer(16);

    private Program program;
    private Program programLine;

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
        window.addMouseListener(mouseListener);

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
        gl.getExtension("OES_standard_derivatives");

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
        //thread.start();
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


        programLine = new Program(gl, getClass(), "gl3/shaders", "line.vert", "line.frag", "model");
        globalMatricesBI = gl.glGetUniformBlockIndex(program.name, "GlobalMatrices");
        if (globalMatricesBI == -1) {
            System.err.println("block index 'GlobalMatrices' not found!");
        }
        gl.glUniformBlockBinding(programLine.name, globalMatricesBI, Constants.Uniform.GLOBAL_MATRICES);

        checkError(gl, "initProgram");
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        world.update();
        world.update();
        world.update();
        Mat4x4 mat = new Mat4x4();
        camProj.inverse(mat);
        cam = cam.plus(mat.times(new Vec4(camV, 0).times(CAM_SPEED)));

        GL3 gl = drawable.getGL().getGL3();
        gl.glBindBufferBase(GL_UNIFORM_BUFFER, Constants.Uniform.GLOBAL_MATRICES, bufferName.get(Buffer.GLOBAL_MATRICES));

        long now = System.currentTimeMillis();
        float diff = (float) (now - start) / 1_000f;
        // view matrix
        {
            Mat4x4 proj = glm.perspective((float) (Math.PI * 0.25f), 4.0f/3.0f, 0.1f, 1000.0f);
            Mat4x4 view = camProj;
            view = view.translate(cam.x, cam.y, cam.z);
            //view = view.translate(0,0,0);
            view = view.rotate((float) ( Math.PI/2), new Vec3(-1.0f, 0.0f, 0.0f));
            proj.times(view).to(matBuffer);

            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.GLOBAL_MATRICES));
            gl.glBufferSubData(GL_UNIFORM_BUFFER, Mat4x4.SIZE, Mat4x4.SIZE, matBuffer);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        gl.glClearBufferfv(GL_COLOR, 0, clearColor.put(0, 0.66f).put(1, .66f).put(2, 0.66f).put(3, 1f));
        gl.glClearBufferfv(GL_DEPTH, 0, clearDepth.put(0, 1f));

        world.draw(gl, program, programLine);

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
        gl.glDeleteProgram(programLine.name);
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
        if( !e.isPrintableKey() || e.isAutoRepeat()  ) {
            return;
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
            case KeyEvent.VK_E:
                camV = camV.plus(0,-1,0);
                break;
            case KeyEvent.VK_Q:
                camV = camV.plus(0,1,0);
                break;
            case KeyEvent.VK_W:
                camV = camV.plus(0,0, 1);
                break;
            case KeyEvent.VK_S:
                camV = camV.plus(0,0, -1);
                break;
            case KeyEvent.VK_A:
                camV = camV.plus(1,0,0);
                break;
            case KeyEvent.VK_D:
                camV = camV.plus(-1,0,0);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if( !e.isPrintableKey() || e.isAutoRepeat()  ) {
            return;
        }
        System.out.println(e.getKeyCode());
        switch (e.getKeyCode()) {
            case KeyEvent.VK_E:
                camV = camV.minus(0,-1,0);
                break;
            case KeyEvent.VK_Q:
                camV = camV.minus(0,1,0);
                break;
            case KeyEvent.VK_W:
                camV = camV.minus(0,0, 1);
                break;
            case KeyEvent.VK_S:
                camV = camV.minus(0,0, -1);
                break;
            case KeyEvent.VK_A:
                camV = camV.minus(1,0,0);
                break;
            case KeyEvent.VK_D:
                camV = camV.minus(-1,0,0);
                break;
        }
    }
}