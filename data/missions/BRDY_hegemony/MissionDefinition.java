package data.missions.BRDY_hegemony;

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
        api.initFleet(FleetSide.ENEMY, "HSS", FleetGoal.ATTACK, true);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Blackrock Fast Attack Doctrine Module");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Anchor-Line Simulation");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Disable, destroy or drive off all enemy vessels.");
        api.addBriefingItem("Keep your engines intact; fighter pressure and stray fire can trap the Desdinova.");
        api.addBriefingItem("Try to distract and flank the enemy Onslaught.");
        api.addBriefingItem("Your smaller escorts are there to create openings, not to trade head-on.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "desdinova_assault", FleetMemberType.SHIP, "BRS Alpha", true);
        api.addToFleet(FleetSide.PLAYER, "brdy_typheus_support", FleetMemberType.SHIP, "BRS Beta", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_barrage", FleetMemberType.SHIP, "BRS Gamma", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_strike", FleetMemberType.SHIP, "BRS Delta", false);
        // Set up the enemy fleet
        api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, "HSS Alpha", false);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, "HSS Beta", false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, "HSS Gamma", false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, "HSS Delta", false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, "HSS Epsilon", false);

        // Set up the map.
        float width = 12000f;
        float height = 11000f;
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
