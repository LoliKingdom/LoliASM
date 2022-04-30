package zone.rong.loliasm.client.sprite.ondemand;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class FloorUVTree {

    private static final float nullKey = -1;
    
    private final Tree<Tree<TextureAtlasSprite>> tree = new Tree<>();
    
    public void put(float minU, float minV, TextureAtlasSprite sprite) {
        Tree<TextureAtlasSprite> uTree = tree.get(minU);
        if (uTree == null) {
            tree.put(minU, uTree = new Tree<>());
        }
        uTree.put(minV, sprite);
    }

    @Nullable
    public TextureAtlasSprite getNearestFloorSprite(FloorUV uv) {
        Tree<TextureAtlasSprite> uTree = tree.getFloor(uv.u);
        if (uTree == null) {
            return null;
        }
        return uTree.get(uv.v);
    }

    @Nullable
    public TextureAtlasSprite getNearestFloorSprite(float u, float v) {
        Tree<TextureAtlasSprite> uTree = tree.getFloor(u);
        if (uTree == null) {
            return null;
        }
        return uTree.get(v);
    }

    /*
    static final class UTree extends Tree<VTree> { }

    static final class VTree extends Tree<TextureAtlasSprite> { }
     */

    private static class Tree<V> {

        private static <V> boolean colorOf(Entry<V> p) {
            return p == null ? BLACK : p.color;
        }

        private static <V> Entry<V> parentOf(Entry<V> p) {
            return p == null ? null: p.parent;
        }

        private static <V> void setColor(Entry<V> p, boolean c) {
            if (p != null) {
                p.color = c;
            }
        }

        private static <V> Entry<V> leftOf(Entry<V> p) {
            return p == null ? null: p.left;
        }

        private static <V> Entry<V> rightOf(Entry<V> p) {
            return p == null ? null: p.right;
        }

        Entry<V> root;
        int size = 0;
        int modCount = 0;

        void put(float key, V value) {
            if (root == null) {
                root = new Entry<>(key, value, null);
                size = 1;
                modCount++;
                return;
            }
            if (key == nullKey) {
                throw new NullPointerException();
            }
            int cmp;
            Entry<V> t = root;
            Entry<V> parent;
            do {
                parent = t;
                cmp = Float.compare(key, t.key);
                if (cmp < 0) {
                    t = t.left;
                } else if (cmp > 0) {
                    t = t.right;
                } else {
                    t.setValue(value);
                    return;
                }
            } while (t != null);
            Entry<V> e = new Entry<>(key, value, parent);
            if (cmp < 0) {
                parent.left = e;
            } else {
                parent.right = e;
            }
            fixAfterInsertion(e);
            size++;
            modCount++;
        }

        final V get(float key) {
            if (key == nullKey) {
                throw new NullPointerException();
            }
            Entry<V> p = root;
            while (p != null) {
                int cmp = Float.compare(key, p.key);
                if (cmp < 0) {
                    p = p.left;
                } else if (cmp > 0) {
                    p = p.right;
                } else {
                    return p.value;
                }
            }
            return null;
        }

        @Nullable
        final V getFloor(float key) {
            Entry<V> p = root;
            while (p != null) {
                int cmp = Float.compare(key, p.key);
                if (cmp > 0) {
                    if (p.right != null)
                        p = p.right;
                    else {
                        return p.value;
                    }
                } else if (cmp < 0) {
                    if (p.left != null) {
                        p = p.left;
                    } else {
                        Entry<V> parent = p.parent;
                        Entry<V> ch = p;
                        while (parent != null && ch == parent.left) {
                            ch = parent;
                            parent = parent.parent;
                        }
                        return parent == null ? null : parent.value;
                    }
                } else {
                    return p.value;
                }
            }
            return null;
        }

        private void fixAfterInsertion(Entry<V> x) {
            x.color = RED;
            while (x != null && x != root && x.parent.color == RED) {
                if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                    Entry<V> y = rightOf(parentOf(parentOf(x)));
                    if (colorOf(y) == RED) {
                        setColor(parentOf(x), BLACK);
                        setColor(y, BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == rightOf(parentOf(x))) {
                            x = parentOf(x);
                            rotateLeft(x);
                        }
                        setColor(parentOf(x), BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        rotateRight(parentOf(parentOf(x)));
                    }
                } else {
                    Entry<V> y = leftOf(parentOf(parentOf(x)));
                    if (colorOf(y) == RED) {
                        setColor(parentOf(x), BLACK);
                        setColor(y, BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        x = parentOf(parentOf(x));
                    } else {
                        if (x == leftOf(parentOf(x))) {
                            x = parentOf(x);
                            rotateRight(x);
                        }
                        setColor(parentOf(x), BLACK);
                        setColor(parentOf(parentOf(x)), RED);
                        rotateLeft(parentOf(parentOf(x)));
                    }
                }
            }
            root.color = BLACK;
        }

        private void rotateLeft(Entry<V> p) {
            if (p != null) {
                Entry<V> r = p.right;
                p.right = r.left;
                if (r.left != null) {
                    r.left.parent = p;
                }
                r.parent = p.parent;
                if (p.parent == null) {
                    root = r;
                } else if (p.parent.left == p) {
                    p.parent.left = r;
                } else {
                    p.parent.right = r;
                }
                r.left = p;
                p.parent = r;
            }
        }

        private void rotateRight(Entry<V> p) {
            if (p != null) {
                Entry<V> l = p.left;
                p.left = l.right;
                if (l.right != null) {
                    l.right.parent = p;
                }
                l.parent = p.parent;
                if (p.parent == null) {
                    root = l;
                } else if (p.parent.right == p) {
                    p.parent.right = l;
                } else {
                    p.parent.left = l;
                }
                l.right = p;
                p.parent = l;
            }
        }

    }

    private static final boolean RED = false;
    private static final boolean BLACK = true;

    private static final class Entry<V> implements Float2ObjectMap.Entry<V> {

        float key;
        V value;
        Entry<V> left;
        Entry<V> right;
        Entry<V> parent;
        boolean color = BLACK;

        Entry(float key, V value, Entry<V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        @Override
        public float getFloatKey() {
            return key;
        }

        @Override
        @Deprecated
        public Float getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }

        @Override
        public int hashCode() {
            int keyHash = (key == nullKey ? 0 : Float.floatToIntBits(key));
            int valueHash = (value == null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }

    }

}
