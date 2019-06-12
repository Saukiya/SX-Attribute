package github.saukiya.sxattribute.data.condition;

import lombok.Getter;

/**
 * @author Saukiya
 */
public class SXConditionReturnType {
    /**
     * 返回当前Lore不被识别
     */
    public static final SXConditionReturnType LORE = new SXConditionReturnType(Type.LORE);
    /**
     * 返回该物品不符合使用
     */
    public static final SXConditionReturnType ITEM = new SXConditionReturnType(Type.ITEM);
    /**
     * 跳过此次判断
     */
    public static final SXConditionReturnType NULL = new SXConditionReturnType(Type.NULL);


    @Getter
    private final String name;

    @Getter
    private final Type type;

    public SXConditionReturnType(Type type) {
        this.type = type;
        this.name = type.getName();
    }

    public SXConditionReturnType(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 判断条件的Type是否相同
     *
     * @param obj SXConditionType
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SXConditionReturnType && type.equals(((SXConditionReturnType) obj).type);
    }

    /**
     * 条件类型枚举
     */
    public enum Type {
        LORE("Lore"),
        ITEM("Item"),
        NULL("Null");

        @Getter
        String name;

        Type(String name) {
            this.name = name;
        }
    }
}
