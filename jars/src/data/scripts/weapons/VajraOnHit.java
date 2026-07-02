package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class VajraOnHit implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(255, 45, 245, 145);
    // How likely it is that the extra damage will be applied (1 = 100% chance)
    private static final float EXTRA_DAMAGE_CHANCE = 1f;
    private static final float MIN_EXTRA_DAMAGE = 1f;
    private static final float MAX_EXTRA_DAMAGE = 2f;
    private static final int NUM_PARTICLES = 14;
    private static final Color PARTICLE_COLOR = new Color(195, 250, 70, 255);
    // The sound the projectile makes if it deals extra damage
    private static final String SOUND_ID = "vajrahit";
    // The damage types that the extra damage can deal (randomly selected)
    private static final DamageType[] TYPES = {
        DamageType.ENERGY
    };

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        // Check if it procs
        if (Math.random() <= EXTRA_DAMAGE_CHANCE) {
            // Apply extra damage of a random type
            engine.applyDamage(target, point, MathUtils.getRandomNumberInRange(MIN_EXTRA_DAMAGE, MAX_EXTRA_DAMAGE),
                               TYPES[(int) ((float) Math.random() *
                                            TYPES.length)], 0f, false,
                               true, projectile.getSource());

            // Spawn visual effects
            Vector2f vel = new Vector2f(target.getVelocity());
            vel.scale(0.5f);
            engine.spawnExplosion(point, vel, EXPLOSION_COLOR, 255f, 0.2f);
            float speed = projectile.getVelocity().length();
            float facing = projectile.getFacing();
            for (int x = 0; x < NUM_PARTICLES; x++) {
                engine.addHitParticle(point, MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                                                                               speed * .010f, speed * .17f),
                                                                               MathUtils.getRandomNumberInRange(facing -
                                                                                       180f, facing + 180f)), 5f, 1f,
                                      1.6f,
                                      PARTICLE_COLOR);
            }

            // Sound follows enemy that was hit
            Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, target.getLocation(), target.getVelocity());
        }
    }
}
