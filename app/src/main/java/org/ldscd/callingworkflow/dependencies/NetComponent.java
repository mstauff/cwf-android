package org.ldscd.callingworkflow.dependencies;

import dagger.Component;
import org.ldscd.callingworkflow.display.CallingDetailActivity;
import org.ldscd.callingworkflow.display.CallingDetailSearchFragment;
import org.ldscd.callingworkflow.display.CreateCallingActivity;
import org.ldscd.callingworkflow.display.IndividualInformationFragment;
import org.ldscd.callingworkflow.display.SettingsActivity;
import org.ldscd.callingworkflow.display.CallingListActivity;
import org.ldscd.callingworkflow.display.OrgListActivity;
import org.ldscd.callingworkflow.display.SplashActivity;

import javax.inject.Singleton;

@Singleton
@Component(modules = NetModule.class)
public interface NetComponent {
    void inject(SplashActivity activity);
    void inject(OrgListActivity activity);
    void inject(CallingListActivity activity);
    void inject(SettingsActivity activity);
    void inject(CallingDetailActivity activity);
    void inject(CallingDetailSearchFragment activity);
    void inject(IndividualInformationFragment activity);
    void inject(CreateCallingActivity activity);
}
