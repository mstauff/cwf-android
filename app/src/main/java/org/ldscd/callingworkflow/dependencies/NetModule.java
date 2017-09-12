package org.ldscd.callingworkflow.dependencies;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import dagger.Module;
import dagger.Provides;

import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.services.GoogleDataService;
import org.ldscd.callingworkflow.services.GoogleDataServiceImpl;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.DataManagerImpl.DataManagerImpl;
import org.ldscd.callingworkflow.web.LocalFileResources;
import org.ldscd.callingworkflow.web.IWebResources;

import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.MemberData;

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
        return new LocalFileResources(context, requestQueue); //new WebResources(context, requestQueue, preferences);
    }

    @Provides
    @Singleton
    GoogleDataService providesGoogleDataServices() {
        return new GoogleDataServiceImpl();
    }

    @Provides
    @Singleton
    CallingData providesCallingData(IWebResources webResources, GoogleDataService googleDataService, MemberData memberData, PermissionManager permissionManager) {
        return new CallingData(webResources, googleDataService, memberData, permissionManager);
    }

    @Provides
    @Singleton
    MemberData providesMemberData(IWebResources webResources) {
        return new MemberData(webResources);
    }

    @Provides
    @Singleton
    DataManager providesDataManagerService(CallingData callingData, MemberData memberData, GoogleDataService googleDataService, IWebResources webResources, PermissionManager permissionManager) {
        return new DataManagerImpl(callingData, memberData, googleDataService, webResources, permissionManager);
    }

    @Provides
    @Singleton
    PermissionManager providesPermissionManagerService() {
        return new PermissionManager();
    }
}