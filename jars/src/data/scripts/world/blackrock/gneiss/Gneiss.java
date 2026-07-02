package data.scripts.world.blackrock.gneiss;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.BRDY_Conditions;
import data.campaign.ids.BRDY_Industries;
import data.scripts.world.blackrock.addMarketplace;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class Gneiss {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Gneiss");
        system.getLocation().set(18000, -18600);
        system.setBackgroundTextureFilename("graphics/BR/backgrounds/9-4.jpg");

        PlanetAPI gneiss = system.initStar("gneiss", "star_brstar", 450f, 500f); // 0.9 solar masses

        SectorEntityToken gneiss_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/gneiss_nebula.png",
                                                                0, 0, // center of nebula
                                                                system, // location to add to
                                                                "terrain", "nebula_greenish", // "nebula_blue", // texture to use, uses xxx_map for map
                                                                4, 4, StarAge.AVERAGE); // number of cells in texture

        //system.addAsteroidBelt(gneiss, 50, 1600, 255, 65, 75); // 0.32 AU
        PlanetAPI blackrock = system.addPlanet("blackrock", gneiss, "Blackrock", "br_blackrockplanet", 300, 140, 3400,
                                               215);
        blackrock.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "blackrock_planet_glow"));
        blackrock.getSpec().setGlowColor(new Color(255, 255, 255, 255));
        blackrock.getSpec().setUseReverseLightForGlow(true);
        blackrock.applySpecChanges();
        blackrock.setInteractionImage("illustrations", "blackrock_sky_city");
        blackrock.setCustomDescriptionId("blackrock_blackrockplanet");

        SectorEntityToken blackrockMirror = system.addCustomEntity("blackrock_mirror", "Blackrock Stellar Mirror",
                                                                   "blackrock_mirror",
                                                                   "blackrock_driveyards");
        blackrockMirror.setFacing(120f);
        blackrockMirror.setCircularOrbitWithSpin(blackrock, 120, 1200, 215, 1f / 2150f, 1f / 2150f); // Interposed between blackrock and gneiss

        PlanetAPI lodestone = system.addPlanet("lodestone", blackrock, "Lodestone", "br_lodestone", 30, 50, 500, 25); // 0.0025 AU
        lodestone.setCustomDescriptionId("br_lodestone");
        lodestone.setInteractionImage("illustrations", "space_bar");
        lodestone.getSpec().setRotation(5f); // 5 degrees/second = 7.2 days/revolution
        lodestone.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        lodestone.getSpec().setGlowColor(new Color(255, 255, 255, 255));
        lodestone.getSpec().setUseReverseLightForGlow(true);
        lodestone.applySpecChanges();

        SectorEntityToken BRstation
                = system.addCustomEntity("gneiss_br_station", "Port Augustmoon", "gneiss_br_station",
                        "blackrock_driveyards");
        BRstation.setCircularOrbitPointingDown(lodestone, 270, 75, -7.2f); // Locked to lodestone
        BRstation.setInteractionImage("illustrations", "city_from_above");

        SectorEntityToken relay = system.addCustomEntity("gneiss_relay", "Gneiss Relay", "comm_relay",
                                                         "blackrock_driveyards");
        relay.setCircularOrbit(gneiss, 220, 3650, 215);
        
                SectorEntityToken stableloc2 = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		stableloc2.setCircularOrbitPointingDown(gneiss, 40, 3650, 215f);
                
                SectorEntityToken stableloc3 = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		stableloc3.setCircularOrbitPointingDown(gneiss, 310, 3650, 215f);

        PlanetAPI creir = system.addPlanet("creir", gneiss, "Creir", "toxic", 100, 130, 6500, 360);
                Misc.initConditionMarket(creir);
        creir.getMarket().addCondition(Conditions.TOXIC_ATMOSPHERE);
        creir.getMarket().addCondition(Conditions.HOT);
        

        PlanetAPI lydia = system.addPlanet("lydia", creir, "Lydia", "barren_iron", 40, 60, 850, 45); // 0.004 AU
        lydia.setInteractionImage("illustrations", "vacuum_colony");
        lydia.setCustomDescriptionId("blackrock_lydia");

        system.addAsteroidBelt(gneiss, 70, 5600, 128, 440, 470);

        PlanetAPI nanoplanet = system.addPlanet("nanoplanet", gneiss, "Verge", "br_nanoplanet", 230, 340, 9500, 800); //
        nanoplanet.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        nanoplanet.getSpec().setGlowColor(new Color(54, 119, 84, 84));
        nanoplanet.getSpec().setUseReverseLightForGlow(true);
        nanoplanet.applySpecChanges();
        
                Misc.initConditionMarket(nanoplanet);
        nanoplanet.getMarket().addCondition(BRDY_Conditions.VERGE_CON);
        nanoplanet.getMarket().addCondition(Conditions.DENSE_ATMOSPHERE);        
        nanoplanet.getMarket().addCondition(Conditions.HIGH_GRAVITY);

        system.addRingBand(nanoplanet, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 360, 7.9f);
        system.addRingBand(nanoplanet, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 370, 9.95f);
        system.addRingBand(nanoplanet, "misc", "rings_dust0", 256f, 1, Color.white, 256f, 380, 11.45f);

        system.addAsteroidBelt(nanoplanet, 70, 900, 128, 10, 16);

        SectorEntityToken vigil = system.addCustomEntity("brstation2", "Vigil Station", "br_station",
                "blackrock_driveyards");
        vigil.setCircularOrbitPointingDown(system.getEntityById("nanoplanet"), 90, 540, 11);
        vigil.setInteractionImage("illustrations", "blackrock_vigil_station");
        vigil.setCustomDescriptionId("blackrock_vigil");

        PlanetAPI preclusion = system.addPlanet("preclusion", gneiss, "Preclusion", "cryovolcanic", 260, 30, 12200, 480);
        preclusion.setInteractionImage("illustrations", "abandoned_station");
        preclusion.setCustomDescriptionId("blackrock_preclusion");

        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("br_jp", "Lodestone Passage");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(blackrock, 90, 550, 25);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setRelatedPlanet(lodestone);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        MarketAPI brlodestoneMarket = addMarketplace.addMarketplace("blackrock_driveyards", blackrock,
                                      new ArrayList<>(Arrays.asList(BRstation, lodestone)),
                                      "Blackrock",
                                      6,
                                      new ArrayList<>(Arrays.asList(BRDY_Conditions.BARREN, Conditions.ORE_ABUNDANT, Conditions.RARE_ORE_RICH,
                                                                    Conditions.HOT,
                                                                    Conditions.ORGANICS_TRACE,
                                                                    //"brdy_space_elevator", 
                                                                    Conditions.POPULATION_6,
                                                                    Conditions.REGIONAL_CAPITAL)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.POPULATION,
                                                            Industries.LIGHTINDUSTRY,
                                                            Industries.MEGAPORT,
                                                            Industries.WAYSTATION,
                                                            BRDY_Industries.BRDYDEFHQ,
                                                            Industries.HEAVYBATTERIES,
                                                            Industries.HIGHCOMMAND,
                                                            Industries.STARFORTRESS_MID,
                                                            Industries.MINING,
                                                            Industries.REFINING)),                                       
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.GENERIC_MILITARY,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f);
brlodestoneMarket.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(Arrays.asList(Items.PRISTINE_NANOFORGE)));        
        

        addMarketplace.addMarketplace("blackrock_driveyards", vigil,
                                      new ArrayList<>(Arrays.asList(vigil)),
                                      "Vigil Station",
                                      3,
                                      new ArrayList<>(Arrays.asList(
                                                                    Conditions.POPULATION_4,
                                                                    Conditions.STEALTH_MINEFIELDS,
                                                                    Conditions.DISSIDENT, Conditions.VICE_DEMAND)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.POPULATION,
                                                            Industries.MILITARYBASE,
                                                            Industries.GROUNDDEFENSES,
                                                            Industries.SPACEPORT, Industries.WAYSTATION,
                                                            Industries.PATROLHQ,
                                                            Industries.BATTLESTATION_MID,
                                                            Industries.LIGHTINDUSTRY)),                                   
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        addMarketplace.addMarketplace(Factions.INDEPENDENT, lydia,
                                      null,
                                      "Lydia",
                                      3,
                                      new ArrayList<>(
                                              Arrays.asList(Conditions.NO_ATMOSPHERE, Conditions.FREE_PORT,
                                                            Conditions.FRONTIER, Conditions.POPULATION_3)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.SPACEPORT,
                                                            Industries.POPULATION,
                                                            Industries.WAYSTATION,
                                                            Industries.PATROLHQ,
                                                            Industries.FUELPROD,
                                                            Industries.LIGHTINDUSTRY)),                                        
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        addMarketplace.addMarketplace(Factions.PIRATES, preclusion,
                                      null,
                                      "Preclusion",
                                      3,
                                      new ArrayList<>(Arrays.asList(Conditions.ICE,
                                                                    Conditions.FREE_PORT,
                                                                    Conditions.POPULATION_3)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.SPACEPORT,
                                                            Industries.POPULATION,
                                                            Industries.WAYSTATION,
                                                            Industries.PATROLHQ,
                                                            Industries.FUELPROD,
                                                            Industries.LIGHTINDUSTRY)),
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, gneiss, StarAge.AVERAGE,
                                                                    2, 4, // min/max entities to add
                                                                    14200, // radius to start adding at
                                                                    3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                                                                    true); // whether to use custom or system-name based names

        system.autogenerateHyperspaceJumpPoints(true, true); //begone evil clouds
                HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
                NebulaEditor editor = new NebulaEditor(plugin);
                float minRadius = plugin.getTileSize() * 2f;

                float radius = system.getMaxRadiusInHyperspace();
                editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
                editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

 //   void cleanup(StarSystemAPI system) {
      //  HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
      //  NebulaEditor editor = new NebulaEditor(plugin);
      //  float minRadius = plugin.getTileSize() * 2f;

      //  float radius = system.getMaxRadiusInHyperspace();
      //  editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
     //   editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    //}
}
