package org.ldscd.callingworkflow.display;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

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
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                handleUncaughtException(ex);
            }
        });

    }

    public NetComponent getNetComponent() {
        return netComponent;
    }

    public void handleUncaughtException(Throwable e)
    {
        /* Any uncaught error in the application will logged and an option from the
           devices interface will be prompted to send an email with the stacktrace to our dev
           google email account.
         */
        String stackTrace = Log.getStackTraceString(e);
        String message = e.getMessage();
        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"lds.community.dev@gmail.com"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "CWF Crash log file");
        intent.putExtra (Intent.EXTRA_TEXT, message + "\n\n\n" + stackTrace);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity(intent);
    }
}