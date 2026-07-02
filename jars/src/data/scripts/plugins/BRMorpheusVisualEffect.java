package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

public class BRMorpheusVisualEffect implements EveryFrameWeaponEffectPlugin {

    private static final float ACTIVATE_SPEED = 5.0f;
    private static final float DEACTIVATE_SPEED = 1.0f;
    private static final String READY_SOUND = "system_rejectoronline";

    private float alpha = 0.7f;
    private boolean soundAlreadyPlayed = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        ShipAPI ship = weapon.getShip();

        if (ship.getPhaseCloak().isCoolingDown() && ship.getPhaseCloak().getCooldownRemaining() < 0.1f &&
                !soundAlreadyPlayed) {
            soundAlreadyPlayed = true;

            if (!weapon.getId().contentEquals("lights_morpheus2")) {
                Global.getSoundPlayer().playSound(READY_SOUND, 1, 1, ship.getLocation(), ship.getVelocity());
            }
        } else if (!ship.getPhaseCloak().isCoolingDown() && soundAlreadyPlayed) {
            soundAlreadyPlayed = false;
        }

        boolean on = !ship.getSystem().isActive() && ship.isAlive() && !ship.getFluxTracker().isOverloaded() &&
                (ship.getPhaseCloak() == null ||
                 !(ship.getPhaseCloak().isActive() ||
                   ship.getPhaseCloak().isCoolingDown()));

        if (alpha == 0 && !on) {
            weapon.getAnimation().setAlphaMult(0f);
            return;
        }

        float wave = (float) Math.cos(engine.getTotalElapsedTime(false) * Math.PI);
        wave *= (float) Math.cos(engine.getTotalElapsedTime(false) * Math.E / 3);
        alpha += engine.getElapsedInLastFrame() * (on ? ACTIVATE_SPEED : -DEACTIVATE_SPEED);
        alpha = Math.max(Math.min(alpha, 1), 0);

        weapon.getAnimation().setAlphaMult(alpha * (wave / 3 + 0.66f));
    }
}
