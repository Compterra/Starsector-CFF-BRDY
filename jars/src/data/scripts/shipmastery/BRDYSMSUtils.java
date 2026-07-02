package data.scripts.shipmastery;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import data.scripts.util.BRDYSettings;

import java.util.HashMap;
import java.util.Map;

public final class BRDYSMSUtils {

    public static final String ASSAULT_OPS = "brassaultop";
    public static final String DIMENSIONAL_ENGINE = "brdimeng";
    public static final String DRIVE_CONVERSION = "brdrive";
    public static final String FLUX_CORE = "brfluxmod";
    public static final String FOCUSED_SHIELDS = "brshields";
    public static final String HARDENED_WEAPONS = "br_hardenedweaps";
    public static final String NANOLATTICE_ARMOR = "brimaginosregen";
    public static final String OVERTUNED_DRIVES = "br_overtuneddrives";
    public static final String STRIKE_SUITE = "brtarget";

    private static final String[] BLACKROCK_HULLMODS = {
            ASSAULT_OPS,
            DIMENSIONAL_ENGINE,
            DRIVE_CONVERSION,
            FLUX_CORE,
            FOCUSED_SHIELDS,
            HARDENED_WEAPONS,
            NANOLATTICE_ARMOR,
            OVERTUNED_DRIVES,
            STRIKE_SUITE
    };

    private static final Map<String, Boolean> BLACKROCK_HULL_CACHE = new HashMap<>();

    private BRDYSMSUtils() {
    }

    public static boolean canUseBlackrockPackage(ShipHullSpecAPI spec) {
        return BRDYSettings.shipMasteryPackagesEnabled() && isBlackrockHull(spec);
    }

    public static boolean isBlackrockHull(ShipHullSpecAPI spec) {
        if (spec == null) {
            return false;
        }

        String id = spec.getHullId();
        if (id != null) {
            Boolean cached = BLACKROCK_HULL_CACHE.get(id);
            if (cached != null) {
                return cached.booleanValue();
            }
        }

        boolean result = isBlackrockHullUncached(spec);
        if (id != null) {
            BLACKROCK_HULL_CACHE.put(id, Boolean.valueOf(result));
        }
        return result;
    }

    public static boolean hasBlackrockHullmod(ShipVariantAPI variant) {
        if (variant == null) {
            return false;
        }
        for (String hullmod : BLACKROCK_HULLMODS) {
            if (variant.hasHullMod(hullmod)) {
                return true;
            }
        }
        return false;
    }

    public static String hullmodName(String id) {
        try {
            return Global.getSettings().getHullModSpec(id).getDisplayName();
        } catch (Throwable ex) {
            return id;
        }
    }

    private static boolean isBlackrockHullUncached(ShipHullSpecAPI spec) {
        String manufacturer = spec.getManufacturer();
        if (manufacturer != null && manufacturer.toLowerCase().contains("blackrock")) {
            return true;
        }
        for (String hullmod : BLACKROCK_HULLMODS) {
            if (spec.getBuiltInMods().contains(hullmod)) {
                return true;
            }
        }
        String id = spec.getHullId();
        return id != null && (id.startsWith("brdy") || id.startsWith("br_"));
    }
}