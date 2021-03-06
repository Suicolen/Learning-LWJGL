package main.renderer;

import main.entity.Entity;
import main.entity.RawModel;
import main.entity.Texture;
import main.entity.TexturedModel;
import main.math.Maths;
import main.shaders.StaticShader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.Map;

public class EntityRenderer {


    private StaticShader shader;

    public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    public void render(Map<TexturedModel, List<Entity>> entities) {
        entities.keySet().forEach(model -> {
            prepareTexturedModel(model);
            List<Entity> batch = entities.get(model);
            batch.forEach(entity -> {
                prepareInstance(entity);
                GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, model.getRawModel().getVertexCount());
             //   GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getIndices().length, GL11.GL_UNSIGNED_INT, 0);
            });
            unbindTexturedModel();
        });
    }

    private void prepareTexturedModel(TexturedModel texturedModel) {
        RawModel rawModel = texturedModel.getRawModel();
        GL30.glBindVertexArray(rawModel.getVaoID());
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        Texture texture = texturedModel.getTexture();
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturedModel.getTexture().getId());

    }

    private void unbindTexturedModel() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL30.glBindVertexArray(0);
    }

    private void prepareInstance(Entity entity) {
        Matrix4f transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
    }


}
