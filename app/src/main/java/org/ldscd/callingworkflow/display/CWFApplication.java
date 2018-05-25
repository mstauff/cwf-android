package org.ldscd.callingworkflow.display;

import android.app.Application;

import org.ldscd.callingworkflow.dependencies.ApplicationContextModule;
import org.ldscd.callingworkflow.dependencies.DaggerNetComponent;
import org.ldscd.callingworkflow.dependencies.NetComponent;

public class CWFApplication extends Application {
    private NetComponent netComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        netComponent = DaggerNetComponent.builder()
                .applicationContextModule(new ApplicationContextModule(this))
                .build();
    }

    public NetComponent getNetComponent() {
        return netComponent;
    }
}
