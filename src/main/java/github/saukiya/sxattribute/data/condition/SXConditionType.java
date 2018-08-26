package github.saukiya.sxattribute.data.condition;

import lombok.Getter;

/**
 * @author Saukiya
 */
public class SXConditionType {
    /**
     * RPGInventory区域判断
     */
    public static final SXConditionType RPG_INVENTORY = new SXConditionType(Type.RPG_INVENTORY);
    /**
     * 装备区域判断
     */
    public static final SXConditionType EQUIPMENT = new SXConditionType(Type.EQUIPMENT);
    /**
     * 手持区域判断
     */
    public static final SXConditionType HAND = new SXConditionType(Type.HAND);
    /**
     * 主手区域判断
     */
    public static final SXConditionType MAIN_HAND = new SXConditionType(Type.HAND, "MainHand");
    /**
     * 副手区域判断
     */
    public static final SXConditionType OFF_HAND = new SXConditionType(Type.HAND, "OffHand");
    /**
     * 自定义槽位区域判断
     */
    public static final SXConditionType SLOT = new SXConditionType(Type.SLOT);
    /**
     * 完全判断 代表判断每个区域时都会判断该类型条件
     */
    public static final SXConditionType ALL = new SXConditionType(Type.ALL);

    @Getter
    private final String name;

    @Getter
    private final Type type;

    public SXConditionType(Type type) {
        this.type = type;
        this.name = type.getName();
    }

    public SXConditionType(Type type, String name) {
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
        return obj instanceof SXConditionType && type.equals(((SXConditionType) obj).type);
    }

    /**
     * 条件类型枚举
     */
    public enum Type {
        RPG_INVENTORY("RpgInventory"),
        EQUIPMENT("Equipment"),
        HAND("Hand"),
        SLOT("Slot"),
        OTHER("Other"),
        ALL("All");

        @Getter
        String name;

        Type(String name) {
            this.name = name;
        }
    }
}
