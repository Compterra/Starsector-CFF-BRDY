package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import org.lazywizard.lazylib.combat.CombatUtils;

public class LtGalePushback implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }
        ShipAPI ship = weapon.getShip();

        if (weapon.isFiring()) {
            CombatUtils.applyForce(ship, weapon.getCurrAngle() + 180, 1000);
        }
    }
}
