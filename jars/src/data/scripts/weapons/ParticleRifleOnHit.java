package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.BRModPlugin;
import data.scripts.util.BRDYOptionalModChecks;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class ParticleRifleOnHit implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(246, 237, 190, 205);
    private static final float EXTRA_DAMAGE = 30f;
    private static final String SOUND_ID = "particlehit";
    private static final DamageType[] TYPES = {
        DamageType.ENERGY
    };

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (point == null) {
            return;
        }
        if (target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (!shieldHit) {
                if (BRDYOptionalModChecks.isTemplarLatticeShieldActive(ship)) {
                    shieldHit = true;
                }
            }

            if (!shieldHit) {
                engine.applyDamage(target, point, EXTRA_DAMAGE, TYPES[(int) ((float) Math.random() *
                                                                             TYPES.length)], 0f, false,
                                   true, projectile.getSource());

                Vector2f vel = new Vector2f(target.getVelocity());
                vel.scale(0.45f);
                engine.spawnExplosion(point, vel, EXPLOSION_COLOR, 35f, 0.25f);
                Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, target.getLocation(), target.getVelocity());
            }
        }
    }
}
