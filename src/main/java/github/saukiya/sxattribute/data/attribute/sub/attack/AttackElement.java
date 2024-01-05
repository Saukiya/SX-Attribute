package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.util.CalculatorUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 元素伤害/元素防御 最多40个
 */
public class AttackElement extends SubAttribute {

    @Getter
    private HashMap<String, ElementData> dataHashMap = new HashMap<>();

    public AttackElement() {
        super(SXAttribute.getInst(), 40, AttributeType.ATTACK);
    }

    /**
     * 属性名:
     * Type: 属性类型 Attack/Defence
     * DiscernName: 属性识别名
     * CombatPower: 战斗力
     * AttackFormula: 伤害公式
     */
    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("火属性.Type", "Attack");
        config.set("火属性.DiscernName", "火攻击");
        config.set("火属性.Group", "火元素");
        config.set("火属性.CombatPower", 1);
        config.set("火属性.AttackFormula", "(<a:火攻击> * 1.5) - <d:火防御>");
        config.set("火属性.Info", "可以利用公式定义伤害 a是攻击者 d是防御者 只可以是本文件内的属性");

        // 火防御
        config.set("火防御.Type", "Other");
        config.set("火防御.DiscernName", "火防御");
        config.set("火防御.Group", "火元素");
        config.set("火防御.CombatPower", 1);
        config.set("火防御.AttackFormula", "0");
        config.set("火防御.Info", "Other类型是占位属性用于其他元素属性进行判断");

        // 火抗性
        config.set("火抗性.Type", "Defence");
        config.set("火抗性.DiscernName", "火抗性");
        config.set("火抗性.Group", "火元素");
        config.set("火抗性.CombatPower", 1);
        config.set("火抗性.AttackFormula", "<d:火抗性> * 0.5");
        config.set("火抗性.Info", "会减少攻击方造成的攻击 damage:Group是某个组已经计算的伤害 damage 是所有Group的伤害");

        // 风属性
        config.set("风属性.Type", "Attack");
        config.set("风属性.DiscernName", "风攻击");
        config.set("风属性.Group", "风元素");
        config.set("风属性.CombatPower", 1);
        config.set("风属性.AttackFormula", "<a:风攻击>");
        config.set("风属性.Info", "也可以不走公式直接运行");
        //风防御
        config.set("风防御.Type", "Defence");
        config.set("风防御.DiscernName", "风防御");
        config.set("风防御.Group", "风元素");
        config.set("风防御.CombatPower", 1);
        config.set("风防御.AttackFormula", "<d:风防御>");
        config.set("风防御.Info", "会减少攻击方造成的攻击");
        return config;
    }

    @Override
    public void onEnable() {
        int index = 0;
        for (String key : getConfig().getKeys(false)) {
            String type = getConfig().getString(key + ".Type");
            String discernName = getConfig().getString(key + ".DiscernName");
            int combatPower = getConfig().getInt(key + ".CombatPower");
            String attackFormula = getConfig().getString(key + ".AttackFormula");
            String group = getConfig().getString(key + ".Group");
            dataHashMap.put(discernName, new ElementData(type, group, discernName, combatPower, attackFormula, new int[]{index, index + 1}));
            index += 2;
            SXAttribute.getInst().getLogger().info("Attribute >>  [AttackElement] LoadSubElementAttribute " + discernName + " (" + type + ":" + group + ")");
        }
        setLength(dataHashMap.size() * 2);
    }

    @Override
    public void onReLoad() {
        onEnable();
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        for (ElementData data : dataHashMap.values()) {
            if (lore.contains(data.discernName)) {
                String[] loreSplit = lore.split("-");
                values[data.index[0]] += getNumber(loreSplit[0]);
                values[data.index[1]] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);
            }
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            for (ElementData data : dataHashMap.values()) {
                if (data.type.equals("Attack")) {
                    damageData.addDamage(getValue(data.attackFormula, damageData, data), data.group);
                }
                if (data.type.equals("Defence")) {
                    damageData.takeDamage(getValue(data.attackFormula, damageData, data), data.group);
                }
            }
        }
    }

    private double getValue(String formatString, DamageData damageData, ElementData data) {
        // 解析 <a:攻击者属性> <d:防御者属性>
        String baseString = formatString;
        Map<String, List<String>> map = convertStringToMap(formatString);
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            switch (key) {
                case "a": {
                    double[] attackElements = damageData.getAttackerData().getValues(this);
                    for (String s : value) {
                        String randomValue = "0";
                        if (dataHashMap.containsKey(s)) {
                            ElementData elementData = dataHashMap.get(s);
                            randomValue = String.valueOf(getRandomValue(attackElements[elementData.index[0]], attackElements[elementData.index[1]]));
                        }
                        baseString = baseString.replace("<a:" + s + ">", randomValue);
                    }
                    break;
                }
                case "d": {
                    double[] attackElements = damageData.getDefenderData().getValues(this);
                    for (String s : value) {
                        String randomValue = "0";
                        if (dataHashMap.containsKey(s)) {
                            ElementData elementData = dataHashMap.get(s);
                            randomValue = String.valueOf(getRandomValue(attackElements[elementData.index[0]], attackElements[elementData.index[1]]));
                        }
                        baseString = baseString.replace("<d:" + s + ">", randomValue);
                    }
                    break;
                }
            }
        }
        baseString = baseString.replace("<damage>", String.valueOf(damageData.getDamage()));
        for (String s : damageData.getDamages().keySet()) {
            baseString = baseString.replace("<damage:" + s + ">", String.valueOf(damageData.getDamages().get(s)));
        }
        // 计算公式
        try {
            return CalculatorUtil.getResult(baseString).doubleValue();
        } catch (Exception e) {
            return 0;
        }

    }

    private double getRandomValue(double valueA, double valueB) {
        return valueA + SXAttribute.getRandom().nextDouble() * (valueA - valueB);
    }

    public static Map<String, List<String>> convertStringToMap(String text) {
        Pattern pattern = Pattern.compile("<([ad]):([^>]+)>"); // 正则表达式匹配 {d:攻击力} 或 {t:防御力}
        Matcher matcher = pattern.matcher(text);
        Map<String, List<String>> map = new HashMap<>();
        while (matcher.find()) {
            String key = matcher.group(1); // 提取匹配结果中的 d 或 t
            String value = matcher.group(2); // 提取匹配结果中的 攻击力 或 防御力

            List<String> list = map.getOrDefault(key, new ArrayList<>()); // 获取 key 对应的列表，如果不存在则创建一个新的列表
            list.add(value); // 将 value 添加到列表中
            map.put(key, list);
        }
        return map;
    }


    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        for (ElementData data : dataHashMap.values()) {
            if (string.equals(data.discernName)) {
                return values[data.index[0]] == values[data.index[1]] ? values[data.index[0]] : (getDf().format(values[data.index[0]]) + " - " + getDf().format(values[data.index[1]]));
            }
        }
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return new ArrayList<>(dataHashMap.keySet());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ElementData {
        // Attack / Defence
        private String type;
        // Group伤害组
        private String group;
        // 识别名
        private String discernName;
        // 战斗力
        private int combatPower;
        // 伤害公式
        private String attackFormula;
        // 索引
        private int[] index;
    }

}
