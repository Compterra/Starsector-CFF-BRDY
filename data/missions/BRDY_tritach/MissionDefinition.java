package data.missions.BRDY_tritach;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "BRS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "TTS", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Blackrock Drive Yards Escort");
        api.setFleetTagline(FleetSide.ENEMY, "Tri-Tachyon Strike Force");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Disable, destroy or drive off all enemy vessels.");
        api.addBriefingItem("Use superior mobility and vent timing to collapse isolated Tri-Tachyon ships.");
        api.addBriefingItem("Do not let the Aurora dictate the center of the map.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "nevermore_advanced", FleetMemberType.SHIP, "BRS Mercurial", true);
        api.addToFleet(FleetSide.PLAYER, "brdy_eurypterus_standard", FleetMemberType.SHIP, "BRS Burgess", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_scorpion_standard", FleetMemberType.SHIP, "BRS Out There", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_cs", FleetMemberType.SHIP, "BRS Rockeye", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_locust_patrol", FleetMemberType.SHIP, false);

        // Set up the enemy fleet
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP, "TTS Illuminant", false);
        api.addToFleet(FleetSide.ENEMY, "medusa_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 14000f;
        float height = 12000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 100; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 4;

            if (x > -1000 && x < 1500 && y < -1000) {
                continue;
            }
            float radius = 200f + (float) Math.random() * 900f;
            api.addNebula(x, y, radius);
        }

        api.addObjective(minX + width * 0.7f, minY + height * 0.65f, "nav_buoy");
        api.addObjective(minX + width * 0.5f, minY + height * 0.35f, "nav_buoy");
        api.addObjective(minX + width * 0.2f, minY + height * 0.6f, "sensor_array");
    }
}
