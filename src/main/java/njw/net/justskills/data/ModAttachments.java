package njw.net.justskills.data;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import njw.net.justskills.JustSkills;

public final class ModAttachments {

    private ModAttachments() {
    }

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(
                    NeoForgeRegistries.ATTACHMENT_TYPES,
                    JustSkills.MODID
            );

    public static final DeferredHolder<
            AttachmentType<?>,
            AttachmentType<PlayerSkillState>
            > PLAYER_SKILL_STATE =
            ATTACHMENTS.register(
                    "player_skill_state",
                    () -> AttachmentType
                            .builder(PlayerSkillState::empty)
                            .serialize(PlayerSkillState.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}