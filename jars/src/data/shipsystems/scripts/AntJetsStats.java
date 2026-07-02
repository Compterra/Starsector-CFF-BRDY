package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class AntJetsStats extends BaseShipSystemScript {

    public boolean activated = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (state == ShipSystemStatsScript.State.IN) {
            stats.getMaxTurnRate().modifyMult(id, 1f + 0.25f * effectLevel);
        }

        if (state == ShipSystemStatsScript.State.OUT) {
            if (activated) {
                Global.getSoundPlayer().playSound("burstjet_use_ant", 1f, 1f, ship.getLocation(), ship.getVelocity());
                activated = false;
            }
            stats.getMaxSpeed().modifyFlat(id, 450f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 300f * effectLevel);
            stats.getDeceleration().modifyFlat(id, 100f * effectLevel);
            stats.getMaxTurnRate().modifyMult(id, 1f + 0.25f * effectLevel);

            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
            ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
            ship.giveCommand(ShipCommand.ACCELERATE, null, 0);

            String key = ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            if (test == null) {
                Global.getCombatEngine().getCustomData().put(key, new Object());
                ship.getEngineController().extendFlame(test, 1.7f, 1.25f, 1.7f);
            }
        } else {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("increased engine power", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        activated = true;

        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship != null) {
            String key = ship.getId() + "_" + id;
            Global.getCombatEngine().getCustomData().remove(key);
        }
    }
}
