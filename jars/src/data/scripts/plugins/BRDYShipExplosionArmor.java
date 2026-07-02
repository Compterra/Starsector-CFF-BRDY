package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.combat.CombatUtils;

// Reduces damage taken by dimensional-engine hulls near disabled-vessel explosions.
public class BRDYShipExplosionArmor extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "BRDYShipExplosionArmor";
    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }
        final Set<ShipAPI> deadShips = localData.deadShips;
        final Map<ShipAPI, Integer> modifiedShips = localData.modifiedShips;

        List<ShipAPI> ships = engine.getShips();

        Iterator<Map.Entry<ShipAPI, Integer>> iter = modifiedShips.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, Integer> entry = iter.next();
            ShipAPI ship = entry.getKey();
            if (entry.getValue() > 0) {
                entry.setValue(entry.getValue() - 1);
            } else {
                if (ship != null) {
                    ship.getMutableStats().getHighExplosiveDamageTakenMult().unmodify(DATA_KEY);
                }
                iter.remove();
            }
        }

        int shipsSize = ships.size();
        for (int i = 0; i < shipsSize; i++) {
            ShipAPI ship = ships.get(i);
            if (ship == null || ship.isDrone() || ship.isFighter()) {
                continue;
            }

            if (ship.isHulk() == true) {
                if (!deadShips.contains(ship)) {
                    deadShips.add(ship);

                    float estimatedExplosionRange = ship.getCollisionRadius() +
                          Math.min(200f, ship.getCollisionRadius()) + 40f;
                    List<ShipAPI> possibleTargets = CombatUtils.getShipsWithinRange(ship.getLocation(),
                                                                                    estimatedExplosionRange);
                    int targetsSize = possibleTargets.size();
                    for (int j = 0; j < targetsSize; j++) {
                        ShipAPI target = possibleTargets.get(j);
                        if (target != null && target.getHullSpec() != null
                                && target.getHullSpec().getHullId().startsWith("brdyx_")) {
                            modifiedShips.put(target, 1);
                            target.getMutableStats().getHighExplosiveDamageTakenMult().modifyMult(DATA_KEY, 0.50f);
                        }
                    }
                }
            }
        }

        Iterator<ShipAPI> iter2 = deadShips.iterator();
        while (iter2.hasNext()) {
            ShipAPI ship = iter2.next();

            if (ship != null && !engine.isEntityInPlay(ship)) {
                iter2.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        engine.getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class LocalData {

        final Set<ShipAPI> deadShips = new LinkedHashSet<>(100);
        final Map<ShipAPI, Integer> modifiedShips = new LinkedHashMap<>(100);
    }
}
