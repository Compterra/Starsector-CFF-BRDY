package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.ProjectileSpawnType;
import data.scripts.util.BRDYMulti;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class GravityField extends BaseShipSystemScript {

    private static final float FRIGATE_MOD = 1f;
    private static final float DESTROYER_MOD = 2f;
    private static final float CRUISER_MOD = 3f;
    private static final float CAPITAL_SHIP_MOD = 4f;

    private static final Color FIELD_COLOR = new Color(142, 248, 128, 15); // some color idk
    private static final float FIELD_RANGE = 1500f;
    private static final float FIELD_STRENGTH = 3f;
    private static final float MAXIMUM_MASS = 20000f;
    private static final float MAX_FRACTION = 10f;

    private static final Vector2f ZERO = new Vector2f(0f, 0f);

    private float accum = 0f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship == null) {
            return;
        }

        float amount = Global.getCombatEngine().getElapsedInLastFrame();

        accum += amount;
        while (accum >= (1f / 60f)) {
            List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(), FIELD_RANGE);
            int size = entities.size();
            for (int i = 0; i < size; i++) {
                CombatEntityAPI entity = entities.get(i);
                if (entity.getOwner() == ship.getOwner()) {
                    // Don't affect friendly entities
                    continue;
                }
                if (entity instanceof ShipAPI) {
                    if (!BRDYMulti.isRoot((ShipAPI) entity)) {
                        continue;
                    }
                }

                float x = entity.getMass();

                float velocityMod = (x / MAXIMUM_MASS) * FIELD_STRENGTH;
                if (entity instanceof ShipAPI) {
                    if (((ShipAPI) entity).isFighter()) {
                        velocityMod *= 1;
                    } else if (((ShipAPI) entity).isFrigate()) {
                        velocityMod *= FRIGATE_MOD;
                    } else if (((ShipAPI) entity).isDestroyer()) {
                        velocityMod *= DESTROYER_MOD;
                    } else if (((ShipAPI) entity).isCruiser()) {
                        velocityMod *= CRUISER_MOD;
                    } else if (((ShipAPI) entity).isCapital()) {
                        velocityMod *= CAPITAL_SHIP_MOD;
                    }
                }
                if (velocityMod > 1) {
                    continue;
                }
                velocityMod = (1f - (1f / MAX_FRACTION)) + (velocityMod / MAX_FRACTION);

                if ((entity instanceof DamagingProjectileAPI)
                        && (((DamagingProjectileAPI) entity).getSpawnType() == ProjectileSpawnType.BALLISTIC_AS_BEAM)) {
                    float idealAngle = VectorUtils.getAngle(entity.getLocation(), ship.getLocation()) + 180f;
                    if (idealAngle >= 360f) {
                        idealAngle -= 360f;
                    }

                    float rotationNeeded = MathUtils.getShortestRotation(entity.getFacing(), idealAngle);
                    entity.setFacing(entity.getFacing() + (rotationNeeded * velocityMod / 60f));
                } else {
                    entity.getVelocity().scale(velocityMod);
                }
            }

            Global.getCombatEngine().addSmoothParticle(ship.getLocation(), ZERO, FIELD_RANGE, 1f * effectLevel, 0.2f * effectLevel, FIELD_COLOR);

            accum -= 1f / 60f;
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("slowing enemy projectiles and drive fields", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        accum = 0f;
    }
}
