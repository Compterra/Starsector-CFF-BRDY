package data.scripts.shipmastery;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import shipmastery.mastery.MasteryDescription;
import shipmastery.mastery.impl.hullmods.ShieldHullmodPackage;
import shipmastery.util.Utils;

public class BRDYShieldHullmodPackage extends ShieldHullmodPackage {

    @Override
    public MasteryDescription getDescription(ShipVariantAPI selectedVariant, FleetMemberAPI selectedFleetMember) {
        return MasteryDescription.initDefaultHighlight(getDescriptionString())
                .params((Object[]) getDescriptionParams(selectedVariant));
    }

    @Override
    protected String getDescriptionString() {
        return "If %1$s is installed, or if 3 of the following hullmods are installed: %2$s, %3$s, %4$s, and %5$s, reduces shield damage taken by %6$s and shield upkeep by %7$s.";
    }

    @Override
    protected String[] getDescriptionParams(ShipVariantAPI selectedVariant) {
        return new String[] {
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.FOCUSED_SHIELDS),
                Utils.getHullmodName(HullMods.ACCELERATED_SHIELDS),
                Utils.getHullmodName(HullMods.EXTENDED_SHIELDS),
                Utils.getHullmodName(HullMods.HARDENED_SHIELDS),
                Utils.getHullmodName(HullMods.STABILIZEDSHIELDEMITTER),
                Utils.asPercent(getStrength(selectedVariant)),
                Utils.asPercent(1f - SHIELD_UPKEEP_MULT)
        };
    }

    @Override
    public boolean hasRequiredCount(ShipVariantAPI variant) {
        if (variant == null) {
            return false;
        }
        return variant.hasHullMod(BRDYSMSUtils.FOCUSED_SHIELDS) || super.hasRequiredCount(variant);
    }

    @Override
    public Float getSelectionWeight(ShipHullSpecAPI spec) {
        Float weight = super.getSelectionWeight(spec);
        if (weight == null || !BRDYSMSUtils.canUseBlackrockPackage(spec)) {
            return null;
        }
        return weight * 1.35f;
    }

    @Override
    public float getNPCWeight(FleetMemberAPI fm) {
        if (fm == null || fm.getVariant() == null) {
            return 0f;
        }
        if (fm.getVariant().hasHullMod(BRDYSMSUtils.FOCUSED_SHIELDS)) {
            return 3f * super.getNPCWeight(fm);
        }
        return super.getNPCWeight(fm);
    }
}