package main.rs.loader;

import main.rs.model.Model;
import main.rs.utils.CompressionUtils;
import org.displee.CacheLibrary;
import org.displee.cache.index.Index;
import org.displee.cache.index.archive.ArchiveSector;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RSModelLoader {

    public Map<Integer, Model> loadedModels = new HashMap<>();

    public void loadRaw() throws IOException {
        Path path = Paths.get("./data/");
        Files.walk(path).filter(Files::isRegularFile).forEach(file -> {
            try {
                String name = file.getFileName().toString();
                int idLength = name.indexOf(".");
                int id = Integer.parseInt(name.substring(0, idLength));
                // System.out.println("Loaded id: " + id);
                if (id != 99944) {
                    byte[] data = Files.readAllBytes(file);
                    Model decoded = Model.decode(data);
                    loadedModels.put(id, decoded);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private float getLength(float[] array, int offset) {
        float x = array[offset];
        float y = array[offset + 1];
        float z = array[offset + 2];
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private void substractVec(float[] result, float[] source, int offsetA, int offsetB) {
        result[0] = source[offsetA] - source[offsetB];
        result[1] = source[offsetA + 1] - source[offsetB + 1];
        result[2] = source[offsetA + 2] - source[offsetB + 2];
    }

    void mulvec_inplace(float[] result, int offset, float amount) {
        result[offset + 0] *= amount;
        result[offset + 1] *= amount;
        result[offset + 2] *= amount;
    }

    void normalise(float[] vecArray, int offset) {
        float length_recip = (float) (1.0 / getLength(vecArray, offset));
        mulvec_inplace(vecArray, offset, length_recip);
    }

    void crossProduct(float[] vecArrayA, float[] vecArrayB,
                      float[] crossProductArray, int offsetA, int offsetB) {

        crossProductArray[0] = vecArrayA[offsetA + 1] * vecArrayB[offsetB + 2]
                - vecArrayA[offsetA + 2] * vecArrayB[offsetB + 1];
        crossProductArray[1] = vecArrayA[offsetA + 2] * vecArrayB[offsetB]
                - vecArrayA[offsetA] * vecArrayB[offsetB + 2];
        crossProductArray[2] = vecArrayA[offsetA] * vecArrayB[offsetB + 1]
                - vecArrayA[offsetA + 1] * vecArrayB[offsetB];
    }


    public void load() {
        CacheLibrary library = null;
        try {
            library = new CacheLibrary("C:/Users/Suic/LuminiteCache");
            Index index = library.getIndex(1);
            for (int i = 2426; i <= 2426; i++) {
                ArchiveSector sector = index.readArchiveSector(i);
                if (sector == null) {
                    continue;
                }
                byte[] modelData = sector.getData();
                if (CompressionUtils.isGZipped(new ByteArrayInputStream(modelData))) {
                    modelData = CompressionUtils.degzip(ByteBuffer.wrap(modelData));
                }

                try {
                    Files.write(Paths.get("./2426.dat"), modelData);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Model decodedModel = Model.decode(modelData);
                loadedModels.put(0, decodedModel);
                System.out.println("Verts: " + decodedModel.vertices);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public float[] getTexCoords(Model model) {
        float[] texCoords = new float[model.faces * 2];
        System.out.println(texCoords.length);
        for (int i = 0; i < model.texture_faces - 1; i++) {
            int baseT = i * 2;
            texCoords[baseT] = model.triangle_texture_edge_a[i];
            System.out.println(texCoords.length + " | Base t + 1: " + baseT + 1);
            texCoords[baseT + 1] = model.triangle_texture_edge_b[i + 1];

        }
        return texCoords;
    }

    public float[] toFloatArray(Vector3f[] vector3f) {
        float[] result = new float[vector3f.length * 3];

        for(int i = 0; i < vector3f.length; i++) {
            int index = i * 3;
            result[index] = vector3f[i].x;
            result[index + 1] = vector3f[i].y;
            result[index + 2] = vector3f[i].z;

        }
        return result;
    }

    public Vector3f[] getSharedNormals(Model model) {
        Vector3f[] vert_normal_list = new Vector3f[model.vertices];
        for(int i = 0; i < vert_normal_list.length; i++) {
            vert_normal_list[i] = new Vector3f();
        }
        int[] vert_normal_count = new int[model.vertices];
        float[] normals = getNormals(model, getVerticesNew(model));
        for (int i = 0; i < model.faces - 2; i += 3) { // for each triangle
            int edgeA = model.triangle_edge_a[i];
            int edgeB = model.triangle_edge_b[i];
            int edgeC = model.triangle_edge_c[i];
            Vector3f normal = new Vector3f(normals[i], normals[i + 1], normals[i + 2]);

            vert_normal_list[edgeA].add(normal);
            vert_normal_list[edgeB].add(normal);
            vert_normal_list[edgeC].add(normal);
            vert_normal_count[edgeA]++;
            vert_normal_count[edgeB]++;
            vert_normal_count[edgeC]++;
        }

        for (int i = 0; i < vert_normal_list.length; i++) { //
            vert_normal_list[i].div((float) vert_normal_count[i]);
        }
        return vert_normal_list;
    }

    public float[] getNormals(Model model, float[] positions) {
        float[] normals = new float[model.faces * 9];
        for (int i = 0; i < model.faces; i++) {
            int baseN = i * 9;

            float[] AB = new float[3];
            float[] AC = new float[3];
            //get two vectors from triangle (see diagram)
            //we use *POSITIONS* to generate vectors, not normals. We don't *know* the normals yet so we can't use that array! that's what we're here to do!

            substractVec(AB, positions, baseN + 3, baseN);
            substractVec(AC, positions, baseN + 6, baseN);


            //normalise these - offset is 0 in these arrays
            normalise(AB, 0);
            normalise(AC, 0);
            float[] normal = new float[3];
            crossProduct(AC, AB, normal, 0, 0); //you have offsets in your routine but they're all 0 here because we've made new temporary arrays
            normalise(normal, 0);
            float nx = normal[0];
            float ny = normal[1];
            float nz = normal[2];
            normals[baseN] = nx;
            normals[baseN + 1] = ny;
            normals[baseN + 2] = nz;

            normals[baseN + 3] = nx;
            normals[baseN + 4] = ny;
            normals[baseN + 5] = nz;

            normals[baseN + 6] = nx;
            normals[baseN + 7] = ny;
            normals[baseN + 8] = nz;
        }
        return normals;
    }

    public float[] getColors(Model model) {
        int alphaLength = model.face_alpha != null ? model.face_alpha.length : 0;
        float[] colors = new float[model.faces + alphaLength];
        for (int i = 0; i < model.faces - (alphaLength > 0 ? 3 : 2); i++) {
            int color = model.hsbToRGB(model.face_color[i]);


            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = 0.0f;
            if (model.face_alpha != null) {
                a = (model.face_alpha[i] & 0xFF) / 255.0f;
            }

            colors[i] = r;
            colors[i + 1] = g;
            colors[i + 2] = b;
            if (model.face_alpha != null) {
                colors[i + 3] = a;
            }
            if (i == 0) {
                System.out.println("Red = " + r + " | Green = " + g + " | Blue = " + b + " Alpha = " + a);
            }
        }

        System.out.println(Arrays.toString(colors));
        return colors;
    }

    public float[] getColorsNew(Model model) {
        boolean hasAlpha = model.face_alpha != null;
        int length = model.face_color.length;
        float[] colors = new float[length * 12];
        for (int i = 0; i < model.faces; i++) {
            int color = model.hsbToRGB(model.face_color[i]);
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = hasAlpha ? (model.face_alpha[i] & 0xFF) / 255.0f : 1.0f;
            System.out.println("Sent r: " + r + " | Sent g: " + g + " | Sent b: " + b);

            int base_i = i * 12;
            colors[base_i] = r;
            colors[base_i + 1] = g;
            colors[base_i + 2] = b;
            colors[base_i + 3] = a;

            colors[base_i + 4] = r;
            colors[base_i + 5] = g;
            colors[base_i + 6] = b;
            colors[base_i + 7] = a;

            colors[base_i + 8] = r;
            colors[base_i + 9] = g;
            colors[base_i + 10] = b;
            colors[base_i + 11] = a;


        }
        return colors;
    }

    public float[] getNormalsNew(Model model) {
        float[] normals = new float[model.vertices * 3];
        for (int i = 0; i < model.vertices - 2; i++) {
            int baseN = i * 3;
            int x = model.vertex_x[i];
            int y = model.vertex_y[i + 1];
            int z = model.vertex_z[i + 2];
            float dX = x / 65536.0f;
            float dY = y / 65536.0f;
            float dZ = z / 65536.0f;
            normals[baseN] = dX;
            normals[baseN + 1] = dY;
            normals[baseN + 2] = dZ;
        }
        return normals;
    }

    public float[] getVerticesNew(Model model) {
        List<Float> verts = new ArrayList<>();
        final float MODEL_SCALE = 512.0f;
        int[] faceIndicesA = model.triangle_edge_a;
        int[] faceIndicesB = model.triangle_edge_b;
        int[] faceIndicesC = model.triangle_edge_c;
        int[] verticesX = model.vertex_x;
        int[] verticesY = model.vertex_y;
        int[] verticesZ = model.vertex_z;
        for (int i = 0; i < model.faces; i++) {
            int faceA = faceIndicesA[i];
            int faceB = faceIndicesB[i];
            int faceC = faceIndicesC[i];
            verts.add(verticesX[faceA] / MODEL_SCALE);
            verts.add(verticesY[faceA] / MODEL_SCALE);
            verts.add(verticesZ[faceA] / MODEL_SCALE);
            verts.add(verticesX[faceB] / MODEL_SCALE);
            verts.add(verticesY[faceB] / MODEL_SCALE);
            verts.add(verticesZ[faceB] / MODEL_SCALE);
            verts.add(verticesX[faceC] / MODEL_SCALE);
            verts.add(verticesY[faceC] / MODEL_SCALE);
            verts.add(verticesZ[faceC] / MODEL_SCALE);
        }
        float[] result = new float[verts.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = verts.get(i);
        }
        return result;

    }


    public float[] getVertices(Model model) {
        List<Float> verts = new ArrayList<>();
        final float MODEL_SCALE = 512.0f;
        int[] verticesX = model.vertex_x;
        int[] verticesY = model.vertex_y;
        int[] verticesZ = model.vertex_z;
        for (int i = 0; i < model.vertices; i++) {
            verts.add(verticesX[i] / MODEL_SCALE);
            verts.add(verticesY[i] / MODEL_SCALE);
            verts.add(verticesZ[i] / MODEL_SCALE);
        }
        float[] result = new float[verts.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = verts.get(i);
        }
        return result;
    }

    public void dumpVerticesAndIndices() {
        Path path = Paths.get("./verticesandindices.txt");
        Model model = loadedModels.get(0);
        List<String> data = new ArrayList<>();
        data.add("Vertex count: " + model.vertices);
        data.add("Face count: " + model.faces);
        data.add("--------------------------------------------------");
        data.add("Vertex_x = " + Arrays.toString(model.vertex_x));
        data.add("Vertex_y = " + Arrays.toString(model.vertex_y));
        data.add("Vertex_z = " + Arrays.toString(model.vertex_z));
        data.add("--------------------------------------------------");
        data.add("Triangle_edge/indices a = " + Arrays.toString(model.triangle_edge_a));
        data.add("Triangle_edge/indices b = " + Arrays.toString(model.triangle_edge_b));
        data.add("Triangle_edge/indices c = " + Arrays.toString(model.triangle_edge_c));
        try {
            Files.write(path, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished writing");
    }

    public int[] getIndices(Model model) {
        int[] result = new int[model.faces * 3];
        int[] faceIndicesA = model.triangle_edge_a;
        int[] faceIndicesB = model.triangle_edge_b;
        int[] faceIndicesC = model.triangle_edge_c;
        int baseI = 0;
        for (int i = 0; i < model.faces; i++, baseI += 3) {
            result[baseI] = faceIndicesA[i];
            result[baseI + 1] = faceIndicesB[i];
            result[baseI + 2] = faceIndicesC[i];
        }
        return result;
    }

}