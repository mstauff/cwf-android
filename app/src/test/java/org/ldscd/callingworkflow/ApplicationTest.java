package org.ldscd.callingworkflow;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import com.google.gson.Gson;
import org.ldscd.callingworkflow.model.*;
import org.junit.Test;
import com.google.gson.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Mock
    Context mMockContext;

    @Test
    public void checkProblem() {
        Calling calling = new Calling();

    }
}