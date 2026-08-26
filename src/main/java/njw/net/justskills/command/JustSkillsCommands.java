package njw.net.justskills.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import njw.net.justskills.JustSkills;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.SkillDefinition;
import njw.net.justskills.skill.SkillRegistry;

@EventBusSubscriber(modid = JustSkills.MODID)
public final class JustSkillsCommands {

    private JustSkillsCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(
            RegisterCommandsEvent event
    ) {

        LiteralArgumentBuilder<CommandSourceStack> root =
                Commands.literal("justskills");

        LiteralArgumentBuilder<CommandSourceStack> cast =
                Commands.literal("cast")
                        .requires(CommandSourceStack::isPlayer);

        /*
         * SkillRegistry에 등록된 모든 스킬을
         * 자동으로 하위 명령어로 추가한다.
         *
         * 예:
         *
         * crimson_wave
         * azure_wave
         * heal
         * blink
         * ...
         */
        for (SkillDefinition definition
                : SkillRegistry.values()) {

            String skillName =
                    definition.id().getPath();

            cast.then(
                    Commands.literal(skillName)
                            .executes(context ->
                                    castImmediately(
                                            context.getSource(),
                                            definition
                                    )
                            )
            );
        }

        root.then(cast);

        event.getDispatcher()
                .register(root);
    }

    private static int castImmediately(
            CommandSourceStack source,
            SkillDefinition definition
    ) throws CommandSyntaxException {

        ServerPlayer player =
                source.getPlayerOrException();

        SkillContext context =
                new SkillContext(
                        player,
                        player.level()
                );

        boolean success =
                definition.skill()
                        .activate(context);

        if (!success) {

            source.sendFailure(
                    Component.literal(
                            "Failed to cast skill: "
                                    + definition.id()
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal("Cast: ")
                        .append(definition.displayName()),
                false
        );

        return 1;
    }
}