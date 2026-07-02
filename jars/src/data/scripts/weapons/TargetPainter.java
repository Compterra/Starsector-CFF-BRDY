package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;

/**
 * @author EnderNerdcore
 */
public class TargetPainter implements BeamEffectPlugin {

    private float damageAmountCounter = 0f;
    private final IntervalUtil fireInterval = new IntervalUtil(0.2f, 0.3f);
    private boolean haveAppliedFullDebuff = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        ShipAPI theShip;
        if (beam.getDamageTarget() instanceof ShipAPI) { //make sure that there is a target before we apply any effects, and that our target is a ship rather than an asteroid, missile, or other object
            theShip = (ShipAPI) beam.getDamageTarget(); //identify who our beam is hitting
        } else {
            return;
        }

        String targetId = theShip.getFleetMemberId(); //we need the fleet member id for the string for identifying who our debuff goes on to
        MutableShipStatsAPI shipStats = theShip.getMutableStats(); //we also need to set up an instance of mutable stats for that ship to debuff it

        if (beam.getBrightness() >= 1f) { //only apply the effects while our beam is full strength
            if (!haveAppliedFullDebuff) { //only add to the debuff until we've hit the max
                shipStats.getArmorDamageTakenMult().modifyMult(targetId, (1f + (damageAmountCounter / 100f))); //modify the debuff to the new values, to a max of 200%
                shipStats.getShieldDamageTakenMult().modifyMult(targetId, (1f + (damageAmountCounter / 100f)));
                shipStats.getHullDamageTakenMult().modifyMult(targetId, (1f + (damageAmountCounter / 100f)));
                if (damageAmountCounter >= 100) { //if we've applied the entire debuff, mark it down so we don't enter this if statement again
                    haveAppliedFullDebuff = true;
                } else { //otherwise, advance the counter
                    damageAmountCounter++;
                }

            }

            fireInterval.advance(amount); //this is a frame counter, it keeps track of our time

            if (fireInterval.intervalElapsed()) { //once our beam is no longer hitting the target, we need to remove the debuffs
                shipStats.getArmorDamageTakenMult().unmodify(targetId);
                shipStats.getShieldDamageTakenMult().unmodify(targetId);
                shipStats.getHullDamageTakenMult().unmodify(targetId);
            }
        }
    }
}
