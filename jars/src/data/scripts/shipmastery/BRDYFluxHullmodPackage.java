package data.scripts.shipmastery;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import shipmastery.mastery.impl.hullmods.FluxHullmodPackage;
import shipmastery.util.Utils;

public class BRDYFluxHullmodPackage extends FluxHullmodPackage {

    @Override
    protected String getDescriptionString() {
        return "If 3 of the following hullmods are installed: %1$s, %2$s, %3$s, %4$s, or %5$s, increases flux capacity and flux dissipation by %6$s.";
    }

    @Override
    protected String[] getDescriptionParams(ShipVariantAPI selectedVariant) {
        return new String[] {
                Utils.getHullmodName(HullMods.FLUX_COIL),
                Utils.getHullmodName(HullMods.FLUX_DISTRIBUTOR),
                Utils.getHullmodName(HullMods.FLUXBREAKERS),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.FLUX_CORE),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.DIMENSIONAL_ENGINE),
                Utils.asPercentNoDecimal(getStrength(selectedVariant))
        };
    }

    @Override
    protected HullmodData[] getHullmodList() {
        return new HullmodData[] {
                new Data(HullMods.FLUX_COIL, false),
                new Data(HullMods.FLUX_DISTRIBUTOR, false),
                new Data(HullMods.FLUXBREAKERS, false),
                new Data(BRDYSMSUtils.FLUX_CORE, false),
                new Data(BRDYSMSUtils.DIMENSIONAL_ENGINE, false)
        };
    }

    @Override
    public Float getSelectionWeight(ShipHullSpecAPI spec) {
        Float weight = super.getSelectionWeight(spec);
        if (weight == null || !BRDYSMSUtils.canUseBlackrockPackage(spec)) {
            return null;
        }
        return weight * 1.4f;
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