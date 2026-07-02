package data.scripts.util;

import com.fs.starfarer.api.Global;

import java.lang.reflect.Method;

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

    private static Class<?> lunaSettingsClass = null;
    private static Method lunaGetBoolean = null;
    private static Method lunaGetFloat = null;

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

        if (!lunaLibExists || !loadLunaLib()) {
            BRDYFx.resetParticleEngineCache();
            return;
        }

        useParticleEngine = getBoolean("brdy_enableParticleEngine", useParticleEngine);
        graphicsLibLights = getBoolean("brdy_enableGraphicsLibLights", graphicsLibLights);
        graphicsLibDistortions = getBoolean("brdy_enableGraphicsLibDistortions", graphicsLibDistortions);
        shipMasteryPackages = getBoolean("brdy_enableShipMasteryPackages", shipMasteryPackages);
        nexCorvusGeneration = getBoolean("brdy_enableNexCorvusGeneration", nexCorvusGeneration);
        effectScale = clamp(getFloat("brdy_effectScale", effectScale), 0.25f, 1.5f);
        BRDYFx.resetParticleEngineCache();
    }

    public static boolean isParticleEngineEnabled() {
        return particleEngineExists && useParticleEngine;
    }

    public static boolean isLunaLibEnabled() {
        return lunaLibExists && lunaSettingsClass != null;
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

    private static boolean loadLunaLib() {
        if (lunaSettingsClass != null && lunaGetBoolean != null && lunaGetFloat != null) {
            return true;
        }

        try {
            lunaSettingsClass = Class.forName("lunalib.lunaSettings.LunaSettings");
            lunaGetBoolean = lunaSettingsClass.getMethod("getBoolean", String.class, String.class);
            lunaGetFloat = lunaSettingsClass.getMethod("getFloat", String.class, String.class);
            return true;
        } catch (Throwable ex) {
            lunaSettingsClass = null;
            lunaGetBoolean = null;
            lunaGetFloat = null;
            Global.getLogger(BRDYSettings.class).warn("LunaLib is enabled but its settings API was not available; using Blackrock defaults.", ex);
            return false;
        }
    }

    private static boolean getBoolean(String fieldId, boolean fallback) {
        try {
            Object value = lunaGetBoolean.invoke(null, MOD_ID, fieldId);
            if (value instanceof Boolean) {
                return ((Boolean) value).booleanValue();
            }
        } catch (Throwable ex) {
            Global.getLogger(BRDYSettings.class).warn("Could not read LunaLib boolean setting " + fieldId, ex);
        }
        return fallback;
    }

    private static float getFloat(String fieldId, float fallback) {
        try {
            Object value = lunaGetFloat.invoke(null, MOD_ID, fieldId);
            if (value instanceof Float) {
                return ((Float) value).floatValue();
            }
            if (value instanceof Number) {
                return ((Number) value).floatValue();
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