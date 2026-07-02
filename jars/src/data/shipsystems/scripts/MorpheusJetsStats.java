package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import java.awt.Color;

public class MorpheusJetsStats extends BaseShipSystemScript {

    private static final Color COLOR_AFTERIMAGE = new Color(150, 255, 200, 100);

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (state == ShipSystemStatsScript.State.OUT) {
            stats.getMaxSpeed().modifyPercent(id, 100f * effectLevel); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
            stats.getDeceleration().modifyMult(id, 1f - effectLevel * 0.5f);
        } else {
            stats.getMaxSpeed().modifyFlat(id, 200f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 5000f * effectLevel);
            stats.getDeceleration().modifyMult(id, 1f - effectLevel * 0.5f);
            stats.getTurnAcceleration().modifyFlat(id, 1000f * effectLevel);
            stats.getMaxTurnRate().modifyFlat(id, 25f * effectLevel);
            stats.getMaxTurnRate().modifyPercent(id, 100f * effectLevel);
        }
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (ship != null) {
            ship.addAfterimage(COLOR_AFTERIMAGE, 0f, 0f, -ship.getVelocity().x * 0.5f, -ship.getVelocity().y * 0.5f,
                               1f * effectLevel,
                               0f, 0.1f, 1f * effectLevel, false, false, false);
        }
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("improved maneuverability", false);
        } else if (index == 1) {
            return new StatusData("increased top speed", false);
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
    }
}
