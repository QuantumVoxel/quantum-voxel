//package dev.ultreon.quantum.client.input.controller;
//
//import io.github.libsdl4j.api.gamecontroller.SDL_GameController;
//
//import java.util.Objects;
//
//public final class Controller {
//    private final SDL_GameController sdlController;
//    private final int deviceIndex;
//    private final short productId;
//    private final short vendorId;
//    private final String name;
//    private final String mapping;
//
//    public Controller(SDL_GameController sdlController, int deviceIndex, short productId, short vendorId,
//                      String name, String mapping) {
//        this.sdlController = sdlController;
//        this.deviceIndex = deviceIndex;
//        this.productId = productId;
//        this.vendorId = vendorId;
//        this.name = name;
//        this.mapping = mapping;
//    }
//
//    public SDL_GameController sdlController() {
//        return sdlController;
//    }
//
//    public int deviceIndex() {
//        return deviceIndex;
//    }
//
//    public short productId() {
//        return productId;
//    }
//
//    public short vendorId() {
//        return vendorId;
//    }
//
//    public String name() {
//        return name;
//    }
//
//    public String mapping() {
//        return mapping;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) return true;
//        if (obj == null || obj.getClass() != this.getClass()) return false;
//        var that = (Controller) obj;
//        return Objects.equals(this.sdlController, that.sdlController) &&
//               this.deviceIndex == that.deviceIndex &&
//               this.productId == that.productId &&
//               this.vendorId == that.vendorId &&
//               Objects.equals(this.name, that.name) &&
//               Objects.equals(this.mapping, that.mapping);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(sdlController, deviceIndex, productId, vendorId, name, mapping);
//    }
//
//    @Override
//    public String toString() {
//        return "Controller[" +
//               "sdlController=" + sdlController + ", " +
//               "deviceIndex=" + deviceIndex + ", " +
//               "productId=" + productId + ", " +
//               "vendorId=" + vendorId + ", " +
//               "name=" + name + ", " +
//               "mapping=" + mapping + ']';
//    }
//
//}
