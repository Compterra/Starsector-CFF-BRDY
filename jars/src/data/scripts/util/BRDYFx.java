package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.lang.reflect.Method;

public final class BRDYFx {

    private static ParticleEngineBridge particleEngineBridge = null;
    private static boolean particleEngineChecked = false;
    private static boolean loggedParticleEngineFailure = false;

    private BRDYFx() {
    }

    public static void resetParticleEngineCache() {
        particleEngineBridge = null;
        particleEngineChecked = false;
        loggedParticleEngineFailure = false;
    }

    public static void addHitBurst(CombatEngineAPI engine, Vector2f location, Vector2f baseVelocity, Color fxColor,
                                   int count, float minSize, float maxSize, float brightness, float minLife,
                                   float maxLife, float velocitySpread, CombatEngineLayers layer) {
        int scaledCount = BRDYSettings.scaleParticleCount(count);
        if (engine == null || location == null || scaledCount <= 0) {
            return;
        }

        if (tryParticleBurst(location, baseVelocity, fxColor, scaledCount, minSize, maxSize, brightness, minLife,
                             maxLife, 0f, velocitySpread, 0f, layer)) {
            return;
        }

        for (int i = 0; i < scaledCount; i++) {
            Vector2f particleVelocity = randomVelocity(baseVelocity, velocitySpread);
            engine.addHitParticle(new Vector2f(location), particleVelocity,
                                  MathUtils.getRandomNumberInRange(minSize, maxSize), brightness,
                                  MathUtils.getRandomNumberInRange(minLife, maxLife), fxColor);
        }
    }

    public static void addSmokeBurst(CombatEngineAPI engine, Vector2f location, Vector2f baseVelocity, Color fxColor,
                                     int count, float minSize, float maxSize, float opacity, float minLife,
                                     float maxLife, float velocitySpread, CombatEngineLayers layer) {
        int scaledCount = BRDYSettings.scaleParticleCount(count);
        if (engine == null || location == null || scaledCount <= 0) {
            return;
        }

        if (tryParticleBurst(location, baseVelocity, fxColor, scaledCount, minSize, maxSize, opacity, minLife,
                             maxLife, 0f, velocitySpread, 0f, layer)) {
            return;
        }

        for (int i = 0; i < scaledCount; i++) {
            Vector2f particleVelocity = randomVelocity(baseVelocity, velocitySpread);
            engine.addSmokeParticle(new Vector2f(location), particleVelocity,
                                    MathUtils.getRandomNumberInRange(minSize, maxSize), opacity,
                                    MathUtils.getRandomNumberInRange(minLife, maxLife), fxColor);
        }
    }

    public static void addInwardSmokeBurst(CombatEngineAPI engine, Vector2f center, Color fxColor, int count,
                                           float radius, float size, float opacity, float life,
                                           CombatEngineLayers layer) {
        int scaledCount = BRDYSettings.scaleParticleCount(count);
        if (engine == null || center == null || scaledCount <= 0) {
            return;
        }

        float inwardSpeed = radius / Math.max(0.1f, life);
        if (tryParticleBurst(center, null, fxColor, scaledCount, size * 0.75f, size * 1.25f, opacity, life * 0.75f,
                             life * 1.25f, radius * 0.95f, 0f, -inwardSpeed, layer)) {
            return;
        }

        for (int i = 0; i < scaledCount; i++) {
            Vector2f particlePos = MathUtils.getRandomPointOnCircumference(center, radius);
            Vector2f particleVel = Vector2f.sub(center, particlePos, null);
            engine.addSmokeParticle(particlePos, particleVel, size, opacity, life, fxColor);
        }
    }

    public static void addExplosionSparkBurst(CombatEngineAPI engine, Vector2f center, Color fxColor, int count,
                                              float radius, float maxSpeed, float size, float brightness, float minLife,
                                              float maxLife, CombatEngineLayers layer) {
        int scaledCount = BRDYSettings.scaleParticleCount(count);
        if (engine == null || center == null || scaledCount <= 0) {
            return;
        }

        if (tryParticleBurst(center, null, fxColor, scaledCount, size * 0.7f, size * 1.3f, brightness, minLife,
                             maxLife, radius * 0.25f, maxSpeed, 0f, layer)) {
            return;
        }

        for (int i = 0; i < scaledCount; i++) {
            float particleRadius = radius * (float) Math.random() * 0.25f;
            Vector2f direction = MathUtils.getRandomPointOnCircumference(null, maxSpeed *
                    ((float) Math.random() * 0.75f + 0.25f));
            Vector2f point = MathUtils.getPointOnCircumference(center, particleRadius,
                                                               VectorUtils.getFacing(direction));
            engine.addHitParticle(point, direction, size, brightness,
                                  MathUtils.getRandomNumberInRange(minLife, maxLife), fxColor);
        }
    }

    private static boolean tryParticleBurst(Vector2f location, Vector2f baseVelocity, Color fxColor, int count,
                                            float minSize, float maxSize, float alpha, float minLife, float maxLife,
                                            float offsetRadius, float velocitySpread, float radialSpeed,
                                            CombatEngineLayers layer) {
        ParticleEngineBridge bridge = getParticleEngineBridge();
        if (bridge == null) {
            return false;
        }

        try {
            Object emitter = bridge.initialize.invoke(null, new Vector2f(location));
            bridge.setLayer.invoke(emitter, layer);
            bridge.life.invoke(emitter, minLife, maxLife);
            bridge.fadeTime.invoke(emitter, 0.02f, Math.min(0.12f, minLife * 0.35f), minLife * 0.45f, maxLife);
            bridge.size.invoke(emitter, minSize, maxSize);
            bridge.growthRate.invoke(emitter, -minSize * 0.2f, maxSize * 0.25f);
            bridge.color.invoke(emitter, fxColor.getRed() / 255f, fxColor.getGreen() / 255f,
                                fxColor.getBlue() / 255f,
                                Math.max(0f, Math.min(1f, alpha * fxColor.getAlpha() / 255f)));
            bridge.randomHSVA.invoke(emitter, 10f, 0.08f, 0.15f, 0.08f);
            bridge.alphaShift.invoke(emitter, -alpha / Math.max(0.1f, maxLife), -alpha / Math.max(0.1f, minLife));

            if (baseVelocity != null) {
                bridge.velocity.invoke(emitter, baseVelocity.x, baseVelocity.x, baseVelocity.y, baseVelocity.y);
            }
            if (offsetRadius > 0f) {
                bridge.circleOffset.invoke(emitter, 0f, offsetRadius);
            }
            if (velocitySpread > 0f) {
                bridge.circleVelocity.invoke(emitter, 0f, velocitySpread);
            }
            if (radialSpeed != 0f) {
                bridge.radialVelocity.invoke(emitter, radialSpeed, radialSpeed * 0.4f);
            }

            Object result = bridge.burst.invoke(null, emitter, count);
            return result instanceof Boolean && ((Boolean) result).booleanValue();
        } catch (Throwable ex) {
            particleEngineBridge = null;
            particleEngineChecked = true;
            if (!loggedParticleEngineFailure) {
                loggedParticleEngineFailure = true;
                Global.getLogger(BRDYFx.class).warn("Particle Engine burst failed; falling back to vanilla particles.",
                                                    ex);
            }
            return false;
        }
    }

    private static ParticleEngineBridge getParticleEngineBridge() {
        if (!BRDYSettings.isParticleEngineEnabled()) {
            return null;
        }
        if (particleEngineBridge != null) {
            return particleEngineBridge;
        }
        if (particleEngineChecked) {
            return null;
        }

        particleEngineChecked = true;
        try {
            Class<?> particles = Class.forName("particleengine.Particles");
            Class<?> emitter = Class.forName("particleengine.Emitter");
            Class<?> iEmitter = Class.forName("particleengine.IEmitter");
            particleEngineBridge = new ParticleEngineBridge(particles, emitter, iEmitter);
            return particleEngineBridge;
        } catch (Throwable ex) {
            particleEngineBridge = null;
            if (!loggedParticleEngineFailure) {
                loggedParticleEngineFailure = true;
                Global.getLogger(BRDYFx.class).warn("Particle Engine is enabled but its API was not available.", ex);
            }
            return null;
        }
    }

    private static Vector2f randomVelocity(Vector2f baseVelocity, float velocitySpread) {
        Vector2f result = baseVelocity == null ? new Vector2f() : new Vector2f(baseVelocity);
        if (velocitySpread > 0f) {
            Vector2f random = MathUtils.getPointOnCircumference(null,
                                                                MathUtils.getRandomNumberInRange(0f, velocitySpread),
                                                                (float) Math.random() * 360f);
            Vector2f.add(result, random, result);
        }
        return result;
    }

    private static final class ParticleEngineBridge {
        private final Method initialize;
        private final Method burst;
        private final Method setLayer;
        private final Method life;
        private final Method fadeTime;
        private final Method size;
        private final Method growthRate;
        private final Method color;
        private final Method randomHSVA;
        private final Method alphaShift;
        private final Method velocity;
        private final Method circleOffset;
        private final Method circleVelocity;
        private final Method radialVelocity;

        private ParticleEngineBridge(Class<?> particles, Class<?> emitter, Class<?> iEmitter) throws NoSuchMethodException {
            initialize = particles.getMethod("initialize", Vector2f.class);
            burst = particles.getMethod("burst", iEmitter, int.class);
            setLayer = emitter.getMethod("setLayer", CombatEngineLayers.class);
            life = emitter.getMethod("life", float.class, float.class);
            fadeTime = emitter.getMethod("fadeTime", float.class, float.class, float.class, float.class);
            size = emitter.getMethod("size", float.class, float.class);
            growthRate = emitter.getMethod("growthRate", float.class, float.class);
            color = emitter.getMethod("color", float.class, float.class, float.class, float.class);
            randomHSVA = emitter.getMethod("randomHSVA", float.class, float.class, float.class, float.class);
            alphaShift = emitter.getMethod("alphaShift", float.class, float.class);
            velocity = emitter.getMethod("velocity", float.class, float.class, float.class, float.class);
            circleOffset = emitter.getMethod("circleOffset", float.class, float.class);
            circleVelocity = emitter.getMethod("circleVelocity", float.class, float.class);
            radialVelocity = emitter.getMethod("radialVelocity", float.class, float.class);
        }
    }
}