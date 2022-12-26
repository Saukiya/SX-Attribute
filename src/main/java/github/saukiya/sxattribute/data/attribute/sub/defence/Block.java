package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * 格挡
 *
 * @author Saukiya
 */
public class Block extends SubAttribute {

    /**
     * double[0] 格挡几率
     * double[1] 格挡比例
     */
    public Block() {
        super(SXAttribute.getInst(), 2, AttributeType.DEFENCE);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&2&o格挡: &b&o{0}");
        config.set("Message.Battle", "[ACTIONBAR]&c{0}&6 格挡了 &c{1}&6 的部分伤害!");
        config.set("BlockRate.DiscernName", "格挡几率");
        config.set("BlockRate.CombatPower", 1);
        config.set("Block.DiscernName", "格挡比例");
        config.set("Block.CombatPower", 1);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            if (probability(values[0])) {
                DamageData damageData = (DamageData) eventData;
                if (!(damageData.getEffectiveAttributeList().contains("Real") || damageData.getEffectiveAttributeList().contains("Reflection"))) {
                    damageData.getEffectiveAttributeList().add(this.getName());
                    double blockDamage = damageData.getDamage() * values[1] / 100;
                    damageData.setDamage(damageData.getDamage() - blockDamage);
                    damageData.sendHolo(getString("Message.Holo", getDf().format(blockDamage)));
                    send(damageData.getAttacker(), "Message.Battle", damageData.getDefenderName(), getFirstPerson(), getDf().format(blockDamage));
                    send(damageData.getDefender(), "Message.Battle", getFirstPerson(), damageData.getAttackerName(), getDf().format(blockDamage));
                }
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        switch (string) {
            case "BlockRate":
                return values[0];
            case "Block":
                return values[1];
        }
        return null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "BlockRate",
                "Block"
        );
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("BlockRate.DiscernName"))) {
            values[0] += getNumber(lore);
        }
        if (lore.contains(getString("Block.DiscernName"))) {
            values[1] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        super.correct(values);
        values[0] = Math.min(values[0], getConfig().getInt("BlockRate.UpperLimit", 100));
        values[1] = Math.min(values[0], getConfig().getInt("Block.UpperLimit", 100));
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * getConfig().getInt("BlockRate.CombatPower") + values[1] * getConfig().getInt("Block.CombatPower");
    }
}
