package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.plugins.TrylobotUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

//original script by Trylobot, adaption for PDE by Cycerin, fix by Uomoz
public class StingerFireEffect implements EveryFrameWeaponEffectPlugin {

    private static final float FIRING_DURATION = 0.25f; // weapon_data.csv: (chargeup + chargedown)
    private static final Color MUZZLE_FLASH_COLOR = new Color(112, 250, 190, 160);
    private static final float OFFSET = 11f;
    private static final Color PARTICLE_COLOR = new Color(183, 255, 182, 195);

    private float elapsed = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) {
            return;
        }

        if (weapon.isFiring()) {
            Vector2f weapon_location = weapon.getLocation();
            ShipAPI ship = weapon.getShip();
            // explosion (frame 0 only)
            if (elapsed <= 0f) {
                Vector2f explosion_offset = TrylobotUtils.translate_polar(weapon_location, OFFSET +
                                                                          ((0.05f * 100f) - 2f), weapon.getCurrAngle());
                engine.spawnExplosion(explosion_offset, ship.getVelocity(), MUZZLE_FLASH_COLOR, 65f, 0.35f);
            }

            elapsed += amount;

            // particles
            Vector2f particle_offset = TrylobotUtils.translate_polar(weapon_location, OFFSET, weapon.getCurrAngle());
            float size, speed, angle;
            Vector2f velocity;
            // more particles to start with, fewer later on
            int particle_count_this_frame = (int) (15f * (FIRING_DURATION - elapsed));
            for (int x = 0; x < particle_count_this_frame; x++) {
                size = TrylobotUtils.get_random(3f, 13f);
                speed = TrylobotUtils.get_random(200f, 400f);
                angle = weapon.getCurrAngle() + TrylobotUtils.get_random(-5f, 5f);
                velocity = TrylobotUtils.translate_polar(ship.getVelocity(), speed, angle);
                engine.addHitParticle(particle_offset, velocity, size, 1.3f, 0.55f, PARTICLE_COLOR);
            }
        } else {
            elapsed = 0f;
        }
    }
}
