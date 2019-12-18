package nl.tudelft.cs4160.trustchain_android;

import android.app.Application;

import nl.tudelft.cs4160.trustchain_android.di.ApplicationComponent;
import nl.tudelft.cs4160.trustchain_android.di.DaggerApplicationComponent;

public class TrustchainApplication extends Application {
    public final ApplicationComponent appComponent = DaggerApplicationComponent.factory().create(this);
}
