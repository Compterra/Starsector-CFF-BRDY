package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class FerroHit implements OnHitEffectPlugin {
   
    private static final Color EXPLOSION_COLOR1 = new Color(244, 241, 240, 160);
    private static final Color EXPLOSION_COLOR2 = new Color(125, 120, 160, 140);

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
            // Spawn visual effects
            Vector2f vel = new Vector2f(target.getVelocity());
            vel.scale(0.5f);
            engine.spawnExplosion(point, vel, EXPLOSION_COLOR1, 75f, 0.2f);
            engine.spawnExplosion(point, vel, EXPLOSION_COLOR2, 125f, 0.7f);
            engine.spawnExplosion(point, vel, EXPLOSION_COLOR2, 220f, 1.25f);            
            }
        }
    
