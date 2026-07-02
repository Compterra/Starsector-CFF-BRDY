package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.util.BRDYMulti;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class HammerclawOnHitEffect implements OnHitEffectPlugin {

    public static final float FORCE = 900f;

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            target = BRDYMulti.getRoot((ShipAPI) target);
        }
        CombatUtils.applyForce(target, projectile.getVelocity(), FORCE);
    }
}
