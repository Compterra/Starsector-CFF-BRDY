package data.scripts.shipmastery;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import shipmastery.config.Settings;
import shipmastery.mastery.impl.hullmods.HullmodPackage;
import shipmastery.util.Utils;

public class BRDYWeaponSystemsHullmodPackage extends HullmodPackage {

    private static final float REQ_NOT_MET_MULT = 0.5f;
    private static final float TURN_RATE_MULT = 1.5f;

    @Override
    protected String getDescriptionString() {
        return "If 2 of the following hullmods are installed: %1$s, %2$s, or %3$s, increases weapon damage by %4$s and weapon turn rate by %5$s.";
    }

    @Override
    protected String[] getDescriptionParams(ShipVariantAPI selectedVariant) {
        float strength = getStrength(selectedVariant);
        return new String[] {
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.STRIKE_SUITE),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.HARDENED_WEAPONS),
                Utils.getHullmodName(HullMods.ARMOREDWEAPONS),
                Utils.asPercentNoDecimal(strength),
                Utils.asPercentNoDecimal(strength * TURN_RATE_MULT)
        };
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipVariantAPI selectedVariant,
                                          FleetMemberAPI selectedFleetMember) {
        tooltip.addPara("Otherwise, increases weapon turn rate by %s.", 0f, Settings.POSITIVE_HIGHLIGHT_COLOR,
                        Utils.asPercentNoDecimal(getStrength(selectedVariant) * TURN_RATE_MULT * REQ_NOT_MET_MULT));
    }

    @Override
    protected HullmodData[] getHullmodList() {
        return new HullmodData[] {
                new Data(BRDYSMSUtils.STRIKE_SUITE, false),
                new Data(BRDYSMSUtils.HARDENED_WEAPONS, false),
                new Data(HullMods.ARMOREDWEAPONS, false)
        };
    }

    @Override
    protected int getRequiredCount() {
        return 2;
    }

    @Override
    protected void apply(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats) {
        float strength = getStrength(stats);
        stats.getBallisticWeaponDamageMult().modifyPercent(id, 100f * strength);
        stats.getEnergyWeaponDamageMult().modifyPercent(id, 100f * strength);
        stats.getMissileWeaponDamageMult().modifyPercent(id, 100f * strength);
        stats.getWeaponTurnRateBonus().modifyPercent(id, 100f * strength * TURN_RATE_MULT);
        stats.getBeamWeaponTurnRateBonus().modifyPercent(id, 100f * strength * TURN_RATE_MULT);
    }

    @Override
    protected void applyIfRequirementNotMet(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats) {
        float strength = getStrength(stats) * TURN_RATE_MULT * REQ_NOT_MET_MULT;
        stats.getWeaponTurnRateBonus().modifyPercent(id, 100f * strength);
        stats.getBeamWeaponTurnRateBonus().modifyPercent(id, 100f * strength);
    }

    @Override
    public Float getSelectionWeight(ShipHullSpecAPI spec) {
        if (!BRDYSMSUtils.canUseBlackrockPackage(spec) || spec.isCivilianNonCarrier()) {
            return null;
        }
        Utils.WeaponSlotCount slots = Utils.countWeaponSlots(spec);
        if (slots.ltotal + slots.mtotal + slots.stotal == 0) {
            return null;
        }
        return 0.85f;
    }

    @Override
    public float getNPCWeight(FleetMemberAPI fm) {
        if (fm == null || fm.getVariant() == null) {
            return 0f;
        }
        if (hasRequiredCount(fm.getVariant())) {
            return 3f * super.getNPCWeight(fm);
        }
        return 0.5f * super.getNPCWeight(fm);
    }
    private static final class Data extends HullmodData {
        private Data(String id, boolean requireBuiltIn) {
            super(id, requireBuiltIn);
        }
    }
}