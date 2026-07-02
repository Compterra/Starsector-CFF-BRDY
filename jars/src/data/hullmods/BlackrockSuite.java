package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlackrockSuite extends BaseHullMod {

    public static final int BONUS_PERCENT = 10;
    public static final int WEAPON_FLUX_REDUCTION = 10;
    public static final int PROJ_ACCEL = 25;    
    public static final int SMOD_RECOIL_RECOVERY = 25;
    public static final int SMOD_AUTOFIRE_PRECISION = 10;
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(2);

    public static final Map<HullSize, Float> mag = new HashMap<>(4);

    static {
        mag.put(HullSize.FRIGATE, 5f);
        mag.put(HullSize.DESTROYER, 10f);
        mag.put(HullSize.CRUISER, 20f);
        mag.put(HullSize.CAPITAL_SHIP, 35f);
    }

    
    static {
        BLOCKED_HULLMODS.add("dedicated_targeting_core");
        BLOCKED_HULLMODS.add("targetingunit");
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
        stats.getEnergyWeaponDamageMult().modifyPercent(id, BONUS_PERCENT);        
        stats.getBallisticWeaponDamageMult().modifyPercent(id, BONUS_PERCENT);
        stats.getMissileWeaponDamageMult().modifyPercent(id, BONUS_PERCENT);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - WEAPON_FLUX_REDUCTION * 0.01f);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, 1f - WEAPON_FLUX_REDUCTION * 0.01f);
        stats.getMissileWeaponFluxCostMod().modifyMult(id, 1f - WEAPON_FLUX_REDUCTION * 0.01f);
        stats.getProjectileSpeedMult().modifyPercent(id, PROJ_ACCEL);        
	stats.getBallisticWeaponRangeBonus().modifyPercent(id, (Float) mag.get(hullSize));
	stats.getEnergyWeaponRangeBonus().modifyPercent(id, (Float) mag.get(hullSize));
        if (isSMod(stats)) {
            stats.getAutofireAimAccuracy().modifyFlat(id + "_smod", SMOD_AUTOFIRE_PRECISION * 0.01f);
            stats.getRecoilDecayMult().modifyPercent(id + "_smod", SMOD_RECOIL_RECOVERY);
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + BONUS_PERCENT + "%";
        }
        if (index == 1) {
            return "" + WEAPON_FLUX_REDUCTION + "%";
        }
        if (index == 2) {
            return "" + PROJ_ACCEL + "%";
        }        
         if (index == 3) {
            return "" + mag.get(HullSize.FRIGATE).intValue() + "%";
        }
        if (index == 4) {
            return "" + mag.get(HullSize.DESTROYER).intValue() + "%";
        }
        if (index == 5) {
            return "" + mag.get(HullSize.CRUISER).intValue() + "%";
        }
        if (index == 6) {
            return "" + mag.get(HullSize.CAPITAL_SHIP).intValue() + "%";
        }       
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + SMOD_AUTOFIRE_PRECISION + "%";
        }
        if (index == 1) {
            return "" + SMOD_RECOIL_RECOVERY + "%";
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a brdy hull id
        return (ship.getHullSpec().getHullId().startsWith("brdy_") ||
                ship.getHullSpec().getHullId().startsWith("brdyx_")) &&
                !ship.getVariant().getHullMods().contains("dedicated_targeting_core") &&
                !ship.getVariant().getHullMods().contains("targetingunit") &&
                !ship.getVariant().getHullMods().contains("safetyoverrides");        
    }
    
    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!(ship.getHullSpec().getHullId().startsWith("brdy_") ||
              ship.getHullSpec().getHullId().startsWith("brdyx_"))) {
            return "Must be installed on a Blackrock ship";
        }
        if (ship.getVariant().getHullMods().contains("dedicated_targeting_core")) {
            return "Incompatible with Dedicated Targeting Core";
        }
        if (ship.getVariant().getHullMods().contains("targetingunit")) {
            return "Incompatible with Integrated Targeting Unit";
        }
        if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
            return "Incompatible with Safety Overrides";
        }        

        return null;
    }
}
