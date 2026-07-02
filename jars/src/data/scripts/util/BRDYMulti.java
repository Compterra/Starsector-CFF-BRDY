package data.scripts.util;

import com.fs.starfarer.api.combat.ShipAPI;

public class BRDYMulti {

    public static ShipAPI getRoot(ShipAPI ship) {
        if (isMultiShip(ship)) {
            ShipAPI root = ship;
            while (root.getParentStation() != null) {
                root = root.getParentStation();
            }
            return root;
        } else {
            return ship;
        }
    }

    public static boolean isMultiShip(ShipAPI ship) {
        if (ship.getParentStation() != null || ship.isShipWithModules()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isRoot(ShipAPI ship) {
        return getRoot(ship) == ship;
    }
}
