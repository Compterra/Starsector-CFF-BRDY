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

public class AchillesOnHitEffect implements OnHitEffectPlugin {

    public static final float damageAmount = 75f; //set this to whatever the damage should be
    public static final boolean dealsSoftFlux = false;
    public static final float empAmount = 500f; //set this to whatever the EMP damage should be
    public static final float explosionDuration = 0.37f;
    public static final float explosionDuration2 = 1.3f;
    public static final float explosionSize = 179f;
    public static final float explosionSize2 = 61f;
    public static final float particleBrightness = 0.98f;
    public static final Color particleColor = new Color(130, 205, 247, 255);
    public static final float particleDuration = 2.0f;
    public static final float particleSize = 3f;
    public static final float pitch = 1f; //sound pitch. Default seems to be 1
    public static final String soundName = "br_empexplosion"; //assign the sound we want to play
    public static final float volume = 0.55f; //volume, scale from 0-1

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (BRDYOptionalModChecks.isTemplarLatticeShieldActive(ship)) {
                shieldHit = true;
            }
        }

        if (target instanceof ShipAPI && !shieldHit) {
            Vector2f particleVelocity1;
            Vector2f particleVelocity2;

            particleVelocity1 = projectile.getVelocity();
            particleVelocity2 = projectile.getVelocity();
            particleVelocity1.scale(0.02f);
            particleVelocity2.scale(0.06f);

            engine.applyDamage(target, point, damageAmount, DamageType.ENERGY, empAmount, false, dealsSoftFlux, engine);
            engine.addHitParticle(point, particleVelocity1, particleSize, particleBrightness, particleDuration,
                                  particleColor);
            engine.spawnExplosion(point, particleVelocity1, particleColor, explosionSize, explosionDuration);
            engine.spawnExplosion(point, particleVelocity2, particleColor, explosionSize2, explosionDuration2);
            Global.getSoundPlayer().playSound(soundName, pitch, volume, point, projectile.getVelocity());
        }
    }
}
