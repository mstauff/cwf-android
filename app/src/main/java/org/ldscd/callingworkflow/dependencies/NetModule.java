package org.ldscd.callingworkflow.dependencies;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import dagger.Module;
import dagger.Provides;

import org.ldscd.callingworkflow.BuildConfig;
import org.ldscd.callingworkflow.model.permissions.PermissionManager;
import org.ldscd.callingworkflow.services.GoogleDriveService;
import org.ldscd.callingworkflow.services.GoogleDriveServiceImpl;
import org.ldscd.callingworkflow.web.DataManager;
import org.ldscd.callingworkflow.web.DataManagerImpl.DataManagerImpl;
import org.ldscd.callingworkflow.web.LocalFileResources;
import org.ldscd.callingworkflow.web.IWebResources;

import org.ldscd.callingworkflow.web.CallingData;
import org.ldscd.callingworkflow.web.MemberData;
import org.ldscd.callingworkflow.web.WebResources;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.inject.Singleton;

@Module(includes = {ApplicationContextModule.class, StorageModule.class})
public class NetModule {

    @Provides
    @Singleton
    CookieManager providesCookieManager() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        return cookieManager;
    }

    @Provides
    @Singleton
    RequestQueue providesRequestQueue(Context applicationContext, CookieManager cookieManager) {
        CookieHandler.setDefault(cookieManager);
        return Volley.newRequestQueue(applicationContext, new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpURLConnection connection = super.createConnection(url);
                connection.setInstanceFollowRedirects(false);

                return connection;
            }
        });
    }

    @Provides
    @Singleton
    IWebResources providesWebResources(Context context, RequestQueue requestQueue, SharedPreferences preferences, CookieManager cookieManager) {
        return BuildConfig.useLDSOrgData ? new WebResources(context, requestQueue, preferences, cookieManager) : new LocalFileResources(context, requestQueue);
    }

    @Provides
    @Singleton
    GoogleDriveService providesGoogleDataServices() {
        return new GoogleDriveServiceImpl();
    }

    @Provides
    @Singleton
    CallingData providesCallingData(IWebResources webResources, GoogleDriveService googleDataService, MemberData memberData, PermissionManager permissionManager) {
        return new CallingData(webResources, googleDataService, memberData, permissionManager);
    }

    @Provides
    @Singleton
    MemberData providesMemberData(Context applicationContext, IWebResources webResources) {
        return new MemberData(applicationContext, webResources);
    }

    @Provides
    @Singleton
    DataManager providesDataManagerService(CallingData callingData, MemberData memberData, GoogleDriveService googleDataService, IWebResources webResources, PermissionManager permissionManager) {
        return new DataManagerImpl(callingData, memberData, googleDataService, webResources, permissionManager);
    }

    @Provides
    @Singleton
    PermissionManager providesPermissionManagerService() {
        return new PermissionManager();
    }
}