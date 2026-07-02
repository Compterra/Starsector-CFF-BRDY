package data.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class FluxEjectorAI implements ShipSystemAIScript {

    private static final float HARD_FLUX_LEVEL_ACTIVATION_THRESHOLD = 0.75f;

    private ShipSystemAPI cloak;
    private ShipAPI ship;
    private ShipSystemAPI system;

    private final IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);
        if (tracker.intervalElapsed()) {
            if (system.getCooldownRemaining() > 0) {
                return;
            }
            if (system.isOutOfAmmo()) {
                return;
            }
            if (system.isActive()) {
                return;
            }
            if (cloak != null && (cloak.isActive() || cloak.isOn())) {
                return; // do not try to activate while cloaked
            }
            float hard_flux_level = ship.getFluxTracker().getHardFlux() / ship.getFluxTracker().getMaxFlux();
            if (hard_flux_level >= HARD_FLUX_LEVEL_ACTIVATION_THRESHOLD) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.cloak = ship.getPhaseCloak();
    }
}
