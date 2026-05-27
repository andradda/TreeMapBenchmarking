package dk.itu.thesis.factory;

import dk.itu.thesis.api.ConcurrentTree;
import dk.itu.thesis.impl.ChromaticTreeAdapter;
import dk.itu.thesis.impl.SnapTreeAdapter;
import dk.itu.thesis.impl.VcasBatchChromaticAdapter;
import dk.itu.thesis.impl.KSTRQAdapter;

public final class TreeFactory {

    private TreeFactory() {}

    public static ConcurrentTree<Integer, Integer, ?> create(String treeType) {
        return switch (treeType.toLowerCase()) {

            case "snaptree" -> new SnapTreeAdapter<>();

            case "chromatic6", "chromatic" -> new ChromaticTreeAdapter<>();

            case "vcas" -> new VcasBatchChromaticAdapter<>();

            case "kary" -> new KSTRQAdapter<>();

            default -> throw new IllegalArgumentException("Unknown tree type: " + treeType);
        };
    }
}