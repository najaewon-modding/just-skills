package njw.net.justskills.skill;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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

    /*
     * ============================================================
     * Common cast settings
     * ============================================================
     */

    /*
     * 현재 모든 스킬 공통 시전 시간.
     *
     * 20 ticks = 1초
     * 20 * 3 = 3초
     *
     * 모든 스킬의 시전 시간을 같이 바꾸고 싶으면
     * 여기만 수정하면 된다.
     */
    private static final int DEFAULT_CAST_DURATION_TICKS =
            20 * 1;

    /*
     * 시전 중 원래 위치에서 허용되는
     * 최대 수평 이동 거리.
     */
    private static final double DEFAULT_MAX_DISPLACEMENT =
            1.0;

    /*
     * ============================================================
     * Registry
     * ============================================================
     */

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

                    Items.REDSTONE,

                    new CrimsonWaveSkill(),

                    CastSpec.stationary(
                            DEFAULT_CAST_DURATION_TICKS,
                            DEFAULT_MAX_DISPLACEMENT
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

                    Items.LAPIS_LAZULI,

                    new AzureWaveSkill(),

                    CastSpec.stationary(
                            DEFAULT_CAST_DURATION_TICKS,
                            DEFAULT_MAX_DISPLACEMENT
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

                    Items.COPPER_INGOT,

                    new AmberDiskSkill(),

                    CastSpec.stationary(
                            DEFAULT_CAST_DURATION_TICKS,
                            DEFAULT_MAX_DISPLACEMENT
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
            Item iconItem,
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
                        iconItem,
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