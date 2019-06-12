package github.saukiya.sxattribute.data.attribute;

import lombok.Getter;

/**
 * 属性类型
 *
 * @author Saukiya
 */
public class SXAttributeType {
    /**
     * 伤害型属性
     */
    public static final SXAttributeType DAMAGE = new SXAttributeType(Type.DAMAGE);
    /**
     * 防御型属性
     */
    public static final SXAttributeType DEFENCE = new SXAttributeType(Type.DEFENCE);
    /**
     * 更新属性(目前仅玩家)
     */
    public static final SXAttributeType UPDATE = new SXAttributeType(Type.UPDATE);
    /**
     * 自定义属性
     */
    public static final SXAttributeType OTHER = new SXAttributeType(Type.OTHER);

    @Getter
    private String name;

    @Getter
    private Type type;

    public SXAttributeType(Type type) {
        this.type = type;
        this.name = type.getName();
    }

    /**
     * 实例化属性方法
     * 仅限自定义属性使用
     *
     * @param type Type
     * @param name String
     */
    public SXAttributeType(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 判断目标属性的类型是否相同
     *
     * @param obj SXAttributeType
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SXAttributeType && type.equals(((SXAttributeType) obj).type);
    }

    /**
     * 属性类型枚举
     */
    public enum Type {
        DAMAGE("DAMAGE"),
        DEFENCE("DEFENCE"),
        UPDATE("UPDATE"),
        OTHER("OTHER");

        @Getter
        String name;

        Type(String name) {
            this.name = name;
        }
    }
}
