package github.saukiya.sxattribute.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.util.nms.NMS;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
public class ListenerHealthChange extends BukkitRunnable implements Listener {

    @Getter
    private List<BossBarData> bossList = new ArrayList<>();

    @Getter
    private List<NameData> nameList = new ArrayList<>();

    @Getter
    private List<HoloData> holoList = new ArrayList<>();

    public ListenerHealthChange() {
        runTaskTimer(SXAttribute.getInst(), 20, 20);
    }

    @Override
    public void run() {
        for (int i = bossList.size() - 1; i >= 0; i--) {
            BossBarData bossBarData = bossList.get(i);
            if (bossBarData.getEntity() == null || bossBarData.getEntity().isDead()) {
                bossBarData.getBossBar().removeAll();
                bossList.remove(i);
                continue;
            }
            if (bossBarData.clearExpired().getList().size() == 0) {
                bossList.remove(i);
            }
        }
        for (int i = nameList.size() - 1; i >= 0; i--) {
            NameData nameData = nameList.get(i);
            if (nameData.getEntity() == null || nameData.getEntity().isDead() || nameData.getEntity().getHealth() == SXAttribute.getApi().getMaxHealth(nameData.getEntity()) || nameData.getClearTime() < System.currentTimeMillis()) {
                if (nameData.getEntity() != null) {
                    nameData.getEntity().setCustomName(nameData.getName());
                    nameData.getEntity().setCustomNameVisible(nameData.isVisible());
                }
                nameList.remove(i);
            }
        }
        for (int i = holoList.size() - 1; i >= 0; i--) {
            if (holoList.get(i).clearTime < System.currentTimeMillis()) {
                holoList.remove(i).getHologram().delete();
            }
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();
        for (BossBarData bossBarData : getBossList()) {
            bossBarData.getBossBar().removeAll();
        }
        for (NameData nameData : getNameList()) {
            if (nameData.getEntity() != null && !nameData.getEntity().isDead()) {
                nameData.getEntity().setCustomName(nameData.getName());
                nameData.getEntity().setCustomNameVisible(nameData.isVisible());
            }
        }
        for (HoloData data : getHoloList()) {
            data.getHologram().delete();
        }
    }

    public String getEntityName(LivingEntity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).getDisplayName();
        }
        String entityName = entity.getName();
        for (NameData nameData : nameList) {
            if (entity.equals(nameData.getEntity())) {
                if (nameData.getName() != null) {
                    entityName = replaceName(nameData.getName());
                } else {
                    String tempName = entityName;
                    entity.setCustomName(null);
                    entityName = replaceName(entity.getName());
                    entity.setCustomName(tempName);
                }
                return entityName;
            }
        }
        return replaceName(entityName);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void OnEntityDamageEvent(EntityDamageEvent event) {
        if (event.isCancelled() || event.getFinalDamage() == 0) return;
        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        LivingEntity damager = null;
        String name = getEntityName(entity);

        double maxHealth = Math.max(SXAttribute.getApi().getMaxHealth(entity), 0);
        double health = Math.min(entity.getHealth() - event.getFinalDamage(), maxHealth);
        double progress = health / maxHealth;
        BossBarData bossBarData = null;
        for (BossBarData barData : bossList) {
            if (entity.equals(barData.getEntity())) {
                bossBarData = barData;
                bossBarData.setProgress(progress);
                break;
            }
        }

        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
            if (event2.getDamager() instanceof LivingEntity) {
                damager = (LivingEntity) event2.getDamager();
            } else if (event2.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event2.getDamager();
                if (projectile.getShooter() instanceof LivingEntity) {
                    damager = (LivingEntity) projectile.getShooter();
                }
            }
            if (damager != null) {
                // BossBar
                if (Config.isHealthBossBar() && damager instanceof Player && NMS.compareTo(1,9,0) >= 0 && !Config.getBossBarBlackCauseList().contains(event.getCause().name()) && !isMythicBossBar(entity)) {
                    if (bossBarData == null) {
                        bossBarData = new BossBarData(entity, name, maxHealth, progress);
                        bossBarData.setProgress(progress);
                    }
                    bossBarData.addPlayer((Player) damager);
                }
            }
        }

        if (damager == null && Config.isHolographic() && Config.isHolographicHealthTake() && SXAttribute.isHolographic() && !Config.getHolographicBlackList().contains(event.getCause().name())) {
            Location loc = entity.getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() / 2, 0);
            loc.setYaw(entity.getLocation().getYaw() - 90);
            loc.add(loc.getDirection().multiply(0.8D));
            HoloData holoData = new HoloData(HologramsAPI.createHologram(SXAttribute.getInst(), loc));
            holoData.getHologram().appendTextLine(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__HURT, event.getFinalDamage()));

        }

        if (Config.isHealthNameVisible()) {
            NameData nameData = null;
            for (NameData data : nameList) {
                if (entity.equals(data.getEntity())) {
                    nameData = data;
                    break;
                }
            }

            if (health <= 0) {
                for (NameData data : nameList) {
                    if (entity.equals(data.getEntity()) || (damager != null && damager.equals(data.getEntity()))) {
                        data.getEntity().setCustomName(data.getName());
                        data.getEntity().setCustomNameVisible(data.isVisible());
                        data.setEntity(null);
                    }
                }
            } else if ((damager instanceof Player || nameData != null) && !(entity instanceof Player)) {
                if (nameData == null) {
                    nameData = new NameData(entity, entity.getCustomName(), entity.isCustomNameVisible());
                    nameData.updateTick();
                    nameList.add(nameData);
                } else if (damager instanceof Player) {
                    nameData.updateTick();
                }
                entity.setCustomName(MessageFormat.format(getHealthProgressName(progress), SXAttribute.getDf().format(health), SXAttribute.getDf().format(maxHealth)));
                entity.setCustomNameVisible(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void OnEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (event.isCancelled() || event.getAmount() == 0 || !(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand)
            return;
        LivingEntity entity = (LivingEntity) event.getEntity();
        double maxHealth = Math.max(SXAttribute.getApi().getMaxHealth(entity), 0);
        double health = Math.min(entity.getHealth() + event.getAmount(), maxHealth);
        double progress = health / maxHealth;

        for (BossBarData bossBarData : bossList) {
            if (entity.equals(bossBarData.getEntity())) {
                if (progress == 1D) {
                    bossBarData.getBossBar().removeAll();
                } else {
                    bossBarData.setProgress(progress);
                }
                break;
            }
        }

        if (Config.isHolographic() && Config.isHolographicHealthTake() && SXAttribute.isHolographic()) {
            Location loc = entity.getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() * 1.5, 0);
            loc.setYaw(entity.getLocation().getYaw() + 90);
            loc.add(loc.getDirection().multiply(0.8D));
            HoloData holoData = new HoloData(HologramsAPI.createHologram(SXAttribute.getInst(), loc));
            holoData.getHologram().appendTextLine(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__HEALTH, SXAttribute.getDf().format(event.getAmount())));
        }
        for (NameData data : nameList) {
            if (entity.equals(data.getEntity())) {
                if (progress == 1D) {
                    data.getEntity().setCustomName(data.getName());
                    data.getEntity().setCustomNameVisible(data.isVisible());
                    data.setEntity(null);
                } else {
                    entity.setCustomName(MessageFormat.format(getHealthProgressName(progress), SXAttribute.getDf().format(health), SXAttribute.getDf().format(maxHealth)));
                }
                break;
            }
        }
    }

    public boolean isMythicBossBar(LivingEntity entity) {
        // TODO 对接SX-Item还是自己接？ (预计未来是要自己接的捏)
//        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
//            ActiveMob activeMob;
//            return (activeMob = MythicMobs.inst().getMobManager().getMythicMobInstance(entity)) != null && activeMob.getType().usesBossBar();
//        }
        return false;
    }


    public String getHealthProgressName(double progress) {
        StringBuilder healthName = new StringBuilder(Config.getConfig().getString(Config.HEALTH_NAME_PREFIX));
        int maxSize = Config.getConfig().getInt(Config.HEALTH_NAME_SIZE);
        int size = (int) (maxSize * progress);
        String current = Config.getConfig().getString(Config.HEALTH_NAME_CURRENT);
        String loss = Config.getConfig().getString(Config.HEALTH_NAME_LOSS);
        for (int i = 0; i < size; i++) {
            healthName.append(current);
        }
        for (int i = size; i < maxSize; i++) {
            healthName.append(loss);
        }
        return healthName.append(Config.getConfig().getString(Config.HEALTH_NAME_SUFFIX)).toString().replace("&", "§");
    }



    /**
     * 替换名字
     *
     * @param str String
     * @return String
     */
    public static String replaceName(String str) {
        if (str != null && Message.getMessages().contains(Message.REPLACE_LIST.toString())) {
            for (String replaceName : Message.getMessages().getConfigurationSection(Message.REPLACE_LIST.toString()).getKeys(false)) {
                if (str.equals(replaceName)) {
                    return Message.getMessages().getString(Message.REPLACE_LIST + "." + replaceName).replace("&", "§");
                }
            }
        }
        return str;
    }

    @Getter
    public class HoloData {

        private Hologram hologram;

        private long clearTime = System.currentTimeMillis() + (Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME) * 1000);

        HoloData(Hologram hologram) {
            this.hologram = hologram;
            getHoloList().add(this);
        }
    }

    @Getter
    public class NameData {

        @Setter
        private LivingEntity entity;

        private String name;

        private boolean visible;

        private int updateTick = Config.getConfig().getInt(Config.HEALTH_NAME_DISPLAY_TIME);

        private long clearTime;

        public NameData(LivingEntity entity, String name, boolean visible) {
            this.entity = entity;
            this.name = name;
            this.visible = visible;
            updateTick();
            getNameList().add(this);
        }

        public void updateTick() {
            this.clearTime = System.currentTimeMillis() + (updateTick * 1000);
        }

    }

    @Getter
    public class BossBarData {

        private LivingEntity entity;

        private BossBar bossBar;

        private double maxHealth;

        private String name;

        private int displayTime = Config.getConfig().getInt(Config.HEALTH_BOSS_BAR_DISPLAY_TIME);

        private String titleFormat = Config.getConfig().getString(Config.HEALTH_BOSS_BAR_FORMAT);

        private List<PlayerData> list = new ArrayList<>();

        public BossBarData(LivingEntity entity, String name, double maxHealth, double progress) {
            this.entity = entity;
            this.name = name;
            this.maxHealth = maxHealth;
            bossBar = Bukkit.createBossBar(getTitle(progress), BarColor.GREEN, BarStyle.SEGMENTED_20);
            getBossList().add(this);
        }

        public void setProgress(double progress) {
            getBossBar().setProgress((progress = Math.max(Math.min(progress, 1D), 0D)));
            getBossBar().setColor(progress > 0.66 ? BarColor.GREEN : progress > 0.33 ? BarColor.YELLOW : BarColor.RED);
            getBossBar().setTitle(getTitle(progress));
        }

        private String getTitle(double progress) {
            return MessageFormat.format(titleFormat, name, SXAttribute.getDf().format(progress * maxHealth), SXAttribute.getDf().format(maxHealth)).replace("&", "§");
        }

        public void addPlayer(Player player) {
            getList().add(new PlayerData(player));
        }

        public BossBarData clearExpired() {
            for (int i = getList().size() - 1; i >= 0; i--) {
                PlayerData data = getList().get(i);
                if (!data.getPlayer().isOnline() || data.getPlayer().isDead() || data.getClearTime() < System.currentTimeMillis()) {
                    getBossBar().removePlayer(getList().remove(i).getPlayer());
                }
            }
            return this;
        }

        @Getter
        private class PlayerData {

            private Player player;

            private long clearTime = System.currentTimeMillis() + (displayTime * 1000);

            public PlayerData(Player player) {
                getBossBar().addPlayer(player);
                this.player = player;
            }
        }
    }
}
