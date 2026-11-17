package com.example.py_damb2026ii.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.py_damb2026ii.network.IncidentEntry;
import com.example.py_damb2026ii.repository.IncidentRepository;

import java.util.List;

public class IncidentListViewModel extends AndroidViewModel {

    public enum State {
        LOADING, SUCCESS, ERROR, EMPTY, MODO_CACHE, AUTH_EXPIRED
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(State.LOADING);
    private final MutableLiveData<List<IncidentEntry>> incidentes = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();

    public IncidentListViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<State> getState() {
        return state;
    }

    public LiveData<List<IncidentEntry>> getIncidentes() {
        return incidentes;
    }

    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    public void cargarIncidentes(String jwt, String userCacheId) {
        state.setValue(State.LOADING);
        IncidentRepository.getInstance().cargarIncidentes(
                getApplication(),
                jwt,
                userCacheId,
                new IncidentRepository.RepositoryCallback<List<IncidentEntry>>() {
                    @Override
                    public void onDataLoaded(List<IncidentEntry> data, boolean isFromCache, String errorMessage) {
                        incidentes.postValue(data);
                        if (data == null || data.isEmpty()) {
                            state.postValue(State.EMPTY);
                        } else if (isFromCache) {
                            state.postValue(State.MODO_CACHE);
                        } else {
                            state.postValue(State.SUCCESS);
                        }
                        if (errorMessage != null) {
                            mensajeError.postValue(errorMessage);
                        }
                    }

                    @Override
                    public void onHttpError(int code) {
                        if (code == 401) {
                            state.postValue(State.MODO_CACHE);
                        } else {
                            state.postValue(State.ERROR);
                            mensajeError.postValue("HTTP " + code);
                        }
                    }
                }
        );
    }
}
