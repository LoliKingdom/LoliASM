package zone.rong.loliasm.api.datastructures.fastmap;

@FunctionalInterface
public interface StateAndKey {

    Comparable<?> getBy(Object state, Object key);

}
