package data.scripts.world.blackrock.rama;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin.MagneticFieldParams;
import com.fs.starfarer.api.util.Misc;
import data.campaign.BRDY_Conditions;
import data.campaign.ids.BRDY_Industries;
import data.scripts.world.blackrock.addMarketplace;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class Rama {

    public void generate(SectorAPI sector) {
        StarSystemAPI system = sector.createStarSystem("Rama");
        system.getLocation().set(11500, -16000);
        system.setBackgroundTextureFilename("graphics/BR/backgrounds/9-1.jpg");

        PlanetAPI ramastar = system.initStar("Rama", "star_red_giant", 590f, 690f);
        system.setLightColor(new Color(255, 220, 200)); // light color in entire system, affects all entities

        SectorEntityToken rama_nebula = Misc.addNebulaFromPNG("data/campaign/terrain/rama_nebula.png",
                                                              0, 0, // center of nebula
                                                              system, // location to add to
                                                              "terrain", "nebula_amber", // "nebula_blue", // texture to use, uses xxx_map for map
                                                              4, 4, StarAge.OLD); // number of cells in texture

        PlanetAPI silencestar = system.addPlanet("silencestar", ramastar, "Sita", "star_white", 35, 90, 20500, 1150);
        system.addCorona(silencestar, 150, 3f, 0.05f, 1f);
        silencestar.setCustomDescriptionId("star_white_dwarf");
        system.addAsteroidBelt(silencestar, 50, 2000, 256, 200, 200);

        system.addAsteroidBelt(ramastar, 50, 1200, 255, 65, 45); // 0.32 AU

        PlanetAPI bharata = system.addPlanet("bharata", ramastar, "Bharata", "arid", 300, 75, 7600, 130);
        bharata.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "asharu"));
        bharata.getSpec().setGlowColor(new Color(255, 245, 235, 255));
        bharata.getSpec().setUseReverseLightForGlow(true);
        bharata.applySpecChanges();
        bharata.setCustomDescriptionId("blackrock_bharata");
        bharata.setInteractionImage("illustrations", "cargo_loading");

        PlanetAPI silenceplanet = system.addPlanet("silenceplanet", silencestar, "Sita I", "barren-bombarded", 10, 35,
                                                   650, -40);
                Misc.initConditionMarket(silenceplanet);
        silenceplanet.getMarket().addCondition(Conditions.NO_ATMOSPHERE);

        PlanetAPI staloplanet =
                  system.addPlanet("staloplanet", silencestar, "Staalo", "staalo_type", 30, 70, 2400, -115);
        staloplanet.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "sindria"));
        staloplanet.getSpec().setGlowColor(new Color(255, 245, 235, 255));
        staloplanet.getSpec().setUseReverseLightForGlow(true);
        staloplanet.applySpecChanges();
        staloplanet.setCustomDescriptionId("blackrock_staalo");
        staloplanet.setInteractionImage("illustrations", "urban03");

        PlanetAPI senroamin = system.addPlanet("senroamin", ramastar, "Senroamin", "sen_gas_giant", 45, 320, 12150, 280);
        senroamin.setCustomDescriptionId("blackrock_sen");
        senroamin.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "banded"));
        senroamin.getSpec().setGlowColor(new Color(177, 144, 162, 85));
        senroamin.getSpec().setUseReverseLightForGlow(true);
        senroamin.applySpecChanges();

        SectorEntityToken sen_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                                                        new MagneticFieldParams(200f, // terrain effect band width
                                                                                400, // terrain effect middle radius
                                                                                senroamin, // entity that it's around
                                                                                300f, // visual band start
                                                                                500f, // visual band end
                                                                                new Color(50, 30, 100, 30), // base color
                                                                                1f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                                                                new Color(50, 20, 110, 130),
                                                                                new Color(150, 30, 120, 150),
                                                                                new Color(200, 50, 130, 190),
                                                                                new Color(250, 70, 150, 240),
                                                                                new Color(200, 80, 130, 255),
                                                                                new Color(75, 0, 160),
                                                                                new Color(127, 0, 255)
                                                        ));
        sen_field.setCircularOrbit(senroamin, 0, 0, 100);

        system.addRingBand(senroamin, "misc", "rings_dust0", 256f, 2, new Color(143, 118, 107, 190), 256f, 570, 16.95f);

        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1200, 30f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 1400, 40f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1600, 50f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 1800, 56f);
        system.addRingBand(ramastar, "misc", "br_dusty_ring", 1024f, 0, new Color(148, 128, 97, 245), 2048f, 3000, 59);

        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1000, 80f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, Color.white, 256f, 1100, 120f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 1200, 160f);

        SectorEntityToken ring = system.addTerrain(Terrain.RING, new RingParams(800 + 256, 1400, null,
                                                                                "Great Rings of Sarayu"));
        ring.setCircularOrbit(ramastar, 0, 0, 100);

        SectorEntityToken relay = system.addCustomEntity("rama_relay", "Rama Relay", "comm_relay",
                                                         "blackrock_driveyards");
        relay.setCircularOrbit(ramastar, 240, 3650, 240);

        system.addAsteroidBelt(ramastar, 70, 5600, 128, 440, 120);

        system.addAsteroidBelt(ramastar, 80, 5900, 128, 410, 150);

        system.addAsteroidBelt(ramastar, 80, 15900, 128, 400, 250);
        system.addAsteroidBelt(ramastar, 65, 15100, 128, 420, 290);
        system.addAsteroidBelt(ramastar, 80, 15500, 128, 415, 340);
        system.addAsteroidBelt(ramastar, 70, 15000, 128, 415, 250);
        system.addAsteroidBelt(ramastar, 75, 15500, 128, 425, 300);
        
                SectorEntityToken ramaL4 = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		ramaL4.setCircularOrbitPointingDown(ramastar, 0, 9500, 450f);

                SectorEntityToken ramaL5 = system.addCustomEntity(null,null, "stable_location",Factions.NEUTRAL); 
		ramaL5.setCircularOrbitPointingDown(ramastar, 180, 9500, 450f);

        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 15200, 500f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, new Color(164, 195, 225, 200), 512f, 15250, 530f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.white, 256f, 15300, 540f);
        system.addRingBand(ramastar, "misc", "br_dusty_ring", 1024f, 0, new Color(164, 195, 225, 220), 2048f, 16000, 550);

        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 2, Color.lightGray, 256f, 17200, 625f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, Color.darkGray, 512f, 17070, 615f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, Color.white, 512f, 17050, 600f);
        system.addRingBand(ramastar, "misc", "rings_dust0", 256f, 3, Color.lightGray, 256f, 17100, 620f);

        ring = system.addTerrain(Terrain.RING, new RingParams(1850 + 256, 16200, null, "The Shattered Bands"));
        ring.setCircularOrbit(ramastar, 0, 0, 100);

        PlanetAPI icegiant = system.addPlanet("icegiant", ramastar, "Niji", "ice_giant", 30, 160, 16036, 375);
        icegiant.getSpec().setPlanetColor(new Color(50, 100, 255, 255));
        icegiant.getSpec().setAtmosphereColor(new Color(120, 130, 130, 100));
        icegiant.getSpec().setCloudColor(new Color(195, 230, 255, 200));
        icegiant.getSpec().setIconColor(new Color(110, 130, 140, 255));
        icegiant.getSpec().setGlowTexture(Global.getSettings().getSpriteName("hab_glows", "aurorae"));
        icegiant.getSpec().setGlowColor(new Color(135, 208, 235, 105));
        icegiant.getSpec().setUseReverseLightForGlow(true);
        icegiant.getSpec().setAtmosphereThickness(0.5f);
        icegiant.applySpecChanges();

        PlanetAPI vena = system.addPlanet("vena", ramastar, "Vena", "rad_planet", 30, 80, 3836, 215);
        vena.getSpec().setAtmosphereColor(new Color(184, 226, 145, 102));
        vena.getSpec().setCloudColor(new Color(195, 230, 255, 200));
        vena.setCustomDescriptionId("blackrock_vena");
        vena.applySpecChanges();
        
                            // Add fixed conditions to Vena
                    Misc.initConditionMarket(vena);
                    vena.getMarket().addCondition(BRDY_Conditions.VENA_CON);
                    vena.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
                    vena.getMarket().addCondition(Conditions.METEOR_IMPACTS);

        SectorEntityToken vena_field = system.addTerrain(Terrain.MAGNETIC_FIELD,
                                                         new MagneticFieldParams(140f, // terrain effect band width
                                                                                 140f, // terrain effect middle radius
                                                                                 vena, // entity that it's around
                                                                                 60f, // visual band start
                                                                                 140f, // visual band end
                                                                                 new Color(19, 100, 94), // base color
                                                                                 0.90f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                                                                                 new Color(69, 220, 164),
                                                                                 new Color(129, 160, 174),
                                                                                 new Color(109, 180, 154),
                                                                                 new Color(49, 200, 134),
                                                                                 new Color(220, 230, 114),
                                                                                 new Color(129, 230, 154),
                                                                                 new Color(89, 240, 164)));
        vena_field.setCircularOrbit(vena, 0, 0, 100);

        PlanetAPI limboplanet = system.addPlanet("limboplanet", icegiant, "Limbo", "rocky_ice", 30, 35, 520, 25);
        limboplanet.getSpec().setIconColor(new Color(255, 0, 0, 255));
        limboplanet.applySpecChanges();
        limboplanet.setCustomDescriptionId("blackrock_limbo");

        PlanetAPI thalm = system.addPlanet("thalm", senroamin, "Thalm", "rocky_unstable", 100, 30, 1100, 26);
        Misc.initConditionMarket(thalm);
        thalm.getMarket().addCondition(Conditions.NO_ATMOSPHERE);
        thalm.getMarket().addCondition(Conditions.TECTONIC_ACTIVITY);
        

        SectorEntityToken thalm_post = system.addCustomEntity("thalm_post", "Thalm Listening Post", "station_side04",
                                                              "tritachyon");
        thalm_post.setCircularOrbitPointingDown(system.getEntityById("thalm"), 90, 200, 7);
        thalm_post.setCustomDescriptionId("blackrock_listeningoutpost");

        JumpPointAPI jumpPoint = Global.getFactory().createJumpPoint("rama_jp1", "Jump Point Bharata");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(bharata, 90, 900, 55);
        jumpPoint.setOrbit(orbit);
        jumpPoint.setRelatedPlanet(bharata);
        jumpPoint.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint);

        JumpPointAPI jumpPoint2 = Global.getFactory().createJumpPoint("rama_jp2", "Jump Point Staalo");
        OrbitAPI orbit2 = Global.getFactory().createCircularOrbit(staloplanet, 45, 1200, -53);
        jumpPoint2.setRelatedPlanet(staloplanet);
        jumpPoint2.setOrbit(orbit2);
        jumpPoint2.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint2);

        JumpPointAPI jumpPoint3 = Global.getFactory().createJumpPoint("rama_jp3", "Rama Gate");
        OrbitAPI orbit3 = Global.getFactory().createCircularOrbit(ramastar, 220, 12000, 500);
        jumpPoint3.setOrbit(orbit3);
        jumpPoint3.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint3);

        addMarketplace.addMarketplace("blackrock_driveyards", bharata,
                                      null,
                                      "Bharata",
                                      5,
                                      new ArrayList<>(Arrays.asList(Conditions.ARID, Conditions.HABITABLE,
                                                                    Conditions.HOT, Conditions.FARMLAND_POOR, Conditions.ORGANICS_COMMON, Conditions.ORE_SPARSE,
                                                                    Conditions.POPULATION_5)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.POPULATION,
                                                            Industries.MEGAPORT,
                                                            Industries.WAYSTATION,
                                                            BRDY_Industries.BRDYDEFHQ,
                                                            Industries.MILITARYBASE,
                                                            Industries.HEAVYINDUSTRY,
                                                            Industries.HEAVYBATTERIES,
                                                            Industries.PATROLHQ,
                                                            Industries.BATTLESTATION_MID,
                                                            Industries.MINING,
                                                            Industries.REFINING,
                                                            Industries.FARMING)),                                    
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.GENERIC_MILITARY,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        addMarketplace.addMarketplace(Factions.HEGEMONY, staloplanet,
                                      null,
                                      "Staalo",
                                      5,
                                      new ArrayList<>(
                                              Arrays.asList(Conditions.NO_ATMOSPHERE,
                                                            Conditions.DISSIDENT,
                                                            Conditions.RARE_ORE_SPARSE,
                                                            Conditions.ORE_MODERATE,
                                                            Conditions.POPULATION_5)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.POPULATION,                                              
                                                            Industries.MILITARYBASE,
                                                            Industries.HEAVYBATTERIES,
                                                            Industries.SPACEPORT,
                                                            Industries.PATROLHQ,
                                                            Industries.BATTLESTATION,
                                                            Industries.MINING,
                                                            Industries.REFINING)),                                      
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.GENERIC_MILITARY,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        addMarketplace.addMarketplace(Factions.TRITACHYON, thalm_post,
                                      null,
                                      "Thalm Listening Post",
                                      3,
                                      new ArrayList<>(Arrays.asList(Conditions.FREE_PORT, Conditions.OUTPOST,
                                                            Conditions.POPULATION_3)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.POPULATION,                                              
                                                            Industries.MILITARYBASE,
                                                            Industries.HEAVYBATTERIES,
                                                            Industries.SPACEPORT, Industries.WAYSTATION,
                                                            Industries.PATROLHQ,
                                                            Industries.BATTLESTATION_HIGH,
                                                            Industries.LIGHTINDUSTRY)),                                        
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.GENERIC_MILITARY,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        addMarketplace.addMarketplace(Factions.PIRATES, limboplanet,
                                      null,
                                      "Limbo",
                                      3,
                                      new ArrayList<>(Arrays.asList(Conditions.ICE,
                                              Conditions.COLD, Conditions.EXTREME_WEATHER, Conditions.VOLATILES_PLENTIFUL,
                                                                    Conditions.FRONTIER, Conditions.FREE_PORT,
                                                                    Conditions.OUTPOST, Conditions.POPULATION_3)),
                                      new ArrayList<>(Arrays.asList(
                                                            Industries.POPULATION,                                              
                                                            Industries.MILITARYBASE,
                                                            Industries.GROUNDDEFENSES,
                                                            Industries.SPACEPORT,
                                                            Industries.MINING,
                                                            Industries.REFINING)),                                      
                                      new ArrayList<>(Arrays.asList(Submarkets.SUBMARKET_STORAGE,
                                                                    Submarkets.SUBMARKET_BLACK,
                                                                    Submarkets.SUBMARKET_OPEN)),
                                      0.3f
        );

        system.autogenerateHyperspaceJumpPoints(true, true);

        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, ramastar, StarAge.OLD,
                                                                    2, 4, // min/max entities to add
                                                                    27800, // radius to start adding at
                                                                    3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
       
                                                                     true); // whether to use custom or system-name based names
                HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
                NebulaEditor editor = new NebulaEditor(plugin);
                float minRadius = plugin.getTileSize() * 2f;

                float radius = system.getMaxRadiusInHyperspace();
                editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f);
                editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
    }

//    void cleanup(StarSystemAPI system) {
//        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
//        NebulaEditor editor = new NebulaEditor(plugin);
//        float minRadius = plugin.getTileSize() * 2f;
//
//        float radius = system.getMaxRadiusInHyperspace();
//        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius * 0.5f, 0, 360f);
//        editor.clearArc(system.getLocation().x, system.getLocation().y, 0, radius + minRadius, 0, 360f, 0.25f);
//    }
}
