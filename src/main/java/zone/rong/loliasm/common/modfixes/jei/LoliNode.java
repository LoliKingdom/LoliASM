package zone.rong.loliasm.common.modfixes.jei;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class LoliNode {

    public int[] data;
    public Char2ObjectMap<LoliEdge> edges;
    @Nullable public LoliNode suffix;

    /**
     * Creates a new Node
     */
    public LoliNode() {
        data = new int[0];
        edges = Char2ObjectMaps.emptyMap();
    }

    /**
     * Gets data from the payload of both this node and its children, the string representation
     * of the path to this node is a substring of the one of the children nodes.
     */
    public void getData(final IntSet ret) {
        for (int d : data) {
            ret.add(d);
        }
        for (LoliEdge e : edges.values()) {
            e.dest.getData(ret);
        }
    }

    /**
     * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
     * returns false if this node already contains the ref
     */
    public boolean addRef(int index) {
        if (contains(index)) {
            return false;
        }
        addIndex(index);
        // add this reference to all the suffixes as well
        LoliNode iter = this.suffix;
        while (iter != null) {
            if (!iter.contains(index)) {
                iter.addIndex(index);
                iter = iter.suffix;
            } else {
                break;
            }
        }

        return true;
    }

    /**
     * Tests whether a node contains a reference to the given index.
     *
     * @param index the index to look for
     * @return true <tt>this</tt> contains a reference to index
     */
    private boolean contains(int index) {
        for (int d : data) {
            if (d == index) {
                return true;
            }
        }
        return false;
    }

    public void addEdge(char ch, LoliEdge e) {
        if (edges instanceof Char2ObjectMaps.EmptyMap) {
            edges = Char2ObjectMaps.singleton(ch, e);
            return;
        } else if (edges instanceof Char2ObjectMaps.Singleton) {
            edges = new Char2ObjectArrayMap<>(edges);
        }
        edges.put(ch, e);
    }

    @Nullable
    public LoliEdge getEdge(char ch) {
        return edges.get(ch);
    }

    private void addIndex(int index) {
        this.data = ArrayUtils.add(this.data, index);
    }

    public ObjectCollection<LoliEdge> edges() {
        return edges.values();
    }

    @Override
    public String toString() {
        return "Node: size:" + data.length + " Edges: " + edges.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof LoliNode) {
            LoliNode other = (LoliNode) o;
            return Arrays.equals(this.data, other.data) && Objects.equals(this.edges, other.edges);
        }
        return false;
    }

}
