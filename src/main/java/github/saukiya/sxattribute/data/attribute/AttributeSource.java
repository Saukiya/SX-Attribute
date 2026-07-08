package github.saukiya.sxattribute.data.attribute;

import lombok.Getter;

/**
 * 属性来源 (多属性源机制)
 * <p>
 * 一个实体的最终属性 = 若干独立命名源之和。每个源有唯一标识名 {@link #name}, 携带一份 {@link #data},
 * 可被单独添加/更新/移除而互不干扰(如 装备-主手 / 药水效果 / 力量加成 / class:插件 / 抛射物)。
 * <p>
 * {@link #silent} 为静态源标记: 增删时不触发 {@code SXAttributeSourceAddEvent/RemoveEvent}
 * (用于内部物品源、抛射物快照、以及 {@code createStaticAttributeSource} 的纯数值注入)。
 *
 * @author Ray_Hughes
 */
public class AttributeSource {

    @Getter
    private final String name;

    @Getter
    private final SXAttributeData data;

    @Getter
    private final boolean silent;

    public AttributeSource(String name, SXAttributeData data, boolean silent) {
        this.name = name;
        this.data = data;
        this.silent = silent;
    }
}
