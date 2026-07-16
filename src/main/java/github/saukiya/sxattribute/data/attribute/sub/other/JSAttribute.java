package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import javax.script.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 */
@Getter
public class JSAttribute extends SubAttribute {
    private static final Object INVOCATION_FAILED = new Object();
    private final ScriptEngine script;
    private final Invocable invocable;
    /** 脚本熔断状态；失败后所有属性回调退化为无效果，避免持续影响事件链。 */
    private volatile boolean active = true;
    private volatile boolean failureLogged;

    @SuppressWarnings("unchecked")
    public JSAttribute(String name, ScriptEngine script) {
        super(name, SXAttribute.getInst(), script.get("valuesLength") != null ? (int) script.get("valuesLength") : 0,
                ((List<AttributeType>) script.get("types")).toArray(new AttributeType[0]));
        this.script = script;
        invocable = (Invocable) script;
        script.put(name, this);
    }

    @Override
    public void setLength(int length) {
        super.setLength(length);
        script.put("valuesLength", length);
    }

    @Override
    public File getConfigFile() {
        return new File(SXAttribute.getInst().getDataFolder(), "Attribute" + File.separator
                + getPlugin().getName() + File.separator + getName() + "_JS.yml");
    }

    /** 加载脚本属性；先迁移已知坏配置，单个脚本失败时只禁用该属性而不阻断插件启动。 */
    @Override
    public void onEnable() {
        script.put("config", config());
        migrateBrokenTickScript();
        active = true;
        failureLogged = false;
        invoke("onEnable", null);
    }

    /**
     * 修复 3.9.x/4.0.0-beta.1 已生成的 Tick1 默认脚本。
     * <p>
     * 旧脚本在三个 JavaScript 语句之间漏写分号，Nashorn 会把 {@code )player} 解析为非法语法并中止
     * 整个插件启动。这里只匹配内置 Tick1 的特征文本，绝不改写服主自行编写的其它脚本。
     */
    private void migrateBrokenTickScript() {
        String path = "List.Tick1.Continued";
        List<String> scripts = new ArrayList<>(config().getStringList(path));
        if (repairLegacyTickScript(scripts)) {
            config().set(path, scripts);
            SXAttribute.getInst().getLogger().info("Migrated invalid built-in JSAttribute Tick1 script.");
        }
    }

    static boolean repairLegacyTickScript(List<String> scripts) {
        boolean changed = false;
        for (int index = 0; index < scripts.size(); index++) {
            String value = scripts.get(index);
            if (!value.contains("js.Tick1Boo") || !value.contains("Particle.SPELL_WITCH")
                    || !value.contains("Particle.SPELL_MOB")) {
                continue;
            }
            String repaired = value
                    .replace("js.drawRound(js.Tick1Angle, 2)player", "js.drawRound(js.Tick1Angle, 2);player")
                    .replace(", 0)player.getWorld().spawnParticle(Particle.SPELL_MOB",
                            ", 0);player.getWorld().spawnParticle(Particle.SPELL_MOB");
            if (!repaired.trim().endsWith(";")) repaired += ";";
            if (!repaired.equals(value)) {
                scripts.set(index, repaired);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void onReLoad() {
        script.put("config", config());
        active = true;
        failureLogged = false;
        migrateBrokenTickScript();
        invoke("onReLoad", null);
    }

    @Override
    public void onDisable() {
        invoke("onDisable", null);
        active = false;
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        Object result = invoke("defaultConfig", config, config);
        return result instanceof YamlConfiguration ? (YamlConfiguration) result : config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        invoke("eventMethod", null, values, eventData);
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        return invoke("getPlaceholder", null, values, player, string);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPlaceholders() {
        try {
            Object object = invoke("getPlaceholders", null);
            if (object instanceof List) {
                return (List<String>) object;
            } else if (object instanceof Bindings) {
                Bindings som = (Bindings) object;
                List<String> list = new ArrayList<>();
                int i = 0;
                while (som.get(String.valueOf(i)) != null) {
                    list.add(som.get(String.valueOf(i++)).toString());
                }
                return list;
            }
            return null;
        } catch (RuntimeException exception) {
            disable("getPlaceholders conversion", exception);
            return null;
        }
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        invoke("loadAttribute", null, values, lore);
    }

    @Override
    public void correct(double[] values) {
        if (invoke("correct", INVOCATION_FAILED, values) == INVOCATION_FAILED) super.correct(values);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        try {
            Object result = invoke("calculationCombatPower", null, values);
            return result == null ? 0D : Double.parseDouble(result.toString());
        } catch (RuntimeException exception) {
            disable("calculationCombatPower conversion", exception);
            return 0;
        }
    }

    /**
     * 非sx调用的方法
     * js属性需要调用此方法则为 属性名.method("参数");
     *
     * @param type
     * @return
     */
    public Object method(String type) {
        return invoke("method", null, type);
    }

    /**
     * 创建一个Runnable 并塞进一个脚本
     * 用处:
     *
     * @param cs       CompiledScript
     * @param bindings Bindings
     * @return
     */
    public Runnable createRunnable(CompiledScript cs, Bindings bindings) {
        return () -> {
            if (!active) return;
            try {
                if (bindings != null) {
                    cs.eval(bindings);
                } else {
                    cs.eval();
                }
            } catch (Throwable exception) {
                rethrowFatal(exception);
                disable("scheduled script", exception);
            }
        };
    }

    /**
     * 统一执行脚本函数。缺少可选函数时返回降级值；脚本自身异常会触发熔断，但不向 Bukkit 事件链抛出。
     */
    private Object invoke(String function, Object fallback, Object... arguments) {
        if (!active) return fallback;
        try {
            return invocable.invokeFunction(function, arguments);
        } catch (NoSuchMethodException exception) {
            if (Boolean.TRUE.equals(script.get(function + "_debug"))) {
                SXAttribute.getInst().getLogger().warning("JS attribute " + getName() + " has no function " + function);
            }
            return fallback;
        } catch (Throwable exception) {
            rethrowFatal(exception);
            disable(function, exception);
            return fallback;
        }
    }

    /** 首次异常后熔断当前属性并只记录一次摘要，防止周期脚本持续刷屏。 */
    private void disable(String phase, Throwable exception) {
        active = false;
        if (failureLogged) return;
        failureLogged = true;
        SXAttribute.getInst().getLogger().severe("JS attribute " + getName() + " failed in " + phase
                + " and was disabled without affecting SX-Attribute: " + exception.getClass().getSimpleName()
                + ": " + exception.getMessage());
    }

    static boolean isFatalFailure(Throwable exception) {
        return exception instanceof VirtualMachineError;
    }

    private static void rethrowFatal(Throwable exception) {
        if (exception instanceof VirtualMachineError) throw (VirtualMachineError) exception;
    }

    /**
     * 向bindings添加数据
     * js脚本中无法对Bindings进行操作，因为本身为 脚本对象镜像 ScriptObjectMirror
     *
     * @param bindings create -> Engine.createBindings()
     * @param key
     * @param value
     */
    public Bindings put(Bindings bindings, String key, Object value) {
        bindings.put(key, value);
        return bindings;
    }
}
