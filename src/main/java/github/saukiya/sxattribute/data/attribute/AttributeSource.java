package github.saukiya.sxattribute.data.attribute;

import lombok.Getter;

/**
 * 属性来源 (多属性源机制)
 * <p>
 * 一个实体的最终属性 = 若干参与计算的独立命名源之和。每个源有唯一标识名 {@link #name}, 携带一份
 * {@link #data}, 可被单独添加/更新/移除而互不干扰(如 装备-主手 / 药水效果 / 力量加成 / class:插件 / 抛射物)。
 * 已由 Bukkit 施加的外部数值也可以登记为不参与求和的诊断来源。
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

    /**
     * 是否参与实体 SX 属性求和。
     * <p>
     * 外部插件已经写入 Bukkit 属性的数值只能作为诊断来源展示；若再次并入 SX 计算，
     * 每次刷新都会重复叠加。因此这类来源必须设为 {@code false}。
     */
    @Getter
    private final boolean contributesToTotal;

    public AttributeSource(String name, SXAttributeData data, boolean silent) {
        this(name, data, silent, true);
    }

    /**
     * 创建属性来源。
     *
     * @param name               稳定的来源名称，同名来源会被覆盖
     * @param data               用于计算或展示的属性数据
     * @param silent             是否跳过来源增删事件
     * @param contributesToTotal 是否参与实体 SX 属性求和
     */
    public AttributeSource(String name, SXAttributeData data, boolean silent, boolean contributesToTotal) {
        this.name = name;
        this.data = data;
        this.silent = silent;
        this.contributesToTotal = contributesToTotal;
    }
}
