package zone.rong.loliasm.common.modfixes.jei.mixins;

import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2IntLinkedOpenHashMap;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import mezz.jei.suffixtree.ISearchTree;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.api.mixins.GeneralizedSuffixTreeExtender;
import zone.rong.loliasm.common.modfixes.jei.LoliEdge;
import zone.rong.loliasm.common.modfixes.jei.LoliNode;
import zone.rong.loliasm.common.modfixes.jei.SerializedLoliEdge;
import zone.rong.loliasm.common.modfixes.jei.SerializedLoliNode;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.stream.Stream;

@Mixin(value = GeneralizedSuffixTree.class, remap = false)
public class GeneralizedSuffixTreeMixin implements ISearchTree, GeneralizedSuffixTreeExtender, Externalizable {

    @Shadow private static String safeCutLastChar(String seq) { throw new AssertionError(); }

    @Shadow private int highestIndex;

    @Unique private static final long serialVersionUID = 1011L;

    /**
     * The root of the suffix tree
     */
    @Unique private LoliNode loliasm$root = new LoliNode();
    /**
     * The last leaf that was added during the update operation
     */
    @Unique private LoliNode loliasm$activeLeaf = loliasm$root;

    @Unique private int lastSearchSize = 1000;

    @Unique private boolean deserialized = false;

    @Override
    public boolean isDeserialized() {
        return deserialized;
    }

    @Override
    public IntSet search(String word) {
        LoliNode tmpNode = loliasm$searchNode(word);
        if (tmpNode == null) {
            return new IntArraySet(0);
        }
        IntSet ret = new IntOpenHashSet(lastSearchSize);
        tmpNode.getData(ret);
        lastSearchSize = Math.max(1000, ret.size());
        return ret;
    }

    private LoliNode loliasm$searchNode(String word) {
        LoliNode currentNode = loliasm$root;
        LoliEdge currentEdge;

        for (int i = 0; i < word.length(); ++i) {
            char ch = word.charAt(i);
            // follow the edge corresponding to this char
            currentEdge = currentNode.getEdge(ch);
            if (null == currentEdge) {
                // there is no edge starting with this char
                return null;
            } else {
                String label = currentEdge.label;
                int lenToMatch = Math.min(word.length() - i, label.length());
                if (!word.regionMatches(i, label, 0, lenToMatch)) {
                    // the label on the edge does not correspond to the one in the string to search
                    return null;
                }
                if (label.length() >= word.length() - i) {
                    return currentEdge.dest;
                } else {
                    // advance to next node
                    currentNode = currentEdge.dest;
                    i += lenToMatch - 1;
                }
            }
        }
        return null;
    }

    /**
     * Adds the specified <tt>index</tt> to the GST under the given <tt>key</tt>.
     * <p>
     * Entries must be inserted so that their indexes are in non-decreasing order,
     * otherwise an IllegalStateException will be raised.
     *
     * @param key   the string key that will be added to the index
     * @param index the value that will be added to the index
     * @author Rongmario
     * @reason Use LoliASM objects, overwriting this method instead of re-writing damn classes everywhere
     */
    @Overwrite
    public void put(String key, int index) throws IllegalStateException {
        if (index < highestIndex) {
            throw new IllegalStateException("The input index must not be less than any of the previously inserted ones. Got " + index + ", expected at least " + highestIndex);
        } else {
            highestIndex = index;
        }
        // reset activeLeaf
        loliasm$activeLeaf = loliasm$root;
        LoliNode s = loliasm$root;
        // proceed with tree construction (closely related to procedure in Ukkonen's paper)
        String text = "";
        // iterate over the string, one char at a time
        for (int i = 0; i < key.length(); i++) {
            // line 6, line 7: update the tree with the new transitions due to this new char
            Tuple<LoliNode, String> active = loliasm$update(s, text, key.charAt(i), key.substring(i), index);
            s = active.getFirst();
            text = active.getSecond();
        }
        // add leaf suffix link, is necessary
        if (loliasm$activeLeaf != loliasm$root && loliasm$activeLeaf != s && loliasm$activeLeaf.suffix == null) {
            loliasm$activeLeaf.suffix = s;
        }
    }

    /**
     * Tests whether the string stringPart + t is contained in the subtree that has inputs as root.
     * If that's not the case, and there exists a path of edges e1, e2, ... such that
     * e1.label + e2.label + ... + $end = stringPart
     * and there is an edge g such that
     * g.label = stringPart + rest
     * <p>
     * Then g will be split in two different edges, one having $end as label, and the other one
     * having rest as label.
     *
     * @param inputs     the starting node
     * @param stringPart the string to search
     * @param t          the following character
     * @param remainder  the remainder of the string to add to the index
     * @param value      the value to add to the index
     * @return a pair containing
     * true/false depending on whether (stringPart + t) is contained in the subtree starting in inputs
     * the last node that can be reached by following the path denoted by stringPart starting from inputs
     */
    private Tuple<Boolean, LoliNode> loliasm$testAndSplit(final LoliNode inputs, final String stringPart, final char t, final String remainder, final int value) {
        // descend the tree as far as possible
        Tuple<LoliNode, String> ret = loliasm$canonize(inputs, stringPart);
        LoliNode s = ret.getFirst();
        String str = ret.getSecond();
        if (!"".equals(str)) {
            LoliEdge g = s.getEdge(str.charAt(0));
            Objects.requireNonNull(g);
            String label = g.label;
            // must see whether "str" is substring of the label of an edge
            if (label.length() > str.length() && label.charAt(str.length()) == t) {
                return new Tuple<>(true, s);
            } else {
                // need to split the edge
                String newlabel = label.substring(str.length());
                assert (label.startsWith(str));
                // build a new node
                LoliNode r = new LoliNode();
                // build a new edge
                LoliEdge newedge = new LoliEdge(str, r);
                g.label = newlabel;
                // link s -> r
                r.addEdge(newlabel.charAt(0), g);
                s.addEdge(str.charAt(0), newedge);
                return new Tuple<>(false, r);
            }
        } else {
            LoliEdge e = s.getEdge(t);
            if (null == e) {
                // if there is no t-transtion from s
                return new Tuple<>(false, s);
            } else {
                if (remainder.equals(e.label)) {
                    // update payload of destination node
                    e.dest.addRef(value);
                    return new Tuple<>(true, s);
                } else if (remainder.startsWith(e.label)) {
                    return new Tuple<>(true, s);
                } else if (e.label.startsWith(remainder)) {
                    // need to split as above
                    LoliNode newNode = new LoliNode();
                    newNode.addRef(value);
                    LoliEdge newEdge = new LoliEdge(remainder, newNode);
                    e.label = e.label.substring(remainder.length());
                    newNode.addEdge(e.label.charAt(0), e);
                    s.addEdge(t, newEdge);
                    return new Tuple<>(false, s);
                } else {
                    // they are different words. No prefix. but they may still share some common substr
                    return new Tuple<>(true, s);
                }
            }
        }

    }

    /**
     * Return a (Node, String) (n, remainder) pair such that n is a farthest descendant of
     * s (the input node) that can be reached by following a path of edges denoting
     * a prefix of inputstr and remainder will be string that must be
     * appended to the concatenation of labels from s to n to get inpustr.
     */
    private Tuple<LoliNode, String> loliasm$canonize(final LoliNode s, final String inputstr) {
        if ("".equals(inputstr)) {
            return new Tuple<>(s, inputstr);
        } else {
            LoliNode currentNode = s;
            String str = inputstr;
            LoliEdge g = s.getEdge(str.charAt(0));
            // descend the tree as long as a proper label is found
            while (g != null && str.startsWith(g.label)) {
                str = str.substring(g.label.length());
                currentNode = g.dest;
                if (str.length() > 0) {
                    g = currentNode.getEdge(str.charAt(0));
                }
            }
            return new Tuple<>(currentNode, str);
        }
    }

    /**
     * Updates the tree starting from inputNode and by adding stringPart.
     * <p>
     * Returns a reference (Node, String) pair for the string that has been added so far.
     * This means:
     * - the Node will be the Node that can be reached by the longest path string (S1)
     * that can be obtained by concatenating consecutive edges in the tree and
     * that is a substring of the string added so far to the tree.
     * - the String will be the remainder that must be added to S1 to get the string
     * added so far.
     *
     * @param inputNode  the node to start from
     * @param stringPart the string to add to the tree
     * @param rest       the rest of the string
     * @param value      the value to add to the index
     */
    private Tuple<LoliNode, String> loliasm$update(final LoliNode inputNode, final String stringPart, final char newChar, final String rest, final int value) {
        LoliNode s = inputNode;
        String tempstr = stringPart + newChar;
        // line 1
        LoliNode oldroot = loliasm$root;
        // line 1b
        Tuple<Boolean, LoliNode> ret = loliasm$testAndSplit(s, stringPart, newChar, rest, value);
        LoliNode r = ret.getSecond();
        boolean endpoint = ret.getFirst();
        LoliNode leaf;
        // line 2
        while (!endpoint) {
            // line 3
            LoliEdge tempEdge = r.getEdge(newChar);
            if (null != tempEdge) {
                // such a node is already present. This is one of the main differences from Ukkonen's case:
                // the tree can contain deeper nodes at this stage because different strings were added by previous iterations.
                leaf = tempEdge.dest;
            } else {
                // must build a new leaf
                leaf = new LoliNode();
                leaf.addRef(value);
                LoliEdge newedge = new LoliEdge(rest, leaf);
                r.addEdge(newChar, newedge);
            }
            // update suffix link for newly created leaf
            if (loliasm$activeLeaf != loliasm$root) {
                loliasm$activeLeaf.suffix = leaf;
            }
            loliasm$activeLeaf = leaf;
            // line 4
            if (oldroot != loliasm$root) {
                oldroot.suffix = r;
            }
            // line 5
            oldroot = r;
            // line 6
            if (null == s.suffix) { // root node
                assert (loliasm$root == s);
                // this is a special case to handle what is referred to as node _|_ on the paper
                tempstr = tempstr.substring(1);
            } else {
                Tuple<LoliNode, String> canret = loliasm$canonize(s.suffix, safeCutLastChar(tempstr));
                s = canret.getFirst();
                tempstr = (canret.getSecond() + tempstr.charAt(tempstr.length() - 1));
            }
            // line 7
            ret = loliasm$testAndSplit(s, safeCutLastChar(tempstr), newChar, rest, value);
            r = ret.getSecond();
            endpoint = ret.getFirst();
        }
        // line 8
        if (oldroot != loliasm$root) {
            oldroot.suffix = r;
        }
        // make sure the active pair is canonical
        return loliasm$canonize(s, tempstr);
    }
    
    /**
     * @author Rongmario
     * @reason New impl means no need for trimming
     */
    @Overwrite
    public void trimToSize() { }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof GeneralizedSuffixTree) {
            GeneralizedSuffixTree other = (GeneralizedSuffixTree) o;
            boolean b$root = this.loliasm$root.equals(((GeneralizedSuffixTreeMixin) (Object) other).loliasm$root);
            boolean b$highestIndex = this.highestIndex == other.getHighestIndex();
            return b$root && b$highestIndex;
        }
        return false;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(highestIndex);
        Reference2IntLinkedOpenHashMap<LoliNode> nodeMapping = new Reference2IntLinkedOpenHashMap<>(4096);
        ArrayDeque<LoliNode> nodes = new ArrayDeque<>(128);
        nodes.add(loliasm$root);
        while (!nodes.isEmpty()) {
            LoliNode node = nodes.remove();
            nodeMapping.put(node, nodeMapping.size());
            for (LoliEdge edge : node.edges()) {
                nodes.add(edge.dest);
            }
        }
        ObjectArrayList<SerializedLoliNode> serializedNodes = new ObjectArrayList<>(nodeMapping.size());
        nodeMapping.forEach((node, id) -> {
            Char2ObjectMap<SerializedLoliEdge> edges = node.edges.char2ObjectEntrySet()
                    .stream()
                    .collect(Char2ObjectArrayMap::new,
                            (m, e) -> m.put(e.getCharKey(), new SerializedLoliEdge(nodeMapping.getInt(e.getValue().dest), e.getValue().label)),
                            Char2ObjectMap::putAll);
            serializedNodes.add(new SerializedLoliNode(id, node.data, edges, node.suffix == null ? -1 : nodeMapping.getInt(node.suffix)));
        });
        out.writeObject(serializedNodes.elements());
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        highestIndex = in.readInt();
        Object[] deserializedNodes = (Object[]) in.readObject();
        LoliNode[] nodes = Stream.generate(LoliNode::new).limit(deserializedNodes.length).toArray(LoliNode[]::new);
        for (int i = 0; i < nodes.length; i++) {
            SerializedLoliNode deserializedNode = (SerializedLoliNode) deserializedNodes[i];
            LoliNode node = nodes[i];
            node.data = deserializedNode.data;
            if (deserializedNode.suffixId != -1) {
                node.suffix = nodes[deserializedNode.suffixId];
            }
            if (deserializedNode.edges instanceof Char2ObjectMaps.EmptyMap) {
                node.edges = Char2ObjectMaps.emptyMap();
            } else if (deserializedNode.edges instanceof Char2ObjectMaps.Singleton) {
                Char2ObjectMap.Entry<SerializedLoliEdge> e = deserializedNode.edges.char2ObjectEntrySet().stream().findFirst().get();
                node.edges = Char2ObjectMaps.singleton(e.getCharKey(), new LoliEdge(e.getValue().label, nodes[e.getValue().destinationNodeId]));
            } else {
                node.edges = deserializedNode.edges.char2ObjectEntrySet()
                        .stream()
                        .collect(Char2ObjectArrayMap::new,
                                (m, e) -> m.put(e.getCharKey(), new LoliEdge(e.getValue().label, nodes[e.getValue().destinationNodeId])),
                                Char2ObjectMap::putAll);
            }
        }
        loliasm$root = nodes[0];
        deserialized = true;
    }

}
