package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.TimeUtil;
import lombok.Getter;
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

    public RandomStringManager() {
        loadData();
    }

    /**
     * 处理随机文本
     *
     * @param string  被随机的文本
     * @param lockMap 存储固定值的Map
     * @return 处理后的Map
     */
    public String processRandomString(String string, Map<String, String> lockMap) {
        if (string != null) {
            // 固定随机
            List<String> replaceLockStringList = getStringList("<l:", ">", string);
            for (String str : replaceLockStringList) {
                String randomStr = lockMap.get(str);
                if (randomStr == null) {
                    lockMap.put(str, randomStr = getRandomString(str, lockMap));
                }
                string = string.replace("<l:" + str + ">", randomStr);
            }
            // 普通随机
            List<String> replaceStringList = getStringList("<s:", ">", string);
            for (String str : replaceStringList) {
                string = string.replaceFirst("<s:" + str + ">", getRandomString(str, lockMap));
            }
            // 数字随机
            List<String> replaceIntList = getStringList("<r:", ">", string);
            for (String str : replaceIntList) {
                String[] strSplit = str.split("_");
                if (strSplit.length > 1) {
                    int i1 = Integer.valueOf(strSplit[0]);
                    int i2 = Integer.valueOf(strSplit[1]) + 1;
                    string = string.replaceFirst("<r:" + str + ">", String.valueOf(SXAttribute.getRandom().nextInt((i2 - i1) < 1 ? 1 : (i2 - i1)) + i1));
                }
            }
            // 小数随机
            List<String> replaceDoubleList = getStringList("<d:", ">", string);
            for (String str : replaceDoubleList) {
                String[] strSplit = str.split("_");
                if (strSplit.length > 1) {
                    double d1 = Double.valueOf(strSplit[0]);
                    double d2 = Double.valueOf(strSplit[1]);
                    string = string.replaceFirst("<d:" + str + ">", SXAttribute.getDf().format(SXAttribute.getRandom().nextDouble() * (d2 - d1) + d1));
                }
            }
            // 日期随机
            List<String> replaceTimeList = getStringList("<t:", ">", string);
            if (replaceTimeList.size() > 0) {
                for (String str : replaceTimeList) {
                    String addTime = str + "000";
                    long time = System.currentTimeMillis() + Long.valueOf(addTime);
                    string = string.replaceFirst("<t:" + str + ">", TimeUtil.getSdf().format(time));
                }
            }
        }
        return string;
    }

    /**
     * 获取字符组
     *
     * @param string  Name
     * @param lockMap Map
     * @return String
     */
    private String getRandomString(String string, Map<String, String> lockMap) {
        List<String> randomList = map.get(string);
        if (randomList != null) {
            string = randomList.get(SXAttribute.getRandom().nextInt(randomList.size()));
            for (String str : getStringList("<l:", ">", string)) {
                String randomStr = lockMap.get(str);
                if (randomStr == null) {
                    lockMap.put(str, randomStr = getRandomString(str, lockMap));
                }
                string = string.replace("<l:" + str + ">", randomStr);
            }
            for (String str2 : getStringList("<s:", ">", string)) {
                string = string.replaceFirst("<s:" + str2 + ">", getRandomString(str2, lockMap));
            }
            return string;
        }
        return "%DeleteLore%";
    }


    /**
     * 获取变量
     *
     * @param prefix 前缀
     * @param suffix 后缀
     * @param string 被读取的字符串
     * @return 被前后缀包围的列表 (不包括前后缀)
     */
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
                    if (yml.get(name) instanceof String) {
                        map.put(name, Collections.singletonList(yml.getString(name)));
                    } else if (yml.get(name) instanceof List) {
                        List<String> list = new ArrayList<>();
                        for (Object obj : yml.getList(name)) {
                            if (obj instanceof List) {
                                List<String> objList = (List<String>) obj;
                                StringBuilder str = new StringBuilder(objList.size() > 0 ? objList.get(0) : "");
                                for (int i = 1; i < objList.size(); i++) {
                                    str.append("/n").append(objList.get(i));
                                }
                                list.add(str.toString());
                            } else {
                                list.add(obj.toString());
                            }
                        }
                        map.put(name, list);
                    }
                }
            }
        }
    }
}