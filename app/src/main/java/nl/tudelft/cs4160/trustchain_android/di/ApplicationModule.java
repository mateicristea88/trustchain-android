package nl.tudelft.cs4160.trustchain_android.di;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import nl.tudelft.cs4160.trustchain_android.network.Network;
import nl.tudelft.cs4160.trustchain_android.storage.database.AppDatabase;
import nl.tudelft.cs4160.trustchain_android.storage.database.dao.BlockDao;
import nl.tudelft.cs4160.trustchain_android.storage.database.dao.PeerDao;
import nl.tudelft.cs4160.trustchain_android.storage.repository.BlockRepository;
import nl.tudelft.cs4160.trustchain_android.storage.repository.PeerRepository;

@Module
public class ApplicationModule {
    @Provides
    public AppDatabase provideDatabase(Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public PeerDao providePeerDao(AppDatabase database) {
        return database.peerDao();
    }

    @Provides
    public BlockDao provideBlockDao(AppDatabase database) {
        return database.blockDao();
    }
}
