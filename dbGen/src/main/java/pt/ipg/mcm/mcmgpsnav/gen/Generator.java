package pt.ipg.mcm.mcmgpsnav.gen;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Generator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "pt.ipg.mcm.mcmgpsnav.app.db.gen");


        Entity note = schema.addEntity("CoordAndCompass");
        note.addIdProperty();
        note.addDoubleProperty("degrees").notNull();
        note.addDoubleProperty("longitude").notNull();
        note.addDoubleProperty("latitude").notNull();
        note.addDateProperty("date").notNull();

        new DaoGenerator().generateAll(schema, args[0]);
    }
}
