package github.saukiya.sxattribute.util.hologram;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.Location;

import java.util.List;

/**
 * HolographicDisplays 适配器.
 * <p>
 * 仅在服务器已启用 HolographicDisplays 时才会被实例化, 从而将
 * {@code com.gmail.filoghost.*} 的类加载隔离在本类内部, 避免缺失该插件时报错.
 *
 * @author Saukiya
 */
public class HolographicDisplaysProvider implements HologramProvider {

    @Override
    public Handle create(Location location, List<String> lines) {
        Hologram hologram = HologramsAPI.createHologram(SXAttribute.getInst(), location);
        for (String line : lines) hologram.appendTextLine(line);
        return new Handle() {
            @Override
            public void delete() {
                hologram.delete();
            }

            @Override
            public void moveUp(double distance) {
                hologram.teleport(hologram.getLocation().add(0, distance, 0));
            }
        };
    }
}
