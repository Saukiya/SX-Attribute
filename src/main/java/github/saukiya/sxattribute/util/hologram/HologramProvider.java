package github.saukiya.sxattribute.util.hologram;

import org.bukkit.Location;

/**
 * 全息显示提供者抽象.
 * <p>
 * 屏蔽 HolographicDisplays / DecentHolograms 等不同全息插件的 API 差异,
 * 上层逻辑仅依赖此接口, 由 {@link github.saukiya.sxattribute.SXAttribute} 在启动时
 * 根据服务器已安装的插件注入具体实现.
 *
 * @author Saukiya
 */
public interface HologramProvider {

    /**
     * 创建一个仅含单行文本的全息, 用于伤害/回血浮空字.
     *
     * @param location 全息位置
     * @param text     文本内容 (已完成颜色符号转换)
     * @return 全息句柄, 用于后续删除
     */
    Handle create(Location location, String text);

    /**
     * 全息句柄, 屏蔽底层全息对象的类型差异, 仅暴露删除能力.
     */
    interface Handle {

        /**
         * 删除该全息.
         */
        void delete();
    }
}
