package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import github.saukiya.sxattribute.util.FoliaScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 生命恢复
 *
 * @author Saukiya
 */
public class HealthRegen extends SubAttribute {

    /** Bukkit/Folia 返回的真实任务句柄；不能直接取消尚未调度的 BukkitRunnable。 */
    private Object scheduledTask;

    private final Runnable runnable = () -> {
        try {
            for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                if (player != null && !player.isDead() && player.isOnline()) {
                    double maxHealth = SXAttribute.getApi().getMaxHealth(player);
                    if (player.getHealth() < maxHealth) {
                        double healthRegen = SXAttribute.getApi().getEntityData(player).getValues(getName())[0];
                        if (healthRegen > 0) {
                            // 生命恢复量公式 (变量 value=生命恢复); 默认恒等
                            healthRegen = formula(player, "Formula", healthRegen, "value", healthRegen);
                            EntityRegainHealthEvent event = new EntityRegainHealthEvent(player, healthRegen, EntityRegainHealthEvent.RegainReason.CUSTOM);
                            Bukkit.getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                healthRegen = (event.getAmount() + player.getHealth()) > maxHealth ? (maxHealth - player.getHealth()) : event.getAmount();
                                player.setHealth(healthRegen + player.getHealth());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SXAttribute.getInst().getLogger().warning("生命恢复系统崩溃 正在重新启动!");
            HealthRegen.this.onEnable();
            SXAttribute.getInst().getLogger().warning("启动完毕!");
            SXAttribute.getInst().getLogger().warning("如果此消息连续刷屏，请反馈作者QQ: 1940208750");
        }
    };


    /**
     * double[0] 生命回复
     */
    public HealthRegen() {
        super(SXAttribute.getInst(), 1, AttributeType.OTHER);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Health.DiscernName", "生命恢复");
        config.set("Health.CombatPower", 1);
        config.set("HealthScaled.Enabled", true);
        config.set("HealthScaled.Value", 40);
        return config;
    }

    /** 启动或重启生命恢复周期；启动前先取消旧句柄，保证同一属性只存在一个任务。 */
    @Override
    public void onEnable() {
        onDisable();
        scheduledTask = FoliaScheduler.runTimer(getPlugin(), runnable, 19, 20);
    }

    /** 通过统一调度器取消 Bukkit/Folia 任务；未启动或重复关闭时保持幂等。 */
    @Override
    public void onDisable() {
        FoliaScheduler.cancel(scheduledTask);
        scheduledTask = null;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("Health.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Health.CombatPower");
    }
}
