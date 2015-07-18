package pt.ipg.mcm.mcmgpsnav.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.CoordAndCompass;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.DaoMaster;
import pt.ipg.mcm.mcmgpsnav.app.db.gen.DaoSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FluentBD {

    private SQLiteDatabase db;
    private DaoSession daoSession;

    public FluentBD init(Context context) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "compass", null);
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        return this;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public FluentBD deleteAllInTx(final Class<?> entityClass) {
        getDaoSession().runInTx(new Runnable() {
            @Override
            public void run() {
                getDaoSession().deleteAll(entityClass);
            }
        });
        return this;
    }

    public String serilizeJson() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        final JSONArray coords = new JSONArray();


        List<CoordAndCompass> all = getDaoSession().getCoordAndCompassDao().loadAll();

        for (CoordAndCompass coordAndCompass : all) {
            final JSONObject coord = new JSONObject();
            coord.put("lon", coordAndCompass.getLongitude());
            coord.put("lat", coordAndCompass.getLatitude());
            coord.put("degrees", coordAndCompass.getDegrees());
            final JSONObject dateTime = new JSONObject();
            final Calendar date = Calendar.getInstance();
            date.setTime(coordAndCompass.getDate());
            dateTime.put("ano", date.get(Calendar.YEAR));
            dateTime.put("mes", date.get(Calendar.MONTH));
            dateTime.put("dia", date.get(Calendar.DAY_OF_MONTH));
            dateTime.put("hora", date.get(Calendar.HOUR_OF_DAY));
            dateTime.put("minuto", date.get(Calendar.MINUTE));
            dateTime.put("segundo", date.get(Calendar.SECOND));
            coord.put("dateTime", dateTime);
            coords.put(coord);
        }

        jsonObject.put("coords", coords);
        return jsonObject.toString();
    }


    public String serializeXml() {
        List<CoordAndCompass> loaded = getDaoSession().loadAll(CoordAndCompass.class);
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "coords");
            for (CoordAndCompass msg : loaded) {
                serializer.startTag("", "coord");
                serializer.attribute("", "data", msg.getDate().toString());

                serializer.startTag("", "lat");
                serializer.text("" + msg.getLatitude());
                serializer.endTag("", "lat");


                serializer.startTag("", "lon");
                serializer.text("" + msg.getLongitude());
                serializer.endTag("", "lon");


                serializer.startTag("", "degree");
                serializer.text("" + msg.getDegrees());
                serializer.endTag("", "degree");
                serializer.endTag("", "coord");

            }
            serializer.endTag("", "coords");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String serializeToJson() {
        ObjectMapper mapper = new ObjectMapper();
        List<CoordAndCompass> loaded = getDaoSession().loadAll(CoordAndCompass.class);

        try {
            return mapper.writeValueAsString(loaded);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void close() {
        db.close();
    }


    public void save() {
        AsyncTask<Void,Void,Void> at = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "test.txt");
                try {
                    file.createNewFile();
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
                    outputStreamWriter.write(serializeToJson());
                    outputStreamWriter.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
                return null;
            }
        };

        at.execute();

        // Get the directory for the user's public pictures directory.
    }
}
