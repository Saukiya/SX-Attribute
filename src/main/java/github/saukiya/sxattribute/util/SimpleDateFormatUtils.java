package github.saukiya.sxattribute.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Saukiya
 */
public class SimpleDateFormatUtils {

    private ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat(Config.getConfig().getString(Config.FORMAT_EXPIRY_TIME)));

    public Date parse(String dateStr) throws ParseException {
        return threadLocal.get().parse(dateStr);
    }

    public String format(Long date) {
        return threadLocal.get().format(date);
    }

    public void reload() {
        threadLocal.remove();
    }
}
