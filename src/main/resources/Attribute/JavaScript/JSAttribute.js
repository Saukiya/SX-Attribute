// var jsManager = Java.type("javax.script.ScriptEngineManager").class.newInstance();
var jsManager = new Packages.javax.script.ScriptEngineManager();
// var Player = Java.type("org.bukkit.entity.Player").class;
var Player = Packages.org.bukkit.entity.Player.class;
var Particle = Packages.org.bukkit.Particle;

var valuesLength = 0;
// 申明个鬼 直接types
var types = Arrays.asList(SXAttributeType.UPDATE);

var JSRunnableList = [];

var js = this;

// 已含有变量:
// -对象:
// 文件名 - JSAttribute java
//
// -静态类:
// SXAttributeType
// SXAttribute
// Bukkit
// Arrays
// API
//

/**
 * 添加String类方法 startsWith
 */
if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function (prefix) {
        return this.slice(0, prefix.length) === prefix;
    };
}

/**
 * 向JSRunnableList添加方法 getRunnable
 */
if (typeof JSRunnableList.getRunnable != 'function') {
    JSRunnableList.getRunnable = function (name) {
        for (var i = 0; i < this.length; i++) {
            if (this[i].name === name) {
                return this[i];
            }
        }
        return null;
    }
}

/**
 * 创建一个与玩家关联的 binding
 */
function getBinding(player) {
    var bindings = engine.createBindings();
    JSAttribute.put(bindings, "player", player);
    JSAttribute.put(bindings, "js", js);
    JSAttribute.put(bindings, "Particle", Particle);
    return bindings;
}

/**
 * JS工厂
 *
 * 创建jsRunnable -> JSRunnableManager.create(name);
 *
 */
var JSRunnableManager = {
    engine: jsManager.getEngineByName("JavaScript"),

    create: function (name) {
        var JSRunnable = {
            cycleRun: engine.compile("JSRunnable.run();"),
            name: name,
            data: {},
            discernName: undefined,
            enabledScript: [],
            disableScript: [],
            continuedScript: [],
            combatPower: undefined,
            players: [],

            load: function () {
                var JSRunnable = this;
                // this.discernName = JSAttribute.getString("List." + name + ".DiscernName");
                this.discernName = config.getString("List." + name + ".DiscernName");
                this.data = {};
                this.enabledScript = [];
                this.disableScript = [];
                this.continuedScript = [];
                this.combatPower = config.getInt("List." + name + ".CombatPower");

                config.getStringList("List." + name + ".Enabled").forEach(function (value) {
                    JSRunnable.enabledScript.push(value.startsWith("delay") ? value : engine.compile(value));
                });
                config.getStringList("List." + name + ".Disable").forEach(function (value) {
                    JSRunnable.disableScript.push(value.startsWith("delay") ? value : engine.compile(value));
                });
                config.getStringList("List." + name + ".Continued").forEach(function (value) {
                    JSRunnable.continuedScript.push(value.startsWith("delay") ? value : engine.compile(value));
                });
                var bindings = engine.createBindings();
                JSAttribute.put(bindings, "js", js);
                JSAttribute.put(bindings, "data", this.data);
                config.getString("List." + name + ".Initialization")
                config.getStringList("List." + name + ".Initialization").forEach(function (value) {
                    engine.compile(value).eval(bindings);
                })
            },


            enabled: function (player) {
                this.players.push(player.getName());
                this.runScript(player, this.enabledScript);
            },

            disable: function (player) {
                var playerName = player.getName();
                for (var i = 0; i < this.players.length; i++) {
                    if (this.players[i] === playerName) {
                        this.players.splice(i, 1);
                    }
                }
                this.runScript(player, this.disableScript);
            },

            runScript: function (player, script) {
                var delay = 0;
                for (var scriptKey in script) {
                    var value = script[scriptKey];
                    if (typeof value == "string") {
                        delay += parseInt(value.slice(6, value.length));
                    } else {
                        Bukkit.getScheduler().runTaskLater(JSAttribute.getPlugin(), JSAttribute.createRunnable(value, JSAttribute.put(getBinding(player), "data", this.data)), delay);
                    }
                }
                return delay;
            },

            run: function () {
                var delay = 0;
                for (var scriptKey in this.continuedScript) {
                    var value = this.continuedScript[scriptKey];
                    if (typeof value == "string") {
                        delay += parseInt(value.slice(6, value.length));
                    } else {
                        for (var playerKey in this.players) {
                            var playerName = this.players[playerKey];
                            var player = Bukkit.getPlayerExact(playerName);
                            if (player != null) {
                                Bukkit.getScheduler().runTaskLater(JSAttribute.getPlugin(), JSAttribute.createRunnable(value, JSAttribute.put(getBinding(player), "data", this.data)), delay);
                            } else {
                                for (var i = 0; i < this.players.length; i++) {
                                    if (this.players[i] === playerName) {
                                        this.players.splice(i, 1);
                                    }
                                }
                            }

                        }
                    }
                }
                Bukkit.getScheduler().runTaskLater(JSAttribute.getPlugin(), JSAttribute.createRunnable(this.cycleRun, this.cycleBinding), delay === 0 ? 20 : delay);
            }
        };
        JSRunnable.cycleBinding = this.engine.createBindings();
        JSAttribute.put(JSRunnable.cycleBinding, "JSRunnable", JSRunnable);

        /**
         * 添加 JSRunnable.players 方法 contains
         */
        if (typeof JSRunnable.players.contains != 'function') {
            JSRunnable.players.contains = function (object) {
                for (var i = 0; i < this.length; i++) {
                    if (this[i] === object) {
                        return true;
                    }
                }
                return false;
            }
        }
        JSRunnable.load();
        JSRunnable.run();

        return JSRunnable;
    }

}

/**
 * 以下为sx调用方法
 *
 * 注意: debug开启方法为方法名+debug（例: eventMethod_debug = true)
 */

/**
 * 创建默认属性设置
 *
 * @param config YamlConfiguration
 * @returns YamlConfiguration
 */
// defaultConfig_debug = true
function defaultConfig(config) {
    config.set("List.Tick1.DiscernName", "耀眼光芒");
    config.set("List.Tick1.Enabled", Arrays.asList("js.Tick1 = js.Tick1 != null ? js.Tick1 : 1;js.Tick1Angle = js.Tick1Angle != null ? js.Tick1Angle : 0;js.Tick1Boo = js.Tick1Boo != null ? js.Tick1Boo : true;if (typeof js.drawRound != \"function\") {js.drawRound = function(angle, radians) {var radiansCircle = angle / 180.0 * Math.PI;return [Math.cos(radiansCircle) * radians, Math.sin(radiansCircle) * radians];}}player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 1);"));
    config.set("List.Tick1.Disable", Arrays.asList("player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation().clone().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);"));
    config.set("List.Tick1.Continued", Arrays.asList("delay 1", "if ((js.Tick1Boo ? js.Tick1++ : js.Tick1--) >= 20) {js.Tick1Boo = false;} else if (js.Tick1 <= 0) {js.Tick1Boo = true;js.Tick1Angle = (js.Tick1Angle += 7) >= 360 ? 0 : js.Tick1Angle;var v = js.drawRound(js.Tick1Angle, 2)player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getLocation().clone().add(-v[0], 0.3 + js.Tick1 / 30.0, -v[1]), 1, 0, 0, 0, 0)player.getWorld().spawnParticle(Particle.SPELL_MOB, player.getLocation().clone().add(v[0], 0.5 + js.Tick1 / 20.0, v[1]), 1, 0, 0, 0, js.Tick1 / 100)"));
    config.set("List.Tick1.CombatPower", 20);
    return config;
}

/**
 * 启动方法
 */
function onEnable() {
    config.getConfigurationSection("List").getKeys(false).forEach(function (key) {
        JSRunnableList.push(JSRunnableManager.create(key));
    });
    JSAttribute.setLength(JSRunnableList.length);
}

/**
 * 重载方法
 */
function onReLoad() {
    JSRunnableList.forEach(function (jsRunnable) {
        jsRunnable.load();
    })
}


/**
 * 执行伤害/更新事件方法
 */
// eventMethod_debug = true
function eventMethod(values, eventData) {
    if (eventData.getClass().getSimpleName() === "UpdateData" && Player.isInstance(eventData.getEntity())) {
        var player = eventData.getEntity();
        for (var i = 0; i < JSRunnableList.length; i++) {
            var jsRunnable = JSRunnableList[i];
            if (jsRunnable.players.contains(player.getName())) {
                if (values[i] === 0) {
                    jsRunnable.disable(player);
                }
            } else {
                if (values[i] > 0) {
                    jsRunnable.enabled(player);
                }
            }
        }
    }

}

/**
 * 获取变量
 */
function getPlaceholder(values, player, string) {
    for (var i = 0; i < JSRunnableList.length; i++) {
        if (JSRunnableList[i].name === string) {
            return values[i];
        }
    }
}

function getPlaceholders() {
    var list = [];
    JSRunnableList.forEach(function (value, index) {
        list[index] = value.name;
    });
    return list;
}

function loadAttribute(values, lore) {
    for (var i = 0; i < JSRunnableList.length; i++) {
        if (lore.contains(JSRunnableList[i].discernName)) {
            values[i] += 1;
        }
    }
}

function calculationCombatPower(values) {
    var value = 0.0;
    for (var i = 0; i < JSRunnableList.length; i++) {
        if (values[i] > 0) {
            value += JSRunnableList[i].combatPower;
        }
    }
    return value;
}

