package pt.ipg.mcm.mcmgpsnav.app.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import pt.ipg.mcm.mcmgpsnav.app.R;
import pt.ipg.mcm.mcmgpsnav.app.db.FluentBD;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.CoordAndCompass;

import java.util.List;

public class DataBaseListActivity extends Activity {

    private FluentBD fluentBD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_base_list);

        fluentBD = new FluentBD();

        fluentBD.init(this);
        final List<CoordAndCompass> coordAndCompasses = fluentBD.getDaoSession().getCoordAndCompassDao().loadAll();
        CoordArrayAdapter adapter = new CoordArrayAdapter(this, coordAndCompasses);

        ListView listView = (ListView) findViewById(R.id.lvDegreesAndCoordinates);
        listView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_data_base_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
