package dev.ultreon.quantum.desktop.platform.win32;

import com.sun.jna.Structure;
import dev.ultreon.quantum.Margins;

// MARGINS structure for DWM
public class MARGINS extends Structure implements Margins {
    public int cxLeftWidth;
    public int cxRightWidth;
    public int cyTopHeight;
    public int cyBottomHeight;

    @Override
    protected java.util.List<String> getFieldOrder() {
        return java.util.Arrays.asList("cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight");
    }

    @Override
    public int getLeft() {
        return cxLeftWidth;
    }

    @Override
    public int getRight() {
        return cxRightWidth;
    }

    @Override
    public int getTop() {
        return cyTopHeight;
    }

    @Override
    public int getBottom() {
        return cyBottomHeight;
    }

    @Override
    public void setMargins(int left, int top, int right, int bottom) {
        this.cxLeftWidth = left;
        this.cyTopHeight = top;
        this.cxRightWidth = right;
        this.cyBottomHeight = bottom;
    }
}
