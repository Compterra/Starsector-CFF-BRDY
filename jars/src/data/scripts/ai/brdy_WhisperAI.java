package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class brdy_WhisperAI extends brdy_BaseMissile
{
    private static final float AIM_FUDGE_TIME = 9.8f;
    private static final float AIM_THRESHOLD = 0.3f;
    private static final float BURN_DELAY_MAX = 0.7f; // Max time until thrusting starts after target acquired
    private static final float BURN_DELAY_MIN = 0.35f;  // Min time until thrusting starts after target acquired
    private static final float ENGINE_DEAD_TIME_MAX = 0.6f; // Max time until engine burn starts
    private static final float ENGINE_DEAD_TIME_MIN = 0.3f; // Min time until engine burn starts
    private static final float FIRE_INACCURACY = 6f;
    private static final float MIRV_DISTANCE = 250f;
    private static final float MIRV_INACCURACY = 5f;
    private static final Color SMOKE_COLOR = new Color(105, 225, 145, 44);
    private static final String START_FLY_SOUND_ID = "brdy_wspr2";
    private static final String SUBMUNITION_WEAPON_ID = "brdy_tacmissile_hax";
    private static final float VELOCITY_DAMPING_FACTOR = 0.5f;
    private static final Vector2f ZERO = new Vector2f();
    private float aimFudgeTimer;
    private float burnDelayTimer;
    private float engineDeadTimer;
    private boolean flying = false;
    private final float inaccuracy;
    private boolean lockedOn = false;
    private boolean readyToFly = false;

    public brdy_WhisperAI(MissileAPI missile, ShipAPI launchingShip)
    {
        super(missile, launchingShip);

        aimFudgeTimer = AIM_FUDGE_TIME;
        burnDelayTimer = MathUtils.getRandomNumberInRange(BURN_DELAY_MIN, BURN_DELAY_MAX);
        engineDeadTimer = MathUtils.getRandomNumberInRange(ENGINE_DEAD_TIME_MIN, ENGINE_DEAD_TIME_MAX);
        inaccuracy = MathUtils.getRandomNumberInRange(-FIRE_INACCURACY, FIRE_INACCURACY);
        missile.setArmedWhileFizzling(true);
    }

    @Override
    public void advance(float amount)
    {
        if (missile.isFading())
        {
            return;
        }

        if (!lockedOn || !readyToFly)
        {
            if (missile.isFizzling())
            {
                return;
            }

            if (engineDeadTimer > 0f)
            {
                engineDeadTimer -= amount;
                if (engineDeadTimer <= 0f)
                {
                    readyToFly = true;
                }
            }

            if (acquireTarget(amount))
            {
                float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
                Vector2f guidedTarget = intercept(missile.getLocation(), missile.getMaxSpeed(), target.getLocation(), target.getVelocity());
                if (guidedTarget == null)
                {
                    Vector2f projection = new Vector2f(target.getVelocity());
                    float scalar = distance / (missile.getVelocity().length() + 1f);
                    projection.scale(scalar);
                    guidedTarget = Vector2f.add(target.getLocation(), projection, null);
                }

                float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                        MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget)
                                + inaccuracy));
                float absDAng = Math.abs(angularDistance);

                missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

                if (absDAng <= AIM_THRESHOLD)
                {
                    lockedOn = true;
                }
            }
        }
        else
        {
            if (burnDelayTimer > 0f)
            {
                burnDelayTimer -= amount;
                missile.giveCommand(ShipCommand.DECELERATE);
                return;
            }

            if (!flying)
            {
                flying = true;
                Global.getSoundPlayer().playSound(START_FLY_SOUND_ID, 1f, 1f, missile.getLocation(), ZERO);
            }

            if (aimFudgeTimer > 0f)
            {
                aimFudgeTimer -= amount;

                if (missile.isFizzling())
                {
                    return;
                }

                if (acquireTarget(amount))
                {
                    float distance = MathUtils.getDistance(target.getLocation(), missile.getLocation());
                    Vector2f guidedTarget = intercept(missile.getLocation(), missile.getMaxSpeed(), target.getLocation(), target.getVelocity());
                    if (guidedTarget == null)
                    {
                        Vector2f projection = new Vector2f(target.getVelocity());
                        float scalar = distance / (missile.getVelocity().length() + 1f);
                        projection.scale(scalar);
                        guidedTarget = Vector2f.add(target.getLocation(), projection, null);
                    }

                    float angularDistance = MathUtils.getShortestRotation(missile.getFacing(),
                            MathUtils.clampAngle(VectorUtils.getAngle(missile.getLocation(), guidedTarget)
                                    + inaccuracy));
                    float absDAng = Math.abs(angularDistance);

                    missile.giveCommand(angularDistance < 0 ? ShipCommand.TURN_RIGHT : ShipCommand.TURN_LEFT);

                    if (absDAng < Math.abs(missile.getAngularVelocity()) * VELOCITY_DAMPING_FACTOR)
                    {
                        missile.setAngularVelocity(angularDistance / VELOCITY_DAMPING_FACTOR);
                    }
                }
            }

            if (!missile.isFizzling())
            {
                missile.giveCommand(ShipCommand.ACCELERATE);
            }

            float mirvDistance = (MIRV_DISTANCE - 100f) * missile.getFlightTime() / missile.getMaxFlightTime() + 100f;

            boolean mirvNow = false;
            CombatEntityAPI mirvTarget = target;
            
            if (target == null) {
                missile.giveCommand(ShipCommand.ACCELERATE);
                return;
            } 
            
                if (MathUtils.isWithinRange(target, missile, mirvDistance))
                {
                    mirvNow = true;
                    mirvTarget = target;
                }                    

            if (mirvNow)
            {
                for (int i = 0; i < 4; i++)
                {
                    Vector2f location = MathUtils.getPointOnCircumference(missile.getLocation(), 5f, (float) Math.random() * 360f);
                    Vector2f velocity = MathUtils.getPointOnCircumference(null, MathUtils.getRandomNumberInRange(10f, 30f), (float) Math.random() * 360f);
                    velocity.x += missile.getVelocity().x * 0.35f;
                    velocity.y += missile.getVelocity().y * 0.35f;
                    Global.getCombatEngine().addSmokeParticle(location, velocity, MathUtils.getRandomNumberInRange(40f, 60f), 1f,
                            MathUtils.getRandomNumberInRange(0.5f, 1f), SMOKE_COLOR);
                }
                // Do not use procedural damage sound player because this just pops the missile
                Global.getCombatEngine().applyDamage(missile, missile.getLocation(), missile.getHitpoints() * 100f, DamageType.FRAGMENTATION, 0f, false, false,
                        missile);

                Vector2f inheritedVelocity = new Vector2f(missile.getVelocity());
                inheritedVelocity.scale(0.5f);
                for (int i = 0; i < 4; i++)
                {
                    float angle = missile.getFacing() + (i - 1) * 
                            20f + MathUtils.getRandomNumberInRange(-MIRV_INACCURACY, MIRV_INACCURACY);
                    if (angle < 0f)
                    {
                        angle += 360f;
                    }
                    else if (angle >= 360f)
                    {
                        angle -= 360f;
                    }
                    Vector2f location = MathUtils.getPointOnCircumference(missile.getLocation(), 5f, angle);
                    CombatEntityAPI fakemirv = Global.getCombatEngine().spawnProjectile(missile.getSource(), missile.getWeapon(), SUBMUNITION_WEAPON_ID, location,
                            angle, null);
                    MissileAPI blarg = (MissileAPI) fakemirv;
                    ((GuidedMissileAI) blarg.getMissileAI()).setTarget(mirvTarget);
                } 
            }
        }
    }

    @Override
    protected boolean acquireTarget(float amount)
    {
        if (!isTargetValidAlternate(target))
        {
            if (target instanceof ShipAPI)
            {
                ShipAPI ship = (ShipAPI) target;
                if (ship.isPhased() && ship.isAlive())
                {
                    return false;
                }
            }
            setTarget(findBestTarget());
            if (target == null)
            {
                setTarget(findBestTargetAlternate());
            }
            if (target == null)
            {
                return false;
            }
        }
        else
        {
            if (!isTargetValid(target))
            {
                if (target instanceof ShipAPI)
                {
                    ShipAPI ship = (ShipAPI) target;
                    if (ship.isPhased() && ship.isAlive())
                    {
                        return false;
                    }
                }
                CombatEntityAPI newTarget = findBestTarget();
                if (newTarget != null)
                {
                    target = newTarget;
                }
            }
        }
        return true;
    }

    // Weighted random picker that favors larger ships.
    @Override
    protected ShipAPI findBestTarget()
    {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValid(tmp))
            {
                mod = 0f;
            }
            else
            {
                switch (tmp.getHullSize())
                {
                    default:
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 50f;
                        break;
                    case DESTROYER:
                        mod = 75f;
                        break;
                    case CRUISER:
                        mod = 100f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 125f;
                        break;
                }
            }
            weight = (2500f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight)
            {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    protected ShipAPI findBestTargetAlternate()
    {
        ShipAPI best = null;
        float weight, bestWeight = 0f;
        List<ShipAPI> ships = AIUtils.getEnemiesOnMap(missile);
        int size = ships.size();
        for (int i = 0; i < size; i++)
        {
            ShipAPI tmp = ships.get(i);
            float mod;
            if (!isTargetValidAlternate(tmp))
            {
                mod = 0f;
            }
            else
            {
                switch (tmp.getHullSize())
                {
                    default:
                    case FIGHTER:
                        mod = 1f;
                        break;
                    case FRIGATE:
                        mod = 50f;
                        break;
                    case DESTROYER:
                        mod = 75f;
                        break;
                    case CRUISER:
                        mod = 100f;
                        break;
                    case CAPITAL_SHIP:
                        mod = 125f;
                        break;
                }
            }
            weight = (2500f / Math.max(MathUtils.getDistance(tmp, missile.getLocation()), 750f)) * mod;
            if (weight > bestWeight)
            {
                best = tmp;
                bestWeight = weight;
            }
        }
        return best;
    }

    @Override
    protected boolean isTargetValid(CombatEntityAPI target)
    {
        if (target instanceof ShipAPI)
        {
            ShipAPI ship = (ShipAPI) target;
            if (ship.isFighter() || ship.isDrone())
            {
                return false;
            }
        }
        return super.isTargetValid(target);
    }

    protected boolean isTargetValidAlternate(CombatEntityAPI target)
    {
        return super.isTargetValid(target);
    }
}
