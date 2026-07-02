package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class brdy_SueprscalarEveryFrame implements EveryFrameWeaponEffectPlugin {

    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 360f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.6f;
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 10f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 150f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 30f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 10f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 5f;
    private static final Color EXPLOSION_COLOR = new Color(150, 255, 25, 100);
    private static final Color EXPLOSION_COLOR_ALT = new Color(100, 255, 150, 100);
    private static final float EXPLOSION_VISUAL_RADIUS = 300f;
    private static final Color GLOWCOLOR = new Color(150, 255, 25, 255);
    private static final Color GLOWCOLOR2 = new Color(172, 255, 135, 255);
    private static final Color GLOWCOLOR_ALT = new Color(49, 255, 210, 255);
    private static final Color GLOWCOLOR_ALT2 = new Color(125, 255, 235, 255);

    public static Color colorBlend(Color a, Color b, float amount) {
        float realAmount = Math.max(Math.min(amount, 1f), 0f);
        float conjAmount = 1f - realAmount;
        return new Color((int) Math.max(0, Math.min(255, a.getRed() * conjAmount + b.getRed() * realAmount)),
                         (int) Math.max(0, Math.min(255, a.getGreen() * conjAmount + b.getGreen() * realAmount)),
                         (int) Math.max(0, Math.min(255, a.getBlue() * conjAmount + b.getBlue() * realAmount)),
                         (int) Math.max(0, Math.min(255, a.getAlpha() * conjAmount + b.getAlpha() * realAmount)));
    }

    private boolean charging = false;
    private boolean firing = false;
    private float lastChargeLevel = 0f;
    private float lastChargeLevelLatch = 0f;
    private SoundAPI sound = null;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        float chargeLevel = weapon.getChargeLevel();
        Vector2f origin = new Vector2f(weapon.getLocation());
        ShipAPI ship = weapon.getShip();
        float shipFacing = weapon.getCurrAngle();
        Vector2f shipVelocity = ship.getVelocity();

        float powerLevel = 1f * ship.getFluxTracker().getFluxLevel() + 0.5f;

        if (weapon.isFiring()) {
            ship.getMutableStats().getAcceleration().modifyMult("brdy_superscalarbeam", 1f - 0.7f * chargeLevel *
                                                                powerLevel);
            ship.getMutableStats().getMaxSpeed().modifyMult("brdy_superscalarbeam", 1f - 0.1f * chargeLevel * powerLevel);
            ship.getMutableStats().getMaxTurnRate().modifyMult("brdy_superscalarbeam", 1f - 0.1f * chargeLevel *
                                                               powerLevel);
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult("brdy_superscalarbeam", 1f -
                                                                        0.5f * chargeLevel * powerLevel);
            ship.getMutableStats().getHullDamageTakenMult().modifyMult("brdy_superscalarbeam", 1f - 0.5f * chargeLevel *
                                                                       powerLevel);
            ship.getMutableStats().getEmpDamageTakenMult().modifyMult("brdy_superscalarbeam", 1f - 0.5f * chargeLevel *
                                                                      powerLevel);
            ship.getMutableStats().getBeamWeaponDamageMult().modifyMult("brdy_superscalarbeam", 1f + 1f * chargeLevel *
                                                                        (powerLevel - 1f));
        } else {
            ship.getMutableStats().getAcceleration().unmodify("brdy_superscalarbeam");
            ship.getMutableStats().getMaxSpeed().unmodify("brdy_superscalarbeam");
            ship.getMutableStats().getMaxTurnRate().unmodify("brdy_superscalarbeam");
            ship.getMutableStats().getArmorDamageTakenMult().unmodify("brdy_superscalarbeam");
            ship.getMutableStats().getHullDamageTakenMult().unmodify("brdy_superscalarbeam");
            ship.getMutableStats().getEmpDamageTakenMult().unmodify("brdy_superscalarbeam");
            ship.getMutableStats().getBeamWeaponDamageMult().unmodify("brdy_superscalarbeam");
        }

        if (charging) {
            if (firing && chargeLevel < 1f) {
                charging = false;
                firing = false;
                if (ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux() <= 100f) {
                    Global.getSoundPlayer().playSound("voidbuster_shutdown", 1f, 1f * powerLevel, origin, shipVelocity);
                    engine.addFloatingText(ship.getLocation(), "Safety Shutdown!", 24f, Color.RED, ship, 2f, 1f);
                    weapon.disable();
                } else {
                    Global.getSoundPlayer().playSound("voidbuster_off", 1f, 1f * powerLevel, origin, shipVelocity);
                }
            } else if (chargeLevel < 1f && weapon.isFiring()) {
                int particleCount = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * chargeLevel * powerLevel);
                float distance, size, angle, speed;
                float remainingDuration = Math.max((1.2f - chargeLevel) * powerLevel, 0.1f);
                Vector2f particleVelocity;
                for (int i = 0; i < particleCount; ++i) {
                    distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN,
                                                                CHARGEUP_PARTICLE_DISTANCE_MAX) * powerLevel;
                    size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX) *
                    powerLevel;
                    angle = MathUtils.getRandomNumberInRange(-0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD, 0.5f *
                                                             CHARGEUP_PARTICLE_ANGLE_SPREAD);
                    Vector2f spawnLocation = MathUtils.getPointOnCircumference(origin, distance, (angle + shipFacing));
                    speed = distance / remainingDuration;
                    particleVelocity = MathUtils.getPointOnCircumference(shipVelocity, speed, 180.0f + angle +
                                                                         shipFacing);
                    engine.addHitParticle(spawnLocation, particleVelocity, size, CHARGEUP_PARTICLE_BRIGHTNESS, Math.min(
                                          remainingDuration, 0.5f),
                                          colorBlend(GLOWCOLOR, GLOWCOLOR_ALT, powerLevel - 0.5f));
                }
                if (Math.random() > 0.75) {
                    Vector2f point1 = MathUtils.getRandomPointInCircle(origin, (float) Math.random() * chargeLevel *
                                                                       powerLevel * 50f + 50f);
                    engine.spawnEmpArc(ship, origin, new SimpleEntity(origin), new SimpleEntity(point1),
                                       DamageType.ENERGY, 0f, 0f, 1000f, null,
                                       chargeLevel * powerLevel * 5f + 5f, colorBlend(GLOWCOLOR, GLOWCOLOR_ALT,
                                                                                      powerLevel - 0.5f),
                                       colorBlend(GLOWCOLOR2, GLOWCOLOR_ALT2, powerLevel - 0.5f));
                }
            } else if (!firing && lastChargeLevelLatch < 0.5f && chargeLevel >= 1f) {
                engine.spawnExplosion(origin, shipVelocity, colorBlend(EXPLOSION_COLOR, EXPLOSION_COLOR_ALT,
                                                                       powerLevel - 0.5f),
                                      EXPLOSION_VISUAL_RADIUS * powerLevel, 0.21f * powerLevel);
                Global.getSoundPlayer().playSound("voidbuster_fire", 1f, 1f * powerLevel, origin, shipVelocity);
                firing = true;
                sound = null;
                lastChargeLevelLatch = chargeLevel;
            } else if (chargeLevel < lastChargeLevel) {
                charging = false;
                firing = false;
                if (sound != null) {
                    sound.stop();
                    sound = null;
                }
                lastChargeLevelLatch = chargeLevel;
                if (ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux() <= 500f) {
                    engine.addFloatingText(ship.getLocation(), "Shutdown!", 24f, Color.RED, ship, 2f, 1f);
                    weapon.disable();
                }
            }
        } else {
            if (chargeLevel > 0f && weapon.isFiring() && chargeLevel > lastChargeLevel) {
                charging = true;
                sound = Global.getSoundPlayer().playSound("voidbuster_charge", 1f, 1f * powerLevel, origin,
                                                          weapon.getShip().getVelocity());
            }
        }

        lastChargeLevel = chargeLevel;
        if (chargeLevel <= lastChargeLevelLatch) {
            lastChargeLevelLatch = chargeLevel;
        }
    }
}
