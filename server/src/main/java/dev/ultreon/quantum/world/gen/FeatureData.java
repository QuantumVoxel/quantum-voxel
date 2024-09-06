package dev.ultreon.quantum.world.gen;

import dev.ultreon.quantum.world.BuilderChunk;
import dev.ultreon.quantum.world.vec.ChunkVec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureData {
    private final Map<ChunkVec, List<FeatureInfo>> featureData = new HashMap<>();

    public void prepareChunk(BuilderChunk chunk) {
        synchronized (this) {
            List<FeatureInfo> featureInfos = this.featureData.remove(chunk.getVec());
            if (featureInfos == null) {
                return;
            }
            for (FeatureInfo featureInfo : featureInfos) {
                featureInfo.points().forEach(point -> {
                    if (point.pos().chunk().equals(chunk.getVec()))
                        chunk.set(point.pos().chunkLocal(), point.state());
                });
            }
        }
    }

    public void writeFeature(BuilderChunk origin, FeatureInfo featureInfo) {
        synchronized (this) {
            for (ChunkVec chunkVec : featureInfo.coveringChunks()) {
                this.featureData.computeIfAbsent(chunkVec, k -> new ArrayList<>()).add(featureInfo);

                if (chunkVec.equals(origin.getVec())) {
                    featureInfo.points().forEach(point -> {
                        if (point.pos().chunk().equals(chunkVec))
                            origin.set(point.pos().chunkLocal(), point.state());
                    });
                }
            }
        }
    }
}
