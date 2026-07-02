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
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.BRDYFx;
import data.scripts.util.BRDYSettings;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class ScalarCloakStats extends BaseShipSystemScript {

    // Sucking effect constants
    private static final float ANGLE_FORCE_MULTIPLIER = 0.5f;

    private static final float DAMAGE_MOD_VS_CAPITAL = 0.15f;
    private static final float DAMAGE_MOD_VS_CRUISER = 0.20f;
    private static final float DAMAGE_MOD_VS_DESTROYER = 0.45f;
    private static final float DAMAGE_MOD_VS_FIGHTER = 0.95f;
    private static final float DAMAGE_MOD_VS_FRIGATE = 0.9f;

    private static final Map<DamageType, Float> DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS = new HashMap<>(5);

    // Distortion constants
    private static final float DISTORTION_AMOUNT = 15f;
    private static final float DISTORTION_SIZE = 300f;

    // Explosion effect constants
    private static final Color EXPLOSION_COLOR = new Color(160, 255, 114);
    private static final float EXPLOSION_DAMAGE_AMOUNT = 1000f;
    private static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
    private static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .11f;
    private static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 750f;
    private static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .05f;
    private static final float EXPLOSION_RADIUS = 650f;
    private static final String EXPLOSION_SOUND = "system_scloak_explode";
    private static final float EXPLOSION_VISUAL_RADIUS = 420f;
    private static final float MAX_EXPLOSION_RADIUS = 1020.0f;

    // Explosion scaling constants
    private static final float MAX_POWER_MULTIPLIER = 10.0f;
    private static final float MAX_RANGE_MULTIPLIER = 10f;
    private static final float MIN_POWER_MULTIPLIER = 0.3f;
    private static final float POWER_PER_FRIENDLY_DAMAGE_ABSORBED = 0.00025f;
    private static final float POWER_PER_HOSTILE_DAMAGE_ABSORBED = 0.0005f;

    // Projectile absorbtion spark constants
    private static final float SPARK_BRIGHTNESS = 1.8f;
    private static final Color SPARK_COLOR = new Color(59, 242, 152);
    private static final float SPARK_DURATION = 0.3f;
    private static final float SPARK_RADIUS = 9f;
    private static final float TEXT_AMOUNT_MULTIPLIER = 1000.0f;

    // Absorbed damage text display constants
    private static final Color TEXT_COLOR = new Color(60, 255, 245);

    private static final float VELOCITY_FORCE_MULTIPLIER = 1000f;

    private static final Vector2f ZERO = new Vector2f();

    static {
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.ENERGY, 1.05f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.FRAGMENTATION, 0.4f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.HIGH_EXPLOSIVE, 1.0f);
        DAMAGE_TYPE_POWER_ABSORBTION_MULTIPLIERS.put(DamageType.KINETIC, 1.0f);
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

        stats.getAcceleration().modifyPercent(id, 200f * effectLevel);
        stats.getTurnAcceleration().modifyPercent(id, 200f * effectLevel);
        stats.getMaxSpeed().modifyPercent(id, 200f * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, 200f * effectLevel);

        ship = (ShipAPI) stats.getEntity();
        engine = Global.getCombatEngine();

        if (wave == null) {
            wave = new WaveDistortion(ship.getLocation(), ZERO);

            wave.setIntensity(DISTORTION_AMOUNT);
            wave.setSize(DISTORTION_SIZE);
            wave.flip(true);
            wave.fadeInIntensity(0.1f);

            if (BRDYSettings.graphicsLibDistortionsEnabled()) {
                DistortionShader.addDistortion(wave);
            }
        } else {
            wave.setLocation(ship.getLocation());
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

            if (state != State.OUT && MathUtils.getDistance(ship, proj) <= ship.getCollisionRadius()) {
                absorbProjectile(proj);
                continue;
            }

            suckInProjectile(proj, state, effectLevel);
        }

        if (state == State.OUT) {
            if (wave != null) {
                wave.fadeOutIntensity(0.5f);
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
        power = (float) Math.sqrt(power);

        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS * power,
                              0.21f * power);
        engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS * power /
                              2f, 0.19f * power);

        Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1f, power, ship.getLocation(), ship.getVelocity());

        float explosionRadius = Math.min(MAX_EXPLOSION_RADIUS, EXPLOSION_RADIUS * power);

        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(), explosionRadius);
        int size = entities.size();
        for (int i = 0; i < size; i++) {
            CombatEntityAPI tmp = entities.get(i);
            if (tmp == ship) {
                continue;
            }

            float mod = 1f - (MathUtils.getDistance(ship, tmp) / explosionRadius);
            mod *= power;
            float damage = EXPLOSION_DAMAGE_AMOUNT * mod;
            float emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

            if (tmp instanceof ShipAPI) {
                ShipAPI victim = (ShipAPI) tmp;

                // Modify push strength based on ship class
                if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                    damage /= DAMAGE_MOD_VS_FIGHTER;
                } else if (victim.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                    damage /= DAMAGE_MOD_VS_FRIGATE;
                } else if (victim.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                    damage /= DAMAGE_MOD_VS_DESTROYER;
                } else if (victim.getHullSize() == ShipAPI.HullSize.CRUISER) {
                    damage /= DAMAGE_MOD_VS_CRUISER;
                } else if (victim.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                    damage /= DAMAGE_MOD_VS_CAPITAL;
                }

                if (victim.getOwner() == ship.getOwner()) {
                    damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                    emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                }

                ShipAPI empTarget = victim;
                for (int x = 0; x < 4; x++) {
                    engine.spawnEmpArc(ship, ship.getLocation(), empTarget, empTarget, EXPLOSION_DAMAGE_TYPE, damage /
                                       10, emp / 5, explosionRadius * 3, null,
                                       20 * power, EXPLOSION_COLOR, EXPLOSION_COLOR);

                    engine.spawnEmpArc(ship, MathUtils.getRandomPointInCircle(victim.getLocation(),
                                                                              victim.getCollisionRadius()), empTarget,
                                       empTarget,
                                       EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5, explosionRadius, null, 10f * power,
                                       EXPLOSION_COLOR, EXPLOSION_COLOR);
                }
            }
        }
    }

    private void suckInProjectile(DamagingProjectileAPI proj, State state, float effectLevel) {
        if (ship == null) {
            return;
        }

        float fromToAngle = VectorUtils.getAngle(ship.getLocation(), proj.getLocation());
        float angleDif = MathUtils.getShortestRotation(fromToAngle, MathUtils.clampAngle(proj.getFacing() + 180));
        float amount = Global.getCombatEngine().getElapsedInLastFrame();
        float distance = MathUtils.getDistance(ship.getLocation(), proj.getLocation());
        float force = (ship.getCollisionRadius() / distance) * effectLevel * ANGLE_FORCE_MULTIPLIER;
        float dAngle = angleDif * amount * force;

        if (proj instanceof MissileAPI && proj.getOwner() != ship.getOwner()) {
            ((MissileAPI) proj).flameOut();
        }

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
