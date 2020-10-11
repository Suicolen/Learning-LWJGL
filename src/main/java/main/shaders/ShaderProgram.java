package main.shaders;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ShaderProgram {

    private int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public ShaderProgram(String vertexFile, String fragmentFile) {
        this.vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
        this.fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
        this.programID = GL20.glCreateProgram();
        GL20.glAttachShader(programID, vertexShaderID);
        GL20.glAttachShader(programID, fragmentShaderID);
        bindAttributes();
        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);
        getAllUniformLocations();
        getAllAttributeLocations();
    }

    protected abstract void getAllUniformLocations();
    protected abstract void getAllAttributeLocations();

    protected int getUniformLocation(String uniformName) {
        return GL20.glGetUniformLocation(programID, uniformName);
    }

    protected int getAttributeLocation(String attributeName) {
        return GL20.glGetAttribLocation(programID, attributeName);
    }

    protected void loadFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    protected void loadVector(int location, Vector3f vector) {
        GL20.glUniform3f(location, vector.x, vector.y, vector.z);
    }

    protected void loadBoolean(int location, boolean value) {
        GL20.glUniform1f(location, value ? 1 : 0);
    }

    protected void loadMatrix(int location, Matrix4f matrix) {
        matrix.get(matrixBuffer);
        // matrixBuffer.flip();
        GL20.glUniformMatrix4fv(location, false, matrixBuffer);
        //System.out.println("Loaded matrix: location = " + location + " | matrixBuffer = " + matrixBuffer);
    }


   /* public void store(Matrix4f matrix, FloatBuffer buffer) {
        buffer.put(matrix.m00());
        buffer.put(matrix.m01());
        buffer.put(matrix.m02());
        buffer.put(matrix.m03());

        buffer.put(matrix.m10());
        buffer.put(matrix.m11());
        buffer.put(matrix.m12());
        buffer.put(matrix.m13());

        buffer.put(matrix.m20());
        buffer.put(matrix.m21());
        buffer.put(matrix.m22());
        buffer.put(matrix.m23());

        buffer.put(matrix.m30());
        buffer.put(matrix.m31());
        buffer.put(matrix.m32());
        buffer.put(matrix.m33());
        buffer.flip();
    }*/

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void cleanUp() {
        stop();
        GL20.glDetachShader(programID, vertexShaderID);
        GL20.glDetachShader(programID, fragmentShaderID);
        GL20.glDeleteShader(vertexShaderID);
        GL20.glDeleteShader(fragmentShaderID);
        GL20.glDeleteProgram(programID);
    }

    protected abstract void bindAttributes();

    protected void bindAttribute(int attribute, String variableName) {
        GL20.glBindAttribLocation(programID, attribute, variableName);
    }

    private static int loadShader(String file, int type) {

        URI uri = null;
        try {
            uri = ShaderProgram.class.getResource(file).toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String mainPath = Path.of(uri).toString();
        Path path = Path.of(mainPath);
        String lines = "";
        try {
            lines = Files.readString(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int shaderID = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderID, lines);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.out.println(GL20.glGetShaderInfoLog(shaderID));
            System.out.println("Could not compile shader");
            System.exit(1);
        }

        return shaderID;

    }

}
