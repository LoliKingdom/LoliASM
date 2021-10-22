package zone.rong.loliasm.common.modfixes.jei;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SerializedLoliNode implements Externalizable {

    public int id;
    public int[] data;
    public Char2ObjectMap<SerializedLoliEdge> edges;
    public int suffixId;

    public SerializedLoliNode() { }

    public SerializedLoliNode(int id, int[] data, Char2ObjectMap<SerializedLoliEdge> edges, int suffixId) {
        this.id = id;
        this.data = data;
        this.edges = edges;
        this.suffixId = suffixId;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeObject(data);
        out.writeObject(edges);
        out.writeInt(suffixId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        data = (int[]) in.readObject();
        edges = (Char2ObjectMap<SerializedLoliEdge>) in.readObject();
        suffixId = in.readInt();
    }
}
