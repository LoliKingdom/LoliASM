package zone.rong.loliasm.common.modfixes.jei;

import java.util.Objects;

public class LoliEdge {

    public final LoliNode dest;

    public String label;

    public LoliEdge(String label, LoliNode dest) {
        this.label = label;
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
