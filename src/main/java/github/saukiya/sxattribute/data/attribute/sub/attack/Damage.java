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
import github.saukiya.sxattribute.util.AttributeUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
public class Damage extends SubAttribute implements Listener {

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

    @EventHandler(priority = EventPriority.LOW)
    public void onSXDamageEvent(SXDamageEvent event) {
        DamageData damageData = event.getData();
        if (!damageData.isCancelled() && !damageData.isCrit()) {
            if (isMessageEnabled()) damageData.sendHolo(getString("Message.Holo", getDf().format(damageData.getEvent().getFinalDamage())));
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
                ItemStack mainHand = SXAttribute.isHigherVersion() ? eq.getItemInMainHand() : eq.getItemInHand();
                if (mainHand != null) {
                    if (Material.BOW.equals(mainHand.getType()) && !Config.isBowCloseRangeAttack()) {
                        SXAttributeData sxAttributeData = SXAttribute.getApi().loadItemData(attackEntity, new PreLoadItem(EquipmentType.MAIN_HAND, mainHand));

                        if (SXAttribute.isHigherVersion()) {
                            damageData.setDamage(event.getDamage() - (values[0] / event.getDamage() * sxAttributeData.getValues(getClass().getSimpleName())[0]));
                        }
                        values = damageData.getAttackerData().take(sxAttributeData).getValues(getClass().getSimpleName());
                    }
                }
            }


            double defaultDamage = getAttribute(values, TYPE_DEFAULT);
            if (usesVanillaAttackDamage(event)) {
                // 高版本玩家近战已由原版攻击属性贡献一部分伤害，因此这里只补足 SX 掷点结果。
                // 原版会按 Spigot 上限裁剪属性基值，扣减时必须使用实际写入值，否则超上限部分会被误抵消。
                defaultDamage -= getVanillaAttackDamage(values[0]);
            }
            damageData.addDamage(defaultDamage);

            damageData.addDamage(getAttribute(values, event.getEntity() instanceof Player ? TYPE_PVP : TYPE_PVE));
            // 如果该事件更新事件，并且更新目标为玩家
        } else if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player && SXAttribute.isHigherVersion()) {
            AttributeInstance instance = AttributeUtil.getInstance(((UpdateData) eventData).getEntity(), "ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");
            if (instance != null) {
                instance.setBaseValue(Config.isDamageGauges() ? getVanillaAttackDamage(values[0]) : values[1] == 0D ? 1 : 0.01);
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
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
        double min = values[type * 2];
        double max = values[type * 2 + 1];
        double fallback = min + SXAttribute.getRandom().nextDouble() * (max - min);
        // 伤害掷点公式 (变量 min/max); 默认 min~max 均匀随机, 可用 <d:0_1> 表达随机
        return formula((Player) null, "Formula", fallback, "min", min, "max", max);
    }

    /**
     * 判断当前事件是否已经包含玩家的原版攻击属性伤害。
     * 投射物、非玩家攻击与旧版本不会经过同一套原版属性同步流程，不能从 SX 伤害中扣减基值。
     */
    private boolean usesVanillaAttackDamage(EntityDamageByEntityEvent event) {
        return Config.isDamageGauges()
                && !(event.getDamager() instanceof Projectile)
                && event.getDamager() instanceof Player
                && SXAttribute.isHigherVersion();
    }

    /**
     * 获取能够同步给原版 {@code generic.attack_damage} 的实际基值。
     * SX 的逻辑伤害允许超过该上限，超出的部分会在伤害事件中作为补差加入。
     */
    private double getVanillaAttackDamage(double damage) {
        return Math.min(damage, SpigotConfig.attackDamage);
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
        // Spigot 的上限只约束原版属性基值，不能裁剪 SX 在伤害事件中独立计算的逻辑伤害。
        values[0] = Math.max(values[0], Config.isDamageGauges() ? 1 : 0);
        values[1] = Math.max(values[1], values[0]);
        values[2] = Math.max(values[2], 0);
        values[3] = Math.max(values[3], values[2]);
        values[4] = Math.max(values[4], 0);
        values[5] = Math.max(values[5], values[4]);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        double value = (values[0] + values[1]) / 2 * config().getInt("Damage.CombatPower");
        value += (values[2] + values[3]) / 2 * config().getInt("PVPDamage.CombatPower");
        value += (values[4] + values[5]) / 2 * config().getInt("PVEDamage.CombatPower");
        return value;
    }
}
