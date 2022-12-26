package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
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
    private ScriptEngine script;
    private Invocable invocable;

    @SuppressWarnings("unchecked")
    public JSAttribute(String name, ScriptEngine script) {
        super(name, SXAttribute.getInst(), script.get("valuesLength") != null ? (int) script.get("valuesLength") : 0, ((List<AttributeType>) script.get("types")).toArray(new AttributeType[0]));
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
        return new File(SXAttribute.getInst().getDataFolder(), "Attribute" + File.separator + getPlugin().getName() + File.separator + getName() + "_JS.yml");
    }

    @Override
    public void onEnable() {
        script.put("config", config());
        try {
            invocable.invokeFunction("onEnable");
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("onEnable_debug"))) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onReLoad() {
        script.put("config", config());
        try {
            invocable.invokeFunction("onReLoad");
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("onReLoad_debug"))) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            invocable.invokeFunction("onDisable");
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("onDisable_debug"))) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        try {
            return (YamlConfiguration) invocable.invokeFunction("defaultConfig", config);
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("defaultConfig_debug"))) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        try {
            invocable.invokeFunction("eventMethod", values, eventData);
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("eventMethod_debug"))) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        try {
            return invocable.invokeFunction("getPlaceholder", values, player, string);
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("getPlaceholder_debug"))) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPlaceholders() {
        try {
            Object object = invocable.invokeFunction("getPlaceholders");
            if (object instanceof List) {
                return (List<String>) object;
            } else if (object instanceof ScriptObjectMirror) {
                ScriptObjectMirror som = (ScriptObjectMirror) object;
                List<String> list = new ArrayList<>();
                int i = 0;
                while (som.get(String.valueOf(i)) != null) {
                    list.add(som.get(String.valueOf(i++)).toString());
                }
                return list;
            }
            return null;
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("getPlaceholders_debug"))) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        try {
            invocable.invokeFunction("loadAttribute", values, lore);
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("loadAttribute_debug"))) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void correct(double[] values) {
        try {
            invocable.invokeFunction("correct", values);
        } catch (ScriptException | NoSuchMethodException e) {
            super.correct(values);
            if (Boolean.TRUE.equals(script.get("correct_debug"))) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public double calculationCombatPower(double[] values) {
        try {
            return Double.valueOf(invocable.invokeFunction("calculationCombatPower", values).toString());
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("calculationCombatPower_debug"))) {
                e.printStackTrace();
            }
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
        try {
            return invocable.invokeFunction("method", type);
        } catch (ScriptException | NoSuchMethodException e) {
            if (Boolean.TRUE.equals(script.get("method_debug"))) {
                e.printStackTrace();
            }
            return null;
        }
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
            try {
                if (bindings != null) {
                    cs.eval(bindings);
                } else {
                    cs.eval();
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        };
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
