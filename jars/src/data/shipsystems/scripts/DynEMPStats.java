package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.BRDYFx;
import data.scripts.util.BRDYSettings;
import java.awt.Color;
import java.util.List;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
//import data.scripts.util.BRDYMulti;

public class DynEMPStats extends BaseShipSystemScript {

    private static final Color COLOR1 = new Color(119, 240, 167);
    private static final Color COLOR2 = new Color(119, 250, 167);
    private static final Vector2f ZERO = new Vector2f();

    public boolean activated = true;

    // effect constants
    private static final int MAX_PARTICLES_PER_FRAME = 12; // Based on charge level
    private static final Color PARTICLE_COLOR = new Color(155, 240, 200);
    private static final float PARTICLE_BRIGHTNESS = 0.25f;
    private static final float PARTICLE_RADIUS = 5f;
    private static final float PARTICLE_SIZE = 20f;
    private static final float LT_VISUAL_RADIUS = 120f;

    // Local variables
    private boolean isActive = false;
    private StandardLight light;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (state == State.IN) {
            if (activated) {
                Global.getSoundPlayer().playSound("dyn_emp_activate", 1f, 0.95f, ship.getLocation(), ship.getVelocity());
                activated = false;
            }
            Vector2f loc = new Vector2f(ship.getLocation());
            loc.x -= 1f * FastTrig.cos(ship.getFacing() * Math.PI / 180f);
            loc.y -= 1f * FastTrig.sin(ship.getFacing() * Math.PI / 180f);

            // Everything in this block is only done once per chargeup
            if (!isActive) {
                isActive = true;

                light = new StandardLight(loc, ZERO, ZERO, null);
                light.setIntensity(0.6f);
                light.setSize(LT_VISUAL_RADIUS);
                light.setColor(PARTICLE_COLOR);
                light.fadeIn(2.15f);
                light.setLifetime(0.1f);
                light.setAutoFadeOutTime(0.17f);
                if (BRDYSettings.graphicsLibLightsEnabled()) {
                    LightShader.addLight(light);
                }
            } else {
                light.setLocation(loc);
            }

            // Exact amount per second doesn't matter since it's purely decorative
            int numParticlesThisFrame = Math.round(effectLevel * MAX_PARTICLES_PER_FRAME);
            BRDYFx.addHitBurst(Global.getCombatEngine(), ship.getLocation(), ship.getVelocity(), PARTICLE_COLOR,
                               numParticlesThisFrame, PARTICLE_SIZE * 0.75f, PARTICLE_SIZE * 1.25f,
                               PARTICLE_BRIGHTNESS, 0.2f, 0.3f, PARTICLE_RADIUS * 4f,
                               CombatEngineLayers.ABOVE_PARTICLES_LOWER);
        } // Cooldown, explode once system is finished

        if (state == State.OUT) {
            if (!activated) {
                //float shipRadius = ship.getCollisionRadius();

                boolean didAnything = false;
                List<ShipAPI> targets = CombatUtils.getShipsWithinRange(ship.getLocation(), 575f);
                for (ShipAPI target : targets) {
                    if (!target.isAlive() || target == ship
                            || target.getVariant().getHullMods().contains("vastbulk") || (target.getOwner() == ship.getOwner())) {
                        continue;
                    }

                    didAnything = true; //the big zap
                    for (int i = 0; i < 8; i++) {
                        //Global.getCombatEngine().spawnEmpArc(ship, MathUtils.getRandomPointInCircle(ship.getLocation(),
                        Global.getCombatEngine().spawnEmpArc(ship, ship.getLocation(),
                                ship, target,
                                DamageType.ENERGY, 125f, 300f, 100000f, "dyn_emp_spark", 20f, COLOR2,
                                COLOR1);
                    }

                    //if (target.getShield().isOn ()) {
                    //    target.getFluxTracker().increaseFlux((100f - MathUtils.getDistance(target, ship)) * 10f, true);
                    // }
                                        //CombatUtils.applyForce(BRDYMulti.getRoot(target), VectorUtils.getDirectionalVector(
                    // target.getLocation(), ship.getLocation()), -500f);
                }

                if (didAnything) {
                    Global.getSoundPlayer().playSound("dyn_emp_discharge", 1f, 2f, ship.getLocation(),
                            ship.getVelocity());
                    StandardLight light2 = new StandardLight(ship.getLocation(), ZERO, ZERO, null);
                    light2.setIntensity(1f);
                    light2.setSize(400f);
                    light2.setColor(COLOR1);
                    light2.fadeOut(1.5f);
                    if (BRDYSettings.graphicsLibLightsEnabled()) {
                        LightShader.addLight(light2);
                    }

                }
            }
            activated = true;
            isActive = false;  //whatever, it works          
        }
    }
}
