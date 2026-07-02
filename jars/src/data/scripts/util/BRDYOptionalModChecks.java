package data.scripts.util;

import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.BRModPlugin;
import data.scripts.util.BRDYOptionalModChecks;
import java.lang.reflect.Method;

public class BRDYOptionalModChecks {

    private static Method templarShieldLevelMethod = null;
    private static boolean templarShieldLookupAttempted = false;

    public static boolean isTemplarLatticeShieldActive(ShipAPI ship) {
        if (!BRModPlugin.templarsExists || ship == null || ship.getVariant() == null) {
            return false;
        }

        if (!ship.getVariant().getHullMods().contains("tem_latticeshield")) {
            return false;
        }

        Method method = getTemplarShieldLevelMethod();
        if (method == null) {
            return false;
        }

        try {
            Object result = method.invoke(null, ship);
            return result instanceof Number && ((Number) result).floatValue() > 0f;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Method getTemplarShieldLevelMethod() {
        if (templarShieldLookupAttempted) {
            return templarShieldLevelMethod;
        }

        templarShieldLookupAttempted = true;
        String[] classNames = new String[] {
            "data.scripts.hullmods.TEM_LatticeShield",
            "data.hullmods.TEM_LatticeShield"
        };

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                templarShieldLevelMethod = clazz.getMethod("shieldLevel", ShipAPI.class);
                return templarShieldLevelMethod;
            } catch (Exception ex) {
            }
        }

        return null;
    }
}
