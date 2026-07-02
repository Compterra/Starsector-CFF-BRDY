package data.scripts.util;

import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;

public final class BRDYSettings {

    private static final String MOD_ID = "blackrock_driveyards";

    private static boolean particleEngineExists = false;
    private static boolean lunaLibExists = false;
    private static boolean graphicsLibExists = false;
    private static boolean shipMasteryExists = false;
    private static boolean useParticleEngine = true;
    private static boolean graphicsLibLights = true;
    private static boolean graphicsLibDistortions = true;
    private static boolean shipMasteryPackages = true;
    private static boolean nexCorvusGeneration = true;
    private static float effectScale = 1f;

    private BRDYSettings() {
    }

    public static void reload() {
        particleEngineExists = isModEnabled("particleengine");
        lunaLibExists = isModEnabled("lunalib");
        graphicsLibExists = isModEnabled("shaderLib");
        shipMasteryExists = isModEnabled("shipmasterysystem");

        useParticleEngine = true;
        graphicsLibLights = true;
        graphicsLibDistortions = true;
        shipMasteryPackages = true;
        nexCorvusGeneration = true;
        effectScale = 1f;

        if (lunaLibExists) {
            useParticleEngine = getBoolean("brdy_enableParticleEngine", useParticleEngine);
            graphicsLibLights = getBoolean("brdy_enableGraphicsLibLights", graphicsLibLights);
            graphicsLibDistortions = getBoolean("brdy_enableGraphicsLibDistortions", graphicsLibDistortions);
            shipMasteryPackages = getBoolean("brdy_enableShipMasteryPackages", shipMasteryPackages);
            nexCorvusGeneration = getBoolean("brdy_enableNexCorvusGeneration", nexCorvusGeneration);
            effectScale = clamp(getFloat("brdy_effectScale", effectScale), 0.25f, 1.5f);
        }

        BRDYFx.resetParticleEngineCache();
    }

    public static boolean isParticleEngineEnabled() {
        return particleEngineExists && useParticleEngine;
    }

    public static boolean isLunaLibEnabled() {
        return lunaLibExists;
    }

    public static boolean graphicsLibLightsEnabled() {
        return graphicsLibExists && graphicsLibLights;
    }

    public static boolean graphicsLibDistortionsEnabled() {
        return graphicsLibExists && graphicsLibDistortions;
    }

    public static boolean shipMasteryPackagesEnabled() {
        return !shipMasteryExists || shipMasteryPackages;
    }

    public static boolean nexCorvusGenerationEnabled() {
        return nexCorvusGeneration;
    }

    public static float getEffectScale() {
        return effectScale;
    }

    public static int scaleParticleCount(int count) {
        if (count <= 0) {
            return 0;
        }
        return Math.max(1, Math.round(count * effectScale));
    }

    private static boolean isModEnabled(String id) {
        try {
            return Global.getSettings().getModManager().isModEnabled(id);
        } catch (Throwable ex) {
            return false;
        }
    }

    private static boolean getBoolean(String fieldId, boolean fallback) {
        try {
            Boolean value = LunaSettings.getBoolean(MOD_ID, fieldId);
            if (value != null) {
                return value.booleanValue();
            }
        } catch (Throwable ex) {
            Global.getLogger(BRDYSettings.class).warn("Could not read LunaLib boolean setting " + fieldId, ex);
        }
        return fallback;
    }

    private static float getFloat(String fieldId, float fallback) {
        try {
            Float value = LunaSettings.getFloat(MOD_ID, fieldId);
            if (value != null) {
                return value.floatValue();
            }
        } catch (Throwable ex) {
            Global.getLogger(BRDYSettings.class).warn("Could not read LunaLib float setting " + fieldId, ex);
        }
        return fallback;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}