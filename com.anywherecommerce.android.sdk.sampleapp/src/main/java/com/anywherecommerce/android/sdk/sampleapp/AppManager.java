package com.anywherecommerce.android.sdk.sampleapp;

import android.app.Activity;
import android.app.Application;

import com.anywherecommerce.android.sdk.SDKManager;
import com.anywherecommerce.android.sdk.endpoints.worldnet.WorldnetTerminal;

import java.lang.reflect.Field;
import java.util.Map;

public class AppManager extends Application {

    public void onCreate()
    {
        super.onCreate();

        // The first step should always be to initialize the SDK.
        // You can optionally specify the terminal type.  The default is a generic AnyPay terminal.
        SDKManager.initialize(this, new WorldnetTerminal("2994001", "password"));
    }
}
