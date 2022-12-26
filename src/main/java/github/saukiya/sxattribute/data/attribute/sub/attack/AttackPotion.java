package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 药水属性
 * <p>
 * PotionEffectType最大数量28个
 *
 * @author Saukiya
 */
public class AttackPotion extends SubAttribute {

    @Getter
    private PotionData[] dataList;

    public AttackPotion() {
        super(SXAttribute.getInst(), 28, AttributeType.ATTACK);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("NetworkLink", "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html");
        config.set("Message.Holo", "&o{0}: &b&o{1}s");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 对 &c{1}&6 造成了 &r{2}&6 效果!");

        config.set("List.Blindness.Type", "BLINDNESS");
        config.set("List.Blindness.DiscernName", "致盲几率");
        config.set("List.Blindness.CombatPower", 1);
        config.set("List.Blindness.MessageName", "&7致盲&7");
        config.set("List.Blindness.MinLevel", 1);
        config.set("List.Blindness.MaxLevel", 2);
        config.set("List.Blindness.MinTime", 2);
        config.set("List.Blindness.MaxTime", 5);

        config.set("List.Wither.Type", "WITHER");
        config.set("List.Wither.DiscernName", "凋零几率");
        config.set("List.Wither.CombatPower", 1);
        config.set("List.Wither.MessageName", "&9凋零&7");
        config.set("List.Wither.MinLevel", 1);
        config.set("List.Wither.MaxLevel", 2);
        config.set("List.Wither.MinTime", 2);
        config.set("List.Wither.MaxTime", 5);

        config.set("List.Slow.Type", "SLOW");
        config.set("List.Slow.DiscernName", "减速几率");
        config.set("List.Slow.CombatPower", 1);
        config.set("List.Slow.MessageName", "&3减速&7");
        config.set("List.Slow.MinLevel", 1);
        config.set("List.Slow.MaxLevel", 2);
        config.set("List.Slow.MinTime", 2);
        config.set("List.Slow.MaxTime", 5);

        config.set("List.Poison.Type", "POISON");
        config.set("List.Poison.DiscernName", "中毒几率");
        config.set("List.Poison.CombatPower", 1);
        config.set("List.Poison.MessageName", "&d中毒&7");
        config.set("List.Poison.MinLevel", 1);
        config.set("List.Poison.MaxLevel", 2);
        config.set("List.Poison.MinTime", 2);
        config.set("List.Poison.MaxTime", 5);

        config.set("List.Hunger1.Type", "HUNGER");
        config.set("List.Hunger1.DiscernName", "夺食几率");
        config.set("List.Hunger1.CombatPower", 1);
        config.set("List.Hunger1.UpperLimit", 50);
        config.set("List.Hunger1.MessageName", "&c饥饿&7");
        config.set("List.Hunger1.MinLevel", 4);
        config.set("List.Hunger1.MaxLevel", 5);
        config.set("List.Hunger1.MinTime", 2);
        config.set("List.Hunger1.MaxTime", 5);
        return config;
    }

    @Override
    public void onEnable() {
        List<PotionData> potionDataList = new ArrayList<>();
        for (String name : getConfig().getConfigurationSection("List").getKeys(false)) {
            String typeName = getConfig().getString("List." + name + ".Type");
            for (PotionEffectType type : PotionEffectType.values()) {
                if (type != null && type.getName().equals(typeName)) {
                    potionDataList.add(new PotionData(name, type));
                }
            }
        }
        dataList = potionDataList.toArray(new PotionData[0]);
        setLength(dataList.length);
    }

    @Override
    public void onReLoad() {
        for (String typeName : getConfig().getConfigurationSection("List").getKeys(false)) {
            for (PotionData potionData : dataList) {
                if (typeName.equals(potionData.type.getName())) {
                    potionData.load();
                    break;
                }
            }
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            double defenseToughness = damageData.getDefenderData().getValues("Toughness")[0];
            for (int i = 0; i < dataList.length; i++) {
                if (probability(values[i] - defenseToughness)) {
                    double time = dataList[i].getTime();
                    damageData.getDefender().addPotionEffect(new PotionEffect(dataList[i].type, (int) (time * 20), dataList[i].getLevel()));
                    damageData.sendHolo(getString("Message.Holo", dataList[i].messageName, getDf().format(time)));
                    send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(time), dataList[i].messageName);
                    send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(time), dataList[i].messageName);
                }
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        for (int i = 0; i < dataList.length; i++) {
            if (string.equals(dataList[i].name)) {
                return values[i];
            }
        }
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.stream(dataList).map(data -> data.name).collect(Collectors.toList());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        for (int i = 0; i < dataList.length; i++) {
            if (lore.contains(dataList[i].getDiscernName())) {
                values[i] += getNumber(lore);
                return;
            }
        }
    }

    @Override
    public void correct(double[] values) {
        for (int i = 0; i < dataList.length; i++) {
            values[i] = Math.min(Math.max(values[i], 0), dataList[i].getUpperLimit());
        }
    }

    @Override
    public double calculationCombatPower(double[] values) {
        double combatPower = 0D;
        for (int i = 0; i < dataList.length; i++) {
            combatPower += values[i] * dataList[i].getCombatPower();
        }
        return combatPower;
    }

    @Getter
    public class PotionData {

        private PotionEffectType type;

        private String name;
        private String discernName;
        private int combatPower;
        private int upperLimit;

        private String messageName;
        private int minLevel;
        private int maxLevel;
        private int minTime;
        private int maxTime;

        public PotionData(String name, PotionEffectType type) {
            this.name = name;
            this.type = type;
            load();
        }

        public void load() {
            this.discernName = getString("List." + name + ".DiscernName");
            this.combatPower = getConfig().getInt("List." + name + ".CombatPower", 1);
            this.upperLimit = getConfig().getInt("List." + name + ".UpperLimit", 100);
            this.messageName = getString("List." + name + ".MessageName");
            this.minLevel = getConfig().getInt("List." + name + ".MinLevel", 1);
            this.maxLevel = getConfig().getInt("List." + name + ".MaxLevel", 2);
            this.minTime = getConfig().getInt("List." + name + ".MinTime", 2);
            this.maxTime = getConfig().getInt("List." + name + ".MaxTime", 5);
        }

        public int getLevel() {
            return SXAttribute.getRandom().nextInt(maxLevel - minLevel + 1) + minLevel;
        }

        public double getTime() {
            return SXAttribute.getRandom().nextDouble() * (maxTime - minTime) + minTime;
        }
    }
}
