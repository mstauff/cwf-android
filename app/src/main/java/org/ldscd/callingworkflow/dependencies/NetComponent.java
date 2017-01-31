package org.ldscd.callingworkflow.dependencies;

import dagger.Component;
import org.ldscd.callingworkflow.CallingDetailActivity;
import org.ldscd.callingworkflow.SettingsActivity;

import javax.inject.Singleton;

@Singleton
@Component(modules = NetModule.class)
public interface NetComponent {
    void inject(SettingsActivity activity);
    void inject(CallingDetailActivity activity);
}
