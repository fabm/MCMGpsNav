package pt.ipg.mcm.mcmgpsnav.app;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@Retention(RetentionPolicy.SOURCE)
@IntDef({GpsNavStatus.STOP, GpsNavStatus.MOVE, GpsNavStatus.ROTATE})
public @interface GpsNavStatus {
    int STOP = 'S';
    int MOVE = 'M';
    int ROTATE = 'R';
}