package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.entity.LivingEntity;

/**
 * 更新事件
 *
 * @author Saukiya
 */
public class UpdateEventData extends EventData {

    private final LivingEntity entity;

    public UpdateEventData(LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * 获取该事件的玩家
     *
     * @return Player
     */
    public LivingEntity getEntity() {
        return entity;
    }
}
