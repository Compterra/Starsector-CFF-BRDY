package data.shipsystems.ai;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class BrakefieldBarrierAI implements ShipSystemAIScript {

    private ShipAPI ship;

    private final IntervalUtil tracker = new IntervalUtil(0.35f, 0.6f);

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        tracker.advance(amount);
        Vector2f shipLoc = ship.getLocation();

        if (tracker.intervalElapsed()) {
            // Can we even use the system right now?
            if (!AIUtils.canUseSystemThisFrame(ship)) {
                return;
            }
            boolean shouldUseSystem = false;
            float repulseRadius;
            repulseRadius = 350f;
            List<ShipAPI> nearbyEnemies = AIUtils.getNearbyEnemies(ship, repulseRadius);//Use this if any enemies might be able to engage us, to push them back.
            List<MissileAPI> nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, repulseRadius);//Use this to try and escape missiles.
            List<DamagingProjectileAPI> nearbyBullets = CombatUtils.getProjectilesWithinRange(shipLoc, repulseRadius);//Use this to try and defend against projectiles.

            /* Filter to just enemy bullets */
            Iterator<DamagingProjectileAPI> iter = nearbyBullets.iterator();
            while (iter.hasNext()) {
                DamagingProjectileAPI nearbyBullet = iter.next();
                if ((nearbyBullet.getOwner() == 100) || (nearbyBullet.getOwner() == ship.getOwner())) {
                    iter.remove();
                }
            }

            if (nearbyMissiles.size() > 0 || nearbyEnemies.size() > 0 || nearbyBullets.size() > 0) {
                shouldUseSystem = true;
            }

            // If system is inactive and should be active, enable it
            // If system is active and shouldn't be, disable it
            if (ship.getSystem().isActive() ^ shouldUseSystem) {
                ship.useSystem();
            }
        }
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
    }
}
