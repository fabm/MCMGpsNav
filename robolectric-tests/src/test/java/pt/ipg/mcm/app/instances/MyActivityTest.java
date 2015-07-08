package pt.ipg.mcm.app.instances;

import android.app.Activity;
import android.util.SparseArray;
import android.view.View;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;

import java.util.Map;

@Implements(Activity.class)
public class MyActivityTest extends ShadowActivity {
    private SparseArray<View> resources;

    public <T extends Activity> T getActivity(Class<T> clazz) {
        return (T) realActivity;
    }

    public void setResources(SparseArray<View> resources) {
        this.resources = resources;
    }

    @Override
    public View findViewById(int id) {
        return resources.get(id);
    }


    @Override
    public boolean setThemeFromManifest() {
        return false;
    }

}
