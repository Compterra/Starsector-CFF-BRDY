package data.scripts.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class BRSystemVisualEffect implements EveryFrameWeaponEffectPlugin {

    private static final float SECONDS_TO_ACTIVATE = 1;
    private static final float SECONDS_TO_DEACTIVATE = 1;

    private float alpha = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        boolean on = weapon.getShip().getSystem().isOn() && weapon.getShip().isAlive();

        if (alpha == 0 && !on) {
            weapon.getAnimation().setFrame(0);
            return;
        }

        weapon.getAnimation().setFrame(1);

        alpha += engine.getElapsedInLastFrame() * (on ? SECONDS_TO_ACTIVATE : -SECONDS_TO_DEACTIVATE);
        alpha = Math.max(Math.min(alpha, 1), 0);

        weapon.getAnimation().setAlphaMult(alpha);
    }
}
