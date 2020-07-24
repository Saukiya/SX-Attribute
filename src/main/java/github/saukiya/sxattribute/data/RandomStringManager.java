package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.SimpleDateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Saukiya
 */
public class RandomStringManager {
    private final File file = new File(SXAttribute.getPluginFile(), "RandomString");
    private final File file1 = new File(file, "DefaultRandom.yml");
    private final File file2 = new File(file, "10Level" + File.separator + "Random.yml");

    private final Map<String, List<String>> map = new HashMap<>();
    private final SXAttribute plugin;

    public RandomStringManager(SXAttribute plugin) throws IOException, InvalidConfigurationException {
        this.plugin = plugin;
        loadData();
    }

    /**
     * 获取随机字符串数据
     *
     * @return Set
     */
    public Set<Map.Entry<String, List<String>>> entrySet() {
        return map.entrySet();
    }

    /**
     * 专业处理随机文本四十年
     *
     * @param itemName 可以为null
     * @param string   被随机的文本
     * @param lockMap  存储固定值的Map
     * @return 处理后的Map
     */
    public String processRandomString(String itemName, String string, Map<String, String> lockMap) {
        if (string != null) {
            if (Config.isRandomString()) {
                List<String> replaceLockStringList = getStringList("<l:", ">", string);
                for (String str : replaceLockStringList) {
                    if (lockMap.containsKey(str)) {
                        string = string.replace("<l:" + str + ">", lockMap.get(str));
                    } else {
                        String randomString = getRandomString(itemName, str, lockMap);
                        if (!randomString.equals("%DeleteLore%")) {
                            string = string.replace("<l:" + str + ">", randomString);
                            // 记录到LockMap中
                            lockMap.put(str, randomString);
                        } else {
                            string = string.replace("<l:" + str + ">", "%DeleteLore%");
                            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c物品 §4" + itemName + "§c 名字中的随机字符串 §4" + str + "§c 不存在!");
                        }
                    }
                }
                // 普通随机
                List<String> replaceStringList = getStringList("<s:", ">", string);
                for (String str : replaceStringList) {
                    String randomString = plugin.getRandomStringManager().getRandomString(itemName, str, lockMap);
                    string = string.replaceFirst("<s:" + str + ">", randomString);
                }
            }
            // 数字随机
            List<String> replaceIntList = getStringList("<r:", ">", string);
            for (String str : replaceIntList) {
                String[] strSplit = str.split("_");
                if (strSplit.length > 1) {
                    int i1 = Integer.valueOf(strSplit[0].replaceAll("[^0-9]", ""));
                    int i2 = Integer.valueOf(strSplit[1].replaceAll("[^0-9]", "")) + 1;
                    string = string.replaceFirst("<r:" + str + ">", String.valueOf(SXAttribute.getRandom().nextInt((i2 - i1) < 1 ? 1 : (i2 - i1)) + i1));
                }
            }
            // 小数随机
            List<String> replaceDoubleList = getStringList("<d:", ">", string);
            for (String str : replaceDoubleList) {
                String[] strSplit = str.split("_");
                if (strSplit.length > 1) {
                    double d1 = Double.valueOf(strSplit[0].replaceAll("[^.0-9]", ""));
                    double d2 = Double.valueOf(strSplit[1].replaceAll("[^.0-9]", "")) + 1;
                    string = string.replaceFirst("<d:" + str + ">", SXAttribute.getDf().format(SXAttribute.getRandom().nextDouble() * (d2 - d1) + d1));
                }
            }
            // 日期随机
            List<String> replaceTimeList = getStringList("<t:", ">", string);
            if (replaceTimeList.size() > 0) {
                SimpleDateFormatUtils ft = SXAttribute.getSdf();
                for (String str : replaceTimeList) {
                    String addTime = str.replaceAll("[^0-9]", "") + "000";
                    long time = System.currentTimeMillis() + Long.valueOf(addTime);
                    string = string.replaceFirst("<t:" + str + ">", ft.format(time));
                }
            }
        }
        return string;
    }

    /**
     * 获取字符组
     *
     * @param itemName String
     * @param name     String
     * @param lockMap  Map
     * @return String
     */
    private String getRandomString(String itemName, String name, Map<String, String> lockMap) {
        List<String> randomList = map.get(name);
        if (randomList != null) {
            String str1 = randomList.get(SXAttribute.getRandom().nextInt(randomList.size()));
            if (lockMap != null) {
                List<String> replaceLockStringList = getStringList("<l:", ">", str1);
                for (String str : replaceLockStringList) {
                    if (lockMap.containsKey(str)) {
                        str1 = str1.replace("<l:" + str + ">", lockMap.get(str));
                    } else {
                        String randomString = getRandomString(itemName, str, lockMap);
                        if (randomString != null) {
                            str1 = str1.replace("<l:" + str + ">", randomString);
                            lockMap.put(str, randomString);
                        } else {
                            str1 = str1.replace("<l:" + str + ">", "");
                            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c物品 §4" + itemName + "§c 名字中的随机字符串 §4" + str + "§c 不存在!");
                        }
                    }
                }
            }
            List<String> replaceStringList = getStringList("<s:", ">", str1);
            for (String str2 : replaceStringList) {
                if (str1.equals(str2)) {
                    Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c请不要造成无限循环 本插件不承担相应责任!");
                    continue;
                }
                str1 = str1.replaceFirst("<s:" + str2 + ">", getRandomString(itemName, str2, lockMap));
            }
            return str1;
        }
        return "%DeleteLore%";
    }


    /**
     * 专业获取变量三十年
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
     *
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    public void loadData() throws IOException, InvalidConfigurationException {
        if (Config.isRandomString()) {
            map.clear();
            if (!file.exists() || Objects.requireNonNull(file.listFiles()).length == 0) {
                createDefaultRandom();
            }
            loadRandom(file);
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + map.size() + " §rRandomString");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§4Disable RandomString");
        }
    }

    /**
     * 遍历读取随机字符串数据
     *
     * @param files File
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    private void loadRandom(File files) throws IOException, InvalidConfigurationException {
        for (File file : Objects.requireNonNull(files.listFiles())) {
            if (file.isDirectory()) {
                loadRandom(file);
            } else {
                YamlConfiguration yml = new YamlConfiguration();
                yml.load(file);
                for (String name : yml.getKeys(false)) {
                    if (map.containsKey(name)) {
                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c不要重复随机字符组名:§4 " + file.getName().replace("plugins" + File.separator + SXAttribute.getPluginName() + File.separator, "") + File.separator + name + "§c !");
                    }
                    if (yml.get(name) instanceof String) {
                        map.put(name, Collections.singletonList(yml.getString(name)));
                    } else if (yml.get(name) instanceof List) {
                        List<String> list = yml.getStringList(name);
                        map.put(name, list);
                    }
                }
            }
        }
    }

    /**
     * 创建默认数据
     *
     * @throws IOException IOException
     */
    private void createDefaultRandom() throws IOException {
        YamlConfiguration yml = new YamlConfiguration();
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cCreate Item/Default.yml");
        yml.set("DefaultLore", Arrays.asList("&7&o他是由什么材质做成的呢?", "&7&o握着它，有不好的预感呢", "&7&o据说夜幕曾经带它攻略沙场"));
        yml.set("DefaultPrefix", Arrays.asList("&c令人兴奋之", "&c煞胁之", "&e兴趣使然之", "&e初心者之", "&e丝质之", "&e精灵之"));
        yml.set("DefaultSuffix", Arrays.asList("&e淦", "&e武", "&e衡"));
        yml.set("品质", Arrays.asList("普通", "普通", "普通", "普通", "普通", "普通", "普通", "优秀", "优秀", "史诗"));
        yml.set("职业", Arrays.asList("射手", "战士", "剑士"));
        yml.set("射手附魔", Arrays.asList("ARROW_DAMAGE:<r:0_3>\nARROW_INFINITE:<r:0_1>", "ARROW_DAMAGE:<r:0_3>\nARROW_FIRE:<r:0_2>", "ARROW_DAMAGE:<r:0_3>\nDURABILITY:<r:0_1>"));
        yml.set("战士附魔", Arrays.asList("DAMAGE_ALL:<r:0_3>\nFIRE_ASPECT:<r:0_1>", "DAMAGE_ARTHROPODS:<r:0_3>\nKNOCKBACK:<r:0_1>", "DAMAGE_UNDEAD:<r:0_3>\nLOOT_BONUS_MOBS:<r:0_1>"));
        yml.set("剑士附魔", Arrays.asList("DAMAGE_ALL:<r:0_3>\nFIRE_ASPECT:<r:0_1>", "DAMAGE_ALL:<r:0_3>\nKNOCKBACK:<r:0_1>", "DAMAGE_ALL:<r:0_3>\nLOOT_BONUS_MOBS:<r:0_1>"));
        yml.set("射手ID", "261");
        yml.set("战士ID", "<s:战士<l:品质>ID>");
        yml.set("剑士ID", "<s:剑士<l:品质>ID>");
        yml.set("战士普通ID", "258");
        yml.set("战士优秀ID", "286");
        yml.set("战士史诗ID", "279");
        yml.set("剑士普通ID", "267");
        yml.set("剑士优秀ID", "283");
        yml.set("剑士史诗ID", "276");
        yml.set("材质", Arrays.asList("&01", "&01", "&02", "&03", "&04", "&05", "&06", "&07", "&08", "&09", "&010", "&011"));
        yml.set("优秀职业", "&6限制职业: <l:职业>");
        yml.set("史诗职业", "&6限制职业: <l:职业>");
        yml.set("普通耐久", "<r:200_300>");
        yml.set("优秀耐久", "<r:500_600>");
        yml.set("史诗耐久", "<r:800_900>");
        yml.set("普通耐久最低", "200");
        yml.set("优秀耐久最低", "500");
        yml.set("史诗耐久最低", "800");
        yml.set("普通Color", "&7");
        yml.set("优秀Color", "&a");
        yml.set("史诗Color", "&5");
        yml.set("普通宝石孔", "&a&l『&7武石槽&a&l』");
        yml.set("优秀宝石孔", "&a&l『&7武石槽&a&l』&a&l『&7武石槽&a&l』");
        yml.set("史诗宝石孔", "&a&l『&7武石槽&a&l』&a&l『&7武石槽&a&l』&a&l『&7武石槽&a&l』");
        yml.set("史诗绑定", "&c已绑定");
        yml.set("好看Color", Arrays.asList("&a", "&b", "&c", "&4", "&d", "&1", "&3", "&9"));
        yml.set("好丑Color", Arrays.asList("&1", "&8", "&7", "&5", "&3", "&2"));
        yml.set("攻随一", Arrays.asList("命中几率", "失明几率", "缓慢几率", "凋零几率"));
        yml.set("攻随二", Arrays.asList("雷霆几率", "破甲几率", "撕裂几率"));
        yml.set("防随一", Arrays.asList("反射几率", "格挡几率", "韧性", "移动速度"));
        yml.set("防随二", Arrays.asList("反射伤害", "格挡伤害", "闪避几率"));
        yml.set("防随三", Arrays.asList("生命恢复", "生命上限", "PVP防御力", "PVE防御力"));
        yml.save(file1);
        yml = new YamlConfiguration();
        yml.set("普通攻击-10", "20 - 30");
        yml.set("优秀攻击-10", "25 - 35");
        yml.set("史诗攻击-10", "30 - 40");
        yml.set("普通等级-10", "10");
        yml.set("优秀等级-10", "13");
        yml.set("史诗等级-10", "15");
        yml.set("普通攻一-10", Arrays.asList("<s:好丑Color>暴击几率: +<r:10_30>%\n<s:好丑Color>暴击伤害: +<r:10_30>%", "<s:好丑Color>攻击速度: +<r:10_30>%\n<s:好丑Color>点燃几率: +<r:10_30>%", "<s:好丑Color>吸血几率: +<r:10_30>%\n<s:好丑Color>吸血倍率: +<r:10_30>%"));
        yml.set("普通攻二-10", Arrays.asList("<s:好丑Color><s:攻随一>: +<r:0_7>.<r:0_99>%", "%DeleteLore%"));
        yml.set("普通攻三-10", Arrays.asList("<s:好丑Color><s:攻随二>: +<r:0_4>.<r:0_99>%", "%DeleteLore%", "%DeleteLore%"));
        yml.set("优秀攻一-10", Arrays.asList("<s:好看Color>暴击几率: +<r:20_40>%\n<s:好看Color>暴击伤害: +<r:20_40>%", "<s:好看Color>攻击速度: +<r:20_40>%\n<s:好看Color>点燃几率: +<r:20_40>%", "<s:好看Color>吸血几率: +<r:20_40>%\n<s:好看Color>吸血倍率: +<r:20_40>%"));
        yml.set("优秀攻二-10", "<s:好丑Color><s:攻随一>: +<r:8_15>.<r:0_99>%");
        yml.set("优秀攻三-10", Arrays.asList("<s:好看Color><s:攻随二>: +<r:5_9>.<r:0_99>%", "%DeleteLore%"));
        yml.set("史诗攻一-10", Arrays.asList("<s:好看Color>暴击几率: +<r:30_50>%\n<s:好看Color>暴击伤害: +<r:30_50>%", "<s:好看Color>攻击速度: +<r:30_50>%\n<s:好看Color>点燃几率: +<r:30_50>%", "<s:好看Color>吸血几率: +<r:30_50>%\n<s:好看Color>吸血倍率: +<r:30_50>%"));
        yml.set("史诗攻二-10", "<s:好看Color><s:攻随一>: +<r:16_23>.<r:0_99>%");
        yml.set("史诗攻三-10", "<s:好看Color><s:攻随二>: +<r:10_14>.<r:0_99>%");
        yml.set("普通防御-10", "10 - 15");
        yml.set("优秀防御-10", "13 - 18");
        yml.set("史诗防御-10", "16 - 21");
        yml.save(file2);
    }
}
