package com.mydms.dms.core.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * @author: zhousf
 */
public class Migration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if(oldVersion == 1){
            RealmObjectSchema table = schema.get("User");
            if(null != table && table.hasField("address")){
                table.removeField("address");
            }
            oldVersion ++;
        }
    }
}
