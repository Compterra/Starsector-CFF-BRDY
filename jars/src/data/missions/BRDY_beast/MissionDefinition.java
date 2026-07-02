package data.missions.BRDY_beast;

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
        api.setFleetTagline(FleetSide.PLAYER, "Last Defender of Orbital Yard III");
        api.setFleetTagline(FleetSide.ENEMY, "Hegemony Siege Fleet Remnants");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Disable, destroy or drive off all enemy vessels.");
        api.addBriefingItem("BRS Year of Silence must absorb pressure without being boxed in by the siege fleet.");
        api.addBriefingItem("Use the nebula field to split enemy fire and punish overextended escorts.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "brdy_karkinos_prototype", FleetMemberType.SHIP, "BRS Year of Silence", true);
        // Set up the enemy fleet
        api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, "HSS Artemis", false);        
        api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, "HSS Diana", false);      
        api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, "HSS Heimdall", false);        
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP,false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP,false);                  
        api.addToFleet(FleetSide.ENEMY, "lasher_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_CS", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);       

        // Set up the map.
        float width = 12000f;
        float height = 15000f;
        api.initMap((float) -width / 2f, (float) width / 2f, (float) -height / 2f, (float) height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 100; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 4;

            if (x > -1000 && x < 1500 && y < -1000) {
                continue;
            }
            float radius = 100f + (float) Math.random() * 600f;
            api.addNebula(x, y, radius);
        }    

        api.addObjective(minX + width * 0.45f, minY + height * 0.325f, "sensor_array");
        api.addObjective(minX + width * 0.55f, minY + height * 0.25f, "nav_buoy");
        api.addObjective(minX + width * 0.5f, minY + height * 0.8f, "nav_buoy");
        
        

    }

}
