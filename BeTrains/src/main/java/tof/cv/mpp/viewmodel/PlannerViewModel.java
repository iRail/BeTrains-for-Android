package tof.cv.mpp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;

import tof.cv.mpp.bo.Connections;
import tof.cv.mpp.bo.TrainComposition;
import tof.cv.mpp.repository.ConnectionRepository;

public class PlannerViewModel extends AndroidViewModel {

    private final ConnectionRepository repository;

    private final MutableLiveData<Connections> connections = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Cache compositions for the UI to observe or request
    private final MutableLiveData<Map<String, TrainComposition.Composition.Segments.Segment.SegmentComposition>> compositions = new MutableLiveData<>(
            new HashMap<>());

    public PlannerViewModel(@NonNull Application application) {
        super(application);
        repository = new ConnectionRepository(application);
    }

    public LiveData<Connections> getConnections() {
        return connections;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Map<String, TrainComposition.Composition.Segments.Segment.SegmentComposition>> getCompositions() {
        return compositions;
    }

    public void search(String url) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        compositions.setValue(new HashMap<>());
        repository.searchConnections(url, (e, result) -> {
            isLoading.setValue(false);
            if (e != null) {
                errorMessage.setValue(e.toString());
            } else if (result == null) {
                errorMessage.setValue("No results found");
            } else {
                connections.setValue(result);
            }
        });
    }

    public void loadComposition(String vehicleId) {
        Map<String, TrainComposition.Composition.Segments.Segment.SegmentComposition> currentMap = compositions
                .getValue();
        if (currentMap != null && currentMap.containsKey(vehicleId)) {
            return; // Already loaded
        }

        repository.getTrainComposition(vehicleId, (e, composition) -> {
            if (composition != null) {
                Map<String, TrainComposition.Composition.Segments.Segment.SegmentComposition> map = compositions
                        .getValue();
                if (map == null)
                    map = new HashMap<>();
                map.put(vehicleId, composition);
                compositions.setValue(map); // Trigger observers
            }
        });
    }
}
