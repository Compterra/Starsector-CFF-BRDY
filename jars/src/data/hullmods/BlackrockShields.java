package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShieldAPI;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

public class BlackrockShields extends BaseHullMod {

    public static final int SPEED_BONUS = 150;
    public static final float HARD_BONUS = 15f;
    public static final int UPKEEP_BONUS = 50;
    public static final float SHIELD_ARC = 90f;
    public static final float SMOD_SHIELD_ARC_BONUS = 10f;
    
    private static final Color SHIELD_RING_COLOR = new Color(120, 255, 222, 255);
    private static final Color SHIELD_INNER_COLOR = new Color(70, 230, 196, 60);
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>(7);


    static {
        BLOCKED_HULLMODS.add("frontshield");
        BLOCKED_HULLMODS.add("frontemitter");
        BLOCKED_HULLMODS.add("adaptiveshields");
        BLOCKED_HULLMODS.add("advancedshieldemitter");
        BLOCKED_HULLMODS.add("hardenedshieldemitter");
        BLOCKED_HULLMODS.add("stabilizedshieldemitter");
        BLOCKED_HULLMODS.add("extendedshieldemitter");
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getShield() != null) {
            ship.getShield().setInnerColor(SHIELD_INNER_COLOR);
            ship.getShield().setRingColor(SHIELD_RING_COLOR);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                ship.getVariant().removeMod(tmp);
            }
        }
        ShieldAPI shield = ship.getShield();
        if (shield != null) {
            float arc = SHIELD_ARC;
            if (isSMod(ship)) {
                arc += SMOD_SHIELD_ARC_BONUS;
            }
            shield.setArc(arc);
        }
    }

    

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldTurnRateMult().modifyPercent(id, SPEED_BONUS);
        stats.getShieldUnfoldRateMult().modifyPercent(id, SPEED_BONUS);
stats.getShieldDamageTakenMult().modifyMult(id, 1f - HARD_BONUS * 0.01f);
        stats.getShieldUpkeepMult().modifyMult(id, 1f - UPKEEP_BONUS * 0.01f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + SPEED_BONUS + "%";
        }
        if (index == 1) {
            return "" + (int) HARD_BONUS + "%";
        }
        if (index == 2) {
            return "" + UPKEEP_BONUS + "%";
        }
        if (index == 3) {
            return "" + (int) SHIELD_ARC;
        }
        return null;
    }

    @Override
    public String getSModDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) SMOD_SHIELD_ARC_BONUS;
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a brdy hull id
        return (ship.getHullSpec().getHullId().startsWith("brdy_")
                || ship.getHullSpec().getHullId().startsWith("brdyx_"))
                && ship.getShield() != null
                && !ship.getVariant().getHullMods().contains("advancedshieldemitter")
                && !ship.getVariant().getHullMods().contains("hardenedshieldemitter")
                && !ship.getVariant().getHullMods().contains("stabilizedshieldemitter")
                && !ship.getVariant().getHullMods().contains("frontshield")
                && !ship.getVariant().getHullMods().contains("frontemitter")
                && !ship.getVariant().getHullMods().contains("adaptiveshields")                
                && !ship.getVariant().getHullMods().contains("extendedshieldemitter");
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!(ship.getHullSpec().getHullId().startsWith("brdy_")
                || ship.getHullSpec().getHullId().startsWith("brdyx_"))) {
            return "Must be installed on a Blackrock ship";
        }
        if (ship.getVariant().getHullMods().contains("advancedshieldemitter")) {
            return "Incompatible with Accelerated Shields";
        }
        if (ship.getVariant().getHullMods().contains("hardenedshieldemitter")) {
            return "Incompatible with Hardened Shields";
        }
        if (ship.getVariant().getHullMods().contains("stabilizedshieldemitter")) {
            return "Incompatible with Stabilized Shields";
        }
        if (ship.getVariant().getHullMods().contains("extendedshieldemitter")) {
            return "Incompatible with Extended Shields";
        }

        return null;
    }

}
