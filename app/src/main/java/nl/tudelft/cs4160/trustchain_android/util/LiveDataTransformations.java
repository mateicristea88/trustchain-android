package nl.tudelft.cs4160.trustchain_android.util;

import android.os.AsyncTask;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

public class LiveDataTransformations {
    private LiveDataTransformations() {}

    @MainThread
    public static <X, Y> LiveData<Y> mapAsync(
            @NonNull LiveData<X> source,
            @NonNull final Function<X, Y> mapFunction) {
        final MediatorLiveData<Y> result = new MediatorLiveData<>();
        result.addSource(source, x -> AsyncTask.execute(() ->
                result.postValue(mapFunction.apply(x))));
        return result;
    }
}
