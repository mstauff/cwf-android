package org.ldscd.callingworkflow.dependencies;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import dagger.Module;
import dagger.Provides;

import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.services.GoogleDataServiceImpl;
import org.ldscd.callingworkflow.web.LocalFileResources;
import org.ldscd.callingworkflow.web.IWebResources;

import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.MemberData;
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
    IWebResources providesWebResources(Context context, RequestQueue requestQueue, SharedPreferences preferences) {
        return new LocalFileResources(context); //new WebResources(context, requestQueue, preferences);
    }

    @Provides
    @Singleton
    GoogleDataService providesGoogleDataServices() {
        return new GoogleDataServiceImpl();
    }

    @Provides
    @Singleton
    CallingData providesCallingData(IWebResources webResources, GoogleDataService googleDataService) {
        return new CallingData(webResources, googleDataService);
    }

    @Provides
    @Singleton
    MemberData providesMemberData(IWebResources webResources) {
        return new MemberData(webResources);
    }
}