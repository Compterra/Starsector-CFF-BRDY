package data.scripts.plugins;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.util.BRDYSettings;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;

public class BRDYLightInjector extends BaseEveryFrameCombatPlugin {

    private static final float ELIGIBLE_SCAN_INTERVAL = 0.25f;

    private static final Color ARCJET_COLOR = new Color(131, 228, 119);
    private static final Color BURSTJETS_COLOR = new Color(140, 250, 200);
    private static final Color DESDINOVAJETS_COLOR = new Color(140, 242, 195);
    private static final Color MORPHEUSJETS_COLOR = new Color(49, 255, 210);
    private static final Vector2f ZERO = new Vector2f();

    private static final Map<CombatEngineAPI, LocalData> dataMap = new WeakHashMap<>();

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = dataMap.get(engine);
        if (localData == null) {
            return;
        }
        final Map<ShipAPI, StandardLight> lights = localData.lights;
        if (!BRDYSettings.graphicsLibLightsEnabled()) {
            clearLights(lights);
            return;
        }

        localData.scanTimer -= amount;
        if (localData.scanTimer <= 0f) {
            localData.scanTimer = ELIGIBLE_SCAN_INTERVAL;
            rebuildEligibleShips(engine, localData.eligibleShips);
        }

        List<ShipAPI> ships = localData.eligibleShips;
        int shipsSize = ships.size();
        for (int i = 0; i < shipsSize; i++) {
            ShipAPI ship = ships.get(i);
            if (ship == null || ship.isHulk() || !ship.isAlive()) {
                continue;
            }

            ShipSystemAPI system = ship.getSystem();
            if (system != null) {
                String id = system.getId();
                switch (id) {
                    case "arcjetburner":
                        if (system.isActive()) {
                            Vector2f location = getActiveEngineCenter(ship);
                            if (location == null) {
                                break;
                            }

                            float intensity = (float) Math.sqrt(ship.getCollisionRadius()) / 10f;
                            updateLight(lights, ship, location, ARCJET_COLOR, intensity, intensity * 100f, 1.43f,
                                        system.isActive() && !system.isOn(), 1.25f);
                        }
                        break;
                    case "burstjets":
                        if (system.isActive()) {
                            Vector2f location = getActiveEngineCenter(ship);
                            if (location == null) {
                                break;
                            }

                            float intensity = (float) Math.sqrt(ship.getCollisionRadius()) / 40f;
                            updateLight(lights, ship, location, BURSTJETS_COLOR, intensity, intensity * 600f, 0.05f,
                                        (system.isActive() && !system.isOn()) || system.isChargedown(), 1.5f);
                        }
                        break;
                    case "desdinovajets":
                        if (system.isActive()) {
                            Vector2f location = getActiveEngineCenter(ship);
                            if (location == null) {
                                break;
                            }

                            float intensity = (float) Math.sqrt(ship.getCollisionRadius()) / 40f;
                            updateLight(lights, ship, location, DESDINOVAJETS_COLOR, intensity, intensity * 600f, 0.05f,
                                        (system.isActive() && !system.isOn()) || system.isChargedown(), 1.1f);
                        }
                        break;
                    case "morpheusjets":
                        if (system.isActive()) {
                            Vector2f location = getActiveEngineCenter(ship);
                            if (location == null) {
                                break;
                            }

                            float intensity = (float) Math.sqrt(ship.getCollisionRadius()) / 40f;
                            updateLight(lights, ship, location, MORPHEUSJETS_COLOR, intensity, intensity * 600f, 0.05f,
                                        (system.isActive() && !system.isOn()) || system.isChargedown(), 1.1f);
                        }
                        break;
                    case "gravityfield":
                        if (system.isActive()) {
                            updateLight(lights, ship, ship.getLocation(), 0.45f, 0.8f, 0.65f, 0.35f, 1500f, 0.5f,
                                        system.isActive() && !system.isOn(), 1f);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        Iterator<Map.Entry<ShipAPI, StandardLight>> iter = lights.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, StandardLight> entry = iter.next();
            ShipAPI ship = entry.getKey();

            ShipSystemAPI system = ship.getSystem();
            if (system == null || !system.isActive() || !ship.isAlive() || !isTrackedSystem(system.getId())) {
                StandardLight light = entry.getValue();

                light.unattach();
                light.fadeOut(0);
                iter.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        BRDYSettings.reload();
        dataMap.put(engine, new LocalData());
    }

    private static void clearLights(Map<ShipAPI, StandardLight> lights) {
        Iterator<Map.Entry<ShipAPI, StandardLight>> iter = lights.entrySet().iterator();
        while (iter.hasNext()) {
            StandardLight light = iter.next().getValue();
            light.unattach();
            light.fadeOut(0);
            iter.remove();
        }
    }

    private static void rebuildEligibleShips(CombatEngineAPI engine, List<ShipAPI> eligibleShips) {
        eligibleShips.clear();

        List<ShipAPI> ships = engine.getShips();
        int shipsSize = ships.size();
        for (int i = 0; i < shipsSize; i++) {
            ShipAPI ship = ships.get(i);
            if (ship == null || ship.isHulk() || !ship.isAlive()) {
                continue;
            }

            ShipSystemAPI system = ship.getSystem();
            if (system != null && isTrackedSystem(system.getId())) {
                eligibleShips.add(ship);
            }
        }
    }

    private static boolean isTrackedSystem(String id) {
        if (id == null) {
            return false;
        }

        switch (id) {
            case "arcjetburner":
            case "burstjets":
            case "desdinovajets":
            case "morpheusjets":
            case "gravityfield":
                return true;
            default:
                return false;
        }
    }

    private static Vector2f getActiveEngineCenter(ShipAPI ship) {
        if (ship.getEngineController() == null) {
            return null;
        }
        List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
        Vector2f location = null;
        int count = 0;
        int enginesSize = engines.size();
        for (int i = 0; i < enginesSize; i++) {
            ShipEngineAPI engine = engines.get(i);
            if (engine.isActive() && !engine.isDisabled()) {
                count++;
                if (location == null) {
                    location = new Vector2f(engine.getLocation());
                } else {
                    Vector2f.add(location, engine.getLocation(), location);
                }
            }
        }
        if (location != null) {
            location.scale(1f / count);
        }
        return location;
    }

    private static void updateLight(Map<ShipAPI, StandardLight> lights, ShipAPI ship, Vector2f location, Color color,
                                    float intensity, float size, float fadeIn, boolean shouldFadeOut,
                                    float fadeOut) {
        StandardLight light = lights.get(ship);
        if (light != null) {
            light.setLocation(location);
            fadeLightOut(light, shouldFadeOut, fadeOut);
            return;
        }

        light = new StandardLight(location, ZERO, ZERO, null);
        light.setIntensity(intensity);
        light.setSize(size);
        light.setColor(color);
        light.fadeIn(fadeIn);
        lights.put(ship, light);
        LightShader.addLight(light);
    }

    private static void updateLight(Map<ShipAPI, StandardLight> lights, ShipAPI ship, Vector2f location, float red,
                                    float green, float blue, float intensity, float size, float fadeIn,
                                    boolean shouldFadeOut, float fadeOut) {
        StandardLight light = lights.get(ship);
        if (light != null) {
            light.setLocation(location);
            fadeLightOut(light, shouldFadeOut, fadeOut);
            return;
        }

        light = new StandardLight(location, ZERO, ZERO, null);
        light.setIntensity(intensity);
        light.setSize(size);
        light.setColor(red, green, blue);
        light.fadeIn(fadeIn);
        lights.put(ship, light);
        LightShader.addLight(light);
    }

    private static void fadeLightOut(StandardLight light, boolean shouldFadeOut, float fadeOut) {
        if (shouldFadeOut && !light.isFadingOut()) {
            light.fadeOut(fadeOut);
        }
    }
    private static final class LocalData {

        final Map<ShipAPI, StandardLight> lights = new LinkedHashMap<>(100);
        final List<ShipAPI> eligibleShips = new ArrayList<>(20);
        float scanTimer = 0f;
    }
}
