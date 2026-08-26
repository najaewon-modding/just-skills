package njw.net.justskills.client.hud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import njw.net.justskills.JustSkills;
import njw.net.justskills.data.ModAttachments;
import njw.net.justskills.data.PlayerSkillState;
import njw.net.justskills.skill.SkillDefinition;
import njw.net.justskills.skill.SkillManager;
import njw.net.justskills.skill.SkillRegistry;

import java.util.Optional;

@EventBusSubscriber(
        modid = JustSkills.MODID,
        value = Dist.CLIENT
)
public final class SkillHud {

    private SkillHud() {
    }

    /*
     * ============================================================
     * Layer
     * ============================================================
     */

    private static final Identifier LAYER_ID =
            Identifier.fromNamespaceAndPath(
                    JustSkills.MODID,
                    "skill_hud"
            );

    /*
     * ============================================================
     * Layout
     * ============================================================
     */

    private static final int SLOT_SIZE =
            26;

    /*
     * 액자 내부.
     */
    private static final int INNER_OFFSET =
            4;

    private static final int INNER_SIZE =
            SLOT_SIZE
                    - INNER_OFFSET * 2;

    /*
     * 기본 Minecraft Item은 16 x 16.
     */
    private static final int ITEM_OFFSET =
            5;

    private static final int RIGHT_MARGIN =
            8;

    private static final int BOTTOM_MARGIN =
            8;

    /*
     * ============================================================
     * Colors
     * ============================================================
     */

    private static final int OUTLINE_COLOR =
            0xFF2B1A0E;

    private static final int FRAME_OUTER_COLOR =
            0xE66B4526;

    private static final int FRAME_INNER_COLOR =
            0xE6A66B35;

    private static final int SLOT_BACKGROUND_COLOR =
            0xD91A1A1A;

    /*
     * 약 60% 불투명 검정.
     */
    private static final int COOLDOWN_OVERLAY_COLOR =
            0x99000000;

    /*
     * ============================================================
     * Register
     * ============================================================
     */

    @SubscribeEvent
    public static void registerGuiLayers(
            RegisterGuiLayersEvent event
    ) {

        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                LAYER_ID,
                SkillHud::render
        );
    }

    /*
     * ============================================================
     * Render
     * ============================================================
     */

    private static void render(
            GuiGraphicsExtractor graphics,
            DeltaTracker deltaTracker
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();

        if (minecraft.player == null
                || minecraft.level == null) {

            return;
        }

        int x =
                graphics.guiWidth()
                        - RIGHT_MARGIN
                        - SLOT_SIZE;

        int y =
                graphics.guiHeight()
                        - BOTTOM_MARGIN
                        - SLOT_SIZE;

        /*
         * ========================================================
         * Frame
         * ========================================================
         */

        graphics.fill(
                x,
                y,
                x + SLOT_SIZE,
                y + SLOT_SIZE,
                FRAME_OUTER_COLOR
        );

        graphics.fill(
                x + 2,
                y + 2,
                x + SLOT_SIZE - 2,
                y + SLOT_SIZE - 2,
                FRAME_INNER_COLOR
        );

        graphics.fill(
                x + INNER_OFFSET,
                y + INNER_OFFSET,
                x + SLOT_SIZE - INNER_OFFSET,
                y + SLOT_SIZE - INNER_OFFSET,
                SLOT_BACKGROUND_COLOR
        );

        graphics.outline(
                x,
                y,
                SLOT_SIZE,
                SLOT_SIZE,
                OUTLINE_COLOR
        );

        /*
         * ========================================================
         * State
         * ========================================================
         */

        PlayerSkillState state =
                minecraft.player.getData(
                        ModAttachments
                                .PLAYER_SKILL_STATE
                                .get()
                );

        /*
         * ========================================================
         * Current skill
         * ========================================================
         */

        if (state.currentSkill()
                .isPresent()) {

            renderSkillItem(
                    graphics,
                    state.currentSkill()
                            .get(),
                    x,
                    y
            );

            return;
        }

        /*
         * ========================================================
         * Cooldown
         * ========================================================
         *
         * 스킬 사용 직후에는 아이템은 이미 없고,
         * 검은 overlay만 남는다.
         */

        renderCooldown(
                graphics,
                minecraft,
                state,
                x,
                y
        );
    }

    /*
     * ============================================================
     * Item
     * ============================================================
     */

    private static void renderSkillItem(
            GuiGraphicsExtractor graphics,
            Identifier skillId,
            int x,
            int y
    ) {

        Optional<SkillDefinition> definition =
                SkillRegistry.get(
                        skillId
                );

        if (definition.isEmpty()) {
            return;
        }

        ItemStack icon =
                definition.get()
                        .iconStack();

        graphics.item(
                icon,
                x + ITEM_OFFSET,
                y + ITEM_OFFSET
        );
    }

    /*
     * ============================================================
     * Cooldown
     * ============================================================
     */

    private static void renderCooldown(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            PlayerSkillState state,
            int x,
            int y
    ) {

        long now =
                minecraft.level
                        .getGameTime();

        long remainingTicks =
                state.cooldownEndTick()
                        - now;

        /*
         * 쿨다운이 끝났다면
         * 아무 overlay도 표시하지 않는다.
         *
         * 빈 액자 상태.
         */
        if (remainingTicks <= 0L) {
            return;
        }

        double cooldownProgress =
                Math.clamp(
                        remainingTicks
                                / (double)
                                SkillManager.SKILL_COOLDOWN_TICKS,

                        0.0,
                        1.0
                );

        /*
         * 10초 시작:
         *
         * overlayHeight = 전체
         *
         * 5초:
         *
         * 절반
         *
         * 종료:
         *
         * 0
         */
        int overlayHeight =
                (int)
                        Math.ceil(
                                INNER_SIZE
                                        * cooldownProgress
                        );

        if (overlayHeight <= 0) {
            return;
        }

        int innerLeft =
                x + INNER_OFFSET;

        int innerTop =
                y + INNER_OFFSET;

        int innerRight =
                innerLeft
                        + INNER_SIZE;

        int innerBottom =
                innerTop
                        + INNER_SIZE;

        /*
         * 아래쪽부터 검은 영역이 줄어드는 형태.
         */
        int overlayTop =
                innerBottom
                        - overlayHeight;

        graphics.fill(
                innerLeft,
                overlayTop,
                innerRight,
                innerBottom,
                COOLDOWN_OVERLAY_COLOR
        );
    }
}