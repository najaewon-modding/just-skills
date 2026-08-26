package njw.net.justskills.skill;

import net.minecraft.resources.Identifier;
import njw.net.justskills.JustSkills;
import njw.net.justskills.skill.cast.CastSpec;
import njw.net.justskills.skill.impl.AmberDiskSkill;
import njw.net.justskills.skill.impl.AzureWaveSkill;
import njw.net.justskills.skill.impl.CrimsonWaveSkill;
import njw.net.justskills.unlock.AlwaysUnlockedCondition;
import njw.net.justskills.unlock.SkillUnlockCondition;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SkillRegistry {

    private SkillRegistry() {
    }

    private static final Map<
            Identifier,
            SkillDefinition
            > SKILLS =
            new LinkedHashMap<>();

    /*
     * ============================================================
     * Crimson Wave
     * ============================================================
     */

    public static final SkillDefinition CRIMSON_WAVE =
            register(
                    "crimson_wave",

                    "skill.jwn_just_skills.crimson_wave",

                    new CrimsonWaveSkill(),

                    CastSpec.stationary(
                            20 * 3,
                            1.0
                    ),

                    AlwaysUnlockedCondition.INSTANCE
            );

    /*
     * ============================================================
     * Azure Wave
     * ============================================================
     */

    public static final SkillDefinition AZURE_WAVE =
            register(
                    "azure_wave",

                    "skill.jwn_just_skills.azure_wave",

                    new AzureWaveSkill(),

                    CastSpec.stationary(
                            20 * 3,
                            1.0
                    ),

                    AlwaysUnlockedCondition.INSTANCE
            );

    /*
     * ============================================================
     * Amber Disk
     * ============================================================
     */

    public static final SkillDefinition AMBER_DISK =
            register(
                    "amber_disk",

                    "skill.jwn_just_skills.amber_disk",

                    new AmberDiskSkill(),

                    CastSpec.stationary(
                            20 * 3,
                            1.0
                    ),

                    AlwaysUnlockedCondition.INSTANCE
            );

    /*
     * ============================================================
     * Register
     * ============================================================
     */

    private static SkillDefinition register(
            String path,
            String translationKey,
            Skill skill,
            CastSpec castSpec,
            SkillUnlockCondition unlockCondition
    ) {

        Identifier id =
                Identifier.fromNamespaceAndPath(
                        JustSkills.MODID,
                        path
                );

        SkillDefinition definition =
                new SkillDefinition(
                        id,
                        translationKey,
                        skill,
                        castSpec,
                        unlockCondition
                );

        if (SKILLS.putIfAbsent(
                id,
                definition
        ) != null) {

            throw new IllegalStateException(
                    "Duplicate skill id: "
                            + id
            );
        }

        return definition;
    }

    public static Optional<SkillDefinition> get(
            Identifier id
    ) {

        return Optional.ofNullable(
                SKILLS.get(id)
        );
    }

    public static Collection<SkillDefinition> values() {

        return SKILLS.values();
    }

    public static void bootstrap() {
        /*
         * static initialization trigger
         */
    }
}