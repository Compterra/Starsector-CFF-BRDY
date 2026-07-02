package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import static data.scripts.weapons.brdy_SueprscalarEveryFrame.colorBlend;

public class brdy_SueprscalarBeamEffect implements BeamEffectPlugin {

    private static final Color BEAM_CORE = new Color(200, 255, 200, 255);
    private static final Color BEAM_CORE_ALT = new Color(255, 255, 255, 255);
    private static final Color BEAM_FRINGE = new Color(70, 255, 120, 255);
    private static final Color BEAM_FRINGE_ALT = new Color(40, 255, 200, 255);
    private static final Color HIT_COLOR = new Color(150, 255, 25, 255);
    private static final Color HIT_COLOR_ALT = new Color(100, 255, 150, 255);
    private static final Vector2f ZERO = new Vector2f();
    private final IntervalUtil interval = new IntervalUtil(0.1f, 0.1f);

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        ShipAPI ship = beam.getWeapon().getShip();
        Vector2f origin = beam.getFrom();
        Vector2f shipVelocity = ship.getVelocity();
        float powerLevel = 1f * ship.getFluxTracker().getFluxLevel() + 0.5f;
        float level = beam.getBrightness();

        Global.getSoundPlayer().playLoop("voidbuster_loop", beam.getWeapon(), 1f * powerLevel, 0.67f * powerLevel,
                                         origin, shipVelocity);
        beam.setCoreColor(colorBlend(BEAM_CORE, BEAM_CORE_ALT, powerLevel - 0.5f));
        beam.setFringeColor(colorBlend(BEAM_FRINGE, BEAM_FRINGE_ALT, powerLevel - 0.5f));
        beam.setWidth(40f * powerLevel);

        interval.advance(amount);
        if (interval.intervalElapsed()) {

            if (beam.getDamageTarget() != null) {
                Global.getCombatEngine().spawnExplosion(new Vector2f(beam.getTo()), ZERO, colorBlend(HIT_COLOR,
                                                                                                     HIT_COLOR_ALT,
                                                                                                     powerLevel - 0.5f),
                                                        level * 75f * powerLevel, 0.25f * powerLevel);
                engine.addHitParticle(new Vector2f(beam.getTo()), ZERO, level * 400f, 0.4f, (float) Math.random() * 3f *
                                      amount + 3f * amount,
                                      colorBlend(HIT_COLOR, HIT_COLOR_ALT, powerLevel - 0.5f));
                int particleCount = (int) (12 * powerLevel);
                for (int x = 0; x < particleCount; x++) {
                    float angle = VectorUtils.getAngle(beam.getTo(), beam.getDamageTarget().getLocation()) + 180f +
                          (float) Math.random() * 210f - 105f;
                    if (angle >= 360f) {
                        angle -= 360f;
                    } else if (angle < 0f) {
                        angle += 360f;
                    }
                    engine.addHitParticle(new Vector2f(beam.getTo()),
                                          MathUtils.getPointOnCircumference(null, (float) Math.random() * 350f *
                                                                            powerLevel + 350f, angle), 7f * powerLevel,
                                          1f, (float) Math.random() * 0.25f * powerLevel + 0.25f, colorBlend(HIT_COLOR,
                                                                                                             HIT_COLOR_ALT,
                                                                                                             powerLevel -
                                                                                                             0.5f));
                }
            }
        }
    }
}
