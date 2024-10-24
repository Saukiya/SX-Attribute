package github.saukiya.sxattribute.util;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeUtil {
    @Getter
    private static SimpleDateFormatUtils sdf = new SimpleDateFormatUtils();

    private static Map<String, Integer> timeMap = new HashMap<>();

    /**
     * 添加冷却时间
     *
     * @param player Player
     * @param name   String
     * @param times  int
     */
    public static void add(Player player, String name, int times) {
        timeMap.put(player.getName() + ":" + name, Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)) + times);
    }

    /**
     * 获取冷却时间
     *
     * @param player Player
     * @param name   String
     * @return
     */
    private static int get(Player player, String name) {
        Integer time = timeMap.get(player.getName() + ":" + name);
        return time != null ? time - Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000)) : -1;
    }

    /**
     * 检测是否还在冷却内
     *
     * @param player Player
     * @param name   String
     * @return Boolean
     */
    public static boolean is(Player player, String name) {
        if (get(player, name) > 0) {
            return true;
        }
        timeMap.remove(player.getName() + ":" + name);
        return false;
    }

    private static Date getThisWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date == null ? new Date() : date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    /**
     * 获取准确的时间
     *
     * @param date   Date / null
     * @param day    星期几 / null
     * @param hour   小时 / -1
     * @param minute 分钟 / -1
     * @return Date
     */
    public static Date getDayTime(Date date, Integer day, int hour, int minute) {
        date = date == null ? new Date() : date;
        Calendar cal = Calendar.getInstance();
        if (day != null) {
            cal.setTime(getThisWeekMonday(date));
            cal.add(Calendar.DATE, day - 1);
        } else {
            cal.setTime(date);
        }
        if (hour > -1) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute > -1) {
            cal.set(Calendar.MINUTE, minute);
        }
        cal.set(Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static class SimpleDateFormatUtils {

        private ThreadLocal<SimpleDateFormat> threadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat(Config.getConfig().getString(Config.FORMAT_EXPIRY_TIME)));

        public Date parseFormDate(String dateStr) throws ParseException {
            return threadLocal.get().parse(dateStr);
        }

        public long parseFormLong(String dateStr) throws ParseException {
            return threadLocal.get().parse(dateStr).getTime();
        }

        public String format(Long date) {
            return threadLocal.get().format(date);
        }

        public String format(Date date) {
            return threadLocal.get().format(date);
        }

        public void reload() {
            threadLocal.remove();
        }
    }
}
