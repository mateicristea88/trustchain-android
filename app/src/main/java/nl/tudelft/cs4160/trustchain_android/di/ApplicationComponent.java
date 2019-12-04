package nl.tudelft.cs4160.trustchain_android.di;

import android.content.Context;

import dagger.BindsInstance;
import dagger.Component;
import nl.tudelft.cs4160.trustchain_android.network.NetworkConnectionService;
import nl.tudelft.cs4160.trustchain_android.ui.chainexplorer.ChainExplorerActivity;
import nl.tudelft.cs4160.trustchain_android.ui.main.OverviewConnectionsActivity;

@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        ApplicationComponent create(@BindsInstance Context context);
    }

    void inject(OverviewConnectionsActivity activity);
    void inject(ChainExplorerActivity activity);
    void inject(NetworkConnectionService service);
}
