package github.saukiya.sxattribute.data.attribute;

import lombok.Getter;

/**
 * @author Saukiya
 */
public class SXAttributeType {

    public static final SXAttributeType DAMAGE = new SXAttributeType(Type.DAMAGE);
    public static final SXAttributeType DEFENCE = new SXAttributeType(Type.DEFENCE);
    public static final SXAttributeType UPDATE = new SXAttributeType(Type.UPDATE);
    public static final SXAttributeType OTHER = new SXAttributeType(Type.OTHER);

    @Getter
    private String name;
    @Getter
    private Type type;

    public SXAttributeType(Type type) {
        this.type = type;
        this.name = type.getName();
    }

    public SXAttributeType(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SXAttributeType && type.equals(((SXAttributeType) obj).type);
    }

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
