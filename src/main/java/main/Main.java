package main;

import main.entity.*;
import main.input.InputHandler;
import main.loaders.Loader;
import main.loaders.OBJLoader;
import main.renderer.MasterRenderer;
import main.rs.loader.RSModelLoader;
import main.rs.model.Model;
import main.shaders.StaticShader;
import main.terrain.Terrain;
import org.joml.*;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.lang.Math;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    private Loader loader;
    private List<Entity> entities;
    private Camera camera;
    private Light light;

    Terrain terrain, terrain2;

    private int r = 150;

    private float TINY_DELTA = 0.001f;

    private Vector2f evaluatePoint2D(float t) {
        float x = (float) (r * Math.cos(t));
        float y = (float) (r * Math.sin(2 * t) / 2);
        return new Vector2f(x, y);
    }

    private Vector3f evaluatePoint3D(float t) {
        float x = (float) (r * Math.cos(t));
        float y = (float) (r * Math.sin(2 * t) / 2);
        return new Vector3f(x, y, 0);
    }

    private Vector2f getTangent(Vector2f point) {
        float t = 0.5f; //half way along, anywhere, doesn't matter
        float d = 0.001f;//some very small delta

        Vector2f direction = evaluatePoint2D(d + t).sub(evaluatePoint2D(d));
        Vector2f tangent = new Vector2f(-direction.y, direction.x);
        return tangent;
    }

    private final int NUM_STEPS = 1000;

    private List<Vector3f> vertices = new ArrayList<>();

    public void addRegularPolygon(int num_sides, Matrix4f matrix) {
        float dt = (float) ((2 * Math.PI) / NUM_STEPS);
        float c = 0.0f;

        for (int i = 0; i < NUM_STEPS; ++i, c += dt) {
            Vector4f myVec = new Vector4f((float) (Math.sin(c) * r), (float) (Math.cos(c) * r), 0.0f, 1.0f).mul(matrix);
            vertices.add(new Vector3f(myVec.x, myVec.y, myVec.z));
        }
    }

    private void generate() {
        for (float t = 0; t < Math.PI * 2; t += 2 * Math.PI / 50.0) {
            Matrix4f matrix = makeBasisMatrix(t);
            System.out.println(matrix);

            addRegularPolygon(20, matrix);
        }
    }

    private Matrix4f makeBasisMatrix(float t) {
        Vector3f pt = evaluatePoint3D(t);
        Vector3f pt2 = evaluatePoint3D(t + TINY_DELTA);
        Vector3f D = new Vector3f(pt2).sub(pt).normalize();
        Vector3f T = new Vector3f(-D.y, D.x, 0);

        Vector3f Z = T.cross(D);

        Matrix4f rotmat = new Matrix4f(D.x, D.y, D.z, 0,
                T.x, T.y, T.y, 0,
                Z.x, Z.y, Z.z, 0,
                pt.x, pt.y, pt.z, 1.0f);

        return rotmat;

    }

    private RawModel model;
    private Texture texture;
    private TexturedModel texturedModel;

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Test Scene", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

     //   GLFWImage glfwImage = glfwima
      //  glfwSetWindowIcon(window, img);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);


        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private int frameCount = 0;

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();


        //glDisable(GL_CULL_FACE);

        loader = new Loader();
        //model = OBJLoader.loadObjModel("/dragon.obj", loader);

        entities = new ArrayList<>();
        /*for(int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++) {
                entities.add(new Entity(model, new Vector3f(i * 1f, j * 0.1f, -1), 120f , 0, 0, 1));
            }
        }*/


        camera = new Camera();

        light = new Light(new Vector3f(0, 0, -80), new Vector3f(1f, 1f, 1f));

        texture = loader.loadTexture("/textures/1.png");
        System.out.println("Texture: " + texture);
        System.out.println("Model: ");
        texturedModel = new TexturedModel(model, texture);

        Texture modelTexture = texturedModel.getTexture();
        modelTexture.setShineDamper(10);
        modelTexture.setReflectivity(1);

        RSModelLoader rsModelLoader = new RSModelLoader();
       // rsModelLoader.load();
        try {
            rsModelLoader.loadRaw();
        } catch (IOException e) {
            e.printStackTrace();
        }
     //   rsModelLoader.dumpVerticesAndIndices();
        Model model = rsModelLoader.loadedModels.get(130000);
        float[] vertices = rsModelLoader.getVerticesNew(model);
        float[] texCoords = rsModelLoader.getTexCoords(model);
        float[] normals = rsModelLoader.toFloatArray(rsModelLoader.getSharedNormals(model));
        int[] indices = rsModelLoader.getIndices(model);
        float[] colors = rsModelLoader.getColorsNew(model);
        RawModel rawModel = loader.loadToVAO(vertices, texCoords, normals, indices, colors);
        TexturedModel texModel = new TexturedModel(rawModel, texture);
        entities.add(new Entity(texModel, new Vector3f(0, 0, 0), 0, 0, 0, 1));
        InputHandler.init(window);

        MasterRenderer renderer = new MasterRenderer();
        // Set the clear color
        glClearColor(0f, 0.0f, 0.0f, 0.0f);


        while (!glfwWindowShouldClose(window)) {
            InputHandler.update();
            if (InputHandler.mouseButtonDown(0)) {
                entities.get(0).increaseRotation((float) InputHandler.getYDifference(),
                        (float) InputHandler.getXDifference(), 0);
                InputHandler.resetMousePosDifferences();
            }
            camera.move();
            entities.forEach(renderer::processEntity);
            renderer.render(light, camera);
            frameCount++;
            glfwSwapBuffers(window); // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
        renderer.cleanUp();
        loader.cleanUp();
    }

    public static void main(String[] args) {
        new Main().run();
    }

}
