package github.saukiya.sxattribute.data.attribute;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Saukiya
 */
public class AttributeMap {

    private Map<Integer, SubAttribute> map = new TreeMap<>();

    public SubAttribute get(int i) {
        return map.get(i);
    }

    public void remove(int i) {
        map.remove(i);
    }

    public int size() {
        return map.size();
    }

    public Collection<SubAttribute> values() {
        return map.values();
    }

    boolean containsKey(Integer i) {
        return map.containsKey(i);
    }

    public SubAttribute put(Integer i, SubAttribute subAttribute) {
        return map.put(i, subAttribute);
    }

    public Set<Map.Entry<Integer, SubAttribute>> entrySet() {
        return map.entrySet();
    }
}
