package njw.net.justskills.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import njw.net.justskills.JustSkills;
import njw.net.justskills.skill.cast.CastManager;
import njw.net.justskills.skill.runtime.AmberDiskManager;
import njw.net.justskills.skill.runtime.AzureWaveManager;
import njw.net.justskills.skill.runtime.CrimsonWaveManager;
import njw.net.justskills.skill.runtime.EmeraldSpikesManager;
import njw.net.justskills.skill.runtime.GoldenFlickerManager;
import njw.net.justskills.skill.runtime.IndigoOrbitManager;
import njw.net.justskills.skill.runtime.VioletRiftManager;

@EventBusSubscriber(modid = JustSkills.MODID)
public final class CommonEvents {
    private CommonEvents() {}

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) CastManager.tick(player);
    }

    @SubscribeEvent
    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        CrimsonWaveManager.tick(level);
        AzureWaveManager.tick(level);
        AmberDiskManager.tick(level);
        EmeraldSpikesManager.tick(level);
        GoldenFlickerManager.tick(level);
        IndigoOrbitManager.tick(level);
        VioletRiftManager.tick(level);
    }

    @SubscribeEvent
    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) CastManager.cancel(player);
    }
}