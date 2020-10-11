package main.input;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public final class InputHandler {
    private static long window;
    private static final int KEYBOARD_SIZE = 512;
    private static final int MOUSE_SIZE = 16;

    private static int[] keyStates = new int[KEYBOARD_SIZE];
    private static boolean[] activeKeys = new boolean[KEYBOARD_SIZE];

    private static int[] mouseButtonStates = new int[MOUSE_SIZE];
    private static boolean[] activeMouseButtons = new boolean[MOUSE_SIZE];
    private static long lastMouseNS = 0;
    private static long mouseDoubleClickPeriodNS = 1000000000 / 5; //5th of a second for double click.

    private static int NO_STATE = -1;


    protected static GLFWKeyCallback keyboard = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            activeKeys[key] = action != GLFW_RELEASE;
            keyStates[key] = action;
         //   System.out.println("CALLED");
        }
    };

    protected static GLFWMouseButtonCallback mouse = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            activeMouseButtons[button] = action != GLFW_RELEASE;
            mouseButtonStates[button] = action;
        }
    };

    private static double previousX, previousY;

    public double getPreviousX() {
        return previousX;
    }

    public static double xDifference, yDifference;

    public static double getXDifference() {
        return xDifference;
    }

    public static double getYDifference() {
        return yDifference;
    }

    public static void resetMousePosDifferences() {
        xDifference = 0;
        yDifference = 0;
    }

    protected static GLFWCursorPosCallback cursor = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            xDifference = x - previousX;
            yDifference = y - previousY;
            previousX = x;
            previousY = y;
        }
    };

    public static void init(long window) {
        InputHandler.window = window;
        keyboard.set(window);
        mouse.set(window);
        cursor.set(window);
        resetKeyboard();
        resetMouse();
    }

    public static void update() {
        resetKeyboard();
        resetMouse();
        if(keyDown(32)) {
            System.out.println("Pressed space");
        }
        glfwPollEvents();
    }

    private static void resetKeyboard() {
        Arrays.fill(keyStates, NO_STATE);
    }

    private static void resetMouse() {
        Arrays.fill(mouseButtonStates, NO_STATE);

        long now = System.nanoTime();

        if (now - lastMouseNS > mouseDoubleClickPeriodNS)
            lastMouseNS = 0;
    }

    public static boolean keyDown(int key) {
        return activeKeys[key];
    }

    public static boolean keyPressed(int key) {
        return keyStates[key] == GLFW_PRESS;
    }

    public static boolean keyReleased(int key) {
        return keyStates[key] == GLFW_RELEASE;
    }

    public static boolean mouseButtonDown(int button) {
        return activeMouseButtons[button];
    }

    public static boolean mouseButtonPressed(int button) {
        return mouseButtonStates[button] == GLFW_RELEASE;
    }

    public static boolean mouseButtonReleased(int button) {
        boolean flag = mouseButtonStates[button] == GLFW_RELEASE;

        if (flag)
            lastMouseNS = System.nanoTime();

        return flag;
    }

    public static boolean mouseButtonDoubleClicked(int button) {
        long last = lastMouseNS;
        boolean flag = mouseButtonReleased(button);

        long now = System.nanoTime();

        if (flag && now - last < mouseDoubleClickPeriodNS) {
            lastMouseNS = 0;
            return true;
        }

        return false;
    }
}