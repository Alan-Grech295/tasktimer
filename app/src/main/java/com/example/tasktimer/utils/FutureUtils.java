package com.example.tasktimer.utils;

import com.example.tasktimer.model.Task;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Date;

public class FutureUtils {
    public interface FutureCallback<T>{
        void run(T result);
    }
    public static <T> void addListener(ListenableFuture<T> future, FutureCallback<T> callback){
        Futures.addCallback(future, new com.google.common.util.concurrent.FutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                callback.run(result);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, MoreExecutors.directExecutor());
    }
}
