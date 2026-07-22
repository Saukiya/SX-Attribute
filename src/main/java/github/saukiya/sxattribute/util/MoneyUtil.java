package github.saukiya.sxattribute.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault 经济服务访问入口。
 *
 * <p>经济实现可能晚于 Vault 和本插件注册，因此服务提供者不能只在插件启用时解析一次。</p>
 */
public class MoneyUtil {
    private static Economy economy = null;

    /**
     * 解析当前已注册的 Vault 经济服务。
     *
     * <p>每次检测都替换缓存，避免经济插件晚注册后永久保持不可用状态，也避免继续使用已注销的提供者。</p>
     *
     * @return 当前是否存在可用的经济服务
     */
    public static boolean setup() {
        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        economy = registeredServiceProvider == null ? null : registeredServiceProvider.getProvider();
        return economy != null;
    }

    /**
     * 获取玩家金币
     *
     * @param player OfflinePlayer
     * @return double
     */
    public static double get(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    /**
     * 检查玩家是否拥有相应金币
     *
     * @param player OfflinePlayer
     * @param money  double
     * @return boolean
     */
    public static boolean has(OfflinePlayer player, double money) {
        return money <= get(player);
    }

    /**
     * 给予玩家金币
     *
     * @param player OfflinePlayer
     * @param money  double
     */
    public static void give(OfflinePlayer player, double money) {
        economy.depositPlayer(player, money);
    }

    /**
     * 扣取玩家金币
     *
     * @param player OfflinePlayer
     * @param money  double
     */
    public static void take(OfflinePlayer player, double money) {
        economy.withdrawPlayer(player, money);
    }
}
