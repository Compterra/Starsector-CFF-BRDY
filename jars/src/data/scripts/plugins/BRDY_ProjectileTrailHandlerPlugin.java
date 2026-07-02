package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class BRDY_ProjectileTrailHandlerPlugin extends BaseEveryFrameCombatPlugin {

    private static final Vector2f ZERO = new Vector2f(0f, 0f);
    private static final Map<String, TrailSpec> TRAILS = new HashMap<>();
    private static final Map<String, SpriteAPI> TRAIL_SPRITE_CACHE = new HashMap<>();

    static {
        addTrail("ferrogun_shot", "projectile_trail_ferro", 0f, 0.15f, 0.55f,
                 13f, 7f, new Color(175, 225, 255), new Color(155, 85, 55), 0.78f,
                 100f, -75f, 0f, 1f, true, false);
        addTrail("ferrocannon_shot", "projectile_trail_ferro", 0f, 0.2f, 0.55f,
                 26f, 13f, new Color(175, 225, 255), new Color(155, 85, 55), 0.78f,
                 100f, -75f, 0f, 1f, true, false);
        addTrail("brdy_amlance_shot", "projectile_trail_smokey", 0f, 0.1f, 0.15f,
                 60f, 20f, new Color(220, 250, 255), new Color(135, 175, 215), 0.45f,
                 300f, -420f, 0f, 1f, true, false);
        addTrail("brdy_squallgun_shot", "projectile_trail_standard", 0f, 0.1f, 0.4f,
                 5f, 7f, new Color(255, 200, 70), new Color(225, 45, 15), 0.4f,
                 600f, -120f, 0f, 1f, true, false);
        addTrail("brdy_raze_shot", "projectile_trail_ferro", 0f, 0.1f, 0.6f,
                 7f, 12f, new Color(255, 170, 50), new Color(225, 15, 15), 0.4f,
                 100f, -120f, 0f, 1f, true, false);
        addTrail("brdy_bolide_shot", "projectile_trail_ferro", 0f, 0.1f, 0.55f,
                 20f, 22f, new Color(115, 235, 210), new Color(255, 235, 70), 0.2f,
                 100f, -75f, 0f, 1f, true, false);
        addTrail("brdy_comet_shot", "projectile_trail_ferro", 0f, 0.1f, 0.5f,
                 8f, 12f, new Color(115, 235, 210), new Color(255, 235, 70), 0.2f,
                 100f, -75f, 0f, 1f, true, false);
        addTrail("stinger_shot", "projectile_trail_ferro", 0f, 0.1f, 0.4f,
                 20f, 10f, new Color(100, 165, 255), new Color(255, 35, 245), 0.35f,
                 100f, -75f, 0f, 1f, true, false);
    }

    private final Map<DamagingProjectileAPI, Float> projectileTrailIDs = new WeakHashMap<>();
    private final Map<DamagingProjectileAPI, Float> projectileTrailIDs2 = new WeakHashMap<>();
    private CombatEngineAPI engine;

    private static void addTrail(String projectileId, String spriteName, float durationIn, float durationMain,
                                 float durationOut, float startSize, float endSize, Color startColor,
                                 Color endColor, float opacity, float loopLength, float scrollSpeed,
                                 float spawnOffset, float lateralCompensation, boolean fadeOutFadesTrail,
                                 boolean angleAdjustment) {
        TRAILS.put(projectileId, new TrailSpec(spriteName, durationIn, durationMain, durationOut, startSize, endSize,
                                               startColor, endColor, opacity, GL_SRC_ALPHA, GL_ONE, loopLength,
                                               scrollSpeed, spawnOffset, lateralCompensation, fadeOutFadesTrail,
                                               angleAdjustment));
    }

    private static SpriteAPI getTrailSprite(String specID, TrailSpec trail) {
        SpriteAPI sprite = TRAIL_SPRITE_CACHE.get(specID);
        if (sprite == null) {
            sprite = Global.getSettings().getSprite("brdy_fx", trail.spriteName);
            TRAIL_SPRITE_CACHE.put(specID, sprite);
        }
        return sprite;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        projectileTrailIDs.clear();
        projectileTrailIDs2.clear();
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) {
            return;
        }

        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            String specID = proj.getProjectileSpecId();
            if (specID == null || proj.didDamage()) {
                continue;
            }

            TrailSpec trail = TRAILS.get(specID);
            if (trail == null) {
                continue;
            }

            SpriteAPI spriteToUse = getTrailSprite(specID, trail);
            Vector2f projVel = new Vector2f(proj.getVelocity());

            if (trail.angleAdjustment && projVel.length() > 0.1f
                    && ProjectileSpawnType.BALLISTIC_AS_BEAM != proj.getSpawnType()) {
                proj.setFacing(VectorUtils.getAngle(ZERO, projVel));
            }

            Float trailId = projectileTrailIDs.get(proj);
            if (trailId == null) {
                trailId = MagicTrailPlugin.getUniqueID();
                projectileTrailIDs.put(proj, trailId);

                if (projVel.length() < 0.1f && proj.getSource() != null) {
                    projVel = new Vector2f(proj.getSource().getVelocity());
                }
            }

            float facing = proj.getFacing();
            Vector2f spawnPosition = getSpawnPosition(proj, facing, trail.spawnOffset);
            Vector2f sidewayVel = getLateralCompensation(projVel, facing, trail.lateralCompensation);
            float opacityMult = getOpacityMult(proj, trail);

            if ("ferrocannon_shot".equals(specID)) {
                addFerrocannonSmoke(proj, trailId, spriteToUse, spawnPosition, facing, sidewayVel, opacityMult, trail);
            } else {
                MagicTrailPlugin.addTrailMemberAdvanced(proj, trailId, spriteToUse, spawnPosition, 0f, 0f,
                                                        facing - 180f, 0f, 0f, trail.startSize, trail.endSize,
                                                        trail.startColor, trail.endColor, trail.opacity * opacityMult,
                                                        trail.durationIn, trail.durationMain, trail.durationOut,
                                                        trail.blendSrc, trail.blendDest, trail.loopLength,
                                                        trail.scrollSpeed, sidewayVel, null);
            }
        }
    }

    private static Vector2f getSpawnPosition(DamagingProjectileAPI proj, float facing, float spawnOffset) {
        float radians = (float) Math.toRadians(facing);
        return new Vector2f(proj.getLocation().x + (float) Math.cos(radians) * spawnOffset,
                            proj.getLocation().y + (float) Math.sin(radians) * spawnOffset);
    }

    private static Vector2f getLateralCompensation(Vector2f projVel, float facing, float compensation) {
        Vector2f projBodyVel = VectorUtils.rotate(projVel, -facing);
        Vector2f projLateralBodyVel = new Vector2f(0f, projBodyVel.getY());
        return (Vector2f) VectorUtils.rotate(projLateralBodyVel, facing).scale(compensation);
    }

    private static float getOpacityMult(DamagingProjectileAPI proj, TrailSpec trail) {
        if (!trail.fadeOutFadesTrail || !proj.isFading()) {
            return 1f;
        }

        float baseDamage = proj.getBaseDamageAmount();
        if (baseDamage <= 0f) {
            return 1f;
        }

        return Math.max(0f, Math.min(1f, proj.getDamageAmount() / baseDamage));
    }

    private void addFerrocannonSmoke(DamagingProjectileAPI proj, Float trailId, SpriteAPI spriteToUse,
                                     Vector2f spawnPosition, float facing, Vector2f sidewayVel, float opacityMult,
                                     TrailSpec trail) {
        MagicTrailPlugin.addTrailMemberAdvanced(proj, trailId, spriteToUse, spawnPosition, 0f,
                                                MathUtils.getRandomNumberInRange(0f, 105f), facing - 180f, 0f,
                                                MathUtils.getRandomNumberInRange(-330f, 330f), trail.startSize,
                                                trail.endSize, trail.startColor, new Color(110, 95, 85),
                                                0.4f * opacityMult, trail.durationIn, 0.2f,
                                                trail.durationOut + 0.2f, trail.blendSrc, trail.blendDest,
                                                trail.loopLength, trail.scrollSpeed, sidewayVel, null);

        Float secondaryTrailId = projectileTrailIDs2.get(proj);
        if (secondaryTrailId == null) {
            secondaryTrailId = MagicTrailPlugin.getUniqueID();
            projectileTrailIDs2.put(proj, secondaryTrailId);
        }

        MagicTrailPlugin.addTrailMemberAdvanced(proj, secondaryTrailId, spriteToUse, spawnPosition, 0f,
                                                MathUtils.getRandomNumberInRange(0f, 105f), facing - 180f, 0f,
                                                MathUtils.getRandomNumberInRange(-330f, 330f), trail.startSize,
                                                trail.endSize, trail.startColor, new Color(110, 90, 25),
                                                0.4f * opacityMult, trail.durationIn, 0.2f,
                                                trail.durationOut + 0.2f, trail.blendSrc, trail.blendDest,
                                                trail.loopLength, trail.scrollSpeed, sidewayVel, null);
    }

    private static final class TrailSpec {
        private final String spriteName;
        private final float durationIn;
        private final float durationMain;
        private final float durationOut;
        private final float startSize;
        private final float endSize;
        private final Color startColor;
        private final Color endColor;
        private final float opacity;
        private final int blendSrc;
        private final int blendDest;
        private final float loopLength;
        private final float scrollSpeed;
        private final float spawnOffset;
        private final float lateralCompensation;
        private final boolean fadeOutFadesTrail;
        private final boolean angleAdjustment;

        private TrailSpec(String spriteName, float durationIn, float durationMain, float durationOut, float startSize,
                          float endSize, Color startColor, Color endColor, float opacity, int blendSrc, int blendDest,
                          float loopLength, float scrollSpeed, float spawnOffset, float lateralCompensation,
                          boolean fadeOutFadesTrail, boolean angleAdjustment) {
            this.spriteName = spriteName;
            this.durationIn = durationIn;
            this.durationMain = durationMain;
            this.durationOut = durationOut;
            this.startSize = startSize;
            this.endSize = endSize;
            this.startColor = startColor;
            this.endColor = endColor;
            this.opacity = opacity;
            this.blendSrc = blendSrc;
            this.blendDest = blendDest;
            this.loopLength = loopLength;
            this.scrollSpeed = scrollSpeed;
            this.spawnOffset = spawnOffset;
            this.lateralCompensation = lateralCompensation;
            this.fadeOutFadesTrail = fadeOutFadesTrail;
            this.angleAdjustment = angleAdjustment;
        }
    }
}