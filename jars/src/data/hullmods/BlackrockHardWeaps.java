package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class BlackrockHardWeaps extends BaseHullMod {

	public static final float HEALTH_BONUS = 80f;
	public static final float TURN_BONUS = 25f;
 	public static final float RANGE_BONUS = 100f;       
        public static final float SMALL_WEAPON_RECOIL_REDUCTION = 50f;
        public static final float TACLASERTAX = 1;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponHealthBonus().modifyPercent(id, HEALTH_BONUS);
		stats.getWeaponTurnRateBonus().modifyPercent(id, TURN_BONUS);
		stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURN_BONUS);
                stats.getBallisticWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);
                stats.getEnergyWeaponRangeBonus().modifyFlat(id, RANGE_BONUS);       
                stats.getBeamWeaponRangeBonus().modifyFlat (id, -RANGE_BONUS);
                stats.getAutofireAimAccuracy().modifyFlat(id, TURN_BONUS * 0.01f);
	        stats.getRecoilPerShotMultSmallWeaponsOnly().modifyMult(id, 1f - SMALL_WEAPON_RECOIL_REDUCTION * 0.01f);
                stats.getDynamic().getMod(Stats.SMALL_BEAM_MOD).modifyFlat(id, TACLASERTAX);               
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HEALTH_BONUS + "%";
		if (index == 1) return "" + (int) TURN_BONUS + "%";
		if (index == 2) return "" + (int) RANGE_BONUS;                
		if (index == 3) return "" + (int) SMALL_WEAPON_RECOIL_REDUCTION + "%";
		if (index == 4) return "+" + (int) TACLASERTAX;
		return null;
	}
	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}
