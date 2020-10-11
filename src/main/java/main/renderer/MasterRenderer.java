package main.renderer;

import main.Main;
import main.entity.Camera;
import main.entity.Entity;
import main.entity.Light;
import main.entity.TexturedModel;
import main.shaders.StaticShader;
import main.shaders.TerrainShader;
import main.terrain.Terrain;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterRenderer {

    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000;

    private Matrix4f projectionMatrix;

    private StaticShader shader = new StaticShader();
    private EntityRenderer entityRenderer;

    private TerrainRenderer terrainRenderer;
    private TerrainShader terrainShader = new TerrainShader();

    private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
    private List<Terrain> terrains = new ArrayList<>();

    public MasterRenderer() {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        createProjectionMatrix();
        entityRenderer = new EntityRenderer(shader, projectionMatrix);
        terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
    }

    public void render(Light sun, Camera camera) {
        prepare();
        shader.start();
        shader.loadLight(sun);
        shader.loadViewMatrix(camera);
        entityRenderer.render(entities);
        shader.stop();
        terrainShader.start();
        terrainShader.loadLight(sun);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();
        terrains.clear();
        entities.clear();
    }

    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    private void prepare() {
           GL11.glEnable(GL11.GL_CULL_FACE);
          GL11.glCullFace(GL11.GL_BACK);
        //  GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        //  GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glClearColor(0.2f, 0.2f, 0.2f, 1);
    }

    private void createProjectionMatrix() {
        float aspectRatio = (float) Main.WIDTH / (float) Main.HEIGHT;
        float yScale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2))) * aspectRatio);
        float xScale = yScale / aspectRatio;
        float frustumLength = FAR_PLANE - NEAR_PLANE;
        projectionMatrix = new Matrix4f();
        projectionMatrix.m00(xScale);
        projectionMatrix.m11(yScale);
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustumLength));
        projectionMatrix.m23(-1);
        projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustumLength));
        projectionMatrix.m33(0);
    }

    public void processEntity(Entity entity) {
        TexturedModel texturedModel = entity.getTexturedModel();
        List<Entity> batch = entities.get(texturedModel);

        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<>();
            newBatch.add(entity);
            entities.put(texturedModel, newBatch);
        }
    }

    public void cleanUp() {
        shader.cleanUp();
        terrainShader.cleanUp();
    }


}
