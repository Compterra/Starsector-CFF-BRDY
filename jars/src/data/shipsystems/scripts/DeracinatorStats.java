package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.BRModPlugin;
import data.scripts.util.BRDYFx;
import data.scripts.util.BRDYSettings;
import data.scripts.util.BRDYOptionalModChecks;
import data.scripts.util.AnamorphicFlare;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.WaveDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

// Phase teleporter system, with some extras
public class DeracinatorStats extends BaseShipSystemScript {

    private static final String CHARGEUP_SOUND = "system_deracinatorcharge";
    private static final float DAMAGE_MOD_VS_CAPITAL = 0.15f;
    private static final float DAMAGE_MOD_VS_CRUISER = 0.20f;
    private static final float DAMAGE_MOD_VS_DESTROYER = 0.5f;
    private static final float DAMAGE_MOD_VS_FIGHTER = 0.7f;
    private static final float DAMAGE_MOD_VS_FRIGATE = 0.8f;

    //Distortion constants
    private static final float DISTORTION_BLAST_RADIUS = 1200f;
    private static final float DISTORTION_CHARGE_RADIUS = 100f;

    // Explosion effect constants
    private static final Color EXPLOSION_COLOR = new Color(55, 160, 88);
    private static final float EXPLOSION_DAMAGE_AMOUNT = 1350f;
    private static final DamageType EXPLOSION_DAMAGE_TYPE = DamageType.ENERGY;
    private static final float EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER = .11f;
    private static final float EXPLOSION_EMP_DAMAGE_AMOUNT = 2000f;
    private static final float EXPLOSION_EMP_VS_ALLIES_MODIFIER = .05f;
    private static final float EXPLOSION_FORCE_VS_ALLIES_MODIFIER = .3f;
    private static final float EXPLOSION_PUSH_RADIUS = 800f;
    private static final String EXPLOSION_SOUND = "luciferdriveactivate";
    private static final float EXPLOSION_VISUAL_RADIUS = 1250f;
    private static final Color FLARE_COLOR = new Color(55, 242, 221);
    private static final float FORCE_VS_ASTEROID = 290f;
    private static final float FORCE_VS_CAPITAL = 35f;
    private static final float FORCE_VS_CRUISER = 60f;
    private static final float FORCE_VS_DESTROYER = 115f;
    private static final float FORCE_VS_FIGHTER = 255f;
    private static final float FORCE_VS_FRIGATE = 180f;

    private static final int MAX_PARTICLES_PER_FRAME = 30; // Based on charge level

    // "Inhale" effect constants
    private static final Color PARTICLE_COLOR = new Color(155, 240, 200);
    private static final float PARTICLE_OPACITY = 0.85f;
    private static final float PARTICLE_RADIUS = 300f;
    private static final float PARTICLE_SIZE = 6f;

    private static final Vector2f ZERO = new Vector2f();

    //Local variables, don't touch these
    private boolean isActive = false;
    private StandardLight light;
    private WaveDistortion wave;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        // instanceof also acts as a null check
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();
        // Chargeup, show particle inhalation effect
        if (state == State.IN) {
            Vector2f loc = new Vector2f(ship.getLocation());
            loc.x -= 70f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
            loc.y -= 70f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

            // Everything in this block is only done once per chargeup
            if (!isActive) {
                isActive = true;
                Global.getSoundPlayer().playSound(CHARGEUP_SOUND, 1f, 1f, ship.getLocation(), ship.getVelocity());

                light = new StandardLight(loc, ZERO, ZERO, null);
                light.setIntensity(1.25f);
                light.setSize(EXPLOSION_VISUAL_RADIUS);
                light.setColor(PARTICLE_COLOR);
                light.fadeIn(1.95f);
                light.setLifetime(0.1f);
                light.setAutoFadeOutTime(0.17f);
                if (BRDYSettings.graphicsLibLightsEnabled()) {
                    LightShader.addLight(light);
                }

                wave = new WaveDistortion(loc, ZERO);
                wave.setSize(DISTORTION_CHARGE_RADIUS);
                wave.setIntensity(DISTORTION_CHARGE_RADIUS / 4f);
                wave.fadeInSize(1.95f);
                wave.fadeInIntensity(1.95f);
                wave.setLifetime(0f);
                wave.setAutoFadeSizeTime(-0.5f);
                wave.setAutoFadeIntensityTime(0.17f);
                if (BRDYSettings.graphicsLibDistortionsEnabled()) {
                    DistortionShader.addDistortion(wave);
                }
            } else {
                light.setLocation(loc);
                wave.setLocation(loc);
            }

            // Exact amount per second doesn't matter since it's purely decorative
            int numParticlesThisFrame = Math.round(effectLevel * MAX_PARTICLES_PER_FRAME);
            BRDYFx.addInwardSmokeBurst(Global.getCombatEngine(), ship.getLocation(), PARTICLE_COLOR,
                                        numParticlesThisFrame, PARTICLE_RADIUS, PARTICLE_SIZE,
                                        PARTICLE_OPACITY, 1f, CombatEngineLayers.ABOVE_PARTICLES_LOWER);
        } // Cooldown, explode once system is finished
        else if (state == State.OUT) {
            // Everything in this section is only done once per cooldown
            if (isActive) {
                CombatEngineAPI engine = Global.getCombatEngine();
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS,
                                      0.2f);
                engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), EXPLOSION_COLOR, EXPLOSION_VISUAL_RADIUS /
                                      2f, 0.2f);

                Vector2f loc = new Vector2f(ship.getLocation());
                loc.x -= 70f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
                loc.y -= 70f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

                light = new StandardLight();
                light.setLocation(loc);
                light.setIntensity(2f);
                light.setSize(EXPLOSION_VISUAL_RADIUS * 2f);
                light.setColor(EXPLOSION_COLOR);
                light.fadeOut(1.25f);
                if (BRDYSettings.graphicsLibLightsEnabled()) {
                    LightShader.addLight(light);
                }

                wave = new WaveDistortion();
                wave.setLocation(loc);
                wave.setSize(DISTORTION_BLAST_RADIUS);
                wave.setIntensity(DISTORTION_BLAST_RADIUS * 0.075f);
                wave.fadeInSize(1.2f);
                wave.fadeOutIntensity(0.9f);
                wave.setSize(DISTORTION_BLAST_RADIUS * 0.25f);
                if (BRDYSettings.graphicsLibDistortionsEnabled()) {
                    DistortionShader.addDistortion(wave);
                }

                Global.getSoundPlayer().playSound(EXPLOSION_SOUND, 1f, 1f, ship.getLocation(), ship.getVelocity());

                AnamorphicFlare.createFlare(ship, new Vector2f(loc), engine, 0.50f, 0.05f, -15f +
                                            (float) Math.random() * 30f, 9.25f, 6f, FLARE_COLOR,
                                            PARTICLE_COLOR);
                AnamorphicFlare.createFlare(ship, new Vector2f(loc), engine, 0.51f, 0.049f, -15f +
                                            (float) Math.random() * 60f, 8.95f, 6f, PARTICLE_COLOR,
                                            FLARE_COLOR);
                AnamorphicFlare.createFlare(ship, new Vector2f(loc), engine, 0.52f, 0.048f, -15f +
                                            (float) Math.random() * 30f, 7.55f, 6f, FLARE_COLOR,
                                            PARTICLE_COLOR);
                AnamorphicFlare.createFlare(ship, new Vector2f(loc), engine, 0.51f, 0.047f, -15f +
                                            (float) Math.random() * 90f, 10.95f, 6f, PARTICLE_COLOR,
                                            FLARE_COLOR);
                AnamorphicFlare.createFlare(ship, new Vector2f(loc), engine, 0.50f, 0.046f, -15f +
                                            (float) Math.random() * 120f, 8.55f, 6f, FLARE_COLOR,
                                            PARTICLE_COLOR);

                ShipAPI victim;
                Vector2f dir;
                float force, damage, emp, mod;
                List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(),
                                                                                    EXPLOSION_PUSH_RADIUS);
                int size = entities.size();
                for (int i = 0; i < size; i++) {
                    CombatEntityAPI tmp = entities.get(i);
                    if (tmp == ship) {
                        continue;
                    }

                    mod = 1f - (MathUtils.getDistance(ship, tmp) / EXPLOSION_PUSH_RADIUS);
                    force = FORCE_VS_ASTEROID * mod;
                    damage = EXPLOSION_DAMAGE_AMOUNT * mod;
                    emp = EXPLOSION_EMP_DAMAGE_AMOUNT * mod;

                    if (tmp instanceof ShipAPI) {
                        victim = (ShipAPI) tmp;

                        // Modify push strength based on ship class
                        if (victim.getHullSize() == ShipAPI.HullSize.FIGHTER) {
                            force = FORCE_VS_FIGHTER * mod;
                            damage /= DAMAGE_MOD_VS_FIGHTER;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.FRIGATE) {
                            force = FORCE_VS_FRIGATE * mod;
                            damage /= DAMAGE_MOD_VS_FRIGATE;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                            force = FORCE_VS_DESTROYER * mod;
                            damage /= DAMAGE_MOD_VS_DESTROYER;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.CRUISER) {
                            force = FORCE_VS_CRUISER * mod;
                            damage /= DAMAGE_MOD_VS_CRUISER;
                        } else if (victim.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) {
                            force = FORCE_VS_CAPITAL * mod;
                            damage /= DAMAGE_MOD_VS_CAPITAL;
                        }

                        if (victim.getOwner() == ship.getOwner()) {
                            damage *= EXPLOSION_DAMAGE_VS_ALLIES_MODIFIER;
                            emp *= EXPLOSION_EMP_VS_ALLIES_MODIFIER;
                            force *= EXPLOSION_FORCE_VS_ALLIES_MODIFIER;
                        }

                        boolean templarShieldHit;
                        templarShieldHit = BRDYOptionalModChecks.isTemplarLatticeShieldActive(victim);

                        if ((victim.getShield() != null && victim.getShield().isOn() && victim.getShield().isWithinArc(
                             ship.getLocation())) || templarShieldHit) {
                            victim.getFluxTracker().increaseFlux(damage * 2, true);
                        } else {
                            ShipAPI empTarget = victim;
                            for (int x = 0; x < 5; x++) {
                                engine.spawnEmpArc(ship, MathUtils.getRandomPointInCircle(victim.getLocation(),
                                                                                          victim.getCollisionRadius()),
                                                   empTarget,
                                                   empTarget, EXPLOSION_DAMAGE_TYPE, damage / 10, emp / 5,
                                                   EXPLOSION_PUSH_RADIUS, null, 2f, EXPLOSION_COLOR,
                                                   EXPLOSION_COLOR);
                            }
                        }
                    }

                    dir = VectorUtils.getDirectionalVector(ship.getLocation(), tmp.getLocation());
                    dir.scale(force);

                    Vector2f.add(tmp.getVelocity(), dir, tmp.getVelocity());
                }

                isActive = false;
            }
        }

    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (state == State.IN) {
            if (index == 0) {
                return new StatusData("charging scalar deracinator", false);
            }
        }

        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
    }
}
