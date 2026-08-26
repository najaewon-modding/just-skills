package njw.net.justskills.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import njw.net.justskills.JustSkills;
import njw.net.justskills.skill.cast.CastSpec;
import njw.net.justskills.skill.impl.AmberDiskSkill;
import njw.net.justskills.skill.impl.AzureWaveSkill;
import njw.net.justskills.skill.impl.CrimsonWaveSkill;
import njw.net.justskills.skill.impl.EmeraldSpikesSkill;
import njw.net.justskills.skill.impl.GoldenFlickerSkill;
import njw.net.justskills.skill.impl.IndigoOrbitSkill;
import njw.net.justskills.skill.impl.VioletRiftSkill;
import njw.net.justskills.unlock.AdvancementUnlockCondition;
import njw.net.justskills.unlock.SkillUnlockCondition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SkillRegistry {
    private SkillRegistry() {}

    private static final int DEFAULT_CAST_DURATION_TICKS = 20 * 3;
    private static final double DEFAULT_MAX_DISPLACEMENT = 1.0;
    private static final SkillUnlockCondition BASIC_SKILL_UNLOCK = new AdvancementUnlockCondition(
            Identifier.fromNamespaceAndPath("blazeandcave", "bacap/root")
    );
    private static final Map<Identifier, SkillDefinition> SKILLS = new LinkedHashMap<>();

    public static final SkillDefinition CRIMSON_WAVE = register(
            "crimson_wave",
            "skill.njw_just_skills.crimson_wave",
            Items.REDSTONE,
            new CrimsonWaveSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    public static final SkillDefinition AZURE_WAVE = register(
            "azure_wave",
            "skill.njw_just_skills.azure_wave",
            Items.LAPIS_LAZULI,
            new AzureWaveSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    public static final SkillDefinition AMBER_DISK = register(
            "amber_disk",
            "skill.njw_just_skills.amber_disk",
            Items.COPPER_INGOT,
            new AmberDiskSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    public static final SkillDefinition EMERALD_SPIKES = register(
            "emerald_spikes",
            "skill.njw_just_skills.emerald_spikes",
            Items.EMERALD,
            new EmeraldSpikesSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    public static final SkillDefinition GOLDEN_FLICKER = register(
            "golden_flicker",
            "skill.njw_just_skills.golden_flicker",
            Items.GLOWSTONE_DUST,
            new GoldenFlickerSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    public static final SkillDefinition INDIGO_ORBIT = register(
            "indigo_orbit",
            "skill.njw_just_skills.indigo_orbit",
            Items.ECHO_SHARD,
            new IndigoOrbitSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    public static final SkillDefinition VIOLET_RIFT = register(
            "violet_rift",
            "skill.njw_just_skills.violet_rift",
            Items.AMETHYST_SHARD,
            new VioletRiftSkill(),
            CastSpec.stationary(DEFAULT_CAST_DURATION_TICKS, DEFAULT_MAX_DISPLACEMENT),
            BASIC_SKILL_UNLOCK
    );

    private static SkillDefinition register(String path, String translationKey, Item iconItem, Skill skill, CastSpec castSpec, SkillUnlockCondition unlockCondition) {
        Identifier id = Identifier.fromNamespaceAndPath(JustSkills.MODID, path);
        SkillDefinition definition = new SkillDefinition(id, translationKey, iconItem, skill, castSpec, unlockCondition);

        if (SKILLS.putIfAbsent(id, definition) != null) {
            throw new IllegalStateException("Duplicate skill id: " + id);
        }

        return definition;
    }

    public static Optional<SkillDefinition> get(Identifier id) {
        return Optional.ofNullable(SKILLS.get(id));
    }

    public static Collection<SkillDefinition> values() {
        return SKILLS.values();
    }

    public static void bootstrap() {}
}