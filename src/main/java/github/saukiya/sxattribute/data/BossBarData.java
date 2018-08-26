package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Saukiya
 */
public class BossBarData {

    @Getter
    private BossBar bossBar;

    @Getter
    private LivingEntity entity;

    @Getter
    private double maxHealth;

    @Getter
    private String name;

    private int updateTick = Config.getConfig().getInt(Config.HEALTH_BOSS_BAR_DISPLAY_TIME);

    @Getter
    private Map<Player, Long> timeMap = new ConcurrentHashMap<>();

    public BossBarData(LivingEntity entity, String name, double maxHealth, double progress) {
        this.entity = entity;
        this.name = name;
        this.maxHealth = maxHealth;
        bossBar = Bukkit.createBossBar(MessageFormat.format(Config.getConfig().getString(Config.HEALTH_BOSS_BAR_FORMAT), name, SXAttribute.getDf().format(progress * maxHealth), SXAttribute.getDf().format(maxHealth)).replace("&", "§"), BarColor.GREEN, BarStyle.SEGMENTED_20);
    }

    public void updateTitle() {
        bossBar.setTitle(MessageFormat.format(Config.getConfig().getString(Config.HEALTH_BOSS_BAR_FORMAT), name, SXAttribute.getDf().format(bossBar.getProgress() * maxHealth), SXAttribute.getDf().format(maxHealth)).replace("&", "§"));
    }

    public void setProgress(double progress) {
        bossBar.setProgress(progress);
        bossBar.setColor(progress > 0.66 ? BarColor.GREEN : progress > 0.33 ? BarColor.YELLOW : BarColor.RED);
    }

    public BossBarData addPlayer(Player player) {
        bossBar.addPlayer(player);
        timeMap.put(player, System.currentTimeMillis() + (updateTick * 1000));
        return this;
    }
}
