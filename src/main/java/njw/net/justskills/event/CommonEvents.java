package njw.net.justskills.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import njw.net.justskills.JustSkills;
import njw.net.justskills.skill.SkillManager;
import njw.net.justskills.skill.cast.CastManager;
import njw.net.justskills.skill.runtime.AmberDiskManager;
import njw.net.justskills.skill.runtime.AzureWaveManager;
import njw.net.justskills.skill.runtime.CrimsonWaveManager;

@EventBusSubscriber(modid = JustSkills.MODID)
public final class CommonEvents {

    private CommonEvents() {
    }

    /*
     * ============================================================
     * Player tick
     * ============================================================
     */

    @SubscribeEvent
    private static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {

        if (!(event.getEntity()
                instanceof ServerPlayer player)) {

            return;
        }

        SkillManager.tickPlayer(player);

        CastManager.tick(player);
    }

    /*
     * ============================================================
     * Level tick
     * ============================================================
     */

    @SubscribeEvent
    private static void onLevelTick(
            LevelTickEvent.Post event
    ) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {
            return;
        }

        CrimsonWaveManager.tick(level);
        AzureWaveManager.tick(level);
        AmberDiskManager.tick(level);
    }

    /*
     * ============================================================
     * Logout
     * ============================================================
     */

    @SubscribeEvent
    private static void onLogout(
            PlayerEvent.PlayerLoggedOutEvent event
    ) {

        if (event.getEntity()
                instanceof ServerPlayer player) {

            CastManager.cancel(player);
        }
    }
}