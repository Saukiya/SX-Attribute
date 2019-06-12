package github.saukiya.sxattribute.util;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MoneyUtil {
    private static Economy economy = null;

    /**
     * 初始化MoneyUtil类
     *
     * @throws NullPointerException NullPointerException
     */
    public static void setup() throws NullPointerException {
        RegisteredServiceProvider<Economy> registeredServiceProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider == null) throw new NullPointerException();
        economy = registeredServiceProvider.getProvider();
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
