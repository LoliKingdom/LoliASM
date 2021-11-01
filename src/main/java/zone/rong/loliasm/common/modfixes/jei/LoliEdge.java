package zone.rong.loliasm.common.modfixes.jei;

import zone.rong.loliasm.api.LoliStringPool;

import java.util.Objects;

public class LoliEdge {

    public final LoliNode dest;

    public String label;

    public LoliEdge(String label, LoliNode dest) {
        this.label = LoliStringPool.canonicalize(label, LoliStringPool.JEI_ID, false);
        this.dest = dest;
    }

    @Override
    public String toString() {
        return "Edge: " + label;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof LoliEdge) {
            LoliEdge other = (LoliEdge) o;
            return this.dest.equals(other.dest) && Objects.equals(this.label, other.label);
        }
        return false;
    }

}
