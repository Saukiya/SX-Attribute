package github.saukiya.sxattribute.data.attribute.sub.attack;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.event.SXDamageEvent;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxitem.util.NMS;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.SpigotConfig;

import java.util.Arrays;
import java.util.List;

/**
 * 伤害
 * <p>
 * double[0] 伤害最小值
 * double[1] 伤害最大值
 * double[2] 伤害最小值 - PVP
 * double[3] 伤害最大值 - PVP
 * double[4] 伤害最小值 - PVE
 * double[5] 伤害最大值 - PVE
 *
 * @author Saukiya
 */
public class Damage extends SubAttribute{

    @Getter
    private static final int TYPE_DEFAULT = 0;
    @Getter
    private static final int TYPE_PVP = 1;
    @Getter
    private static final int TYPE_PVE = 2;

    public Damage() {
        super(SXAttribute.getInst(), 6, AttributeType.ATTACK, AttributeType.UPDATE);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Message.Holo", "&c&o伤害: &b&o{0}");
        config.set("Damage.DiscernName", "攻击力");
        config.set("Damage.CombatPower", 1);
        config.set("PVPDamage.DiscernName", "PVP攻击力");
        config.set("PVPDamage.CombatPower", 1);
        config.set("PVEDamage.DiscernName", "PVE攻击力");
        config.set("PVEDamage.CombatPower", 1);
        return config;
    }

    @EventHandler
    public void onSXDamageEvent(SXDamageEvent event) {
        DamageData damageData = event.getData();
        if (!damageData.isCancelled() && !damageData.isCrit()) {
            damageData.sendHolo(getString("Message.Holo", getDf().format(damageData.getEvent().getFinalDamage())));
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof DamageData) {
            DamageData damageData = (DamageData) eventData;
            LivingEntity attackEntity = damageData.getAttacker();
            EntityDamageByEntityEvent event = damageData.getEvent();

            if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                EntityEquipment eq = attackEntity.getEquipment();
                ItemStack mainHand = NMS.compareTo(1, 9, 0) >= 0 ? eq.getItemInMainHand() : eq.getItemInHand();
                if (mainHand != null) {
                    if (Material.BOW.equals(mainHand.getType()) && !Config.isBowCloseRangeAttack()) {
                        SXAttributeData sxAttributeData = SXAttribute.getApi().loadItemData(attackEntity, new PreLoadItem(EquipmentType.MAIN_HAND, mainHand));
                        if (NMS.compareTo(1, 9, 0) >= 0) {
                            damageData.setDamage(event.getDamage() - (values[0] / event.getDamage() * sxAttributeData.getValues(getClass().getSimpleName())[0]));
                        }
                        values = damageData.getAttackerData().take(sxAttributeData).getValues(getClass().getSimpleName());
                    }
                }
            }

//            System.out.println("Debug: " + Arrays.toString(values));
//            damageData.addDamage(((!Config.isDamageGauges() || event.getDamager() instanceof Projectile) || !(event.getDamager() instanceof Player)) || NMS.compareTo(1,9,0) < 0 ? getAttribute(values, TYPE_DEFAULT) : getAttribute(values, TYPE_DEFAULT) - values[0]);
            if (((!Config.isDamageGauges() || damageData.isFromAPI()) || event.getDamager() instanceof Projectile) || !(event.getDamager() instanceof Player) || NMS.compareTo(1, 9, 0) < 0) {
                damageData.addDamage(getAttribute(values, TYPE_DEFAULT));
            } else {
                damageData.addDamage(getAttribute(values, TYPE_DEFAULT) - values[0]);
            }
            damageData.addDamage(getAttribute(values, event.getEntity() instanceof Player ? TYPE_PVE : TYPE_PVP));
            // 如果该事件更新事件，并且更新目标为玩家
        } else if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player && NMS.compareTo(1, 9, 0) >= 0) {
            ((UpdateData) eventData).getEntity().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(Config.isDamageGauges() ? values[0] : values[1] == 0D ? 1 : 0.01);
        }
    }

    @Override
    public Object getPlaceholder(double[] values, LivingEntity player, String string) {
        switch (string) {
            case "MinDamage":
                return values[0];
            case "MaxDamage":
                return values[1];
            case "Damage":
                return values[0] == values[1] ? values[0] : (getDf().format(values[0]) + " - " + getDf().format(values[1]));
            case "PvpMinDamage":
                return values[2];
            case "PvpMaxDamage":
                return values[3];
            case "PvpDamage":
                return values[2] == values[3] ? values[2] : (getDf().format(values[2]) + " - " + getDf().format(values[3]));
            case "PveMinDamage":
                return values[4];
            case "PveMaxDamage":
                return values[5];
            case "PveDamage":
                return values[4] == values[5] ? values[4] : (getDf().format(values[4]) + " - " + getDf().format(values[5]));
            default:
                return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "MinDamage",
                "MaxDamage",
                "Damage",
                "PvpMinDamage",
                "PvpMaxDamage",
                "PvpDamage",
                "PveMinDamage",
                "PveMaxDamage",
                "PveDamage"
        );
    }

    private double getAttribute(double[] values, int type) {
        return values[type * 2] + SXAttribute.getRandom().nextDouble() * (values[type * 2 + 1] - values[type * 2]);
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        String[] loreSplit = lore.split("-");
        if (lore.contains(getString("PVEDamage.DiscernName"))) {
            values[4] += getNumber(loreSplit[0]);
            values[5] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);
        } else if (lore.contains(getString("PVPDamage.DiscernName"))) {
            values[2] += getNumber(loreSplit[0]);
            values[3] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);
        } else if (lore.contains(getString("Damage.DiscernName"))) {
            values[0] += getNumber(loreSplit[0]);
            values[1] += getNumber(loreSplit[loreSplit.length > 1 ? 1 : 0]);
        }
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.min(Math.max(values[0], Config.isDamageGauges() ? 1 : 0), SpigotConfig.attackDamage);
        values[1] = Math.min(values[1], SpigotConfig.attackDamage);
        values[1] = Math.max(values[1], values[0]);
        values[2] = Math.max(values[2], 0);
        values[3] = Math.max(values[3], values[2]);
        values[4] = Math.max(values[4], 0);
        values[5] = Math.max(values[5], values[4]);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        double value = (values[0] + values[1]) / 2 * getConfig().getInt("Damage.CombatPower");
        value += (values[2] + values[3]) / 2 * getConfig().getInt("PVPDamage.CombatPower");
        value += (values[4] + values[5]) / 2 * getConfig().getInt("PVEDamage.CombatPower");
        return value;
    }
}
