package data.missions.BRDY_endtimes;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    @Override
    public void defineMission(MissionDefinitionAPI api) {
        // Set up the fleets
        api.initFleet(FleetSide.PLAYER, "BRS", FleetGoal.ATTACK, false, 2);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true, 5);

        // Set a blurb for each fleet
        api.setFleetTagline(FleetSide.PLAYER, "Blackrock IA Logistics Fleet 13-4");
        api.setFleetTagline(FleetSide.ENEMY, "Black Fox Mercenary Detachment");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat the enemy forces - BRS Prime Mover must survive.");
        api.addBriefingItem("You can retreat the BRS Prime Mover, but you will require its firepower and flight decks.");
        api.addBriefingItem("Your Scalar Deracinator/Scalar Rejector can be used to destroy fighters.");
        api.addBriefingItem("Many enemy ships are vulnerable from behind; use the Shadowcat to break pursuit angles.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "brdy_imaginos_elite", FleetMemberType.SHIP, "BRS Shadowcat", true);
        api.addToFleet(FleetSide.PLAYER, "brdy_eschaton_armed", FleetMemberType.SHIP, "BRS Prime Mover", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_megaceras_tactical", FleetMemberType.SHIP, "BRS World Citizen", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_hawkmoth_armed", FleetMemberType.SHIP, "BRS Homeostasis", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_megaceras_tactical", FleetMemberType.SHIP, "BRS Ill Gotten Gains", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_silverfish_b_standard", FleetMemberType.SHIP, "ISS Bulldog King II", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_light", FleetMemberType.SHIP, "BRS Gutter Song", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_cs", FleetMemberType.SHIP, "BRS Spring Heeled", false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_strike", FleetMemberType.SHIP, "BRS Cloud Surfer", false);

        // Mark flagship as essential
        api.defeatOnShipLoss("BRS Prime Mover");

        api.addToFleet(FleetSide.ENEMY, "eagle_Balanced", FleetMemberType.SHIP, "ISS Drake", false);
        api.addToFleet(FleetSide.ENEMY, "falcon_CS", FleetMemberType.SHIP, "ISS Thundergod", false);
        api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, "ISS Raven", false);
        api.addToFleet(FleetSide.ENEMY, "gryphon_FS", FleetMemberType.SHIP, "ISS Ichiro", false);
        api.addToFleet(FleetSide.ENEMY, "shade_Assault", FleetMemberType.SHIP, "ISS Rainyday", false);
        api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, "ISS Tusk", false);

        api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, "ISS Hegre", false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, "ISS Firestar", false);

        api.addToFleet(FleetSide.ENEMY, "wolf_PD", FleetMemberType.SHIP, "ISS Locus", false);
        api.addToFleet(FleetSide.ENEMY, "wolf_Strike", FleetMemberType.SHIP, "ISS Faint", false);
        api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, "ISS Suliman", false);

        // Set up the map.
        float width = 24000f;
        float height = 18000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        // All the addXXX methods take a pair of coordinates followed by data for
        // whatever object is being added.
        // And a few random ones to spice up the playing field.
        // A similar approach can be used to randomize everything
        // else, including fleet composition.
        for (int i = 0; i < 25; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 400f + (float) Math.random() * 1000f;
            api.addNebula(x, y, radius);
        }

        // Add objectives. These can be captured by each side
        // and provide stat bonuses and extra command points to
        // bring in reinforcements.
        // Reinforcements only matter for large fleets - in this
        // case, assuming a 100 command point battle size,
        // both fleets will be able to deploy fully right away.
        api.addObjective(minX + width * 0.2f + 400 + 3000, minY + height * 0.2f + 400 + 2000, "sensor_array");
        api.addObjective(minX + width * 0.4f + 2000, minY + height * 0.7f, "sensor_array");
        api.addObjective(minX + width * 0.75f - 2000, minY + height * 0.7f, "comm_relay");
        api.addObjective(minX + width * 0.2f + 3000, minY + height * 0.5f, "nav_buoy");
        api.addObjective(minX + width * 0.85f - 3000, minY + height * 0.4f, "nav_buoy");

        api.addPlanet(0, 0, 520f, "gas_giant", 300f, true);
    }
}
