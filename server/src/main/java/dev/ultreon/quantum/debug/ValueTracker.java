package dev.ultreon.quantum.debug;

public class ValueTracker {
    private static long meshDisposes = 0L;
    private static long vertexCount;
    private static int packetsSent;
    private static int packetsReceived;
    private static int packetsReceivedTotal;
    private static int chunkLoads;
    private static long chunkMeshFrees;
    private static long poolFree;
    private static int poolPeak;
    private static int poolMax;
    private static int obtainedRenderables;
    private static int renderableFlushes;
    private static int obtainRequests;
    private static int freeRequests;
    private static int flushRequests;
    private static int renderableCount;
    private static int chunkTracks;
    private static int shaderSwitches;
    private static int textureBindings;

    public static long getMeshDisposes() {
        return ValueTracker.meshDisposes;
    }

    public static void setMeshDisposes(long meshDisposes) {
        ValueTracker.meshDisposes = meshDisposes;
    }

    public static long getVertexCount() {
        return ValueTracker.vertexCount;
    }

    public static void setVertexCount(long vertexCount) {
        ValueTracker.vertexCount = vertexCount;
    }

    public static int getPacketsSent() {
        return ValueTracker.packetsSent;
    }

    public static void setPacketsSent(int packetsSent) {
        ValueTracker.packetsSent = packetsSent;
    }

    public static int getPacketsReceived() {
        return ValueTracker.packetsReceived;
    }

    public static void setPacketsReceived(int packetsReceived) {
        ValueTracker.packetsReceived = packetsReceived;
    }

    public static int getPacketsReceivedTotal() {
        return ValueTracker.packetsReceivedTotal;
    }

    public static void setPacketsReceivedTotal(int packetsReceivedTotal) {
        ValueTracker.packetsReceivedTotal = packetsReceivedTotal;
    }

    public static int getChunkLoads() {
        return ValueTracker.chunkLoads;
    }

    public static void setChunkLoads(int chunkLoads) {
        ValueTracker.chunkLoads = chunkLoads;
    }

    public static long getChunkMeshFrees() {
        return ValueTracker.chunkMeshFrees;
    }

    public static void setChunkMeshFrees(long chunkMeshFrees) {
        ValueTracker.chunkMeshFrees = chunkMeshFrees;
    }

    public static long getPoolFree() {
        return ValueTracker.poolFree;
    }

    public static void setPoolFree(long poolFree) {
        ValueTracker.poolFree = poolFree;
    }

    public static int getPoolPeak() {
        return ValueTracker.poolPeak;
    }

    public static void setPoolPeak(int poolPeak) {
        ValueTracker.poolPeak = poolPeak;
    }

    public static int getPoolMax() {
        return ValueTracker.poolMax;
    }

    public static void setPoolMax(int poolMax) {
        ValueTracker.poolMax = poolMax;
    }

    public static int getObtainedRenderables() {
        return obtainedRenderables;
    }

    public static void setObtainedRenderables(int size) {
        ValueTracker.obtainedRenderables = size;
    }

    public static int getRenderableFlushes() {
        return renderableFlushes;
    }

    public static void trackFlushes(int flushes) {
        renderableFlushes += flushes;
    }

    public static void resetFlushed() {
        renderableFlushes = 0;
    }

    public static void resetFlushAttempts() {
    }

    public static void trackObtainRequest() {
        obtainRequests++;
    }

    public static int getObtainRequests() {
        return obtainRequests;
    }

    public static void resetObtainRequests() {
//        obtainRequests = 0;
    }

    public static void trackFreeRequest() {
        freeRequests++;
    }

    public static void trackFreeRequests(int size) {
        freeRequests += size;
    }

    public static int getFreeRequests() {
        return freeRequests;
    }

    public static void trackFlushRequest() {
        flushRequests++;
    }

    public static int getFlushRequests() {
        return flushRequests;
    }

    public static void trackRenderables(int count) {
        renderableCount+=count;
        chunkTracks++;
    }

    public static int getRenderableCount() {
        return renderableCount;
    }

    public static void resetRenderables() {
        renderableCount = 0;
        chunkTracks = 0;
    }

    public static int getAverageRenderables() {
        if (chunkTracks == 0) return 0;
        return renderableCount / chunkTracks;
    }

    public static void setShaderSwitches(int switches) {
        shaderSwitches = switches;
    }

    public static int getShaderSwitches() {
        return shaderSwitches;
    }

    public static void setTextureBindings(int textureBindings) {
        ValueTracker.textureBindings = textureBindings;
    }

    public static int getTextureBindings() {
        return textureBindings;
    }
}
