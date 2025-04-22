//package dev.ultreon.quantum.client.input.controller.gui;
//
//import com.badlogic.gdx.Input;
//import dev.ultreon.quantum.client.gui.Renderer;
//import dev.ultreon.quantum.util.NamespaceID;
//
//public enum KeyMappingIcon {
//    ESC(0, 0),
//    F1(16, 0),
//    F2(32, 0),
//    F3(48, 0),
//    F4(64, 0),
//    F5(80, 0),
//    F6(96, 0),
//    F7(112, 0),
//    F8(128, 0),
//    F9(144, 0),
//    F10(160, 0),
//    F11(176, 0),
//    F12(192, 0),
//    TILDE(208, 0),
//    EXCLAMATION(224, 0),
//    AT(240, 0),
//    HASH(256, 0),
//    KEY_1(0, 16),
//    KEY_2(16, 16),
//    KEY_3(32, 16),
//    KEY_4(48, 16),
//    KEY_5(64, 16),
//    KEY_6(80, 16),
//    KEY_7(96, 16),
//    KEY_8(112, 16),
//    KEY_9(128, 16),
//    KEY_0(144, 16),
//    MINUS(160, 16),
//    PLUS(176, 16),
//    EQUALS(192, 16),
//    UNDERSCORE(208, 16),
//    BROKEN_BAR(224, 16),
//    BACKSPACE(240, 16, 32, 16),
//    Q(0, 32),
//    W(16, 32),
//    E(32, 32),
//    R(48, 32),
//    T(64, 32),
//    Y(80, 32),
//    U(96, 32),
//    I(112, 32),
//    O(128, 32),
//    P(144, 32),
//    LEFT_BRACKET(160, 32),
//    RIGHT_BRACKET(176, 32),
//    LEFT_CURLY(192, 32),
//    RIGHT_CURLY(208, 32),
//    BACKSLASH(224, 32),
//    ENTER(240, 32, 32, 32),
//    A(16, 48),
//    S(32, 48),
//    D(48, 48),
//    F(64, 48),
//    G(80, 48),
//    H(96, 48),
//    J(112, 48),
//    K(128, 48),
//    L(144, 48),
//    QUOTE(160, 48),
//    DOUBLE_QUOTE(176, 48),
//    COLON(192, 48),
//    SEMICOLON(208, 48),
//    ASTERISK(224, 48),
//    SPACE_SMALL(0, 64),
//    WINDOWS(16, 64),
//    Z(32, 64),
//    X(48, 64),
//    C(64, 64),
//    V(80, 64),
//    B(96, 64),
//    N(112, 64),
//    M(128, 64),
//    LESS(144, 64),
//    GREATER(160, 64),
//    QUESTION(176, 64),
//    SLASH(192, 64),
//    UP(208, 64),
//    RIGHT(224, 64),
//    DOWN(240, 64),
//    LEFT(256, 64),
//    ALT(0, 80, 23, 16),
//    TAB(32, 80, 23, 16),
//    DELETE(64, 80, 23, 16),
//    END(96, 80, 23, 16),
//    NUM_LOCK(128, 80, 23, 16),
//    PERIOD(160, 80),
//    DOLLAR(176, 80),
//    PERCENT(192, 80),
//    CIRCUMFLEX(208, 80),
//    CENT(224, 80),
//    LEFT_PARENTHESIS(240, 80),
//    RIGHT_PARENTHESIS(256, 80),
//    CTRL(0, 96, 27, 16),
//    CAPS(32, 96, 27, 16),
//    HOME(64, 96, 27, 16),
//    PAGE_UP(96, 96, 27, 16),
//    PAGE_DOWN(128, 96, 27, 16),
//    COMMA(160, 96),
//    ENLARGE(176, 96),
//    EMPTY(192, 96),
//    RECORD(208, 96),
//    SPACE_BIG(224, 96, 48, 16),
//    SHIFT(0, 112, 33, 16),
//    INSERT(32, 112, 33, 16),
//    PRINT(64, 112, 33, 16),
//    SCROLL_LOCK(96, 112, 33, 16),
//    PAUSE_BREAK(128, 112, 3, 16),
//    PLAY(160, 112),
//    PAUSE(176, 112),
//    STOP(192, 112),
//    FAST_BACKWARD(208, 112),
//    FAST_FORWARD(224, 112),
//    PREVIOUS(240, 112),
//    NEXT(256, 112),
//    MOUSE(0, 128),
//    MOUSE_LEFT(16, -1),
//    MOUSE_RIGHT(32, -1),
//    MOUSE_MIDDLE(48, -1),
//    MOUSE_SCROLL_UP(64, -1),
//    MOUSE_SCROLL_DOWN(80, -1),
//    MOUSE_SCROLL(96, -1),
//    POWER;
//
//    private static final NamespaceID TEXTURE = new NamespaceID("textures/gui/icons.png");
//    public final int u;
//    public final int v;
//    public final int width;
//    public final int height;
//
//    KeyMappingIcon() {
//        this(0, 0);
//    }
//
//    KeyMappingIcon(int u, int v) {
//        this(u, v, 16, 16);
//    }
//
//    KeyMappingIcon(int u, int v, int width, int height) {
//        this.u = u + 272;
//        this.v = v + 128;
//        this.width = width;
//        this.height = height;
//    }
//
//    public static KeyMappingIcon byChar(char c) {
//        switch (c) {
//            case '0':
//                return KEY_0;
//            case '1':
//                return KEY_1;
//            case '2':
//                return KEY_2;
//            case '3':
//                return KEY_3;
//            case '4':
//                return KEY_4;
//            case '5':
//                return KEY_5;
//            case '6':
//                return KEY_6;
//            case '7':
//                return KEY_7;
//            case '8':
//                return KEY_8;
//            case '9':
//                return KEY_9;
//            case 'a':
//                return A;
//            case 'b':
//                return B;
//            case 'c':
//                return C;
//            case 'd':
//                return D;
//            case 'e':
//                return E;
//            case 'f':
//                return F;
//            case 'g':
//                return G;
//            case 'h':
//                return H;
//            case 'i':
//                return I;
//            case 'j':
//                return J;
//            case 'k':
//                return K;
//            case 'l':
//                return L;
//            case 'm':
//                return M;
//            case 'n':
//                return N;
//            case 'o':
//                return O;
//            case 'p':
//                return P;
//            case 'q':
//                return Q;
//            case 'r':
//                return R;
//            case 's':
//                return S;
//            case 't':
//                return T;
//            case 'u':
//                return U;
//            case 'v':
//                return V;
//            case 'w':
//                return W;
//            case 'x':
//                return X;
//            case 'y':
//                return Y;
//            case 'z':
//                return Z;
//            case '~':
//                return TILDE;
//            case '-':
//                return MINUS;
//            case '=':
//                return EQUALS;
//            case '[':
//                return LEFT_BRACKET;
//            case ']':
//                return RIGHT_BRACKET;
//            case '\\':
//                return BACKSLASH;
//            case ':':
//                return COLON;
//            case ';':
//                return SEMICOLON;
//            case '\'':
//                return QUOTE;
//            case '"':
//                return DOUBLE_QUOTE;
//            case ',':
//                return COMMA;
//            case '.':
//                return PERIOD;
//            case '<':
//                return LESS;
//            case '>':
//                return GREATER;
//            case '/':
//                return SLASH;
//            case '?':
//                return QUESTION;
//            case ' ':
//                return SPACE_BIG;
//            case '\0':
//                return CTRL;
//            case '\3':
//                return CAPS;
//            case '\4':
//                return EMPTY;
//            case '\5':
//            case '\6':
//                return SHIFT;
//            case '\7':
//                return ENLARGE;
//            case '\b':
//                return BACKSPACE;
//            case '\t':
//                return TAB;
//            case '\n':
//            case '\r':
//                return ENTER;
//            default:
//                return EMPTY;
//        }
//    }
//
//    public void render(Renderer gfx, int x, int y, boolean selected) {
//        if (this == POWER) {
//            gfx.blit(TEXTURE, x, y, 16, 16, 240, 336, 16, 16, 544, 384);
//            return;
//        }
//
//        if (v == 127) {
//            gfx.blit(TEXTURE, x, y, 16, 16, u - 272 + 128, 48, width, height, 544, 384);
//            return;
//        }
//
//        gfx.blit(TEXTURE, x, y, width, height, u, selected ? v - 128 : v, width, height, 544, 384);
//    }
//
//    public static KeyMappingIcon byKey(int keyCode) {
//        switch (keyCode) {
//            case Input.Keys.ESCAPE:
//                return ESC;
//            case Input.Keys.F1:
//                return F1;
//            case Input.Keys.F2:
//                return F2;
//            case Input.Keys.F3:
//                return F3;
//            case Input.Keys.F4:
//                return F4;
//            case Input.Keys.F5:
//                return F5;
//            case Input.Keys.F6:
//                return F6;
//            case Input.Keys.F7:
//                return F7;
//            case Input.Keys.F8:
//                return F8;
//            case Input.Keys.F9:
//                return F9;
//            case Input.Keys.F10:
//                return F10;
//            case Input.Keys.F11:
//                return F11;
//            case Input.Keys.F12:
//                return F12;
//            case Input.Keys.GRAVE:
//                return TILDE;
//            case Input.Keys.NUM_1:
//                return KEY_1;
//            case Input.Keys.NUM_2:
//                return KEY_2;
//            case Input.Keys.NUM_3:
//                return KEY_3;
//            case Input.Keys.NUM_4:
//                return KEY_4;
//            case Input.Keys.NUM_5:
//                return KEY_5;
//            case Input.Keys.NUM_6:
//                return KEY_6;
//            case Input.Keys.NUM_7:
//                return KEY_7;
//            case Input.Keys.NUM_8:
//                return KEY_8;
//            case Input.Keys.NUM_9:
//                return KEY_9;
//            case Input.Keys.NUM_0:
//                return KEY_0;
//            case Input.Keys.MINUS:
//                return MINUS;
//            case Input.Keys.EQUALS:
//                return EQUALS;
//            case Input.Keys.BACKSPACE:
//                return BACKSPACE;
//            case Input.Keys.TAB:
//                return TAB;
//            case Input.Keys.INSERT:
//                return INSERT;
//            case Input.Keys.FORWARD_DEL:
//                return DELETE;
//            case Input.Keys.RIGHT:
//                return RIGHT;
//            case Input.Keys.LEFT:
//                return LEFT;
//            case Input.Keys.DOWN:
//                return DOWN;
//            case Input.Keys.UP:
//                return UP;
//            case Input.Keys.PAGE_UP:
//                return PAGE_UP;
//            case Input.Keys.PAGE_DOWN:
//                return PAGE_DOWN;
//            case Input.Keys.HOME:
//                return HOME;
//            case Input.Keys.END:
//                return END;
//            case Input.Keys.CAPS_LOCK:
//                return CAPS;
//            case Input.Keys.SCROLL_LOCK:
//                return SCROLL_LOCK;
//            case Input.Keys.NUM_LOCK:
//                return NUM_LOCK;
//            case Input.Keys.PRINT_SCREEN:
//                return PRINT;
//            case Input.Keys.PAUSE:
//                return PAUSE;
//            case Input.Keys.BACKSLASH:
//                return BACKSLASH;
//            case Input.Keys.LEFT_BRACKET:
//                return LEFT_BRACKET;
//            case Input.Keys.RIGHT_BRACKET:
//                return RIGHT_BRACKET;
//            case Input.Keys.SEMICOLON:
//                return SEMICOLON;
//            case Input.Keys.COMMA:
//                return COMMA;
//            case Input.Keys.PERIOD:
//                return PERIOD;
//            case Input.Keys.SLASH:
//                return SLASH;
//            case Input.Keys.SPACE:
//                return SPACE_SMALL;
//            case Input.Keys.SHIFT_LEFT:
//            case Input.Keys.SHIFT_RIGHT:
//                return SHIFT;
//            case Input.Keys.CONTROL_LEFT:
//            case Input.Keys.CONTROL_RIGHT:
//                return CTRL;
//            case Input.Keys.ALT_LEFT:
//            case Input.Keys.ALT_RIGHT:
//                return ALT;
//            case Input.Keys.A:
//                return A;
//            case Input.Keys.B:
//                return B;
//            case Input.Keys.C:
//                return C;
//            case Input.Keys.D:
//                return D;
//            case Input.Keys.E:
//                return E;
//            case Input.Keys.F:
//                return F;
//            case Input.Keys.G:
//                return G;
//            case Input.Keys.H:
//                return H;
//            case Input.Keys.I:
//                return I;
//            case Input.Keys.J:
//                return J;
//            case Input.Keys.K:
//                return K;
//            case Input.Keys.L:
//                return L;
//            case Input.Keys.M:
//                return M;
//            case Input.Keys.N:
//                return N;
//            case Input.Keys.O:
//                return O;
//            case Input.Keys.P:
//                return P;
//            case Input.Keys.Q:
//                return Q;
//            case Input.Keys.R:
//                return R;
//            case Input.Keys.S:
//                return S;
//            case Input.Keys.T:
//                return T;
//            case Input.Keys.U:
//                return U;
//            case Input.Keys.V:
//                return V;
//            case Input.Keys.W:
//                return W;
//            case Input.Keys.X:
//                return X;
//            case Input.Keys.Y:
//                return Y;
//            case Input.Keys.Z:
//                return Z;
//            case Input.Keys.ENTER:
//                return ENTER;
//            default:
//                return EMPTY;
//        }
//    }
//
//    public NamespaceID getTexture() {
//        return new NamespaceID("textures/gui/icons.png");
//    }
//}
