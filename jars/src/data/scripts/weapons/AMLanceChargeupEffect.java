package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class AMLanceChargeupEffect implements EveryFrameWeaponEffectPlugin {

    private static final float CHARGEUP_PARTICLE_ANGLE_SPREAD = 360.0f;
    private static final float CHARGEUP_PARTICLE_BRIGHTNESS = 0.7f;
    private static final Color CHARGEUP_PARTICLE_COLOR = new Color(141, 205, 235, 100);
    private static final float CHARGEUP_PARTICLE_COUNT_FACTOR = 15.0f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MAX = 85.0f;
    private static final float CHARGEUP_PARTICLE_DISTANCE_MIN = 4.0f;
    private static final float CHARGEUP_PARTICLE_DURATION = 0.25f;
    private static final float CHARGEUP_PARTICLE_SIZE_MAX = 8.0f;
    private static final float CHARGEUP_PARTICLE_SIZE_MIN = 1.0f;
    private static final Color MUZZLE_FLASH_COLOR = new Color(201, 204, 247, 100);
    private static final float MUZZLE_FLASH_DURATION = 0.37f;
    private static final float MUZZLE_FLASH_SIZE = 200.0f;
    private static final float MUZZLE_OFFSET = 35.0f;

    // weapon state (per weapon instance)
    private float last_charge_level = 0.0f;
    private int last_weapon_ammo = 0;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        // Don't bother with any checks if the game is paused
        if (engine.isPaused()) {
            return;
        }

        float charge_level = weapon.getChargeLevel();
        int weapon_ammo = weapon.getAmmo();

        if (charge_level > last_charge_level || weapon_ammo < last_weapon_ammo) {
            // shared vars
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            float ship_facing = ship.getFacing();
            Vector2f ship_velocity = ship.getVelocity();
            Vector2f muzzle_location = MathUtils.getPointOnCircumference(weapon_location, MUZZLE_OFFSET, ship_facing);

            // chargeup (fire button held down, not cooling down after firing)
            if (charge_level > last_charge_level && weapon.isFiring()) {
                // do chargeup particles, number based on charge level
                int particle_count = (int) (CHARGEUP_PARTICLE_COUNT_FACTOR * charge_level);
                float distance, size, angle, speed;
                Vector2f particle_velocity;
                for (int i = 0; i < particle_count; ++i) {
                    // distance from muzzle
                    distance = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_DISTANCE_MIN,
                                                                CHARGEUP_PARTICLE_DISTANCE_MAX);
                    // particle size
                    size = MathUtils.getRandomNumberInRange(CHARGEUP_PARTICLE_SIZE_MIN, CHARGEUP_PARTICLE_SIZE_MAX);
                    // angle (spread in virtual firing arc) reversed
                    angle = MathUtils.getRandomNumberInRange(-0.5f * CHARGEUP_PARTICLE_ANGLE_SPREAD, 0.5f *
                                                             CHARGEUP_PARTICLE_ANGLE_SPREAD);
                    // spawn location
                    Vector2f spawn_location = MathUtils.getPointOnCircumference(muzzle_location, distance, (angle +
                                                                                                            ship_facing));
                    // speed from "distance to muzzle" as required by specified duration
                    speed = distance / CHARGEUP_PARTICLE_DURATION;
                    particle_velocity = MathUtils.getPointOnCircumference(ship_velocity, speed, 180.0f + angle +
                                                                          ship_facing);
                    engine.addHitParticle(spawn_location, particle_velocity, size, CHARGEUP_PARTICLE_BRIGHTNESS,
                                          CHARGEUP_PARTICLE_DURATION,
                                          CHARGEUP_PARTICLE_COLOR);
                }
            }

            // muzzle flash on fire after charging; ammo decreased indicates shot fired
            if (weapon_ammo < last_weapon_ammo) {
                // do muzzle flash
                engine.spawnExplosion(muzzle_location, ship_velocity, MUZZLE_FLASH_COLOR, MUZZLE_FLASH_SIZE,
                                      MUZZLE_FLASH_DURATION);
            }
        }

        last_charge_level = charge_level;
        last_weapon_ammo = weapon_ammo;
    }
}
