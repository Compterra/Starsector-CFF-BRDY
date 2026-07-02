package data.scripts.weapons;

public class SolenoidQuenchAnimation extends BaseAnimateOnFireEffect {

    public SolenoidQuenchAnimation() {
        super(); // doesn't actually do anything
        setFramesPerSecond(15);
        pauseOnFrame(13, 4);
    }
}
