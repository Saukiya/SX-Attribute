package github.saukiya.sxattribute.data.itemdata.sub;

import github.saukiya.sxitem.data.expression.ExpressionHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated
 */
public class GeneratorSX extends github.saukiya.sxitem.data.item.impl.GeneratorDefault {

    public GeneratorSX(String key, ConfigurationSection config, String group) {
        super(key, config, group);
    }

    @Override
    protected ItemStack getItem(Player player, Object... args) {
        return super.getItem(player, args);
    }

    public static class SXExpressionHandler extends ExpressionHandler {

    }
}
