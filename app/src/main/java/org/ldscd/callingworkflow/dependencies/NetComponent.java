package org.ldscd.callingworkflow.dependencies;

import org.ldscd.callingworkflow.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = NetModule.class)
public interface NetComponent {
    void inject(SettingsActivity activity);
}
