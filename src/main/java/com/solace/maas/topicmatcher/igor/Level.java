package com.solace.maas.topicmatcher.igor;

import java.util.Objects;

public class Level {
    private final short depth;
    private final String name;
    private final String parent;
    private final boolean leaf;

    Level(Level other) {
        depth = other.depth;
        name = other.name;
        parent = other.parent;
        leaf = other.leaf;
    }

    Level(short depth, String name, String parent, boolean leaf) {
        this.depth = depth;
        this.name = name;
        this.parent = parent;
        this.leaf = leaf;
    }

    public short getDepth() {
        return depth;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return leaf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Level level = (Level) o;
        return depth == level.depth &&
                leaf == level.leaf &&
                Objects.equals(name, level.name) &&
                Objects.equals(parent, level.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, name, parent, leaf);
    }

    @Override
    public String toString() {
        return "TreeLevel{" +
                "depth=" + depth +
                ", name='" + name + '\'' +
                ", parent='" + parent + '\'' +
                ", leaf=" + leaf +
                '}';
    }
}
