package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.weapons.ScalaronPulseOnHit;
import data.scripts.weapons.brdy_RepulsorPulseOnHitEffect;
import java.util.List;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class brdy_EnergyTorpedoAI implements MissileAIPlugin, GuidedMissileAI {

    private static final float LOW_ACCURACY_FACTOR = 2.5f;
    private static final float WEAVE_TIME_FACTOR = 12f;

    public static Vector2f intercept(Vector2f point, float speed, Vector2f target,
                                     Vector2f targetVel) {
        final Vector2f difference = new Vector2f(target.x - point.x, target.y - point.y);

        final float a = targetVel.x * targetVel.x + targetVel.y * targetVel.y - speed * speed;
        final float b = 2 * (targetVel.x * difference.x + targetVel.y * difference.y);
        final float c = difference.x * difference.x + difference.y * difference.y;

        final Vector2f solutionSet = quad(a, b, c);

        Vector2f intercept = null;
        if (solutionSet != null) {
            float bestFit = Math.min(solutionSet.x, solutionSet.y);
            if (bestFit < 0) {
                bestFit = Math.max(solutionSet.x, solutionSet.y);
            }
            if (bestFit > 0) {
                intercept = new Vector2f(target.x + targetVel.x * bestFit, target.y + targetVel.y * bestFit);
            }
        }

        return intercept;
    }

    private static ShipAPI findBestTarget(MissileAPI missile) {
        ShipAPI closest = null;
        float distance, closestDistance = Float.MAX_VALUE;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++) {
            ShipAPI tmp = ships.get(i);
            float mod = 0f;
            if (tmp.isFighter() || tmp.isDrone() || tmp.getCollisionClass() == CollisionClass.NONE) {
                mod = 4000f;
            }
            distance = MathUtils.getDistance(tmp, missile.getLocation()) + mod;
            if (distance < closestDistance) {
                closest = tmp;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private static Vector2f quad(float a, float b, float c) {
        Vector2f solution = null;
        if (Float.compare(Math.abs(a), 0) == 0) {
            if (Float.compare(Math.abs(b), 0) == 0) {
                solution = (Float.compare(Math.abs(c), 0) == 0) ? new Vector2f(0, 0) : null;
            } else {
                solution = new Vector2f(-c / b, -c / b);
            }
        } else {
            float d = b * b - 4 * a * c;
            if (d >= 0) {
                d = (float) Math.sqrt(d);
                a = 2 * a;
                solution = new Vector2f((-b - d) / a, (-b + d) / a);
            }
        }
        return solution;
    }

    private final MissileAPI missile;
    private final float offsetAngle;
    private float sinTimer;
    private CombatEntityAPI target;

    public brdy_EnergyTorpedoAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        this.offsetAngle = (float) Math.random() * 360f;
        this.sinTimer = (float) (Math.random() * 2.0 * Math.PI);

        if (launchingShip.getShipTarget() != null && !launchingShip.getShipTarget().isHulk()) {
            target = launchingShip.getShipTarget();
        }

        if (target == null) {
            Vector2f mouseTarget = launchingShip.getMouseTarget();
            if (mouseTarget != null) {
                List<ShipAPI> directTargets = CombatUtils.getShipsWithinRange(mouseTarget, 200f);
                ShipAPI closest = null;
                float closestDistance = Float.MAX_VALUE;
                int size = directTargets.size();
                for (int i = 0; i < size; i++) {
                    ShipAPI tmp = directTargets.get(i);
                    if (!tmp.isHulk() && tmp.getOwner() != launchingShip.getOwner() && !tmp.isDrone() &&
                            !tmp.isFighter()) {
                        float distance = MathUtils.getDistanceSquared(mouseTarget, tmp.getLocation());
                        if (distance < closestDistance) {
                            closest = tmp;
                            closestDistance = distance;
                        }
                    }
                }
                setTarget(closest);
            }
        }

        if (target == null) {
            setTarget(findBestTarget(missile));
        }
    }

    @Override
    public void advance(float amount) {
        if (missile.isFizzling() || missile.isFading()) {
            if (Math.random() > 0.95) {
                if (missile.getProjectileSpecId() != null && missile.getProjectileSpecId().contentEquals(
                        "brdy_repulsorpulse")) {
                    brdy_RepulsorPulseOnHitEffect.boom(missile.getLocation(), Global.getCombatEngine());
                } else {
                    ScalaronPulseOnHit.boom(missile.getLocation(), Global.getCombatEngine());
                }
                Global.getCombatEngine().removeEntity(missile);
            }
            return;
        }

        if (target == null || (target instanceof ShipAPI && (((ShipAPI) target).isHulk())) || (missile.getOwner() ==
                                                                                               target.getOwner()) ||
                !Global.getCombatEngine().isEntityInPlay(target)) {
            setTarget(findBestTarget(missile));
            if (target == null) {
                missile.giveCommand(ShipCommand.ACCELERATE);
                return;
            }
        }

        sinTimer += amount * WEAVE_TIME_FACTOR;

        float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
        float guidance = 0.35f;
        if (missile.getSource() != null) {
            guidance += Math.min(missile.getSource().getMutableStats().getMissileGuidance().getModifiedValue() -
            missile.getSource().getMutableStats().getMissileGuidance().getBaseValue(), 1f) * 0.3f;
        }
        Vector2f guidedTarget = intercept(missile.getLocation(), missile.getVelocity().length(), target.getLocation(),
                                          target.getVelocity());
        if (guidedTarget == null) {
            Vector2f projection = new Vector2f(target.getVelocity());
            float scalar = distance / (missile.getVelocity().length() + 1f);
            projection.scale(scalar);
            guidedTarget = Vector2f.add(target.getLocation(), projection, null);
        }
        Vector2f.sub(guidedTarget, target.getLocation(), guidedTarget);
        guidedTarget.scale(guidance);
        Vector2f.add(guidedTarget, target.getLocation(), guidedTarget);

        float offset = target.getCollisionRadius() * LOW_ACCURACY_FACTOR * (float) FastTrig.sin(sinTimer);
        guidedTarget = MathUtils.getPointOnCircumference(guidedTarget, offset, offsetAngle);

        float angularDistance = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(
                                                              missile.getLocation(), guidedTarget));

        missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

        // This missile values pressure over precision; keep it accelerating even while wobbling.
        missile.giveCommand(ShipCommand.ACCELERATE);
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public final void setTarget(CombatEntityAPI target) {
        this.target = target;
    }
}
