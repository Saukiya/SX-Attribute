package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxitem.data.expression.ExpressionSpace;
import lombok.Getter;
import lombok.val;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * @author Saukiya
 */
@Getter
public class RandomStringManager {

    private final File file = new File(SXAttribute.getInst().getDataFolder(), "RandomString");

    private final Map<String, List<String>> map = new HashMap<>();

    private final Space instSpace = new Space(null);

    public RandomStringManager() {
        loadData();
    }

    /**
     * 处理随机文本
     *
     * @param stringList 被随机的文本列表
     * @param space      表达式空间
     * @return 处理后的Map
     */
    public List<String> processRandomString(List<String> stringList, Space space) {
        return space.replace(stringList);
    }

    /**
     * 处理随机文本
     *
     * @param string  被随机的文本
     * @param space 表达式空间
     * @return 处理后的Map
     */
    public String processRandomString(String string, Space space) {
        return space.replace(string);
    }


    @Deprecated
    public List<String> processRandomString(List<String> stringList, Map<String, String> lockMap) {
        return processRandomString(stringList, new Space(lockMap));
    }

    @Deprecated
    public String processRandomString(String string, Map<String, String> lockMap) {
        return processRandomString(string, new Space(lockMap));
    }

    @Deprecated
    private String getRandomString(String string, Map<String, String> lockMap) {
        string = random(string);
        return processRandomString(string, new Space(lockMap));
    }

    @Deprecated
    public List<String> getStringList(String prefix, String suffix, String string) {
        List<String> stringList = new ArrayList<>();
        if (string.contains(prefix)) {
            String[] args = string.split(prefix);
            if (args.length > 1 && args[1].contains(suffix)) {
                for (int i = 1; i < args.length && args[i].contains(suffix); i++) {
                    stringList.add(args[i].split(suffix)[0]);
                }
            }
        }
        return stringList;
    }

    /**
     * 单纯获取随机值， 不做递归
     *
     * @param key
     * @return
     */
    public String random(String key) {
        List<String> randomList = map.get(key);
        if (randomList == null || randomList.isEmpty()) return null;
        if (randomList.size() == 1) return randomList.get(0);
        return randomList.get(SXAttribute.getRandom().nextInt(randomList.size()));
    }

    /**
     * 加载随机字符串数据
     */
    public void loadData() {
        map.clear();
        if (!file.exists() || Objects.requireNonNull(file.listFiles()).length == 0) {
            SXAttribute.getInst().saveResource("RandomString/DefaultRandom.yml", true);
            SXAttribute.getInst().saveResource("RandomString/10Level/Random.yml", true);
        }
        loadRandom(file);
        SXAttribute.getInst().getLogger().info("Loaded " + map.size() + " RandomString");
    }

    /**
     * 遍历读取随机字符串数据
     *
     * @param files File
     */
    private void loadRandom(File files) {
        for (File file : Objects.requireNonNull(files.listFiles())) {
            if (file.isDirectory()) {
                loadRandom(file);
            } else {
                YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
                for (String name : yml.getKeys(false)) {
                    if (map.containsKey(name)) {
                        SXAttribute.getInst().getLogger().info("不要重复随机字符组名: " + file.getName().replace("plugins" + File.separator + SXAttribute.getInst().getName() + File.separator, "") + File.separator + name + " !");
                    }
                    val value = yml.get(name);
                    if (value instanceof String) {
                        map.put(name, Collections.singletonList(loadFormat(value)));
                    } else if (value instanceof List) {
                        List<String> list = new ArrayList<>();
                        for (Object subValue : (List) value) {
                            list.add(loadFormat(subValue));
                        }
                        map.put(name, list);
                    }
                }
            }
        }
    }

    private String loadFormat(Object value) {
        if (value == null) return null;
        if (value instanceof List) value = String.join("\n", (List) value);
        return value.toString().replace("/n", "\n").replace("\\n", "\n");
    }

    public static class Space extends ExpressionSpace {

        public Space(Map<String, String> lockMap) {
            super(null, null, lockMap);
        }

        public Space() {
            super();
        }

        @Override
        public String random(String key) {
            String str;
            str = getOtherMap().get(key);
            if (str != null) return str;
//            str = RandomManager.random(key, getLocalMap());
//            if (str != null) return str;
            return SXAttribute.getRandomStringManager().random(key);
        }
    }
}