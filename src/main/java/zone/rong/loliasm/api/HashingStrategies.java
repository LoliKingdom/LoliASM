package zone.rong.loliasm.api;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public class HashingStrategies {

    public static final Hash.Strategy<Object> IDENTITY_OBJECT_HASH = new Hash.Strategy<Object>() {
        @Override
        public int hashCode(Object o) {
            return System.identityHashCode(o);
        }
        @Override
        public boolean equals(Object a, Object b) {
            return a == b;
        }
    };

    public static final Hash.Strategy<Object> GENERIC_OBJECT_HASH = new Hash.Strategy<Object>() {
        @Override
        public int hashCode(Object o) {
            return Objects.hashCode(o);
        }
        @Override
        public boolean equals(Object o1, Object o2) {
            return Objects.equals(o1, o2);
        }
    };

    public static final Hash.Strategy<float[][]> FLOAT_2D_ARRAY_HASH = new Hash.Strategy<float[][]>() {
        @Override
        public int hashCode(float[][] o) {
            return Arrays.deepHashCode(o);
        }
        @Override
        public boolean equals(float[][] a, float[][] b) {
            return Arrays.deepEquals(a, b);
        }
    };

    public static final Hash.Strategy<ItemCameraTransforms> ITEM_CAMERA_TRANSFORMS_HASH = new Hash.Strategy<ItemCameraTransforms>() {
        @Override
        public int hashCode(ItemCameraTransforms ict) {
            int hash = vectorHash(ict.firstperson_left);
            hash = hash * 31 + vectorHash(ict.firstperson_right);
            hash = hash * 31 + vectorHash(ict.fixed);
            hash = hash * 31 + vectorHash(ict.ground);
            hash = hash * 31 + vectorHash(ict.gui);
            hash = hash * 31 + vectorHash(ict.head);
            hash = hash * 31 + vectorHash(ict.thirdperson_left);
            return hash * 31 + vectorHash(ict.thirdperson_right);
        }
        @Override
        public boolean equals(ItemCameraTransforms ict1, ItemCameraTransforms ict2) {
            if (ict1 == null) {
                return ict2 == null;
            } else {
                return Objects.equals(ict1.firstperson_left, ict2.firstperson_left) && Objects.equals(ict1.firstperson_right, ict2.firstperson_right) && Objects.equals(ict1.fixed, ict2.fixed) && Objects.equals(ict1.ground, ict2.ground) && Objects.equals(ict1.gui, ict2.gui) && Objects.equals(ict1.head, ict2.head) && Objects.equals(ict1.thirdperson_left, ict2.thirdperson_left) && Objects.equals(ict1.thirdperson_right, ict2.thirdperson_right);
            }
        }
    };

    private static int vectorHash(ItemTransformVec3f vector) {
        int hash = ((Float.floatToIntBits(vector.rotation.getX())) * 31 + Float.floatToIntBits(vector.rotation.getY())) * 31 + Float.floatToIntBits(vector.rotation.getZ());
        hash = hash * 31 + ((Float.floatToIntBits(vector.scale.getX())) * 31 + Float.floatToIntBits(vector.scale.getY())) * 31 + Float.floatToIntBits(vector.scale.getZ());
        return hash * 31 + ((Float.floatToIntBits(vector.translation.getX())) * 31 + Float.floatToIntBits(vector.translation.getY())) * 31 + Float.floatToIntBits(vector.translation.getZ());
    }

    public static final Hash.Strategy<ItemStack> FURNACE_INPUT_HASH = new Hash.Strategy<ItemStack>() {
        @Override
        public int hashCode(ItemStack o) {
            return 31 * o.getItem().hashCode();
        }
        @Override
        public boolean equals(ItemStack a, ItemStack b) {
            if (a == null || b == null) {
                return false;
            }
            return a.getItem() == b.getItem() && (a.getMetadata() == b.getMetadata() || b.getMetadata() == Short.MAX_VALUE);
        }
    };

}
