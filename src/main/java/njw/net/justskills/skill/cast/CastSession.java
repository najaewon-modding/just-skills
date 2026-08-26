package njw.net.justskills.skill.cast;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import njw.net.justskills.skill.SkillDefinition;

public final class CastSession {

    private final SkillDefinition skill;
    private final Vec3 origin;
    private final ResourceKey<Level> dimension;
    private final ServerBossEvent bossBar;

    private int elapsedTicks;

    public CastSession(
            SkillDefinition skill,
            Vec3 origin,
            ResourceKey<Level> dimension,
            ServerBossEvent bossBar
    ) {
        this.skill = skill;
        this.origin = origin;
        this.dimension = dimension;
        this.bossBar = bossBar;
    }

    public SkillDefinition skill() {
        return skill;
    }

    public Vec3 origin() {
        return origin;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public ServerBossEvent bossBar() {
        return bossBar;
    }

    public int elapsedTicks() {
        return elapsedTicks;
    }

    public void tick() {
        elapsedTicks++;
    }
}