package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashSet;
import java.util.Set;

public class BlackrockDrive extends BaseHullMod {

    public static final int BONUS1 = 30;
    public static final int BONUS2 = 10;
    public static final int NEG_PERCENT = 10;
    public static final int SMOD_NEG_PERCENT = 5;
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(3);
    private static final int BURN_LEVEL_BONUS = 1;

    static {
        BLOCKED_HULLMODS.add("augmentedengines");
        BLOCKED_HULLMODS.add("unstable_injector");
        BLOCKED_HULLMODS.add("safetyoverrides");
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getZeroFluxSpeedBoost().modifyFlat(id, BONUS1);
        stats.getMaxSpeed().modifyPercent(id, BONUS2);
        int handlingPenalty = isSMod(stats) ? SMOD_NEG_PERCENT : NEG_PERCENT;
        stats.getMaxTurnRate().modifyPercent(id, -handlingPenalty);
        stats.getDeceleration().modifyPercent(id, -handlingPenalty);
        stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + BONUS1;
        }
        if (index == 1) {
            return "" + BONUS2;
        }
        if (index == 2) {
            return "" + BURN_LEVEL_BONUS;
        }
        if (index == 3) {
            return "" + NEG_PERCENT;
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + SMOD_NEG_PERCENT + "%";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a brdy hull id
        return (ship.getHullSpec().getHullId().startsWith("brdy_") ||
                ship.getHullSpec().getHullId().startsWith("brdyx_")) &&
                !ship.getVariant().getHullMods().contains("unstable_injector") &&
                !ship.getVariant().getHullMods().contains("augmentedengines") &&
                !ship.getVariant().getHullMods().contains("safetyoverrides");
    }
    
    
    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!(ship.getHullSpec().getHullId().startsWith("brdy_") ||
              ship.getHullSpec().getHullId().startsWith("brdyx_"))) {
            return "Must be installed on a Blackrock ship";
        }
        if (ship.getVariant().getHullMods().contains("unstable_injector")) {
            return "Incompatible with Unstable Injector";
        }
        if (ship.getVariant().getHullMods().contains("augmentedengines")) {
            return "Incompatible with Augmented Engines";
        }
        if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
            return "Incompatible with Safety Overrides";
        }

        return null;
    }
    
}
