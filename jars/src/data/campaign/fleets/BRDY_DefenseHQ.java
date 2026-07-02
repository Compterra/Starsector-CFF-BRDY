package data.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.Random;

public class BRDY_DefenseHQ extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

    private static final String BLACKROCK_FACTION_ID = "blackrock_driveyards";
    private static final String CONSORTIUM_FLEET_FACTION_ID = "br_consortium";
    private static final String ROUTE_SUFFIX = "blackrock_consortium";

    private static final int STABILITY_BONUS = 2;
    private static final int LIGHT_PATROLS = 2;
    private static final int MEDIUM_PATROLS = 1;
    private static final int HEAVY_PATROLS = 1;
    private static final int GROUND_DEFENSE_BONUS = 150;

    private static final float QUALITY_OVERRIDE = 1.35f;
    private static final int OFFICER_LEVEL_BONUS = 10;
    private static final float OFFICER_NUMBER_MULT = 1.5f;

    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
                                                      Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);
    protected float returningPatrolValue = 0f;

    @Override
    public boolean isHidden() {
        return market == null || !BLACKROCK_FACTION_ID.equals(market.getFactionId());
    }

    @Override
    public boolean isFunctional() {
        return super.isFunctional() && market != null && BLACKROCK_FACTION_ID.equals(market.getFactionId());
    }

    @Override
    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.SUPPLIES, size - 1);
        demand(Commodities.FUEL, size - 1);
        demand(Commodities.SHIPS, size - 1);
        demand(Commodities.HAND_WEAPONS, size);

        supply(Commodities.CREW, size);
        supply(Commodities.MARINES, size);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
        applyDeficitToProduction(1, deficit, Commodities.MARINES);

        modifyStabilityWithBaseMod();
        applyDefenseStats();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }

    @Override
    public void unapply() {
        super.unapply();

        if (market != null) {
            MemoryAPI memory = market.getMemoryWithoutUpdate();
            Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
            Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);
            unapplyDefenseStats();
        }

        unmodifyStabilityWithBaseMod();
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, IndustryTooltipMode mode) {
        return mode != IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
            addStabilityPostDemandSection(tooltip, hasDemand, mode);
            tooltip.addPara("Coordinates Blackrock Special Operations patrols and adds %s ground defense strength.",
                            10f, Misc.getHighlightColor(), String.valueOf(GROUND_DEFENSE_BONUS));
        }
    }

    @Override
    protected int getBaseStabilityMod() {
        return STABILITY_BONUS;
    }

    @Override
    public String getNameForModifier() {
        if (getSpec().getName().contains("HQ")) {
            return getSpec().getName();
        }
        return Misc.ucFirst(getSpec().getName());
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS, Commodities.HAND_WEAPONS);
    }

    @Override
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    @Override
    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    @Override
    protected void buildingFinished() {
        super.buildingFinished();
        tracker.forceIntervalElapsed();
    }

    @Override
    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);
        tracker.forceIntervalElapsed();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (Global.getSector().getEconomy().isSimMode() || !isFunctional()) {
            return;
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        float spawnRate = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();

        float extraTime = 0f;
        if (returningPatrolValue > 0) {
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) returningPatrolValue = 0;
        }
        tracker.advance(days * spawnRate + extraTime);

        if (tracker.intervalElapsed()) {
            spawnPatrolRoute();
        }
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
    }

    @Override
    public boolean shouldRepeat(RouteData route) {
        return false;
    }

    public int getCount(PatrolType... types) {
        int count = 0;
        for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
            if (data.getCustom() instanceof PatrolFleetData) {
                PatrolFleetData custom = (PatrolFleetData) data.getCustom();
                for (PatrolType type : types) {
                    if (type == custom.type) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public int getMaxPatrols(PatrolType type) {
        if (type == PatrolType.FAST) {
            return LIGHT_PATROLS;
        }
        if (type == PatrolType.COMBAT) {
            return MEDIUM_PATROLS;
        }
        if (type == PatrolType.HEAVY) {
            return HEAVY_PATROLS;
        }
        return 0;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        return false;
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (!isFunctional() || reason != FleetDespawnReason.REACHED_DESTINATION) {
            return;
        }

        RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
        if (route == null || !(route.getCustom() instanceof PatrolFleetData)) {
            return;
        }

        PatrolFleetData custom = (PatrolFleetData) route.getCustom();
        if (custom.spawnFP > 0) {
            float fraction = fleet.getFleetPoints() / custom.spawnFP;
            returningPatrolValue += fraction;
        }
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {
        if (route == null || !(route.getCustom() instanceof PatrolFleetData)) {
            return null;
        }

        PatrolFleetData custom = (PatrolFleetData) route.getCustom();
        PatrolType type = custom.type;
        Random random = route.getRandom();

        float combat = 0f;
        float tanker = 0f;
        float freighter = 0f;
        String fleetType = type.getFleetType();
        switch (type) {
            case FAST:
                combat = Math.round(3f + random.nextFloat() * 2f) * 5f;
                break;
            case COMBAT:
                combat = Math.round(6f + random.nextFloat() * 4f) * 5f;
                tanker = Math.round(random.nextFloat()) * 2f;
                break;
            case HEAVY:
                combat = Math.round(10f + random.nextFloat() * 6f) * 5f;
                tanker = Math.round(random.nextFloat()) * 4f;
                freighter = Math.round(random.nextFloat()) * 4f;
                break;
        }

        FleetParamsV3 params = new FleetParamsV3(
                market,
                null,
                CONSORTIUM_FLEET_FACTION_ID,
                route.getQualityOverride(),
                fleetType,
                combat,
                freighter,
                tanker,
                0f,
                0f,
                0f,
                0f
        );
        params.timestamp = route.getTimestamp();
        params.random = random;
        params.qualityOverride = QUALITY_OVERRIDE;
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL;
        params.officerLevelBonus = OFFICER_LEVEL_BONUS;
        params.officerNumberMult = OFFICER_NUMBER_MULT;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        fleet.setFaction(market.getFactionId(), true);
        fleet.setNoFactionInName(true);
        fleet.addEventListener(this);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);

        if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
        }

        assignCommanderRank(fleet, type);

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing(random.nextFloat() * 360f);
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);
        fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

        if (custom.spawnFP <= 0) {
            custom.spawnFP = fleet.getFleetPoints();
        }

        return fleet;
    }

    public String getRouteSourceId() {
        return getMarket().getId() + "_" + ROUTE_SUFFIX;
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    private void applyDefenseStats() {
        String desc = getNameForModifier();
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(getModId(), LIGHT_PATROLS, desc);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(getModId(), MEDIUM_PATROLS, desc);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(getModId(), HEAVY_PATROLS, desc);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(getModId(), GROUND_DEFENSE_BONUS, desc);
    }

    private void unapplyDefenseStats() {
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).unmodifyFlat(getModId());
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).unmodifyFlat(getModId());
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).unmodifyFlat(getModId());
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodifyFlat(getModId());
    }

    private void spawnPatrolRoute() {
        int light = getCount(PatrolType.FAST);
        int medium = getCount(PatrolType.COMBAT);
        int heavy = getCount(PatrolType.HEAVY);

        WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<PatrolType>();
        addPatrolType(picker, PatrolType.HEAVY, getMaxPatrols(PatrolType.HEAVY), heavy);
        addPatrolType(picker, PatrolType.COMBAT, getMaxPatrols(PatrolType.COMBAT), medium);
        addPatrolType(picker, PatrolType.FAST, getMaxPatrols(PatrolType.FAST), light);

        if (picker.isEmpty()) {
            return;
        }

        PatrolType type = picker.pick();
        PatrolFleetData custom = new PatrolFleetData(type);
        OptionalFleetData extra = new OptionalFleetData(market);
        extra.fleetType = type.getFleetType();

        RouteData route = RouteManager.getInstance().addRoute(getRouteSourceId(), market, Misc.genRandomSeed(), extra,
                                                              this, custom);
        float patrolDays = 35f + (float) Math.random() * 10f;
        route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
    }

    private static void addPatrolType(WeightedRandomPicker<PatrolType> picker, PatrolType type, int max, int current) {
        int weight = max - current;
        if (weight > 0) {
            picker.add(type, weight);
        }
    }

    private static void assignCommanderRank(CampaignFleetAPI fleet, PatrolType type) {
        String rankId = Ranks.SPACE_COMMANDER;
        switch (type) {
            case FAST:
                rankId = Ranks.SPACE_LIEUTENANT;
                break;
            case COMBAT:
                rankId = Ranks.SPACE_COMMANDER;
                break;
            case HEAVY:
                rankId = Ranks.SPACE_CAPTAIN;
                break;
        }

        fleet.getCommander().setPostId(Ranks.POST_PATROL_COMMANDER);
        fleet.getCommander().setRankId(rankId);
    }
}