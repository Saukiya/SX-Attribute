#!/usr/bin/env python3
"""Read official JAR signatures without compiling or executing plugin/server code.

The contracts cover the MM adapter calls and the shared skill's baseline Bukkit
API. This detects return-type and inheritance changes that a method-name search
misses; it does not replace compiling SX or exercising a running server.
"""

import argparse
import hashlib
import json
import zipfile
from pathlib import Path


class ClassReader:
    """Read only the class-file metadata needed for ABI contracts, skipping code."""

    def __init__(self, data):
        self.data = data
        self.offset = 0

    def number(self, size):
        """Class files use unsigned big-endian numbers independent of host CPU."""
        value = int.from_bytes(self.data[self.offset:self.offset + size], "big")
        self.offset += size
        return value

    def attributes(self):
        """Unknown attributes can safely be skipped using their declared length."""
        for _ in range(self.number(2)):
            self.number(2)
            length = self.number(4)
            self.offset += length

    def read(self):
        """Return exact JVM descriptors and parent names, without loading classes."""
        if self.number(4) != 0xCAFEBABE:
            raise ValueError("Invalid class-file magic")
        self.number(2)
        major = self.number(2)
        pool = [None] * self.number(2)
        index = 1
        while index < len(pool):
            tag = self.number(1)
            if tag == 1:
                length = self.number(2)
                pool[index] = self.data[self.offset:self.offset + length].decode("utf-8", "replace")
                self.offset += length
            elif tag in (7, 8, 16, 19, 20):
                pool[index] = self.number(2)
            elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
                self.offset += 4
            elif tag in (5, 6):
                self.offset += 8
                index += 1
            elif tag == 15:
                self.offset += 3
            else:
                raise ValueError(f"Unknown constant-pool tag {tag}")
            index += 1
        self.number(2)
        self.number(2)
        parent = self.number(2)
        parents = [pool[pool[parent]]] if parent else []
        parents.extend(pool[pool[self.number(2)]] for _ in range(self.number(2)))
        groups = []
        for _ in range(2):
            members = set()
            for _ in range(self.number(2)):
                access = self.number(2)
                name = pool[self.number(2)]
                descriptor = pool[self.number(2)]
                # Public/protected methods and fields form the adapter's callable ABI.
                if access & 0x0005:
                    members.add((name, descriptor))
                self.attributes()
            groups.append(members)
        return {"major": major, "parents": parents, "fields": groups[0], "methods": groups[1]}


class Jar:
    """Resolve inherited members inside one version's JAR, never mixing versions."""

    def __init__(self, path):
        self.path = Path(path)
        self.archive = zipfile.ZipFile(path)
        self.cache = {}

    def read(self, name):
        """A missing superclass is not assumed to provide the requested member."""
        if name not in self.cache:
            try:
                self.cache[name] = ClassReader(self.archive.read(name + ".class")).read()
            except KeyError:
                self.cache[name] = None
        return self.cache[name]

    def has(self, owner, name, descriptor, group="methods"):
        """Constructors are never inherited, unlike interface/entity methods."""
        info = self.read(owner)
        return info is not None and ((name, descriptor) in info[group] or name != "<init>" and any(
            self.has(parent, name, descriptor, group) for parent in info["parents"]))


def mythic_contracts(jar):
    """Use package capabilities, matching the plugin's MM 4/5 dispatch strategy."""
    modern = jar.read("io/lumine/mythic/api/skills/ITargetedEntitySkill") is not None
    root = "io/lumine/mythic/" if modern else "io/lumine/xikage/mythicmobs/"
    api = root + "api/" if modern else root
    skills = api + "skills/"
    entity = api + "adapters/AbstractEntity"
    config = api + "config/MythicLineConfig" if modern else root + "io/MythicLineConfig"
    event = root + ("bukkit/events/" if modern else "api/bukkit/events/") + "MythicMechanicLoadEvent"
    mechanic = skills + "ISkillMechanic" if modern else skills + "SkillMechanic"
    metadata = skills + "SkillMetadata"
    caster = skills + "SkillCaster"
    result = "L" + skills + "SkillResult;" if modern else "Z"
    checks = [
        (event, "getMechanicName", "()Ljava/lang/String;"),
        (event, "getConfig", f"()L{config};"),
        (event, "register", f"(L{mechanic};)V"),
        (skills + "ITargetedEntitySkill", "castAtEntity", f"(L{metadata};L{entity};){result}"),
        (metadata, "getCaster", f"()L{caster};"),
        (metadata, "getTrigger", f"()L{entity};"),
        (caster, "getEntity", f"()L{entity};"),
        (caster, "isUsingDamageSkill", "()Z"),
        (caster, "setUsingDamageSkill", "(Z)V"),
        (entity, "getBukkitEntity", "()Lorg/bukkit/entity/Entity;"),
    ]
    # Spawn events historically do not implement Bukkit Cancellable and MM 4
    # changed getMobLevel from int to double; check the capability we actually read.
    spawn = root + ("bukkit/events/" if modern else "api/bukkit/events/") + "MythicMobSpawnEvent"
    mob_type = api + "mobs/MythicMob"
    mob_config = api + "config/MythicConfig" if modern else root + "io/MythicConfig"
    level_descriptor = "()D" if jar.has(spawn, "getMobLevel", "()D") else "()I"
    checks.extend([
        (spawn, "isCancelled", "()Z"),
        (spawn, "getEntity", "()Lorg/bukkit/entity/Entity;"),
        (spawn, "getMobType", f"()L{mob_type};"),
        (spawn, "getMobLevel", level_descriptor),
        (mob_type, "getInternalName", "()Ljava/lang/String;"),
        (mob_type, "getConfig", f"()L{mob_config};"),
        (mob_config, "isSet", "(Ljava/lang/String;)Z"),
        (mob_config, "getStringList", "(Ljava/lang/String;)Ljava/util/List;"),
    ])
    if modern:
        placeholder = skills + "placeholders/PlaceholderString"
        meta = root + "core/skills/placeholders/PlaceholderMeta"
        checks.extend([
            (config, "getString", "([Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/String;"),
            (config, "getLine", "()Ljava/lang/String;"),
            (mechanic, "getThreadSafetyLevel", f"()L{skills}ThreadSafetyLevel;"),
            (placeholder, "of", f"(Ljava/lang/String;)L{placeholder};"),
            (placeholder, "get", f"(L{meta};L{entity};)Ljava/lang/String;"),
        ])
    else:
        checks.extend([
            (config, "getString", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
            (config, "getLine", "()Ljava/lang/String;"),
            (mechanic, "<init>", f"(Ljava/lang/String;L{config};)V"),
            (mechanic, "forceSync", "Z", "fields"),
        ])
        placeholder = skills + "placeholders/parsers/PlaceholderString"
        if jar.read(placeholder):
            checks.extend([
                (placeholder, "of", f"(Ljava/lang/String;)L{placeholder};"),
                (placeholder, "get", f"(L{skills}placeholders/PlaceholderMeta;L{entity};)Ljava/lang/String;"),
            ])
        else:
            checks.append((skills + "SkillString", "parseMobVariables",
                           f"(Ljava/lang/String;L{caster};L{entity};L{entity};)Ljava/lang/String;"))
    return checks


def bukkit_contracts():
    """Baseline calls must exist even in the Java 8 / Bukkit 1.8 target."""
    return [
        ("org/bukkit/Bukkit", "isPrimaryThread", "()Z"),
        ("org/bukkit/Bukkit", "getScheduler", "()Lorg/bukkit/scheduler/BukkitScheduler;"),
        ("org/bukkit/scheduler/BukkitScheduler", "runTask",
         "(Lorg/bukkit/plugin/Plugin;Ljava/lang/Runnable;)Lorg/bukkit/scheduler/BukkitTask;"),
        ("org/bukkit/entity/LivingEntity", "damage", "(DLorg/bukkit/entity/Entity;)V"),
        ("org/bukkit/entity/LivingEntity", "getHealth", "()D"),
        ("org/bukkit/entity/LivingEntity", "getMaxHealth", "()D"),
        ("org/bukkit/entity/LivingEntity", "setHealth", "(D)V"),
        ("org/bukkit/entity/LivingEntity", "getNoDamageTicks", "()I"),
        ("org/bukkit/entity/LivingEntity", "setNoDamageTicks", "(I)V"),
        ("org/bukkit/entity/Entity", "isValid", "()Z"),
        ("org/bukkit/entity/Entity", "isDead", "()Z"),
        ("org/bukkit/entity/Entity", "getWorld", "()Lorg/bukkit/World;"),
        ("org/bukkit/entity/Entity", "getCustomName", "()Ljava/lang/String;"),
        ("org/bukkit/event/entity/EntityDamageByEntityEvent", "getDamager", "()Lorg/bukkit/entity/Entity;"),
        ("org/bukkit/event/entity/EntityDamageByEntityEvent", "setDamage", "(D)V"),
        ("org/bukkit/event/entity/EntityDamageByEntityEvent", "getFinalDamage", "()D"),
        ("org/bukkit/event/entity/EntityRegainHealthEvent", "<init>",
         "(Lorg/bukkit/entity/Entity;DLorg/bukkit/event/entity/EntityRegainHealthEvent$RegainReason;)V"),
        ("org/bukkit/event/entity/EntityRegainHealthEvent$RegainReason", "CUSTOM",
         "Lorg/bukkit/event/entity/EntityRegainHealthEvent$RegainReason;", "fields"),
    ]


def folia_contracts():
    """Entity relay must use the ownership API and retirement-aware scheduler."""
    return [
        ("org/bukkit/Bukkit", "isOwnedByCurrentRegion", "(Lorg/bukkit/entity/Entity;)Z"),
        ("org/bukkit/entity/Entity", "getScheduler",
         "()Lio/papermc/paper/threadedregions/scheduler/EntityScheduler;"),
        ("io/papermc/paper/threadedregions/scheduler/EntityScheduler", "execute",
         "(Lorg/bukkit/plugin/Plugin;Ljava/lang/Runnable;Ljava/lang/Runnable;J)Z"),
    ]


def main():
    """Accept explicit local artifacts so verification never downloads or runs builds."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mythic", action="append", default=[], metavar="VERSION=JAR")
    parser.add_argument("--bukkit", action="append", default=[], metavar="VERSION=JAR")
    parser.add_argument("--folia", action="append", default=[], metavar="VERSION=JAR")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    results = []
    for kind, artifacts in (("mythic", args.mythic), ("bukkit", args.bukkit), ("folia", args.folia)):
        for artifact in artifacts:
            label, path = artifact.split("=", 1)
            jar = Jar(path)
            contracts = mythic_contracts(jar) if kind == "mythic" else folia_contracts() if kind == "folia" else bukkit_contracts()
            missing = [list(contract) for contract in contracts if not jar.has(*contract)]
            results.append({"kind": kind, "version": label, "sha256": hashlib.sha256(jar.path.read_bytes()).hexdigest(),
                            "checks": len(contracts), "missing": missing})
            jar.archive.close()
    if not results:
        parser.error("At least one --mythic, --bukkit or --folia artifact is required")
    report = json.dumps(results, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(report, encoding="utf-8")
    print(report, end="")
    return 1 if any(result["missing"] for result in results) else 0


if __name__ == "__main__":
    raise SystemExit(main())
