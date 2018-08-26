package github.saukiya.sxattribute.data.attribute;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 实体属性存储区
 *
 * @author Saukiya
 */
public class SXAttributeData {

    @Getter
    private double value = 0D;

    @Getter
    private boolean valid = false;

    @Getter
    private Map<Integer, SubAttribute> attributeMap = SXAttributeManager.cloneSXAttributeList();

    /**
     * 获取一个属性值
     *
     * @param attributeName String
     * @return SubAttribute
     */
    public SubAttribute getSubAttribute(String attributeName) {
        for (SubAttribute subAttribute : attributeMap.values()) {
            if (subAttribute.getName().equalsIgnoreCase(attributeName)) {
                return subAttribute;
            }
        }
        return null;
    }

    /**
     * 属性生效时，设置为有效
     */
    public void valid(){
        this.valid = true;
    }

    /**
     * 将两个SXAttributeData合并
     * 如果对方不为null 并且有效 那么设置为有效
     *
     * @param attributeData 另一个SXAttributeData
     * @return 合并后的此类
     */
    public SXAttributeData add(SXAttributeData attributeData) {
        if (attributeData != null && attributeData.isValid()) {
            valid();
            for (SubAttribute attribute : getAttributeMap().values()) {
                for (SubAttribute subAttribute : attributeData.getAttributeMap().values()) {
                    if (attribute.getName().equals(subAttribute.getName())) {
                        attribute.addAttribute(subAttribute.getAttributes());
                        break;
                    }
                }
            }
        }
        return this;
    }

    /**
     * 纠正错误的属性 几率不能为负数 有些几率不能大于100%
     */
    void correct() {
        getAttributeMap().values().forEach(SubAttribute::correct);
    }

    /**
     * getAttribute
     * 计算成战斗点数
     *
     * @return double 战斗点数
     */
    public double calculationValue() {
        this.value = 0D;
        getAttributeMap().values().forEach(attribute -> this.value += attribute.getValue());
        return this.value;
    }

    /**
     * String 转 SXAttributeData
     *
     * @param string 存有SXAttributeData数据的String
     * @return SXAttributeData 此类
     */
    public SXAttributeData loadFromString(String string) {
        return loadFromList(Arrays.asList(string.split("//")));
    }

    /**
     * List 转 SXAttributeData
     *
     * @param list 列表
     * @return SXAttributeData
     */
    public SXAttributeData loadFromList(List<String> list) {
        for (String attributeString : list) {
            for (SubAttribute attribute : getAttributeMap().values()) {
                attribute.loadFromString(attributeString);
            }
        }
        return this;
    }

    /**
     * 数据转为String
     *
     * @return String
     */
    public String saveToString() {
        List<String> list = saveToList();
        return IntStream.range(0, list.size()).mapToObj(i -> i == list.size() - 1 ? list.get(i) : list.get(i) + "//").collect(Collectors.joining());
    }

    /**
     * 数据转为List
     *
     * @return List
     */
    public List<String> saveToList() {
        return getAttributeMap().values().stream().map(SubAttribute::saveToString).filter(Objects::nonNull).collect(Collectors.toList());
    }


}
