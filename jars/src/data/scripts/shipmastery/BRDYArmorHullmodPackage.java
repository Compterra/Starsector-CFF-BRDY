package data.scripts.shipmastery;

import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import shipmastery.mastery.impl.hullmods.ArmorHullmodPackage;
import shipmastery.util.Utils;

public class BRDYArmorHullmodPackage extends ArmorHullmodPackage {

    @Override
    protected String getDescriptionString() {
        return "If 2 of the following hullmods are installed: %1$s, %2$s, %3$s, or %4$s, increases armor by an additional %5$s, stacking multiplicatively on top of other armor modifiers.";
    }

    @Override
    protected String[] getDescriptionParams(ShipVariantAPI selectedVariant) {
        return new String[] {
                Utils.getHullmodName(HullMods.HEAVYARMOR),
                Utils.getHullmodName(HullMods.ARMOREDWEAPONS),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.ASSAULT_OPS),
                BRDYSMSUtils.hullmodName(BRDYSMSUtils.NANOLATTICE_ARMOR),
                Utils.asPercentNoDecimal(getStrength(selectedVariant))
        };
    }

    @Override
    protected HullmodData[] getHullmodList() {
        return new HullmodData[] {
                new Data(HullMods.HEAVYARMOR, false),
                new Data(HullMods.ARMOREDWEAPONS, false),
                new Data(BRDYSMSUtils.ASSAULT_OPS, false),
                new Data(BRDYSMSUtils.NANOLATTICE_ARMOR, false)
        };
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