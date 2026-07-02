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
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class QuillOnHitEffect implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(130, 235, 217, 255);
    // How likely it is that the extra damage will be applied (1 = 100% chance)
    private static final float EXTRA_DAMAGE_CHANCE = 0.25f;
    private static final float MAX_EXTRA_DAMAGE = 250f;
    private static final float MIN_EXTRA_DAMAGE = 200f;
    private static final int NUM_PARTICLES = 19;
    private static final Color PARTICLE_COLOR = new Color(130, 235, 217, 255);
    // The sound the projectile makes if it deals extra damage
    private static final String SOUND_ID = "shardsplode";
    // The damage types that the extra damage can deal (randomly selected)
    private static final DamageType[] TYPES = {
        DamageType.ENERGY
    };

    @Override
    @SuppressWarnings("AssignmentToMethodParameter")
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && target instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) target;
            if (BRDYOptionalModChecks.isTemplarLatticeShieldActive(ship)) {
                shieldHit = true;
            }
        }

        // Check if we hit a ship (not its shield)
        if (target instanceof ShipAPI && !shieldHit && Math.random() <= EXTRA_DAMAGE_CHANCE) {
            // Apply extra damage of a random type
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(MIN_EXTRA_DAMAGE, MAX_EXTRA_DAMAGE),
                               TYPES[(int) ((float) Math.random() *
                                            TYPES.length)], 0f, false,
                               true, projectile.getSource());

            // Spawn visual effects
            Vector2f vel = new Vector2f(target.getVelocity());
            vel.scale(0.85f);
            engine.spawnExplosion(point, vel, EXPLOSION_COLOR, 96f, 0.9f);
            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addHitParticle(point, MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                                                                               speed * .007f, speed * .17f),
                                                                               MathUtils.getRandomNumberInRange(facing -
                                                                                       180f, facing + 180f)), 7f, 1f,
                                      1.6f,
                                      PARTICLE_COLOR);
            }

            // Sound follows enemy that was hit
            Global.getSoundPlayer().playSound(SOUND_ID, 0.91f, 0.6f, target.getLocation(), target.getVelocity());
        }
    }
}
