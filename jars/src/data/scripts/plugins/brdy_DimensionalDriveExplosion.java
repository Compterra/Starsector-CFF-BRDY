package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.util.AnamorphicFlare;
import data.scripts.util.BRDYFx;
import data.scripts.util.BRDYSettings;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class brdy_DimensionalDriveExplosion extends BaseEveryFrameCombatPlugin {

    private static final Set<String> APPLICABLE_SHIPS = new HashSet<>(4);

    private static final Color COLOR_ATTACHED_LIGHT = new Color(200, 255, 150);
    private static final Color COLOR_EMP_CORE = new Color(100, 255, 225, 150);
    private static final Color COLOR_EMP_FRINGE = new Color(50, 200, 100, 100);
    private static final Color COLOR_PARTICLE = new Color(150, 255, 50);
    private static final Color COLOR_SUPERBRITE = new Color(200, 255, 255);

    private static final Map<String, Float> CORE_OFFSET = new HashMap<>(4);

    private static final String DATA_KEY = "brdy_DimensionalDriveExplosion";

    private static final Map<HullSize, Float> EXPLOSION_AREA_INCREASE = new HashMap<>(5);
    private static final Map<HullSize, Float> EXPLOSION_INTENSITY = new HashMap<>(5);
    private static final Map<HullSize, Float> EXPLOSION_LENGTH = new HashMap<>(5);
    private static final Map<HullSize, Float> PITCH_BEND = new HashMap<>(5);

    private static final Vector2f ZERO = new Vector2f();

    static {
        APPLICABLE_SHIPS.add("brdyx_imaginos");
        APPLICABLE_SHIPS.add("brdy_dynastos");        
        APPLICABLE_SHIPS.add("brdyx_morpheus");
        APPLICABLE_SHIPS.add("brdy_nevermore");
        APPLICABLE_SHIPS.add("brdy_karkinos");
     
        CORE_OFFSET.put("brdy_dynastos", -1f);
        CORE_OFFSET.put("brdyx_imaginos", -3f);
        CORE_OFFSET.put("brdyx_morpheus", -2.5f);
        CORE_OFFSET.put("brdy_nevermore", 52f);
        CORE_OFFSET.put("brdy_karkinos", -86f);
    }

    static {
        EXPLOSION_LENGTH.put(HullSize.FIGHTER, 1.5f);
        EXPLOSION_INTENSITY.put(HullSize.FIGHTER, 0.5f);
        EXPLOSION_AREA_INCREASE.put(HullSize.FIGHTER, 50f);
        PITCH_BEND.put(HullSize.FIGHTER, 1.2f);

        EXPLOSION_LENGTH.put(HullSize.FRIGATE, 3f);
        EXPLOSION_INTENSITY.put(HullSize.FRIGATE, 1.1f);
        EXPLOSION_AREA_INCREASE.put(HullSize.FRIGATE, 300f);
        PITCH_BEND.put(HullSize.FRIGATE, 1.07f);

        EXPLOSION_LENGTH.put(HullSize.DESTROYER, 4.5f);
        EXPLOSION_INTENSITY.put(HullSize.DESTROYER, 1.225f);
        EXPLOSION_AREA_INCREASE.put(HullSize.DESTROYER, 400f);
        PITCH_BEND.put(HullSize.DESTROYER, 1f);

        EXPLOSION_LENGTH.put(HullSize.CRUISER, 6f);
        EXPLOSION_INTENSITY.put(HullSize.CRUISER, 1.25f);
        EXPLOSION_AREA_INCREASE.put(HullSize.CRUISER, 500f);
        PITCH_BEND.put(HullSize.CRUISER, 0.92f);

        EXPLOSION_LENGTH.put(HullSize.CAPITAL_SHIP, 7.5f);
        EXPLOSION_INTENSITY.put(HullSize.CAPITAL_SHIP, 1.5f);
        EXPLOSION_AREA_INCREASE.put(HullSize.CAPITAL_SHIP, 650f);
        PITCH_BEND.put(HullSize.CAPITAL_SHIP, 0.85f);

        EXPLOSION_LENGTH.put(HullSize.DEFAULT, 7.5f);
        EXPLOSION_INTENSITY.put(HullSize.DEFAULT, 1.5f);
        EXPLOSION_AREA_INCREASE.put(HullSize.DEFAULT, 650f);
        PITCH_BEND.put(HullSize.DEFAULT, 1f);
    }

    private static void explode(CombatEngineAPI engine, ExplodingShip exploder) {
        ShipAPI ship = exploder.ship;
        Vector2f shipLoc = MathUtils.getPointOnCircumference(ship.getLocation(), CORE_OFFSET.get(
                                                             ship.getHullSpec().getHullId()), ship.getFacing());
        ship.setOwner(ship.getOriginalOwner());

        float explosionTime = EXPLOSION_LENGTH.get(ship.getHullSize());
        float area = EXPLOSION_AREA_INCREASE.get(ship.getHullSize()) + ship.getCollisionRadius();
        float damage = 6f * (float) Math.sqrt(ship.getFluxTracker().getMaxFlux()) * EXPLOSION_INTENSITY.get(
              ship.getHullSize());
        float emp = 25f * (float) Math.sqrt(ship.getFluxTracker().getMaxFlux()) * EXPLOSION_INTENSITY.get(
              ship.getHullSize());

        for (int i = 0; i <= (float) Math.sqrt(ship.getCollisionRadius()) * 8f * EXPLOSION_INTENSITY.get(
             ship.getHullSize()); i++) {
            float angle = (float) Math.random() * 360f;
            float distance = (float) Math.random() * area * 0.5f + area * 0.5f;
            Vector2f point1 = MathUtils.getPointOnCircumference(shipLoc, distance * (float) Math.random(), angle);
            Vector2f point2 = MathUtils.getPointOnCircumference(shipLoc, distance * (float) Math.random(), angle + 45f *
                                                                (float) Math.random());
            engine.spawnEmpArc(ship, point1, new SimpleEntity(point1), new SimpleEntity(point2), DamageType.ENERGY, 0f,
                               0f, 1000f, null,
                               EXPLOSION_INTENSITY.get(ship.getHullSize()) * 10f + 10f, COLOR_EMP_FRINGE, COLOR_EMP_CORE);
        }
        for (int i = 0; i <= ship.getCollisionRadius() * EXPLOSION_INTENSITY.get(ship.getHullSize()); i++) {
            if (Math.random() > 0.5) {
                Vector2f point1 = MathUtils.getRandomPointInCircle(shipLoc, (float) Math.random() * area * 0.5f + area *
                                                                   0.5f);
                Vector2f point2 = MathUtils.getRandomPointInCircle(shipLoc, ship.getCollisionRadius() * 0.25f);
                engine.spawnEmpArc(ship, point2, new SimpleEntity(point2), new SimpleEntity(point1), DamageType.ENERGY,
                                   0f, 0f, 1000f, null,
                                   EXPLOSION_INTENSITY.get(ship.getHullSize()) * 10f + 10f, COLOR_EMP_FRINGE,
                                   COLOR_EMP_CORE);
            }
        }

        engine.spawnExplosion(shipLoc, ZERO, COLOR_SUPERBRITE, area, 0.1f * explosionTime);
        engine.spawnExplosion(shipLoc, ZERO, COLOR_ATTACHED_LIGHT, area * 0.4f, explosionTime * 1.25f);
        engine.addHitParticle(shipLoc, ZERO, area * 2.5f, 10f, 0.05f * explosionTime, COLOR_SUPERBRITE);
        engine.addHitParticle(shipLoc, ZERO, area * 0.125f, 10f, explosionTime * 0.75f, COLOR_SUPERBRITE);
        engine.addHitParticle(shipLoc, ZERO, area * 0.25f, 10f, explosionTime, COLOR_SUPERBRITE);
        engine.addHitParticle(shipLoc, ZERO, area * 0.50f, 10f, explosionTime * 1.25f, COLOR_SUPERBRITE);
        engine.addSmoothParticle(shipLoc, ZERO, area * 1.5f, 0.5f, explosionTime * 1.5f, COLOR_EMP_FRINGE);
        AnamorphicFlare.createFlare(ship, new Vector2f(shipLoc), engine, 1f, 0.04f / EXPLOSION_INTENSITY.get(
                                    ship.getHullSize()), 0f, 0f, 2f,
                                    COLOR_PARTICLE, COLOR_ATTACHED_LIGHT);
        int particleCount = Math.max(1, Math.round(ship.getCollisionRadius() *
                                            EXPLOSION_INTENSITY.get(ship.getHullSize())));
        float particleLife = (float) Math.sqrt(EXPLOSION_LENGTH.get(ship.getHullSize()));
        BRDYFx.addExplosionSparkBurst(engine, shipLoc, COLOR_PARTICLE, particleCount,
                                      ship.getCollisionRadius(),
                                      ship.getCollisionRadius() * EXPLOSION_INTENSITY.get(ship.getHullSize()),
                                      10f, 1f, particleLife, particleLife * 2f,
                                      CombatEngineLayers.ABOVE_PARTICLES_LOWER);

        List<ShipAPI> nearbyShips = CombatUtils.getShipsWithinRange(shipLoc, area);
        for (ShipAPI thisShip : nearbyShips) {
            if (thisShip.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }

            Vector2f damagePoint = CollisionUtils.getCollisionPoint(shipLoc, thisShip.getLocation(), thisShip);
            if (damagePoint == null) {
                damagePoint = thisShip.getLocation();
            }
            Vector2f forward = new Vector2f(damagePoint);
            forward.normalise();
            forward.scale(5f);
            Vector2f.add(forward, damagePoint, damagePoint);
            float falloff = 1f - MathUtils.getDistance(ship, thisShip) / area;
            if (ship.getOwner() == thisShip.getOwner() && ship != thisShip) {
                falloff *= 0.5f;
            }
            engine.applyDamage(thisShip, damagePoint, damage * falloff, DamageType.ENERGY, emp * falloff * 0.25f, false,
                               false, ship);

            ShipAPI empTarget = thisShip;
            for (int i = 0; i <= (int) (damage * (falloff / 250f) * EXPLOSION_INTENSITY.get(ship.getHullSize())); i++) {
                Vector2f point = MathUtils.getRandomPointInCircle(thisShip.getLocation(),
                                                                  thisShip.getCollisionRadius() * 1.5f);
                engine.spawnEmpArc(ship, point, empTarget, empTarget, DamageType.ENERGY, damage * falloff * 0.5f, emp *
                                   falloff * 0.5f, 1000f, null,
                                   (float) Math.sqrt(damage), COLOR_EMP_FRINGE, COLOR_EMP_CORE);
            }
        }

        StandardLight light = new StandardLight(shipLoc, ZERO, ZERO, null);
        light.setColor(COLOR_ATTACHED_LIGHT);
        light.setSize(area * 1.5f);
        light.setIntensity(1f * EXPLOSION_INTENSITY.get(ship.getHullSize()));
        light.fadeOut(explosionTime);
        if (BRDYSettings.graphicsLibLightsEnabled()) {
            LightShader.addLight(light);
        }

        float time = EXPLOSION_INTENSITY.get(ship.getHullSize());
        RippleDistortion ripple = new RippleDistortion(shipLoc, ZERO);
        ripple.setSize(area);
        ripple.setIntensity(100f * EXPLOSION_INTENSITY.get(ship.getHullSize()));
        ripple.setFrameRate(60f / (time));
        ripple.fadeInSize(time);
        ripple.fadeOutIntensity(time);
        if (BRDYSettings.graphicsLibDistortionsEnabled()) {
            DistortionShader.addDistortion(ripple);
        }

        Global.getSoundPlayer().playSound("dimensional_engine_explosion", PITCH_BEND.get(ship.getHullSize()),
                                          EXPLOSION_INTENSITY.get(ship.getHullSize()),
                                          shipLoc, ZERO);

        ship.setOwner(100);
        ship.splitShip();
    }

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        if (localData == null) {
            return;
        }
        final Set<ShipAPI> deadShips = localData.deadShips;
        final List<ExplodingShip> explodingShips = localData.explodingShips;

        List<ShipAPI> ships = engine.getShips();
        int shipsSize = ships.size();
        for (int i = 0; i < shipsSize; i++) {
            ShipAPI ship = ships.get(i);
            if (ship == null) {
                continue;
            }

            if (ship.isHulk() && !ship.isPiece()) {
                if (!APPLICABLE_SHIPS.contains(ship.getHullSpec().getHullId())) {
                    continue;
                }

                if (!deadShips.contains(ship)) {
                    deadShips.add(ship);

                    Vector2f shipLoc = MathUtils.getPointOnCircumference(ship.getLocation(), CORE_OFFSET.get(
                                                                         ship.getHullSpec().getHullId()),
                                                                         ship.getFacing());
                    float chargingTime = EXPLOSION_LENGTH.get(ship.getHullSize());
                    float soundLength = 6.9f;
                    Global.getSoundPlayer().playSound("dimensional_engine_malfunction", soundLength / chargingTime,
                                                      EXPLOSION_INTENSITY.get(ship.getHullSize()), shipLoc,
                                                      ship.getVelocity());
                    StandardLight light = new StandardLight(ZERO, ZERO, ZERO, ship);
                    light.setColor(COLOR_ATTACHED_LIGHT);
                    light.setSize(ship.getCollisionRadius() * 2f);
                    light.setIntensity(EXPLOSION_INTENSITY.get(ship.getHullSize()));
                    light.fadeIn(chargingTime);
                    light.setLifetime(0f);
                    if (BRDYSettings.graphicsLibLightsEnabled()) {
            LightShader.addLight(light);
        }
                    ExplodingShip exploder = new ExplodingShip(ship, chargingTime);
                    explodingShips.add(exploder);
                }
            }
        }

        Iterator<ShipAPI> iter = deadShips.iterator();
        while (iter.hasNext()) {
            ShipAPI ship = iter.next();

            if (ship != null && !engine.isEntityInPlay(ship)) {
                iter.remove();
            }
        }

        Iterator<ExplodingShip> iter2 = explodingShips.iterator();
        while (iter2.hasNext()) {
            ExplodingShip exploder = iter2.next();
            ShipAPI ship = exploder.ship;

            if (ship == null || !ships.contains(exploder.ship) || ship.isPiece()) {
                explode(engine, exploder);
                iter2.remove();
                continue;
            }

            exploder.chargeLevel += amount * ship.getMutableStats().getTimeMult().getModifiedValue() /
            exploder.chargingTime;
            Vector2f shipLoc = MathUtils.getPointOnCircumference(ship.getLocation(), CORE_OFFSET.get(
                                                                 ship.getHullSpec().getHullId()), ship.getFacing());

            if (exploder.chargeLevel >= 1f) {
                ship.setAngularVelocity(ship.getAngularVelocity() * 0.05f);
                ship.getVelocity().scale(0.05f);
                explode(engine, exploder);
                iter2.remove();
            } else {
                float angVel = ship.getAngularVelocity();
                angVel += (MathUtils.getRandomNumberInRange(0f, 180f) *
                           (float) Math.sin(50f * exploder.chargeLevel * exploder.chargeLevel * Math.sqrt(
                           exploder.chargingTime)) *
                           exploder.chargeLevel / exploder.chargingTime);
                ship.setAngularVelocity(angVel);
                Vector2f velAdjust = new Vector2f(MathUtils.getRandomNumberInRange(0f, 300f) * exploder.chargeLevel /
                         exploder.chargingTime, 0f);
                VectorUtils.rotate(velAdjust, 360f * (float) Math.sin(
                                   50f * exploder.chargeLevel * exploder.chargeLevel * Math.sqrt(exploder.chargingTime)),
                                   velAdjust);

                for (int i = 0; i <= ship.getCollisionRadius() * 0.02f * EXPLOSION_INTENSITY.get(ship.getHullSize());
                     i++) {
                    if (Math.random() > 0.8) {
                        Vector2f point1 = MathUtils.getRandomPointInCircle(shipLoc, ship.getCollisionRadius() *
                                                                           ((float) Math.random() + 0.5f) *
                                                                           exploder.chargeLevel *
                                                                           EXPLOSION_INTENSITY.get(ship.getHullSize()));
                        Vector2f point2 = MathUtils.getRandomPointInCircle(shipLoc, ship.getCollisionRadius() * 0.1f);
                        engine.spawnEmpArc(ship, point2, new SimpleEntity(point2), new SimpleEntity(point1),
                                           DamageType.ENERGY, 0f, 0f, 1000f, null,
                                           (exploder.chargeLevel * 15f + 15f) * EXPLOSION_INTENSITY.get(
                                                   ship.getHullSize()), COLOR_EMP_FRINGE, COLOR_EMP_CORE);
                    }
                }
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
    }

    private static final class ExplodingShip {

        float chargeLevel;
        float chargingTime;
        ShipAPI ship;

        private ExplodingShip(ShipAPI ship, float chargingTime) {
            this.ship = ship;
            this.chargingTime = chargingTime;
            this.chargeLevel = 0f;
        }
    }

    private static final class LocalData {

        final Set<ShipAPI> deadShips = new LinkedHashSet<>(50);
        final List<ExplodingShip> explodingShips = new ArrayList<>(50);
    }
}
