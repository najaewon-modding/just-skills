package njw.net.justskills.skill;

public interface Skill {

    /**
     * 실제 스킬 효과를 실행한다.
     *
     * @return 성공적으로 사용되었으면 true.
     */
    boolean activate(SkillContext context);
}