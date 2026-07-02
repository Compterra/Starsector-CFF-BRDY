package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class BlackrockAssault extends BaseHullMod {

    public static final int HEALTH_BONUS = 25;
    public static final int CR_BONUS = 20;
    public static final int SMOD_CREW_LOSS_REDUCTION = 25;

    public static final Map<HullSize, Float> mag = new HashMap<>(4);

    static {
        mag.put(HullSize.FRIGATE, 50f);
        mag.put(HullSize.DESTROYER, 100f);
        mag.put(HullSize.CRUISER, 150f);
        mag.put(HullSize.CAPITAL_SHIP, 200f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyFlat(id, mag.get(hullSize));
        stats.getHullBonus().modifyPercent(id, HEALTH_BONUS);
        stats.getPeakCRDuration().modifyPercent(id, CR_BONUS);
        if (isSMod(stats)) {
            stats.getCrewLossMult().modifyMult(id + "_smod", 1f - SMOD_CREW_LOSS_REDUCTION * 0.01f);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + mag.get(HullSize.FRIGATE).intValue();
        }
        if (index == 1) {
            return "" + mag.get(HullSize.DESTROYER).intValue();
        }
        if (index == 2) {
            return "" + mag.get(HullSize.CRUISER).intValue();
        }
        if (index == 3) {
            return "" + mag.get(HullSize.CAPITAL_SHIP).intValue();
        }
        if (index == 4) {
            return "" + HEALTH_BONUS + "%";
        }
        if (index == 5) {
            return "" + CR_BONUS + "%";
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + SMOD_CREW_LOSS_REDUCTION + "%";
        }
        return null;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Must be installed on a Blackrock ship";
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a brdy hull id
        return ship.getHullSpec().getHullId().startsWith("brdy_") || ship.getHullSpec().getHullId().startsWith("brdyx_");
    }
}
