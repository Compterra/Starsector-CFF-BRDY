package data.missions.BRDY_bigbattle;

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
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "Blackrock Full-Spectrum Battleline");
        api.setFleetTagline(FleetSide.ENEMY, "Sector Composite Opposition Fleet");

        // These show up as items in the bulleted list under 
        // "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("Defeat the enemy fleet.");
        api.addBriefingItem("Use Blackrock capitals to anchor the center while fast wings and destroyers contest objectives.");
        api.addBriefingItem("The enemy has a broad mixed-tech roster; preserve your elite hulls for the second wave.");

        // Set up the player's fleet
        api.addToFleet(FleetSide.PLAYER, "brdy_morpheus_proto", FleetMemberType.SHIP, true);        
        api.addToFleet(FleetSide.PLAYER, "brdy_imaginos_elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_karkinos_prototype", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_karkinos_assault", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_kurmaraja_elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_kurmaraja_heavy", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_nevermore_00_retro", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "nevermore_assault", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_antaeus_standard", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_antaeus_assault", FleetMemberType.SHIP, true);        
        api.addToFleet(FleetSide.PLAYER, "brdy_stenos_exploration", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_convergence_standard", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_knight_elite", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "gonodactylus_elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_hawkmoth_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "gonodactylus_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_eurypterus_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_scorpion_standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_scorpion_fs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_eurypterus_assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_typheus_support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_typheus_defender", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_megaceras_standard", FleetMemberType.SHIP, true); 
        api.addToFleet(FleetSide.PLAYER, "desdinova_assault", FleetMemberType.SHIP, true);        
        api.addToFleet(FleetSide.PLAYER, "brdy_dynastos_standard", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "desdinova_fastattack", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_asura_strike", FleetMemberType.SHIP, true);
        api.addToFleet(FleetSide.PLAYER, "brdy_mantis_strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_mantis_elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_dragonfly_skirmisher", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.PLAYER, "brdy_dragonfly_attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "scarab_attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_silverfish_mod_elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_zabrus_attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_locust_strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_locust_patrol", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_locust_hunter", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_barrage", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_light", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_light", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_cs", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.PLAYER, "brdy_robberfly_strike", FleetMemberType.SHIP, false);

        // Set up the enemy fleet
        api.addToFleet(FleetSide.ENEMY, "legion_FS", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "astral_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "onslaught_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "conquest_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "odyssey_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "falcon_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "venture_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "apogee_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "dominator_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "aurora_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "gryphon_FS", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "enforcer_XIV_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "dominator_Support", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "condor_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "condor_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "medusa_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "medusa_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "sunder_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "enforcer_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hammerhead_Balanced", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "mora_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hound_hegemony_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "cerberus_d_pirates_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "cerberus_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "afflictor_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "shade_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "heron_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "buffalo2_FS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "colossus2_Pather", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "colossus3_Pirate", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "shrike_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "gonodactylus_p_berserker", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "gonodactylus_p_strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brdy_silverfish_mod_p_raider", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brdy_robberfly_p_raider", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "centurion_Assault", FleetMemberType.SHIP, false);        
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "drover_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "hyperion_Strike", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "vigilance_FS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_Standard", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brawler_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "scarab_Experimental", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_hegemony_Assault", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "tempest_Attack", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "wolf_CS", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brawler_Elite", FleetMemberType.SHIP, false);
        api.addToFleet(FleetSide.ENEMY, "brawler_tritachyon_Standard", FleetMemberType.SHIP, false);

        // Set up the map.
        float width = 20000f;
        float height = 26000f;
        api.initMap(-width / 2f, width / 2f, -height / 2f, height / 2f);

        float minX = -width / 2;
        float minY = -height / 2;

        for (int i = 0; i < 50; i++) {
            float x = (float) Math.random() * width - width / 2;
            float y = (float) Math.random() * height - height / 2;
            float radius = 100f + (float) Math.random() * 400f;
            api.addNebula(x, y, radius);
        }

        // Add objectives
        api.addObjective(minX + width * 0.25f, minY + height * 0.25f, "nav_buoy");
        api.addObjective(minX + width * 0.75f, minY + height * 0.25f, "comm_relay");
        api.addObjective(minX + width * 0.75f, minY + height * 0.75f, "nav_buoy");
        api.addObjective(minX + width * 0.25f, minY + height * 0.75f, "comm_relay");
        api.addObjective(minX + width * 0.5f, minY + height * 0.5f, "sensor_array");
    }
}
