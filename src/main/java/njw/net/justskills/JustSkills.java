package njw.net.justskills;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(JustSkills.MODID)
public class JustSkills {
    public static final String MODID = "jwn_just_skills";
    public static final Logger LOGGER = LogUtils.getLogger();

    public JustSkills(IEventBus modEventBus) {

    }
}