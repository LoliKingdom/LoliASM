package zone.rong.blahajasm.api.datastructures;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class ResourceCache extends Object2ObjectOpenHashMap<String, byte[]> {

    public byte[] add(String s, byte[] bytes) {
        return super.put(s, bytes);
    }

    @Override
    public byte[] put(String s, byte[] bytes) {
        return bytes;
    }
}
