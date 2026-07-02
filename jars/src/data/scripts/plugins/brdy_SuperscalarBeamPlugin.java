package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;

import static data.scripts.weapons.brdy_SueprscalarEveryFrame.colorBlend;

public class brdy_SuperscalarBeamPlugin extends BaseEveryFrameCombatPlugin {

    private static final Color GLOWCOLOR = new Color(150, 255, 25, 255);
    private static final Color GLOWCOLOR2 = new Color(165, 255, 190, 255);
    private static final Color GLOWCOLOR2_ALT = new Color(110, 255, 230, 255);
    private static final Color GLOWCOLOR_ALT = new Color(100, 255, 150, 255);

    private CombatEngineAPI engine;
    private float flash;
    private float glowSize;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    @Override
    public void renderInWorldCoords(ViewportAPI viewport) {
        if (engine == null) {
            return;
        }

        List<BeamAPI> beams = engine.getBeams();
        int size = beams.size();
        for (int i = 0; i < size; i++) {
            BeamAPI beam = beams.get(i);
            String spec = beam.getWeapon().getId();

            if (spec.contentEquals("brdy_superscalarbeam")) {
                ShipAPI ship = beam.getWeapon().getShip();
                float powerLevel = 1f * ship.getFluxTracker().getFluxLevel() + 0.5f;

                SpriteAPI glow = Global.getSettings().getSprite("glow", "brdy_superscalarglow");
                if (!engine.isPaused()) {
                    flash = (Math.random() > 0.5) ? 0.75f : 0.5f;
                    flash = Math.max(flash * powerLevel, 1f);
                }
                glow.setAlphaMult(flash);
                glow.setAdditiveBlend();
                glow.setAngle((float) Math.random() * 360f);
                if (!engine.isPaused()) {
                    glowSize = MathUtils.getRandomNumberInRange(350f, 700f) * powerLevel;
                }
                glow.setSize(glowSize, glowSize);
                glow.setColor(colorBlend(GLOWCOLOR, GLOWCOLOR_ALT, powerLevel - 0.5f));
                glow.renderAtCenter(beam.getFrom().x, beam.getFrom().y);

                glow = Global.getSettings().getSprite("glow", "brdy_superscalarglow2");
                glow.setAlphaMult(1f);
                glow.setAdditiveBlend();
                glow.setColor(colorBlend(GLOWCOLOR2, GLOWCOLOR2_ALT, powerLevel - 0.5f));
                glow.setAngle((float) Math.random() * 360f);
                float gSize = MathUtils.getRandomNumberInRange(100f, 150f) * powerLevel;
                glow.setSize(gSize, gSize);
                glow.renderAtCenter(beam.getFrom().x, beam.getFrom().y);
                glow.setAngle((float) Math.random() * 360f);
                gSize = MathUtils.getRandomNumberInRange(50f, 75f) * powerLevel;
                glow.setSize(gSize, gSize);
                glow.renderAtCenter(beam.getFrom().x, beam.getFrom().y);
            }
        }
    }
}
