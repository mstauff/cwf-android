package org.ldscd.callingworkflow.dependencies;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import dagger.Module;
import dagger.Provides;
import org.ldscd.callingworkflow.services.localFileResources;
import org.ldscd.callingworkflow.web.WebResources;

import javax.inject.Singleton;

@Module(includes = {ApplicationContextModule.class, StorageModule.class})
public class NetModule {

    @Provides
    @Singleton
    RequestQueue providesRequestQueue(Context applicationContext) {
        return Volley.newRequestQueue(applicationContext);
    }

    @Provides
    @Singleton
    WebResources providesWebResources(Context applicationContext, RequestQueue requestQueue, SharedPreferences preferences) {
        return new WebResources(applicationContext, requestQueue, preferences);
    }

    @Provides
    @Singleton
    localFileResources providesLocalFileResources(Context context) {
        return new localFileResources(context);
    }
}