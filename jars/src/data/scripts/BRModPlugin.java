package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import data.scripts.ai.SeekerAI;
import data.scripts.ai.brdy_EnergyTorpedoAI;
import data.scripts.ai.brdy_HomingLaserAI;
import data.scripts.ai.brdy_WarlockSubAI;
import data.scripts.ai.brdy_VoidSpearAI;
import data.scripts.ai.brdy_WhisperAI;
import data.scripts.util.BRDYSettings;
import data.scripts.world.blackrock.BRGen;
import org.dark.shaders.light.LightData;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;

public class BRModPlugin extends BaseModPlugin {

    public static final String BRDY_AFMISSILE = "brdy_afmissile";
    public static final String BRDY_AFMISSILEBIG = "brdy_afmissilebig";
    public static final String BRDY_AFMISSILEFTR = "brdy_dart_fighter";
    public static final String BRDY_ENERGYTORPEDO = "scalaron_torp";
    public static final String BRDY_ENERGYTORPEDO2 = "scalaron_torp_big";
    public static final String BRDY_ENERGYTORPEDO_SPECIAL = "brdy_repulsorpulse_torp";
    public static final String BRDY_HOMINGLASER = "homing_laser_proj";
    public static final String BRDY_VOIDSPEAR = "voidspear";
    public static final String BRDY_VOIDSPEARFTR = "voidspear_ftr";
    public static final String BRDY_MAGE = "br_tacmissileproj"; 
    public static final String BRDY_MAGESUB = "br_tacmissilestage2";   

    public static boolean isExerelin = false;
    public static boolean templarsExists = false;
    public static boolean particleEngineExists = false;
    public static boolean lunaLibExists = false;
    private static final String MEMKEY_INITIALIZED = "$brdy_initialized";

    private static boolean isNexCorvusMode() {
        if (Global.getSector() != null) {
            Object result = Global.getSector().getMemoryWithoutUpdate().get("$nex_corvusMode");
            if (result instanceof Boolean) {
                return ((Boolean) result).booleanValue();
            }
        }

        Global.getLogger(BRModPlugin.class).warn("Nexerelin Corvus mode was not available in sector memory; skipping Blackrock static sector generation for safety.");
        return false;
    }

    private static boolean hasBlackrockCoreWorlds() {
        return Global.getSector() != null
                && (Global.getSector().getEntityById("blackrock") != null
                || Global.getSector().getEntityById("bharata") != null
                || Global.getSector().getEntityById("brstation2") != null);
    }

    private static void initBR() {
        if (isExerelin && (!BRDYSettings.nexCorvusGenerationEnabled() || !isNexCorvusMode())) {
            return;
        }
        if (hasBlackrockCoreWorlds()) {
            Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_INITIALIZED, true);
            return;
        }

        ProcgenUsedNames.notifyUsed("Gneiss");
        ProcgenUsedNames.notifyUsed("Blackrock");
        ProcgenUsedNames.notifyUsed("Lodestone");
        ProcgenUsedNames.notifyUsed("Augustmoon");
        ProcgenUsedNames.notifyUsed("Creir");
        ProcgenUsedNames.notifyUsed("Lydia");
        ProcgenUsedNames.notifyUsed("Verge");
        ProcgenUsedNames.notifyUsed("Vigil");
        ProcgenUsedNames.notifyUsed("Preclusion");

        ProcgenUsedNames.notifyUsed("Rama");
        ProcgenUsedNames.notifyUsed("Sita");
        ProcgenUsedNames.notifyUsed("Bharata");
        ProcgenUsedNames.notifyUsed("Staalo");
        ProcgenUsedNames.notifyUsed("Senroamin");
        ProcgenUsedNames.notifyUsed("Sarayu");
        ProcgenUsedNames.notifyUsed("Niji");
        ProcgenUsedNames.notifyUsed("Vena");
        ProcgenUsedNames.notifyUsed("Limbo");
        ProcgenUsedNames.notifyUsed("Thalm");

        new BRGen().generate(Global.getSector());
        Global.getSector().getMemoryWithoutUpdate().set(MEMKEY_INITIALIZED, true);
    }

    @Override
    public void onApplicationLoad() {
        isExerelin = Global.getSettings().getModManager().isModEnabled("nexerelin");
        templarsExists = Global.getSettings().getModManager().isModEnabled("Templars");
        particleEngineExists = Global.getSettings().getModManager().isModEnabled("particleengine");
        lunaLibExists = Global.getSettings().getModManager().isModEnabled("lunalib");

        boolean hasLazyLib = Global.getSettings().getModManager().isModEnabled("lw_lazylib");
        if (!hasLazyLib) {
            throw new RuntimeException("Blackrock Drive Yards requires LazyLib!" +
                    "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444");
        }

        boolean hasGraphicsLib = Global.getSettings().getModManager().isModEnabled("shaderLib");
        if (!hasGraphicsLib) {
            throw new RuntimeException("Blackrock Drive Yards requires GraphicsLib!" +
                    "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=10982");
        }

        boolean hasMagicLib = Global.getSettings().getModManager().isModEnabled("MagicLib");
        if (!hasMagicLib) {
            throw new RuntimeException("Blackrock Drive Yards requires MagicLib!" +
                    "\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718.0");
        }

        ShaderLib.init();
        LightData.readLightDataCSV("data/lights/brdy_light_data.csv");
        TextureData.readTextureDataCSV("data/lights/brdy_texture_data.csv");
        BRDYSettings.reload();
    }
    @Override
    public void onNewGame() {
        initBR();
    }

    @Override
    public void onGameLoad(boolean newGame) {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(MEMKEY_INITIALIZED)) {
            initBR();
        }
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        if (missile == null || missile.getProjectileSpecId() == null) {
            return null;
        }

        switch (missile.getProjectileSpecId()) {
            case BRDY_AFMISSILE:
            case BRDY_AFMISSILEBIG:
            case BRDY_AFMISSILEFTR:
                return new PluginPick<MissileAIPlugin>(new SeekerAI(missile, launchingShip),
                                                       CampaignPlugin.PickPriority.MOD_SET);
            case BRDY_ENERGYTORPEDO:
            case BRDY_ENERGYTORPEDO2:
            case BRDY_ENERGYTORPEDO_SPECIAL:
                return new PluginPick<MissileAIPlugin>(new brdy_EnergyTorpedoAI(missile, launchingShip),
                                                       CampaignPlugin.PickPriority.MOD_SET);
            case BRDY_HOMINGLASER:  
                return new PluginPick<MissileAIPlugin>(new brdy_HomingLaserAI(missile, launchingShip),
                                                       CampaignPlugin.PickPriority.MOD_SET);
            case BRDY_VOIDSPEAR:
            case BRDY_VOIDSPEARFTR:
                return new PluginPick<MissileAIPlugin>(new brdy_VoidSpearAI(missile, launchingShip),
                                                       CampaignPlugin.PickPriority.MOD_SPECIFIC);
            case BRDY_MAGE:
                return new PluginPick<MissileAIPlugin>(new brdy_WhisperAI(missile, launchingShip),
                                                       CampaignPlugin.PickPriority.MOD_SPECIFIC);                
            case BRDY_MAGESUB: 
                return new PluginPick<MissileAIPlugin>(new brdy_WarlockSubAI(missile, launchingShip),
                                                       CampaignPlugin.PickPriority.MOD_SPECIFIC); 
            default:
                return null;
        }
    }
    }

