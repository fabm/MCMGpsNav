package pt.ipg.mcm.app.tests;

import android.content.Context;
import android.view.View;
import org.robolectric.Robolectric;
import pt.ipg.mcm.mcmgpsnav.app.MainActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public class MyActivity extends MainActivity {

    private Map<Integer, View> resources;

    public void onCreate() {
        try {
            Method mt = MainActivity.class.getDeclaredMethod("initLocationListener");
            mt.setAccessible(true);
            mt.invoke(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setResources(Map<Integer, View> resources) {
        this.resources = resources;
    }

    @Override
    public View findViewById(int id) {
        return resources.get(id);
    }

    @Override
    public Object getSystemService(String name) {
        return Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void setContentView(int layoutResID) {

    }
}
