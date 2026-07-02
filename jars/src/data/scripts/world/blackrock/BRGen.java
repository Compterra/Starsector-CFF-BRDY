package data.scripts.world.blackrock;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import data.scripts.world.blackrock.gneiss.Gneiss;
import data.scripts.world.blackrock.rama.Rama;

public class BRGen implements SectorGeneratorPlugin {

    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI hegemony = sector.getFaction(Factions.HEGEMONY);
        FactionAPI tritachyon = sector.getFaction(Factions.TRITACHYON);
        FactionAPI pirates = sector.getFaction(Factions.PIRATES);
        FactionAPI kol = sector.getFaction(Factions.KOL);
        FactionAPI church = sector.getFaction(Factions.LUDDIC_CHURCH);
        FactionAPI path = sector.getFaction(Factions.LUDDIC_PATH);
        FactionAPI blackrock = sector.getFaction("blackrock_driveyards");

        blackrock.setRelationship(path.getId(), RepLevel.VENGEFUL);
        blackrock.setRelationship(hegemony.getId(), RepLevel.HOSTILE);
        blackrock.setRelationship(pirates.getId(), RepLevel.HOSTILE);
        blackrock.setRelationship(tritachyon.getId(), RepLevel.HOSTILE);
        blackrock.setRelationship(church.getId(), RepLevel.HOSTILE);
        blackrock.setRelationship(kol.getId(), RepLevel.HOSTILE);
    }

    @Override
    public void generate(SectorAPI sector) {
        SharedData.getData().getPersonBountyEventData().addParticipatingFaction("blackrock_driveyards");

        initFactionRelationships(sector);

        new Gneiss().generate(sector);
        new Rama().generate(sector);
    }
}
