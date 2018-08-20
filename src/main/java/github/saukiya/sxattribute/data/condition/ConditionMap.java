package github.saukiya.sxattribute.data.condition;


import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Saukiya
 */
public class ConditionMap {

    private Map<Integer, SubCondition> map = new TreeMap<>();

    public SubCondition get(Integer i) {
        return map.get(i);
    }

    public int size() {
        return map.size();
    }

    public Collection<SubCondition> values() {
        return map.values();
    }

    boolean containsKey(Integer i) {
        return map.containsKey(i);
    }

    public SubCondition put(Integer i, SubCondition subCondition) {
        return map.put(i, subCondition);
    }

    public Set<Map.Entry<Integer, SubCondition>> entrySet() {
        return map.entrySet();
    }
}
