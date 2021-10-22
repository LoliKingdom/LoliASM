package zone.rong.loliasm.common.modfixes.jei;

import javax.annotation.Nullable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SerializedLoliEdge implements Externalizable {

    public int destinationNodeId;
    @Nullable public String label;

    public SerializedLoliEdge() { }

    public SerializedLoliEdge(int destinationNodeId, @Nullable String label) {
        this.destinationNodeId = destinationNodeId;
        this.label = label;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(destinationNodeId);
        out.writeBoolean(label != null);
        if (label != null) {
            out.writeObject(label.toCharArray());
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        destinationNodeId = in.readInt();
        if (in.readBoolean()) {
            label = new String((char[]) in.readObject());
        }
    }
}
