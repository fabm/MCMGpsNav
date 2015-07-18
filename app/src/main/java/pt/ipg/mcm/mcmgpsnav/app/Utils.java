package pt.ipg.mcm.mcmgpsnav.app;


public class Utils {
    public static float normalizeAngle(float relToNorth ,float angle) {
        float relAngle = angle - relToNorth;
        if (relAngle > 180) {
            return 360 - relAngle;
        } else if (relAngle < -180) {
            return 360 + relAngle;
        } else {
            return relAngle;
        }
    }

}