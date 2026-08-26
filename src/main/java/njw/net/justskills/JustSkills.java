package njw.net.justskills;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import njw.net.justskills.data.ModAttachments;
import njw.net.justskills.network.ModNetworking;
import njw.net.justskills.particle.ModParticles;
import njw.net.justskills.skill.SkillRegistry;
import org.slf4j.Logger;

@Mod(JustSkills.MODID)
public class JustSkills {

    public static final String MODID = "njw_just_skills";

    public static final Logger LOGGER = LogUtils.getLogger();

    public JustSkills(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);
        ModParticles.register(modEventBus);

        modEventBus.addListener(ModNetworking::registerPayloads);

        SkillRegistry.bootstrap();
    }
}