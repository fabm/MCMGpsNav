package pt.ipg.mcm.mcmgpsnav.gen;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Generator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "pt.ipg.mcm.mcmgpsnav.app.db.gen");


        Entity coordAndCompass = schema.addEntity("CoordAndCompass");
        coordAndCompass.addIdProperty();
        coordAndCompass.addDoubleProperty("degrees").notNull();
        coordAndCompass.addDoubleProperty("longitude").notNull();
        coordAndCompass.addDoubleProperty("latitude").notNull();
        coordAndCompass.addFloatProperty("degreesToNext");
        coordAndCompass.addDoubleProperty("nextLon");
        coordAndCompass.addDoubleProperty("nextLat");
        coordAndCompass.addFloatProperty("distanceToNext");
        coordAndCompass.addDateProperty("date").notNull();

        new DaoGenerator().generateAll(schema, args[0]);
    }
}
