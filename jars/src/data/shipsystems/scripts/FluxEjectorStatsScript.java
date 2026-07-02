package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class FluxEjectorStatsScript extends BaseShipSystemScript {

    public static final float BONUS = 0.3f;
    public static final float DISSIPATION_BUFF = 3f;

    private static final Color SMOKE_COLOR = new Color(101, 168, 117, 117);

    private boolean armed = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == State.ACTIVE) {
            float mult = 1f + BONUS * effectLevel;
            float mult2 = 1f - BONUS * effectLevel;
            stats.getEnergyRoFMult().modifyMult(id, mult);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, mult2);
            stats.getMaxSpeed().modifyFlat(id, 50f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 50f * effectLevel);
            stats.getDeceleration().modifyFlat(id, 20f * effectLevel);
            stats.getMaxTurnRate().modifyMult(id, 1f + 0.25f * effectLevel);

            armed = true;
        } else if (state == State.OUT) {
            float mult = 1f + DISSIPATION_BUFF * effectLevel;
            stats.getEnergyRoFMult().unmodify(id);
            stats.getEnergyWeaponFluxCostMod().unmodify(id);
            stats.getFluxDissipation().modifyMult(id, mult);
            stats.getMaxSpeed().unmodify(id);
            stats.getMaxTurnRate().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);

            ShipAPI ship = (ShipAPI) stats.getEntity();
            if (ship != null) {
                if (armed) {
                    armed = false;
                    for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
                        if (slot.isSystemSlot()) {
                            float angle = slot.getAngle() + ship.getFacing();
                            if (angle >= 360f) {
                                angle -= 360f;
                            }
                            Vector2f slotLoc = slot.computePosition(ship);
                            Global.getCombatEngine().spawnProjectile(ship, null, "br_fluxejector", slotLoc, angle, ship.getVelocity());
                            for (int i = 0; i < 8; i++) {
                                Vector2f loc = MathUtils.getRandomPointInCircle(slotLoc, 8f);
                                float size = MathUtils.getRandomNumberInRange(10f, 19f);
                                Global.getCombatEngine().addSmokeParticle(loc, ship.getVelocity(), size, 1f, 0.9f, SMOKE_COLOR);
                            }
                            for (int i = 0; i < 6; i++) {
                                Vector2f vel = new Vector2f(MathUtils.getRandomNumberInRange(0f, 30f), 0f);
                                VectorUtils.rotate(vel, angle, vel);
                                Vector2f.add(vel, ship.getVelocity(), vel);
                                float size = MathUtils.getRandomNumberInRange(10f, 19f);
                                Global.getCombatEngine().addSmokeParticle(slotLoc, vel, size, 1f, 1.1f, SMOKE_COLOR);
                            }
                        }
                    }
                    Global.getSoundPlayer().playSound("system_fluxeject", 1f, 1f, ship.getLocation(), ship.getVelocity());
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyRoFMult().unmodify(id);
        stats.getFluxDissipation().unmodify(id);
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);

        armed = false;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("Increased energy weapon output and engine power", false);
        }
        return null;
    }
}
