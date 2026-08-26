package njw.net.justskills.skill.cast;

public record CastSpec(
        int durationTicks,
        double maxDisplacement,
        boolean lockMovement,
        boolean showBossBar
) {

    public static CastSpec instant() {
        return new CastSpec(
                0,
                Double.POSITIVE_INFINITY,
                false,
                false
        );
    }

    public static CastSpec stationary(
            int durationTicks,
            double maxDisplacement
    ) {
        return new CastSpec(
                durationTicks,
                maxDisplacement,
                true,
                true
        );
    }

    public boolean isInstant() {
        return durationTicks <= 0;
    }
}