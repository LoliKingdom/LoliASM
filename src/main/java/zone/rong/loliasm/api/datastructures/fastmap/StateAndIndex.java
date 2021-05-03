package zone.rong.loliasm.api.datastructures.fastmap;

import java.util.Map;

public interface StateAndIndex {

    Map.Entry<?, Comparable<?>> getBy(Object state, int index);

}
