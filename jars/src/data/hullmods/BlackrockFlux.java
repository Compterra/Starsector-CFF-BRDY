package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.DerivedWeaponStatsAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lazywizard.lazylib.CollectionUtils;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class BlackrockFlux extends BaseHullMod {

    private static final float DAMAGE_FACTOR = 0.015f;
    private static final float DAMAGE_POWER = 2.0f;
    private static final float EMP_FACTOR = 0.67f;
    private static final Set<String> INTERRUPTIBLE_PHASE_SYSTEMS = new HashSet<>(1);
    private static final Set<String> INTERRUPTIBLE_SYSTEMS = new HashSet<>(0);
    private static final Set<String> VENT_AI_FOR_THESE_SHIPS = new HashSet<>(0);
    private static final Map<HullSize, Float> MAG = new HashMap<>(5);
    private static final Map<HullSize, Float> RESERVED_FLUX_DEFAULTS = new HashMap<>(6);
    private static final Map<String, Float> RESERVED_FLUX_EXCEPTIONS = new HashMap<>(1);
    private static final Map<String, Float> SPECIAL_PHASE_SYSTEMS = new HashMap<>(0);
    private static final Map<String, Float> SPECIAL_SYSTEMS = new HashMap<>(0);

    static {
//        INTERRUPTIBLE_PHASE_SYSTEMS.add("brphasecloak");
    }

    static {
        RESERVED_FLUX_EXCEPTIONS.put("brdyx_imaginos", 0.7f);
        RESERVED_FLUX_EXCEPTIONS.put("brdyx_morpheus", 0.7f);
    }

    static {
        RESERVED_FLUX_DEFAULTS.put(HullSize.FIGHTER, 0.4f);
        RESERVED_FLUX_DEFAULTS.put(HullSize.FRIGATE, 0.5f);
        RESERVED_FLUX_DEFAULTS.put(HullSize.DESTROYER, 0.6f);
        RESERVED_FLUX_DEFAULTS.put(HullSize.DEFAULT, 0.6f);
        RESERVED_FLUX_DEFAULTS.put(HullSize.CRUISER, 0.7f);
        RESERVED_FLUX_DEFAULTS.put(HullSize.CAPITAL_SHIP, 0.8f);
    }

    static {
        MAG.put(HullSize.FIGHTER, 0f);
        MAG.put(HullSize.FRIGATE, 60f);
        MAG.put(HullSize.DESTROYER, 50f);
        MAG.put(HullSize.CRUISER, 40f);
        MAG.put(HullSize.CAPITAL_SHIP, 30f);
    }

    private static float armorLevel(ShipAPI ship) {
        if (ship == null || !Global.getCombatEngine().isEntityInPlay(ship)) {
            return 0f;
        }
        float current = 0f;
        float total = 0f;
        float worst = 1f;
        ArmorGridAPI armorGrid = ship.getArmorGrid();
        for (int x = 0; x < armorGrid.getGrid().length; x++) {
            for (int y = 0; y < armorGrid.getGrid()[x].length; y++) {
                float fraction = armorGrid.getArmorFraction(x, y);
                current += fraction;
                total += 1f;
                if (fraction < worst) {
                    worst = fraction;
                }
            }
        }
        return (current / total) * (float) Math.sqrt(worst * 0.75f + 0.25f);
    }

    private static float getTimeToAim(WeaponAPI weapon, Vector2f aimAt) {
        float turnSpeed;
        float time;
        if (Math.abs(weapon.distanceFromArc(aimAt)) >= 10f) {
            turnSpeed = weapon.getShip().getMutableStats().getMaxTurnRate().getModifiedValue();
            time = Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), VectorUtils.getAngle(
                    weapon.getLocation(), aimAt))) / turnSpeed;
        } else {
            turnSpeed = Math.max(weapon.getTurnRate(),
                    weapon.getShip().getMutableStats().getMaxTurnRate().getModifiedValue());
            time = Math.abs(MathUtils.getShortestRotation(weapon.getCurrAngle(), VectorUtils.getAngle(
                    weapon.getLocation(), aimAt))) / turnSpeed;
        }

        // Divide by zero - can't turn, only a threat if already aimed
        if (Float.isNaN(time) || turnSpeed <= 0f) {
            if (weapon.distanceFromArc(aimAt) == 0) {
                return 0f;
            } else {
                return Float.MAX_VALUE;
            }
        }

        return time;
    }

    private static boolean shipBurst(ShipAPI ship) {
        List<WeaponAPI> weapons = ship.getAllWeapons();
        int weaponsSize = weapons.size();
        for (int i = 0; i < weaponsSize; i++) {
            WeaponAPI weapon = weapons.get(i);
            if (weapon.getType() == WeaponType.DECORATIVE || weapon.getType() == WeaponType.LAUNCH_BAY
                    || weapon.getType() == WeaponType.SYSTEM) {
                continue;
            }
            if (((!weapon.isBeam() && weapon.getDerivedStats().getBurstFireDuration() > 0f
                    && weapon.getDerivedStats().getBurstFireDuration() <= 15f)
                    || weapon.isBurstBeam()) && (weapon.getChargeLevel() >= 0.75f || weapon.isFiring())
                    && weapon.getCooldownRemaining() <= 0.25f) {
                if (weapon.getSize() == WeaponSize.SMALL && (ship.getHullSize() == HullSize.FRIGATE
                        || ship.getHullSize() == HullSize.DESTROYER)) {
                    return true;
                }
                if (weapon.getSize() == WeaponSize.MEDIUM && (ship.getHullSize() == HullSize.FRIGATE
                        || ship.getHullSize() == HullSize.DESTROYER
                        || ship.getHullSize() == HullSize.CRUISER)) {
                    return true;
                }
                if (weapon.getSize() == WeaponSize.LARGE) {
                    return true;
                }
            }
        }
        return false;
    }
    private final Color CONTRAIL_COLOR = new Color(40, 49, 39, 44);
    private final Color ENGINE_COLOR = new Color(170, 255, 50, 235);

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();

        if (Global.getCombatEngine().isPaused()) {
            return;
        }



        //////////////////////////////////////////
        //                                      //
        //  SAFETY OVERRIDES COLOR ADJUSTEMENT  //
        //                                      //
        //////////////////////////////////////////
        if (ship.getVariant().getHullMods().contains("safetyoverrides")) {
            ship.getEngineController().fadeToOtherColor(
                    this,
                    ENGINE_COLOR,
                    CONTRAIL_COLOR,
                    1, //"opacity"
                    1f //max blend
            );
        }
        
                if (!VENT_AI_FOR_THESE_SHIPS.contains(ship.getHullSpec().getBaseHullId())) {
            // Only explicitly listed hulls need the custom venting AI.
            return;
        }

        //////////////////////////////////////////
        ShipwideAIFlags flags = ship.getAIFlags();

        if (ship.isShuttlePod() || ship.isDrone() || ship.getShipAI() == null || flags == null) {
            return;
        }

        if (ship.getHullSpec().getHullId().contentEquals("ssp_excelsior")) {
            flags.setFlag(AIFlags.DO_NOT_VENT);
            flags.removeFlag(AIFlags.DO_NOT_USE_FLUX);
            flags.removeFlag(AIFlags.HAS_INCOMING_DAMAGE);
            flags.removeFlag(AIFlags.KEEP_SHIELDS_ON);
            return;
        }

        if (flags.hasFlag(AIFlags.DO_NOT_VENT)) {
            return;
        }

        if (ship.isFighter() && !ship.getHullSpec().getHullId().contentEquals("ssp_lightning")) {
            return;
        }

        if (Math.random() > 0.97) {
            FluxTrackerAPI shipFT = ship.getFluxTracker();
            MutableShipStatsAPI shipMS = ship.getMutableStats();
            if (shipFT.isOverloadedOrVenting()) {
                return;
            }

            float range = (float) Math.sqrt(ship.getCollisionRadius()) * 200f;

            List<DamagingProjectileAPI> projectiles = engine.getProjectiles();
            List<DamagingProjectileAPI> nearbyThreats = new ArrayList<>(projectiles.size() / 4);
            for (DamagingProjectileAPI tmp : projectiles) {
                if (MathUtils.isWithinRange(tmp.getLocation(), ship.getLocation(), range)) {
                    nearbyThreats.add(tmp);
                }
            }
            nearbyThreats = CollectionUtils.filter(nearbyThreats, new FilterMisses(ship));
            List<MissileAPI> nearbyMissiles = AIUtils.getNearbyEnemyMissiles(ship, range / 2f);
            for (MissileAPI missile : nearbyMissiles) {
                if (missile.getMissileAI() != null && !missile.isFizzling() && !missile.isFlare()
                        && !missile.getMissileAI().getClass().getSimpleName().contentEquals("RocketAI")) {
                    nearbyThreats.add(missile);
                }
            }

            FleetMemberAPI member = CombatUtils.getFleetMember(ship);
            float shipStrength = 1f;
            if (member != null) {
                shipStrength = 0.1f + member.getFleetPointCost();
            }

            float armorlevel = armorLevel(ship);
            float maxVentTime = shipFT.getMaxFlux() / (shipMS.getFluxDissipation().getModifiedValue() * 2f
                    * shipMS.getVentRateMult().getModifiedValue());
            float decisionLevel = (5f * (float) Math.sqrt(ship.getHitpoints() / 100f) + 0.5f * (float) Math.sqrt(
                    ship.getHitpoints() / 100f)
                    * (float) Math.sqrt(armorlevel
                            * ship.getArmorGrid().getArmorRating() / 10f)
                    / (maxVentTime / 8f));
            float threatLevel = 0f;
            float opportunityLevel = 0f;
            int threatsSize = nearbyThreats.size();
            for (int j = 0; j < threatsSize; j++) {
                DamagingProjectileAPI threat = nearbyThreats.get(j);
                float damage = threat.getDamageAmount() + threat.getEmpAmount() * EMP_FACTOR;
                damage /= (float) Math.sqrt(Math.max((ship.getHitpoints() / 1000000f) * armorlevel
                        * ship.getArmorGrid().getArmorRating(), 0.1f));
                if (threat.getDamageType() == DamageType.HIGH_EXPLOSIVE) {
                    damage = (float) Math.pow(damage * (1f + armorlevel * 0.25f) * DAMAGE_FACTOR * 1.25f, DAMAGE_POWER);
                } else if (threat.getDamageType() == DamageType.KINETIC) {
                    damage = (float) Math.pow(damage * (1f - armorlevel * 0.25f) * DAMAGE_FACTOR * 1.25f, DAMAGE_POWER);
                } else if (threat.getDamageType() == DamageType.ENERGY) {
                    damage = (float) Math.pow(damage * DAMAGE_FACTOR * 1.25f, DAMAGE_POWER);
                } else if (threat.getDamageType() == DamageType.FRAGMENTATION) {
                    damage = (float) Math.pow(damage * (1f - armorlevel * 0.5f) * DAMAGE_FACTOR * 1.25f, DAMAGE_POWER);
                }

                //Global.getCombatEngine().addFloatingText(threat.getLocation(), "" + damage, 15f, Color.white, ship, 0f, 0f);
                threatLevel += damage;
            }

            List<BeamAPI> nearbyBeams = engine.getBeams();
            threatsSize = nearbyBeams.size();
            for (int j = 0; j < threatsSize; j++) {
                BeamAPI beam = nearbyBeams.get(j);
                if (beam.getDamageTarget() == ship) {
                    float damage;
                    if (beam.getWeapon().isBurstBeam()) {
                        damage = beam.getWeapon().getDerivedStats().getBurstDamage()
                                / beam.getWeapon().getDerivedStats().getBurstFireDuration()
                                + beam.getWeapon().getDerivedStats().getEmpPerSecond() * EMP_FACTOR;
                    } else {
                        damage = beam.getWeapon().getDerivedStats().getDps()
                                + beam.getWeapon().getDerivedStats().getEmpPerSecond() * EMP_FACTOR;
                    }
                    damage /= (float) Math.sqrt(Math.max((ship.getHitpoints() / 1000000f) * armorlevel
                            * ship.getArmorGrid().getArmorRating(), 0.1f));
                    if (beam.getWeapon().getDamageType() == DamageType.HIGH_EXPLOSIVE) {
                        threatLevel
                                += Math.pow(damage * (1f + armorlevel * 0.25f) * DAMAGE_FACTOR * 0.75f, DAMAGE_POWER);
                    } else if (beam.getWeapon().getDamageType() == DamageType.KINETIC) {
                        threatLevel
                                += Math.pow(damage * (1f - armorlevel * 0.25f) * DAMAGE_FACTOR * 0.75f, DAMAGE_POWER);
                    } else if (beam.getWeapon().getDamageType() == DamageType.ENERGY) {
                        threatLevel += Math.pow(damage * DAMAGE_FACTOR * 0.75f, DAMAGE_POWER);
                    } else if (beam.getWeapon().getDamageType() == DamageType.FRAGMENTATION) {
                        threatLevel += Math.pow(damage * (1f - armorlevel * 0.5f) * DAMAGE_FACTOR * 0.75f, DAMAGE_POWER);
                    }
                }
            }

            List<ShipAPI> nearbyEnemies = AIUtils.getEnemiesOnMap(ship);
            threatsSize = nearbyEnemies.size();
            for (int j = 0; j < threatsSize; j++) {
                ShipAPI enemy = nearbyEnemies.get(j);
                float falloff = 1f;
                float distance = MathUtils.getDistance(ship, enemy);
                if (distance >= range) {
                    continue;
                }

                if (distance >= range / 2f) {
                    falloff = (1f - distance / range) * 2f;
                }
                FluxTrackerAPI enemyFT = enemy.getFluxTracker();
                MutableShipStatsAPI enemyMS = enemy.getMutableStats();
                float fluxDifference = ((enemyFT.getMaxFlux() - enemyFT.getCurrFlux()) - (shipFT.getMaxFlux()
                        - shipFT.getCurrFlux()))
                        / (shipFT.getMaxFlux() + 1f);
                if ((enemyFT.isOverloadedOrVenting() || fluxDifference <= -0.5f) && (member == null
                        || !member.isCivilian())) {
                    FleetMemberAPI enemyMember = CombatUtils.getFleetMember(enemy);
                    if (enemyMember != null) {
                        if (ship.getShipTarget() == enemy) {
                            opportunityLevel += 100f * Math.max(-fluxDifference, 0.5f) * (float) Math.sqrt(
                                    enemyMember.getFleetPointCost()) / shipStrength;
                        } else {
                            opportunityLevel += 30f * Math.max(-fluxDifference, 0.5f) * (float) Math.sqrt(
                                    enemyMember.getFleetPointCost()) / shipStrength;
                        }
                    }
                }

                float speedFactor = (float) Math.sqrt(enemy.getMutableStats().getMaxSpeed().getModifiedValue()
                        / (ship.getMutableStats().getMaxSpeed().getModifiedValue() + 20f));

                if (distance <= range / 2f) {
                    FleetMemberAPI enemyMember = CombatUtils.getFleetMember(enemy);
                    if (enemyMember != null) {
                        float fall = (range / 2f - distance) / (range / 2f);
                        if (ship.getShipTarget() == enemy) {
                            threatLevel += speedFactor * 100f * fall
                                    * (float) Math.sqrt(enemyMember.getFleetPointCost()) / shipStrength;
                        } else {
                            threatLevel += speedFactor * 30f * fall * (float) Math.sqrt(
                                    (enemyMember.getFleetPointCost())) / shipStrength;
                        }
                    }
                }

                float shipTTV = shipFT.getCurrFlux() / (shipMS.getFluxDissipation().getModifiedValue() * 2f
                        * shipMS.getVentRateMult().getModifiedValue());
                float enemyTTV = enemyFT.getCurrFlux() / (enemyMS.getFluxDissipation().getModifiedValue() * 2f
                        * enemyMS.getVentRateMult().getModifiedValue());
                if (enemyFT.isOverloaded() && enemyFT.getOverloadTimeRemaining() > shipTTV + 2.5f && distance >= range
                        / 2f) {
                    continue;
                }
                if (enemyFT.isVenting() && enemyTTV > shipTTV + 2.5f && distance >= range / 2f) {
                    continue;
                }
                List<WeaponAPI> weapons = enemy.getAllWeapons();
                int weaponsSize = weapons.size();
                for (int k = 0; k < weaponsSize; k++) {
                    WeaponAPI weapon = weapons.get(k);
                    float rangeSlip = enemyMS.getMaxSpeed().getModifiedValue() * Math.max(enemyTTV, Math.max(
                            weapon.getCooldownRemaining(),
                            shipTTV));
                    float weaponDist = MathUtils.getDistance(ship, weapon.getLocation());
                    float weaponRange = weapon.getRange() + rangeSlip;
                    float availableFlux = Math.min(enemyFT.getMaxFlux() - enemyFT.getCurrFlux() + Math.max(
                            (enemyMS.getFluxDissipation().getModifiedValue()
                            + enemyMS.getVentRateMult().getModifiedValue()) * enemyTTV,
                            enemyMS.getFluxDissipation().getModifiedValue() * Math.max(weapon.getCooldownRemaining(),
                            shipTTV)),
                            enemyFT.getMaxFlux());
                    if (!weapon.isPermanentlyDisabled()
                            && ((!weapon.isFiring() && weapon.getCooldownRemaining() <= 0f)
                            || weapon.getCooldownRemaining()
                            <= shipTTV)
                            && (weapon.getAmmo() > 0 || !weapon.usesAmmo()) && weapon.getFluxCostToFire() <= availableFlux
                            && ((getTimeToAim(weapon, ship.getLocation()) <= shipTTV && weaponRange >= weaponDist)
                            || ((weapon.getSpec().getAIHints().contains(AIHints.DO_NOT_AIM)
                            || weapon.getSpec().getAIHints().contains(
                                    AIHints.HEATSEEKER)) && weaponRange >= weaponDist))) {
                        float damage;
                        DerivedWeaponStatsAPI stats = weapon.getDerivedStats();
                        if (weapon.isBurstBeam()) {
                            damage = stats.getBurstDamage() + stats.getEmpPerSecond() * EMP_FACTOR
                                    * stats.getBurstFireDuration();
                        } else if (stats.getSustainedDps() < stats.getDps() && weapon.usesAmmo()) {
                            damage = Math.max((stats.getDamagePerShot() + stats.getEmpPerShot() * EMP_FACTOR),
                                    (stats.getDps() + stats.getEmpPerSecond()
                                    * 0.25f) * weapon.getAmmo()
                                    / weapon.getMaxAmmo()
                                    + stats.getSustainedDps() * (1f
                                    - weapon.getAmmo()
                                    / weapon.getMaxAmmo()));
                        } else {
                            damage = Math.max((stats.getDamagePerShot() + stats.getEmpPerShot() * EMP_FACTOR),
                                    stats.getDps() + stats.getEmpPerSecond()
                                    * EMP_FACTOR);
                        }
                        if (Math.abs(weapon.distanceFromArc(ship.getLocation())) >= 30f
                                && (weapon.getSpec().getAIHints().contains(AIHints.DO_NOT_AIM)
                                || weapon.getSpec().getAIHints().contains(AIHints.HEATSEEKER))) {
                            damage /= Math.abs(weapon.distanceFromArc(ship.getLocation())) / 30f;
                        }
                        if (weapon.getDamageType() == DamageType.HIGH_EXPLOSIVE) {
                            damage = (float) Math.sqrt(falloff) * (float) Math.pow(damage * (1f + armorlevel * 0.25f)
                                    * DAMAGE_FACTOR, DAMAGE_POWER);
                        } else if (weapon.getDamageType() == DamageType.KINETIC) {
                            damage = (float) Math.sqrt(falloff) * (float) Math.pow(damage * (1f - armorlevel * 0.25f)
                                    * DAMAGE_FACTOR, DAMAGE_POWER);
                        } else if (weapon.getDamageType() == DamageType.ENERGY) {
                            damage = (float) Math.sqrt(falloff) * (float) Math.pow(damage * DAMAGE_FACTOR, DAMAGE_POWER);
                        } else if (weapon.getDamageType() == DamageType.FRAGMENTATION) {
                            damage = (float) Math.sqrt(falloff) * (float) Math.pow(damage * (1f - armorlevel * 0.5f)
                                    * DAMAGE_FACTOR, DAMAGE_POWER);
                        }

                        //Global.getCombatEngine().addFloatingText(weapon.getLocation(), "" + damage, 15f, Color.white, enemy, 0f, 0f);
                        threatLevel += speedFactor * damage;
                    }
                }
            }

            float allyLevel = 0f;
            List<ShipAPI> nearbyAllies = AIUtils.getNearbyAllies(ship, range / 2f);
            threatsSize = nearbyAllies.size();
            for (int j = 0; j < threatsSize; j++) {
                ShipAPI ally = nearbyAllies.get(j);
                if (ally == ship || ally.isDrone() || ally.isFighter() || ally.getHullSpec().getHints().contains(
                        ShipTypeHints.CIVILIAN)) {
                    continue;
                }
                FleetMemberAPI allyMember = CombatUtils.getFleetMember(ally);
                if (allyMember != null) {
                    allyLevel += allyMember.getFleetPointCost();
                } else {
                    if (ally.getHullSize() == HullSize.FRIGATE) {
                        allyLevel += 4f;
                    } else if (ally.getHullSize() == HullSize.DESTROYER) {
                        allyLevel += 8f;
                    } else if (ally.getHullSize() == HullSize.CRUISER) {
                        allyLevel += 14f;
                    } else if (ally.getHullSize() == HullSize.CAPITAL_SHIP) {
                        allyLevel += 28f;
                    }
                }
            }

            float reserved = RESERVED_FLUX_DEFAULTS.get(ship.getHullSize());
            Float specificReserved = RESERVED_FLUX_EXCEPTIONS.get(ship.getHullSpec().getBaseHullId());
            if (specificReserved != null) {
                reserved = specificReserved;
            }

            decisionLevel *= (shipFT.getCurrFlux() + 0.5f * shipFT.getHardFlux() - reserved * shipFT.getMaxFlux())
                    / shipFT.getMaxFlux();
            if (shipFT.getFluxLevel() <= 0.5f) {
                decisionLevel *= shipFT.getFluxLevel() * 2f;
            }

            if (ship.getHullSpec().getHullId().startsWith("tem_")) {
                if (shipFT.getFluxLevel() >= 0.75f) {
                    decisionLevel *= 1.5f;
                } else {
                    decisionLevel *= shipFT.getFluxLevel() / 0.75f;
                }
            } else if (ship.getHullSpec().getHullId().startsWith("ms_") || ship.getHullSpec().getHullId().startsWith(
                    "msp_")) {
                decisionLevel *= 0.75f;
            } else if (ship.getHullSpec().getHullId().startsWith("exigency_")) {
                decisionLevel *= 0.5f;
            } else {
                decisionLevel *= 0.85f;
            }

            threatLevel = (float) Math.pow(threatLevel, 0.75) / (float) Math.sqrt(shipStrength);
            threatLevel = Math.max(threatLevel - (float) Math.sqrt(allyLevel / 3f) * 6f, 0f);

            if (threatLevel > shipStrength) {
                decisionLevel *= shipStrength / threatLevel;
            }

            decisionLevel -= threatLevel;
            decisionLevel -= opportunityLevel;

            if (shipBurst(ship)) {
                decisionLevel *= 0.0f;
            }

            if (ship.getSystem() != null && ship.getSystem().isActive() && (ship.getDeployedDrones() == null
                    || ship.getDeployedDrones().isEmpty())) {
                if (INTERRUPTIBLE_SYSTEMS.contains(ship.getSystem().getId())) {
                    decisionLevel *= 0.5f;
                } else {
                    decisionLevel *= 0.0f;
                }
            }

            if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isActive()) {
                if (INTERRUPTIBLE_PHASE_SYSTEMS.contains(ship.getPhaseCloak().getId())) {
                    decisionLevel *= 0.5f;
                } else {
                    decisionLevel *= 0.0f;
                }
            }

            if (ship.getSystem() != null && ship.getSystem().isActive() && (ship.getDeployedDrones() == null
                    || ship.getDeployedDrones().isEmpty())
                    && SPECIAL_SYSTEMS.containsKey(ship.getSystem().getId())) {
                decisionLevel *= SPECIAL_SYSTEMS.get(ship.getSystem().getId());
            }

            if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isActive() && SPECIAL_PHASE_SYSTEMS.containsKey(
                    ship.getPhaseCloak().getId())) {
                decisionLevel *= SPECIAL_PHASE_SYSTEMS.get(ship.getPhaseCloak().getId());
            }

            if (flags.hasFlag(AIFlags.BACK_OFF)) {
                decisionLevel *= 1.15f;
            }

            if (flags.hasFlag(AIFlags.IN_ATTACK_RUN)) {
                decisionLevel *= 0.5f;
            }

            if (flags.hasFlag(AIFlags.KEEP_SHIELDS_ON)) {
                decisionLevel *= 0.75f;
            }

            if (flags.hasFlag(AIFlags.RUN_QUICKLY)) {
                decisionLevel *= 1.3f;
            }

            if (flags.hasFlag(AIFlags.PURSUING)) {
                decisionLevel *= 0.5f;
            }

            if (shipFT.getFluxLevel() <= 0.25f) {
                decisionLevel *= shipFT.getFluxLevel() * 4f;
            }

            float threshold = ((0.6f * (float) Math.sqrt(ship.getMaxHitpoints() / 50f) + 0.05f * (float) Math.sqrt(
                    ship.getMaxHitpoints() / 50f)
                    * (float) Math.sqrt(ship.getArmorGrid().getArmorRating() / 5f))
                    * maxVentTime / 8f) * (1.5f - reserved);
            if (decisionLevel >= threshold) {
                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
            }
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getVentRateMult().modifyPercent(id, MAG.get(hullSize));
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (MAG.get(HullSize.FRIGATE)).intValue();
        }
        if (index == 1) {
            return "" + (MAG.get(HullSize.DESTROYER)).intValue();
        }
        if (index == 2) {
            return "" + (MAG.get(HullSize.CRUISER)).intValue();
        }
        if (index == 3) {
            return "" + (MAG.get(HullSize.CAPITAL_SHIP)).intValue();
        }
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a brdy hull id
        return (ship.getHullSpec().getHullId().startsWith("brdy_") && !ship.getVariant().getHullMods().contains(
                "brfluxmod"));
    }

    private static final class FilterMisses implements CollectionUtils.CollectionFilter<DamagingProjectileAPI> {

        private final ShipAPI ship;

        private FilterMisses(ShipAPI ship) {
            this.ship = ship;
        }

        @Override
        public boolean accept(DamagingProjectileAPI proj) {
            if (proj.getOwner() == ship.getOwner()) {
                return false;
            }

            if (proj instanceof MissileAPI) {
                MissileAPI missile = (MissileAPI) proj;
                if (missile.isFlare()) {
                    return false;
                }
            }

            return (CollisionUtils.getCollides(proj.getLocation(), Vector2f.add(proj.getLocation(),
                    (Vector2f) new Vector2f(
                            proj.getVelocity()).scale(
                            ship.getFluxTracker().getTimeToVent()
                            + 1f), null), ship.getLocation(),
                    ship.getCollisionRadius() + 50f) && Math.abs(
                    MathUtils.getShortestRotation(proj.getFacing(),
                            VectorUtils.getAngle(
                                    proj.getLocation(), ship.getLocation())))
                    <= 90f);
        }
    };
}
