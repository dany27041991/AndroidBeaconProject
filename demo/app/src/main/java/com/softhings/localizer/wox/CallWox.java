package com.softhings.localizer.wox;

import android.content.Context;

import com.lwoxLib.parameters.LocationParams;
import com.lwoxLib.role.SourceCapability;
import com.lwoxLib.role.factory.RoleFactory;
import com.lwoxLib.topic.RoleTopicPair;
import com.lwoxLib.topic.Topic;
import com.lwoxLib.topic.WoxProfile;

/**
 * Created by danilogiovannico on 20/06/17.
 */

public class CallWox {
    SourceCapability iDSender;
    WoxProfile profile;


    public CallWox(Context context) {
        Topic phoneCall = new Topic(2, LocationParams.LOCATION_SELF);

        iDSender = RoleFactory.getSensorCapabilityRole(context);
        profile = WoxProfile.getInstance();
        profile.add(new RoleTopicPair<SourceCapability>(iDSender, phoneCall));
    }

    public void sendToWox (String value) {
        iDSender.setTopicActualValue(value);
    }

}
