package data.scripts.shipmastery;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import shipmastery.mastery.impl.hullmods.EngineHullmodPackage;
import shipmastery.util.Utils;

public class BRDYEngineHullmodPackage extends EngineHullmodPackage {

    @Override
    protected String getDescriptionString() {
        return "If 3 of the following hullmods are installed: %1$s, %2$s, %3$s, %4$s, or %5$s, negates the range and fighter replacement time penalties of %1$s if present.";
    }

    @Override
    protected String[] getDescriptionParams(ShipVariantAPI selectedVariant) {
        return new String[] {
                Utils.getHullmodName(HullMods.UNSTABLE_INJECTOR),
                Utils.getHullmodName(HullMods.AUXILIARY_THRUSTERS),
                Utils.getHullmodName(HullMods.INSULATEDENGINE),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.DRIVE_CONVERSION),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.OVERTUNED_DRIVES)
        };
    }

    @Override
    protected HullmodData[] getHullmodList() {
        return new HullmodData[] {
                new Data(HullMods.UNSTABLE_INJECTOR, false),
                new Data(HullMods.AUXILIARY_THRUSTERS, false),
                new Data(HullMods.INSULATEDENGINE, false),
                new Data(BRDYSMSUtils.DRIVE_CONVERSION, false),
                new Data(BRDYSMSUtils.OVERTUNED_DRIVES, false)
        };
    }

    @Override
    public Float getSelectionWeight(ShipHullSpecAPI spec) {
        Float weight = super.getSelectionWeight(spec);
        if (weight == null || !BRDYSMSUtils.canUseBlackrockPackage(spec)) {
            return null;
        }
        return weight * 1.25f;
    }

    @Override
    public float getNPCWeight(FleetMemberAPI fm) {
        if (fm == null || fm.getVariant() == null) {
            return 0f;
        }
        if (!BRDYSMSUtils.hasBlackrockHullmod(fm.getVariant())) {
            return 0.5f * super.getNPCWeight(fm);
        }
        return super.getNPCWeight(fm);
    }
    private static final class Data extends HullmodData {
        private Data(String id, boolean requireBuiltIn) {
            super(id, requireBuiltIn);
        }
    }
}