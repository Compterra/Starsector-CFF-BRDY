package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class FluxEjector implements EveryFrameWeaponEffectPlugin {

    private static final float FLUX_AMOUNT_PER_WEAPON_PER_USE = 1000f;
    // weapon state (per weapon instance)
    private int last_weapon_ammo = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }
        int weapon_ammo = weapon.getAmmo();
        if (weapon_ammo < last_weapon_ammo) {
            weapon.getShip().getFluxTracker().decreaseFlux(FLUX_AMOUNT_PER_WEAPON_PER_USE);
        }
        last_weapon_ammo = weapon_ammo;
    }
}
