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

public class ScalaronBlasterOnHit implements OnHitEffectPlugin {

    private static final Color COLOR1 = new Color(49, 242, 214, 70);
    private static final Color COLOR2 = new Color(135, 255, 180, 50);
    private static final Vector2f ZERO = new Vector2f();

    public static void boom(Vector2f point, CombatEngineAPI engine) {
        engine.spawnExplosion(point, ZERO, COLOR1, 125f, 1.0f);
        engine.spawnExplosion(point, ZERO, COLOR2, 250f, 0.55f);
        Global.getSoundPlayer().playSound("scalar_impact", 1f, 0.5f, point, ZERO);
    }

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
                engine.spawnExplosion(point, ZERO, COLOR1, 155f, 1.3f);
                engine.spawnExplosion(point, ZERO, COLOR2, 305f, 0.6f);
                float emp = projectile.getEmpAmount() * 0.25f;
                float dam = projectile.getDamageAmount() * 0.10f;
                ShipAPI empTarget = ship;
                for (int x = 0; x < 4; x++) {
                    engine.spawnEmpArc(projectile.getSource(), point, empTarget, empTarget, DamageType.ENERGY, dam, emp,
                                       100000f, null, 20f, COLOR1, COLOR2);
                }
                Global.getSoundPlayer().playSound("scalar_impact", 1f, 1f, point, ZERO);
            }
        }
    }
}
