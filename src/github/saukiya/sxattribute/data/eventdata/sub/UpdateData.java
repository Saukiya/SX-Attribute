package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.entity.LivingEntity;

/**
 * 更新事件
 *
 * @author Saukiya
 */
public class UpdateData implements EventData {

    private final LivingEntity entity;

    public UpdateData(LivingEntity entity) {
        this.entity = entity;
    }

    /**
     * 获取该事件的实体
     *
     * @return LivingEntity
     */
    public LivingEntity getEntity() {
        return entity;
    }
}
