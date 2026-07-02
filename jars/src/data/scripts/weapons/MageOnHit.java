package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class MageOnHit implements OnHitEffectPlugin {

    private static final Color EXPLOSION_COLOR = new Color(195, 250, 70, 210);
    private static final int NUM_PARTICLES = 5;
    private static final Color PARTICLE_COLOR = new Color(195, 250, 70, 255);
    private static final String SOUND_ID = "brdy_plasmapop";
    private static final Vector2f ZERO = new Vector2f();
    
    //simple explosion to kill the missile when it fizzles, called from brdy_EnergyTorpedoAI
        public static void pop (Vector2f point, CombatEngineAPI engine) {
        engine.spawnExplosion(point, ZERO, EXPLOSION_COLOR, 65f, 0.3f);
        Global.getSoundPlayer().playSound("brdy_plasmapop", 1f, 0.5f, point, ZERO);
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        // Spawn visual effects
        Vector2f vel = new Vector2f(target.getVelocity());
        vel.scale(0.5f);
        engine.spawnExplosion(point, vel, EXPLOSION_COLOR, 100f, 0.7f);
        float speed = projectile.getVelocity().length();
        float facing = projectile.getFacing();
        for (int x = 0; x < NUM_PARTICLES; x++) {
            engine.addHitParticle(point, MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(
                    speed * .010f, speed * .2f),
                    MathUtils.getRandomNumberInRange(facing
                            - 180f, facing + 180f)), 5f, 1f,
                    1.6f,
                    PARTICLE_COLOR);
        }

        // Sound follows enemy that was hit
        Global.getSoundPlayer().playSound(SOUND_ID, 1f, 1f, target.getLocation(), target.getVelocity());
    }
}
