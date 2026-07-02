package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class SentinelDroneStats extends BaseShipSystemScript {

    public static final float AUTOFIRE_PERCENT = 50f;
    public static final float SENSOR_RANGE_PERCENT = 30f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float autofirePercent = AUTOFIRE_PERCENT * effectLevel;

        stats.getSightRadiusMod().modifyPercent(id, sensorRangePercent);

        stats.getAutofireAimAccuracy().modifyPercent(id, autofirePercent);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        float sensorRangePercent = SENSOR_RANGE_PERCENT * effectLevel;
        float autofirePercent = AUTOFIRE_PERCENT * effectLevel;
        if (index == 0) {
            return new StatusData("sensor range +" + (int) sensorRangePercent + "%", false);
        } else if (index == 1) {
            return new StatusData("autoaim accuracy +" + (int) autofirePercent + "%", false);
        }
        return null;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getSightRadiusMod().unmodify(id);

        stats.getAutofireAimAccuracy().unmodify(id);
    }
}
