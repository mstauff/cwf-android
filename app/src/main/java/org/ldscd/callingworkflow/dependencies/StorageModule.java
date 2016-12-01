package org.ldscd.callingworkflow.dependencies;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = ApplicationContextModule.class)
public class StorageModule {
    private static final String prefsName = "CWFPrefs";

    @Provides
    @Singleton
    SharedPreferences providesSharedPreferences(Context applicationContext) {
        return applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
    }

}
