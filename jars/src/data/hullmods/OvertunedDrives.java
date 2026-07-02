package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Strings;

public class OvertunedDrives extends BaseHullMod {

    public static final float EXTRA_DAMAGE = 200f;
   // public static final float HULL_DAMAGE_CR_MULT = 1.25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //stats.getDynamic().getStat(Stats.HULL_DAMAGE_CR_LOSS).modifyMult(id, HULL_DAMAGE_CR_MULT);
        stats.getEngineDamageTakenMult().modifyPercent(id, EXTRA_DAMAGE);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "3" + Strings.X;
        }
        return null;
    }
}
