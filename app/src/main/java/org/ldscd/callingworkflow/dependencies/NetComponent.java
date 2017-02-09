package org.ldscd.callingworkflow.dependencies;

import dagger.Component;
import org.ldscd.callingworkflow.display.CallingDetailActivity;
import org.ldscd.callingworkflow.display.CallingDetailSearchFragment;
import org.ldscd.callingworkflow.display.SettingsActivity;

import javax.inject.Singleton;

@Singleton
@Component(modules = NetModule.class)
public interface NetComponent {
    void inject(SettingsActivity activity);
    void inject(CallingDetailActivity activity);
    void inject(CallingDetailSearchFragment activity);
}
