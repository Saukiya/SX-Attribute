package github.saukiya.sxattribute.data.attribute.sub.defence;

import com.sucy.skill.SkillAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.util.AttributeUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 生命 - 当前不更新怪物生命
 *
 * @author Saukiya
 */
public class Health extends SubAttribute {

    /**
     * SX 最大生命修饰符协议 UUID。
     * <p>
     * 所有刷新都先移除该 UUID 再写入新值，避免重复叠加；不得修改此值，否则升级后旧修饰符会残留。
     */
    private static final UUID SX_HEALTH_MODIFIER_ID = UUID.fromString("ef3a83e2-6a7b-4ced-939d-b73f0afab11f");

    /** SkillAPI 的职业生命由 SX 公式显式合并，仅作为只读来源展示。 */
    private static final String SKILL_API_SOURCE = "SkillAPI-生命";

    /** Bukkit 最终生命中除 SX 与 SkillAPI 显式贡献外的部分，仅作为只读来源展示。 */
    private static final String EXTERNAL_HEALTH_SOURCE = "外部插件-生命";

    private boolean skillAPI = false;

    private boolean healthScaled = false;

    private int healthScaledValue = 40;

    /**
     * 生命
     * double[0] 生命值
     */
    public Health() {
        super(SXAttribute.getInst(), 1, AttributeType.UPDATE);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Health.DiscernName", "生命上限");
        config.set("Health.CombatPower", 1);
        config.set("HealthScaled.Enabled", true);
        config.set("HealthScaled.Value", 40);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();
            int skillAPIHealth = getSkillAPIHealth(player);
            // 生命上限公式 (变量 value=生命上限词条, skillapi=SkillAPI 附加生命); 默认 词条+SkillAPI
            double maxHealth = formula(player, "Formula", values[0] + skillAPIHealth, "value", values[0], "skillapi", skillAPIHealth);
            if (SXAttribute.isHigherVersion()) {
                AttributeInstance instance = AttributeUtil.getInstance(player, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
                if (instance != null) {
                    applyHealthModifier(instance, maxHealth);
                    updateInformationalSource(player, SKILL_API_SOURCE, skillAPIHealth);
                    updateInformationalSource(player, EXTERNAL_HEALTH_SOURCE, instance.getValue() - maxHealth);
                } else {
                    player.setMaxHealth(maxHealth);
                }
            } else {
                player.setMaxHealth(maxHealth);
            }
            // 必须在 Bukkit 合并外部 base/modifier 后再截断当前生命，否则会把合法的外部生命误降到 SX 值。
            double effectiveMaxHealth = SXAttribute.getApi().getMaxHealth(player);
            if (player.getHealth() > effectiveMaxHealth) player.setHealth(effectiveMaxHealth);
            if (healthScaled && healthScaledValue < SXAttribute.getApi().getMaxHealth(player)) {
                player.setHealthScaled(true);
                player.setHealthScale(healthScaledValue);
            } else {
                player.setHealthScaled(false);
            }
        }
    }

    /**
     * 只维护 SX 自己相对原版默认值的固定增量，不占用 Bukkit base value。
     * AuraSkills 等插件无论修改基础值还是注册 AttributeModifier，都能继续参与最终生命计算。
     */
    private void applyHealthModifier(AttributeInstance instance, double sxMaxHealth) {
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (SX_HEALTH_MODIFIER_ID.equals(modifier.getUniqueId())) {
                instance.removeModifier(modifier);
                break;
            }
        }
        double amount = sxMaxHealth - instance.getDefaultValue();
        if (Math.abs(amount) > 0.000001D) {
            instance.addModifier(new AttributeModifier(SX_HEALTH_MODIFIER_ID, "SX-Attribute max health", amount,
                    AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    /**
     * 更新只读诊断来源。该来源不参与 SX 求和，只让 source/statssource 能解释最终生命差额。
     */
    private void updateInformationalSource(Player player, String sourceName, double health) {
        if (Math.abs(health) <= 0.000001D) {
            SXAttribute.getAttributeManager().removeSource(player.getUniqueId(), sourceName);
            return;
        }
        SXAttributeData data = new SXAttributeData();
        data.getValues(this)[0] = health;
        SXAttribute.getAttributeManager().putSource(player.getUniqueId(), new AttributeSource(sourceName, data, true, false));
    }

    private int getSkillAPIHealth(Player player) {
        return skillAPI ? SkillAPI.getPlayerData(player).getClasses().stream().mapToInt(aClass -> (int) aClass.getHealth()).sum() : 0;
    }

    @Override
    public void onEnable() {
        healthScaled = config().getBoolean("HealthScaled.Enabled");
        healthScaledValue = config().getInt("HealthScaled.Value", 40);
        skillAPI = Bukkit.getPluginManager().getPlugin("SkillAPI") != null;
    }

    @Override
    public void onReLoad() {
        healthScaled = config().getBoolean("HealthScaled.Enabled");
        healthScaledValue = config().getInt("HealthScaled.Value", 40);
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        switch (string) {
            case "MaxHealth":
                return SXAttribute.getApi().getMaxHealth(player);
            case "Health":
                return player.getHealth();
            case "HealthValue":
                return values[0];
            default:
                return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "MaxHealth",
                "Health",
                "HealthValue"
        );
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("Health.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.max(values[0], 1D);
        values[0] = Math.min(values[0], SpigotConfig.maxHealth);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Health.CombatPower");
    }
}
