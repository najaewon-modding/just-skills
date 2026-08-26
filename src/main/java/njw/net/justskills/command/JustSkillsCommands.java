package njw.net.justskills.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import njw.net.justskills.JustSkills;
import njw.net.justskills.skill.SkillContext;
import njw.net.justskills.skill.SkillDefinition;
import njw.net.justskills.skill.SkillRegistry;

@EventBusSubscriber(modid = JustSkills.MODID)
public final class JustSkillsCommands {
    private JustSkillsCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("justskills");
        LiteralArgumentBuilder<CommandSourceStack> cast = Commands.literal("cast")
                .requires(source -> source.isPlayer() && source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));

        for (SkillDefinition definition : SkillRegistry.values()) {
            cast.then(Commands.literal(definition.id().getPath())
                    .executes(context -> castImmediately(context.getSource(), definition)));
        }

        root.then(cast);
        event.getDispatcher().register(root);
    }

    private static int castImmediately(CommandSourceStack source, SkillDefinition definition) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        SkillContext context = new SkillContext(player, player.level());

        if (!definition.skill().activate(context)) {
            source.sendFailure(Component.literal("Failed to cast skill: " + definition.id()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Cast: ").append(definition.displayName()), false);
        return 1;
    }
}