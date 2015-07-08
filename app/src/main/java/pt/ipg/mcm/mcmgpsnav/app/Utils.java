package pt.ipg.mcm.mcmgpsnav.app;

public class Utils {
    public static float normalizeAngle(float angle) {
        if (angle > 180) {
            return 360 - angle;
        } else if (angle < -180) {
            return 360 + angle;
        } else {
            return angle;
        }
    }
}