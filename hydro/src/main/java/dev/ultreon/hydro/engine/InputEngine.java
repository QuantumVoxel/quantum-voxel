package dev.ultreon.hydro.engine;

public interface InputEngine {
    boolean isKeyPressed(int key);
    boolean isKeyReleased(int key);

    boolean isMousePressed(int button);
    boolean isMouseReleased(int button);
    boolean isMouseMoved();
    boolean isMouseInsideWindow();

    float getMouseX();
    float getMouseY();

    float getMouseDX();
    float getMouseDY();

    void setCursorCaptured(boolean capture);

    boolean isCursorCaptured();

    class Buttons {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int MIDDLE = 2;
        public static final int BACK = 3;
        public static final int FORWARD = 4;
        public static final int UNKNOWN = -1;
        public static final int MAX = 5;
    }

    class Keys {
        public static final int ESCAPE = 1;
        public static final int F1 = 2;
        public static final int F2 = 3;
        public static final int F3 = 4;
        public static final int F4 = 5;
        public static final int F5 = 6;
        public static final int F6 = 7;
        public static final int F7 = 8;
        public static final int F8 = 9;
        public static final int F9 = 10;
        public static final int F10 = 11;
        public static final int F11 = 12;
        public static final int F12 = 13;
        public static final int PRINT_SCREEN = 14;
        public static final int SCROLL_LOCK = 15;
        public static final int PAUSE = 16;
        public static final int INSERT = 17;
        public static final int HOME = 18;
        public static final int PAGE_UP = 19;
        public static final int DELETE = 20;
        public static final int END = 21;
        public static final int PAGE_DOWN = 22;
        public static final int NUM_LOCK = 23;
        public static final int KEYPAD_DIVIDE = 24;
        public static final int KEYPAD_MULTIPLY = 25;
        public static final int KEYPAD_SUBTRACT = 26;
        public static final int KEYPAD_ADD = 27;
        public static final int KEYPAD_ENTER = 28;
        public static final int KEYPAD_0 = 29;
        public static final int KEYPAD_1 = 30;
        public static final int KEYPAD_2 = 31;
        public static final int KEYPAD_3 = 32;
        public static final int KEYPAD_4 = 33;
        public static final int KEYPAD_5 = 34;
        public static final int KEYPAD_6 = 35;
        public static final int KEYPAD_7 = 36;
        public static final int KEYPAD_8 = 37;
        public static final int KEYPAD_9 = 38;
        public static final int KEYPAD_DECIMAL = 39;
        public static final int KEYPAD_EQUALS = 40;
        public static final int LEFT_SHIFT = 41;
        public static final int LEFT_CONTROL = 42;
        public static final int LEFT_ALT = 43;
        public static final int LEFT_SUPER = 44;
        public static final int RIGHT_SHIFT = 45;
        public static final int RIGHT_CONTROL = 46;
        public static final int RIGHT_ALT = 47;
        public static final int RIGHT_SUPER = 48;
        public static final int CAPS_LOCK = 49;
        public static final int SCROLL_UP = 50;
        public static final int SCROLL_DOWN = 51;
        public static final int NUM_0 = 52;
        public static final int NUM_1 = 53;
        public static final int NUM_2 = 54;
        public static final int NUM_3 = 55;
        public static final int NUM_4 = 56;
        public static final int NUM_5 = 57;
        public static final int NUM_6 = 58;
        public static final int NUM_7 = 59;
        public static final int NUM_8 = 60;
        public static final int NUM_9 = 61;
        public static final int A = 65;
        public static final int B = 66;
        public static final int C = 67;
        public static final int D = 68;
        public static final int E = 69;
        public static final int F = 70;
        public static final int G = 71;
        public static final int H = 72;
        public static final int I = 73;
        public static final int J = 74;
        public static final int K = 75;
        public static final int L = 76;
        public static final int M = 77;
        public static final int N = 78;
        public static final int O = 79;
        public static final int P = 80;
        public static final int Q = 81;
        public static final int R = 82;
        public static final int S = 83;
        public static final int T = 84;
        public static final int U = 85;
        public static final int V = 86;
        public static final int W = 87;
        public static final int X = 88;
        public static final int Y = 89;
        public static final int Z = 90;
        public static final int LEFT_BRACKET = 91;
        public static final int BACKSLASH = 92;
        public static final int RIGHT_BRACKET = 93;
        public static final int GRAVE_ACCENT = 96;
        public static final int WORLD_1 = 161;
        public static final int WORLD_2 = 162;
        public static final int SPACE = 97;
        public static final int ENTER = 98;
        public static final int TAB = 99;
        public static final int BACKSPACE = 100;
        public static final int SEMICOLON = 101;
        public static final int APOSTROPHE = 102;
        public static final int BACKSLASH_2 = 103;
        public static final int COMMA = 104;
        public static final int PERIOD = 105;
        public static final int SLASH = 106;
        public static final int EQUAL = 107;
        public static final int PLUS = 108;
        public static final int MINUS = 109;
        public static final int LEFT_BRACE = 110;
        public static final int RIGHT_BRACE = 111;
        public static final int MAX = 255;
        public static final int UNKNOWN = -1;
    }
}
