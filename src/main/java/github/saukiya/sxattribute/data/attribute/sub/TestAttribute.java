package github.saukiya.sxattribute.data.attribute.sub;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Saukiya
 */
public class TestAttribute extends SubAttribute {

    public TestAttribute() {
        super("TestAttribute", 2, SXAttributeType.DAMAGE);
    }

    @Override
    public void eventMethod(EventData eventData) {

    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Text1") ? getDf().format(getAttributes()[0]) :
                string.equalsIgnoreCase("Text2") ? getDf().format(getAttributes()[1]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return null;
    }

    @Override
    public boolean loadAttribute(String lore) {
        return false;
    }

    @Override
    public double getValue() {
        return 0;
    }
}
