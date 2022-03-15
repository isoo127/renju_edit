package com.renju_note.isoo;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().schemaVersion(2).allowWritesOnUiThread(true).build();
        Realm.setDefaultConfiguration(config);
        try {
            Realm realm = Realm.getDefaultInstance();
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(config);
            Realm realm = Realm.getDefaultInstance();
        }

    }
}
