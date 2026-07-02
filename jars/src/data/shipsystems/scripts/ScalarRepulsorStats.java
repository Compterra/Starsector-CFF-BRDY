package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.BRDYFx;
import data.scripts.util.BRDYSettings;
import data.scripts.util.BRDYMulti;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class ScalarRepulsorStats extends BaseShipSystemScript {

    // Sucking effect constants
    private static final float ANGLE_FORCE_MULTIPLIER = 1f;

    private static final Map<DamageType, Float> DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS = new HashMap<>(5);

    // Distortion constants
    private static final float DISTORTION_AMOUNT = 25f;

    // Explosion effect constants
    private static final Color EXPLOSION_COLOR = new Color(160, 255, 114, 50);
    private static final float EXPLOSION_PULSES_PER_POWER = 5.5f;
    private static final float EXPLOSION_RADIUS = 700f;
    private static final String EXPLOSION_SOUND = "morpheusrepulsor";
    private static final float EXPLOSION_VISUAL_RADIUS = 450f;
    private static final float MAX_EXPLOSION_RADIUS = 3000.0f;

    // Explosion scaling constants
    private static final float MAX_POWER_MULTIPLIER = 15.0f;
    private static final float MAX_RANGE_MULTIPLIER = 10f;
    private static final float MIN_POWER_MULTIPLIER = 0.5f;
    private static final float POWER_PER_FRIENDLY_DAMAGE_ABSORBED = 0.0004f;
    private static final float POWER_PER_HOSTILE_DAMAGE_ABSORBED = 0.0006f;

    // Projectile absorbtion spark constants
    private static final float SPARK_BRIGHTNESS = 1.8f;
    private static final Color SPARK_COLOR = new Color(59, 242, 152);
    private static final float SPARK_DURATION = 0.4f;
    private static final float SPARK_RADIUS = 10f;
    private static final float TEXT_AMOUNT_MULTIPLIER = 1000.0f;

    // Absorbed damage text display constants
    private static final Color TEXT_COLOR = new Color(60, 255, 245);

    private static final float VELOCITY_FORCE_MULTIPLIER = 2000f;

    private static final Vector2f ZERO = new Vector2f();

    static {
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.ENERGY, 1.05f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.FRAGMENTATION, 0.4f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.HIGH_EXPLOSIVE, 1.0f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.KINETIC, 0.90f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.OTHER, 0.5f);
    }

    // Local variables, don't touch these
    private float absorbedPower = 0;
    private CombatEngineAPI engine;
    private ShipAPI ship;
    private WaveDistortion wave = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        // instanceof also acts as a null check
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        stats.getAcceleration().modifyPercent(id, 100f * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
        stats.getMaxSpeed().modifyPercent(id, 50f * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);

        ship = (ShipAPI) stats.getEntity();
        engine = Global.getCombatEngine();

        if (wave == null) {
            wave = new WaveDistortion(ship.getLocation(), ZERO);

            wave.setIntensity(DISTORTION_AMOUNT);
            wave.setSize(ship.getCollisionRadius() * MAX_RANGE_MULTIPLIER / 2f);
            wave.setArc(ship.getFacing() - 65f, ship.getFacing() + 65f);
            wave.setArcAttenuationWidth(10f);
            wave.flip(true);
            wave.fadeInIntensity(0.1f);

            if (BRDYSettings.graphicsLibDistortionsEnabled()) {
                DistortionShader.addDistortion(wave);
            }
        } else {
            wave.setLocation(ship.getLocation());
            wave.setArc(ship.getFacing() - 65f, ship.getFacing() + 65f);
        }

        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(),
                                                                            ship.getCollisionRadius() *
                                                                            MAX_RANGE_MULTIPLIER);
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            CombatEntityAPI entity = entities.get(i);
            if (!(entity instanceof DamagingProjectileAPI)) {
                continue;
            }

            DamagingProjectileAPI proj = (DamagingProjectileAPI) entity;

            float angleDiff = Math.abs(MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(
                                                                     ship.getLocation(), proj.getLocation())));

            if (state != State.OUT && MathUtils.getDistance(ship, proj) <= ship.getCollisionRadius() && angleDiff <= 65f) {
                absorbProjectile(proj);
                continue;
            }

            if (state != State.OUT) {
                suckInProjectile(proj, state, effectLevel);
            }
        }

        if (state == State.OUT) {
            if (wave != null) {
                wave.fadeOutIntensity(0.3f);
                wave = null;
            }
            ship.setPhased(false);
        } else {
            ship.setPhased(true);
        }

        if (state == State.OUT && absorbedPower > 0) {
            doASplosion();
            absorbedPower = 0;
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased speed and maneuverability", false);
        } else {
            return null;
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ship = (ShipAPI) stats.getEntity();
        ship.setPhased(false);
    }

    private void absorbProjectile(DamagingProjectileAPI proj) {
        if (ship == null || engine == null) {
            return;
        }
        float powerAbsorbed = proj.getDamageAmount();
        powerAbsorbed *= (proj.getOwner() == ship.getOwner()) ? POWER_PER_FRIENDLY_DAMAGE_ABSORBED :
                         POWER_PER_HOSTILE_DAMAGE_ABSORBED;
        powerAbsorbed *= DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.get(proj.getDamageType());

        engine.addFloatingDamageText(ship.getLocation(), powerAbsorbed * TEXT_AMOUNT_MULTIPLIER, TEXT_COLOR, ship, proj);

        absorbedPower += powerAbsorbed;

        float sparkAngle = VectorUtils.getAngle(proj.getLocation(), ship.getLocation());
        sparkAngle *= Math.PI / 180f;
        Vector2f sparkVect = new Vector2f((float) Math.cos(sparkAngle), (float) Math.sin(sparkAngle));
        float distance = MathUtils.getDistance(proj, ship);
        float visualEffect = (float) Math.sqrt(powerAbsorbed * 1000);

        sparkVect.scale(3 * distance / SPARK_DURATION);

        Global.getSoundPlayer().playSound("system_scloak_absorb", 1, visualEffect, proj.getLocation(), sparkVect);

        BRDYFx.addHitBurst(engine, proj.getLocation(), sparkVect, SPARK_COLOR, 1,
                           SPARK_RADIUS * visualEffect + SPARK_RADIUS,
                           SPARK_RADIUS * visualEffect + SPARK_RADIUS, SPARK_BRIGHTNESS,
                           SPARK_DURATION, SPARK_DURATION, 0f,
                           CombatEngineLayers.ABOVE_PARTICLES_LOWER);
        engine.removeEntity(proj);
    }

    private void doASplosion() {
        if (ship == null || engine == null) {
            return;
        }

        float power = absorbedPower;
        power = Math.max(power, MIN_POWER_MULTIPLIER);
        power = Math.min(power, MAX_POWER_MULTIPLIER);

        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS *
                              (float) Math.sqrt(power), 0.21f *
                              (float) Math.sqrt(
                                      power));
        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS *
                              (float) Math.sqrt(power) / 2f, 0.19f *
                              (float) Math.sqrt(
                                      power));

        Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1.0f / (float) Math.pow(power, 0.25), power / 2f,
                                          ship.getLocation(), ship.getVelocity());

        float explosionRadius = Math.min(MAX_EXPLOSION_RADIUS, EXPLOSION_RADIUS * power);

        RippleDistortion ripple = new RippleDistortion(ship.getLocation(), ZERO);

        ripple.setIntensity(DISTORTION_AMOUNT * 5f * (float) Math.sqrt(power));
        ripple.setSize(explosionRadius * 1.5f);
        ripple.setArc(ship.getFacing() + 180f - 65f, ship.getFacing() + 180f + 65f);
        ripple.setArcAttenuationWidth(10f);
        ripple.fadeInSize(0.35f);
        ripple.setFrameRate(RippleDistortion.FRAMES / 0.35f);
        ripple.fadeOutIntensity(0.35f);

        if (BRDYSettings.graphicsLibDistortionsEnabled()) {
            DistortionShader.addDistortion(ripple);
        }

        WeaponAPI weapon = null;
        for (WeaponAPI wep : ship.getAllWeapons()) {
            if (wep.getId().contentEquals("homing_laser")) {
                weapon = wep;
                break;
            }
        }

        List<DamagingProjectileAPI> projectiles = CombatUtils.getProjectilesWithinRange(ship.getLocation(),
                                                                                        explosionRadius);
        int size = projectiles.size();
        for (int i = 0; i < size; i++) {
            DamagingProjectileAPI proj = projectiles.get(i);

            float fromToAngle = VectorUtils.getAngle(ship.getLocation(), proj.getLocation());
            float angleDif = MathUtils.getShortestRotation(fromToAngle, ship.getFacing());
            float amount = Global.getCombatEngine().getElapsedInLastFrame();
            float distance = MathUtils.getDistance(ship.getLocation(), proj.getLocation());
            float force = (ship.getCollisionRadius() / distance) * ANGLE_FORCE_MULTIPLIER;
            if (Math.abs(angleDif) >= 70f) {
                continue;
            } else if (Math.abs(angleDif) >= 60f) {
                force *= (70f - Math.abs(angleDif)) / 10f;
            }

            if (proj instanceof MissileAPI && proj.getOwner() != ship.getOwner()) {
                ((MissileAPI) proj).flameOut();
            }

            float dAngle = angleDif * amount * force;
            fromToAngle *= Math.PI / 180;
            Vector2f speedUp = new Vector2f((float) Math.cos(fromToAngle) * amount, (float) Math.sin(fromToAngle) *
                                            amount);
            speedUp.scale(VELOCITY_FORCE_MULTIPLIER * power);

            Vector2f.add(proj.getVelocity(), speedUp, proj.getVelocity());
            VectorUtils.rotate(proj.getVelocity(), dAngle, proj.getVelocity());
            proj.setFacing(MathUtils.clampAngle(proj.getFacing() + dAngle * (float) (180 / Math.PI)));
        }

        for (int i = 0; i <= power * EXPLOSION_PULSES_PER_POWER; i++) {
            Vector2f origin = MathUtils.getPointOnCircumference(ship.getLocation(), MathUtils.getRandomNumberInRange(
                                                                5f * power, 30f * power),
                                                                ship.getFacing() +
                                                                MathUtils.getRandomNumberInRange(
                                                                        -65f, 65f));
            Vector2f vel = MathUtils.getRandomPointInCircle(ship.getVelocity(), 150f + power * 150f);
            engine.spawnProjectile(ship, weapon, "brdy_repulsorpulse", origin, ship.getFacing() +
                                   MathUtils.getRandomNumberInRange(-65f, 65f), vel);
        }

        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(), explosionRadius);
        size = entities.size();
        for (int i = 0; i < size; i++) {
            CombatEntityAPI tmp = entities.get(i);
            if (tmp == ship) {
                continue;
            }
            if (!BRDYMulti.isRoot(ship)) {
                continue;
            }

            float angleDiff = Math.abs(MathUtils.getShortestRotation(ship.getFacing(), VectorUtils.getAngle(
                                                                     ship.getLocation(), tmp.getLocation())));

            float mod = 1f - (MathUtils.getDistance(ship, tmp) / explosionRadius);
            if (angleDiff >= 70f) {
                continue;
            } else if (angleDiff >= 60f) {
                mod *= (70f - angleDiff) / 10f;
            }

            mod *= power;
            CombatUtils.applyForce(tmp, VectorUtils.getAngle(ship.getLocation(), tmp.getLocation()),
                                   VELOCITY_FORCE_MULTIPLIER * (float) Math.sqrt(mod) / 2f);
        }
    }

    private void suckInProjectile(DamagingProjectileAPI proj, State state, float effectLevel) {
        if (ship == null) {
            return;
        }

        float fromToAngle = VectorUtils.getAngle(ship.getLocation(), proj.getLocation());
        float angleDif = MathUtils.getShortestRotation(fromToAngle, ship.getFacing());
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        float distance = MathUtils.getDistance(ship.getLocation(), proj.getLocation());
        float force = (ship.getCollisionRadius() / distance) * effectLevel * ANGLE_FORCE_MULTIPLIER;
        if (Math.abs(angleDif) >= 70f) {
            return;
        } else if (Math.abs(angleDif) >= 60f) {
            force *= (70f - Math.abs(angleDif)) / 10f;
        }

        if (proj instanceof MissileAPI && proj.getOwner() != ship.getOwner()) {
            ((MissileAPI) proj).flameOut();
        }

        float dAngle = angleDif * amount * force;
        fromToAngle *= Math.PI / 180;
        Vector2f speedUp = new Vector2f((float) Math.cos(fromToAngle) * amount, (float) Math.sin(fromToAngle) * amount);
        speedUp.scale(VELOCITY_FORCE_MULTIPLIER);

        if (state != State.OUT) {
            dAngle = -dAngle;
            speedUp.scale(-1);
        }

        Vector2f.add(proj.getVelocity(), speedUp, proj.getVelocity());
        VectorUtils.rotate(proj.getVelocity(), dAngle, proj.getVelocity());
        proj.setFacing(MathUtils.clampAngle(proj.getFacing() + dAngle * (float) (180 / Math.PI)));
    }
}
