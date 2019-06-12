package github.saukiya.sxattribute.listener;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.BossBarData;
import github.saukiya.sxattribute.data.NameData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Saukiya
 */
public class OnHealthChangeDisplayListener implements Listener {

    @Getter
    private final List<BossBarData> bossList = new ArrayList<>();

    @Getter
    private final List<NameData> nameList = new ArrayList<>();

    private final SXAttribute plugin;

    public OnHealthChangeDisplayListener(SXAttribute plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                //TODO 架构修改为List
                if (bossList.size() > 0) {
                    Iterator<BossBarData> bossDataIterator = bossList.iterator();
                    while (bossDataIterator.hasNext()) {
                        BossBarData bossBarData = bossDataIterator.next();
                        if (bossBarData.getEntity() != null && !bossBarData.getEntity().isDead() && bossBarData.getTimeMap().size() > 0) {
                            Iterator<Map.Entry<Player, Long>> entryIterator = bossBarData.getTimeMap().entrySet().iterator();
                            while (entryIterator.hasNext()) {
                                Map.Entry<Player, Long> entry = entryIterator.next();
                                if (!entry.getKey().isOnline() || entry.getKey().isDead() || entry.getValue() < System.currentTimeMillis()) {
                                    bossBarData.getBossBar().removePlayer(entry.getKey());
                                    entryIterator.remove();
                                }
                            }
                            if (bossBarData.getTimeMap().size() == 0) {
                                bossBarData.getBossBar().removeAll();
                                bossDataIterator.remove();
                            }
                        } else {
                            bossBarData.getBossBar().removeAll();
                            bossDataIterator.remove();
                        }
                    }
                }
                if (nameList.size() > 0) {
                    Iterator<NameData> nameDataIterator = nameList.iterator();
                    while (nameDataIterator.hasNext()) {
                        NameData nameData = nameDataIterator.next();
                        if (nameData.getEntity() == null || nameData.getEntity().isDead() || nameData.getEntity().getHealth() == getMaxHealth(nameData.getEntity()) || nameData.getTick() < System.currentTimeMillis()) {
                            if (nameData.getEntity() != null) {
                                nameData.getEntity().setCustomName(nameData.getName());
                                nameData.getEntity().setCustomNameVisible(nameData.isVisible());
                            }
                            nameDataIterator.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    public static double getMaxHealth(LivingEntity entity) {
        return SXAttribute.getVersionSplit()[1] >= 9 ? entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() : entity.getMaxHealth();
    }

    public String getEntityName(LivingEntity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).getDisplayName();
        }
        String entityName = entity.getName();
        for (NameData nameData : nameList) {
            if (nameData.getEntity().equals(entity)) {
                if (nameData.getName() != null) {
                    entityName = Message.replace(nameData.getName());
                } else {
                    String tempName = entityName;
                    entity.setCustomName(null);
                    entityName = Message.replace(entity.getName());
                    entity.setCustomName(tempName);
                }
                return entityName;
            }
        }
        return Message.replace(entityName);
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

        double health = entity.getHealth() - event.getFinalDamage();
        if (health < 0) health = 0;
        Double maxHealth = getMaxHealth(entity);
        if (health > maxHealth) health = maxHealth;
        double progress = health / maxHealth;
        BossBarData bossBarData = null;
        for (BossBarData barData : bossList) {
            if (barData.getEntity().equals(entity)) {
                bossBarData = barData;
                bossBarData.setProgress(progress);
                bossBarData.updateTitle();
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
                if (Config.isHealthBossBar() && damager instanceof Player && SXAttribute.getVersionSplit()[1] >= 9 && !Config.getBossBarBlackCauseList().contains(event.getCause().name())) {
                    if (bossBarData == null) {
                        bossBarData = new BossBarData(entity, name, maxHealth, progress);
                        bossBarData.setProgress(entity.getHealth() / maxHealth);
                        bossList.add(bossBarData);
                    }
                    bossBarData.addPlayer((Player) damager);
                    bossBarData.setProgress(progress);
                }
            }
        }

        if (damager == null && Config.isHolographic() && Config.isHolographicHealthTake() && SXAttribute.isHolographic() && !Config.getHolographicBlackList().contains(event.getCause().name())) {
            Location loc = entity.getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() / 2, 0);
            loc.setYaw(entity.getLocation().getYaw() - 90);
            loc.add(loc.getDirection().multiply(0.8D));
            Hologram hologram = HologramsAPI.createHologram(plugin, loc);
            hologram.appendTextLine(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__TAKE, event.getFinalDamage()));
            plugin.getOnDamageListener().getHologramsList().add(hologram);
            new BukkitRunnable() {
                @Override
                public void run() {
                    hologram.delete();
                    plugin.getOnDamageListener().getHologramsList().remove(hologram);
                }
            }.runTaskLater(plugin, Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME));
        }
        if (Config.isHealthNameVisible()) {
            NameData nameData = null;
            for (NameData data : nameList) {
                if (data.getEntity().equals(entity)) {
                    nameData = data;
                    break;
                }
            }

            if (health == 0) {
                for (int i = nameList.size() - 1; i >= 0; i--) {
                    NameData data = nameList.get(i);
                    if (data.getEntity().equals(entity)) {
                        entity.setCustomName(data.getName());
                        entity.setCustomNameVisible(data.isVisible());
                        nameList.remove(i);
                    }
                    if (data.getEntity().equals(damager)) {
                        damager.setCustomName(data.getName());
                        damager.setCustomNameVisible(data.isVisible());
                        nameList.remove(i);
                    }
                }
            } else if ((damager instanceof Player || nameData != null) && !(entity instanceof Player)) {
                StringBuilder healthName = new StringBuilder(Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_PREFIX));
                int maxSize = Config.getConfig().getInt(Config.HEALTH_NAME_VISIBLE_SIZE);
                int size = (int) (maxSize * progress);
                String current = Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_CURRENT);
                String loss = Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_LOSS);
                for (int i = 0; i < size; i++) {
                    healthName.append(current);
                }
                for (int i = size; i < maxSize; i++) {
                    healthName.append(loss);
                }
                healthName = new StringBuilder((healthName.append(Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_SUFFIX))).toString().replace("&", "§"));

                if (nameData == null) {
                    nameData = new NameData(entity, entity.getCustomName(), entity.isCustomNameVisible());
                    nameData.updateTick();
                    nameList.add(nameData);
                } else if (damager instanceof Player) {
                    nameData.updateTick();
                }
                entity.setCustomName(MessageFormat.format(healthName.toString(), SXAttribute.getDf().format(health), SXAttribute.getDf().format(maxHealth)));
                entity.setCustomNameVisible(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void OnEntityRegainHealthEvent(EntityRegainHealthEvent event) {
        if (event.isCancelled() || event.getAmount() == 0) return;
        if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof ArmorStand) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        double health = entity.getHealth() + event.getAmount();
        Double maxHealth = getMaxHealth(entity);
        double progress = health > maxHealth ? 1D : health < 0 ? 0D : health / maxHealth;

        for (BossBarData bossBarData : bossList) {
            if (bossBarData.getEntity().equals(entity)) {
                if (progress == 1D) {
                    bossBarData.getBossBar().removeAll();
                } else {
                    bossBarData.setProgress(progress);
                    bossBarData.updateTitle();
                }
                break;
            }
        }

        if (Config.isHolographic() && Config.isHolographicHealthTake() && SXAttribute.isHolographic()) {
            Location loc = entity.getEyeLocation().clone().add(0, 0.6 - SXAttribute.getRandom().nextDouble() * 1.5, 0);
            loc.setYaw(entity.getLocation().getYaw() + 90);
            loc.add(loc.getDirection().multiply(0.8D));
            Hologram hologram = HologramsAPI.createHologram(plugin, loc);
            hologram.appendTextLine(Message.getMsg(Message.PLAYER__HOLOGRAPHIC__HEALTH, SXAttribute.getDf().format(event.getAmount())));
            plugin.getOnDamageListener().getHologramsList().add(hologram);
            new BukkitRunnable() {
                @Override
                public void run() {
                    hologram.delete();
                    plugin.getOnDamageListener().getHologramsList().remove(hologram);
                }
            }.runTaskLater(plugin, Config.getConfig().getInt(Config.HOLOGRAPHIC_DISPLAY_TIME));
        }
        for (NameData nameData : nameList) {
            if (nameData.getEntity().equals(entity)) {
                if (progress == 1D) {
                    nameData.getEntity().setCustomName(nameData.getName());
                    nameData.getEntity().setCustomNameVisible(nameData.isVisible());
                    nameList.remove(nameData);
                } else {
                    StringBuilder healthName = new StringBuilder(Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_PREFIX));
                    int maxSize = Config.getConfig().getInt(Config.HEALTH_NAME_VISIBLE_SIZE);
                    int size = (int) (maxSize * progress);
                    String current = Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_CURRENT);
                    String loss = Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_LOSS);
                    for (int i = 0; i < size; i++) {
                        healthName.append(current);
                    }
                    for (int i = size; i < maxSize; i++) {
                        healthName.append(loss);
                    }
                    healthName = new StringBuilder((healthName + Config.getConfig().getString(Config.HEALTH_NAME_VISIBLE_SUFFIX)).replace("&", "§"));
                    entity.setCustomName(MessageFormat.format(healthName.toString(), SXAttribute.getDf().format(health), SXAttribute.getDf().format(maxHealth)));
                }
                break;
            }
        }
    }
}
