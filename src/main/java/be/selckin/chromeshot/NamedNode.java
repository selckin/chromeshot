package be.selckin.chromeshot;

import static java.util.Objects.requireNonNull;

public record NamedNode(Integer nodeId, String name) {

    public NamedNode {
        requireNonNull(nodeId);
        requireNonNull(name);
    }

}
