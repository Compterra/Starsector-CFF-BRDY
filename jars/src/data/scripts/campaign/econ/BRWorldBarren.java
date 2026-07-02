package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.WorldFarming;

public class BRWorldBarren extends WorldFarming {

    private static final float WORLD_BARREN_FARMING_MULT = 0.025f;
    private static final float WORLD_BARREN_MACHINERY_MULT = 0.001f;

    public BRWorldBarren() {
        super(WORLD_BARREN_FARMING_MULT, WORLD_BARREN_MACHINERY_MULT);
    }
}
