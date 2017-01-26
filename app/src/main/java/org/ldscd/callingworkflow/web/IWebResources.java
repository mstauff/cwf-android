package org.ldscd.callingworkflow.web;

import com.android.volley.Response;
import dagger.Component;
import org.json.JSONObject;
import org.ldscd.callingworkflow.model.Member;
import org.ldscd.callingworkflow.model.Org;
import org.ldscd.callingworkflow.services.localFileResources;

import javax.inject.Singleton;
import java.util.List;

/**
 * Interface to WebResources.  Only because we are implementing the same interface on localFileResources.
 */
@Singleton
@Component(modules={localFileResources.class})
public interface IWebResources {
    void getUserInfo(Response.Listener<JSONObject> userCallback);

    void getOrgs(Response.Listener<List<Org>> orgsCallback);

    void getWardList(Response.Listener<List<Member>> wardCallback);
}