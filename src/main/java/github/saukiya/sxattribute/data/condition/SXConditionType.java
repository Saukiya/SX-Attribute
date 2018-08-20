package github.saukiya.sxattribute.data.condition;

import lombok.Getter;

/**
 * @author Saukiya
 */
public class SXConditionType {
    public static final SXConditionType RPG_INVENTORY = new SXConditionType(Type.RPG_INVENTORY);
    public static final SXConditionType EQUIPMENT = new SXConditionType(Type.EQUIPMENT);
    public static final SXConditionType HAND = new SXConditionType(Type.HAND);
    public static final SXConditionType MAIN_HAND = new SXConditionType(Type.HAND, "MainHand");
    public static final SXConditionType OFF_HAND = new SXConditionType(Type.HAND, "OffHand");
    public static final SXConditionType SLOT = new SXConditionType(Type.SLOT);
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SXConditionType && type.equals(((SXConditionType) obj).type);
    }

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
