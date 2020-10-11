package main.entity;

public class RawModel {

    private int vaoID;
    private int[] indices;
    private int vertexCount;

    public RawModel(int vaoID, int[] indices) {
        this.vaoID = vaoID;
        this.indices = indices;
    }

    public RawModel(int vaoID, int[] indices, int vertexCount) {
        this.vaoID = vaoID;
        this.indices = indices;
        this.vertexCount = vertexCount;

    }

    private int indexOf(int[] arr, int value) {
        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return index;
            }
        }
        return index;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int[] getIndices() {
        return indices;
    }
}
