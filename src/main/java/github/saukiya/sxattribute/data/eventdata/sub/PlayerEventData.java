package github.saukiya.sxattribute.data.eventdata.sub;

import github.saukiya.sxattribute.data.eventdata.EventData;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * @author Saukiya
 */
public class PlayerEventData extends EventData {

    @Getter
    private final Player player;

    public PlayerEventData(Player player) {
        this.player = player;
    }

}
