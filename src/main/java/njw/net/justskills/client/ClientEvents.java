package njw.net.justskills.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import njw.net.justskills.JustSkills;
import njw.net.justskills.network.UseSkillPayload;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = JustSkills.MODID,
        value = Dist.CLIENT
)
public final class ClientEvents {

    private ClientEvents() {
    }

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(
                    Identifier.fromNamespaceAndPath(
                            JustSkills.MODID,
                            "skills"
                    )
            );

    private static final KeyMapping USE_SKILL =
            new KeyMapping(
                    "key.jwn_just_skills.use_skill",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    CATEGORY
            );

    @SubscribeEvent
    private static void registerKeys(
            RegisterKeyMappingsEvent event
    ) {

        event.registerCategory(
                CATEGORY
        );

        event.register(
                USE_SKILL
        );
    }

    @SubscribeEvent
    private static void onClientTick(
            ClientTickEvent.Post event
    ) {

        while (USE_SKILL.consumeClick()) {

            ClientPacketDistributor
                    .sendToServer(
                            UseSkillPayload.INSTANCE
                    );
        }
    }
}