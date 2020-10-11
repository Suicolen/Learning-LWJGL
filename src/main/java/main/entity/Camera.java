package main.entity;

import main.input.InputHandler;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch;
    private float yaw;
    private float roll;

    private float speed = 0.05f;

    public void move() {
        if (InputHandler.keyDown(87)) {
            position.z -= speed;
        }

        if (InputHandler.keyDown(83)) {
            position.z += speed;
        }

        if (InputHandler.keyDown(68)) {
            position.x += speed;
        }

        if (InputHandler.keyDown(65)) {
            position.x -= speed;
        }

        if (InputHandler.keyDown(90)) {
            position.y += speed;
        }

        if (InputHandler.keyDown(88)) {
            position.y -= speed;
        }

       // System.out.println(position.x + " | " + position.y + " | " + position.z);


    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }
}
