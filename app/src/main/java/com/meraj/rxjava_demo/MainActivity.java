package com.meraj.rxjava_demo;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.AsyncEmitter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Subscription sensorChangedSubscription;

    private final int MAX_RETRIES = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ex - 1
        Log.d("RxJava", "Example 1");
        Observable.just(1, 2, 3, 4, 5, 6)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return (integer % 2) == 1;
                    }
                })          /* argument, return */
                .map(new Func1<Integer, Double>() {
                    @Override
                    public Double call(Integer integer) {
                        return Math.sqrt(integer);
                    }
                })
                .subscribe(new Subscriber<Double>() {
                    @Override
                    public void onCompleted() {
                        Log.d("RxJava", "Sequence complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RxJava", "Error found");
                    }

                    @Override
                    public void onNext(Double val) {
                        Log.d("RxJava", "Event: " + val);
                    }
                });

        // ex -2: using Java 8
        Log.d("RxJava", "Example 2");
        Observable.just(1, 2, 3, 4, 5, 6)
                .subscribeOn(Schedulers.computation())
                .filter((i) -> {
                    Log.d("RxJava", "filter thread enter: " + Utils.getThreadName());
                    return (i % 2) == 1;
                })
                .map((i) -> Math.sqrt(i))
                .observeOn(Schedulers.newThread())
                .subscribe(
                        (val) -> {
                            Log.d("RxJava", "onNext thread enter: " + Utils.getThreadName());
                            Log.d("RxJava", "Event: " + val);
                        },
                        (t) -> {
                        },
                        () -> {
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // ex -3:
        Log.d("RxJava", "Example 3");
        Log.d("RxJava", "Current Thread - " + Utils.getThreadName());

        Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5, 6);
        observable
                .observeOn(Schedulers.newThread())
                .subscribe(
                        // onNext
                        (i) -> {
                            Log.d("RxJava", "onNext thread enter: " + Utils.getThreadName());
                            Log.d("RxJava", "Val: " + i);
                            Log.d("RxJava", "onNext thread exit: " + Utils.getThreadName());
                        },
                        // onError
                        (t) -> {
                            t.printStackTrace();
                        },
                        // onCompleted
                        () -> {
                            Log.d("RxJava", "Completed Sequence");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }


        // ex -4:
        Log.d("RxJava", "Example 4");
        Log.d("RxJava", "Current Thread - " + Utils.getThreadName());

        Observable<Integer> observable1 = Observable.just(1, 2, 3, 4, 5, 6);
        observable1
                .observeOn(Schedulers.computation())
                .subscribe(
                        // onNext
                        (i) -> {
                            Log.d("RxJava", "onNext thread enter: " + Utils.getThreadName());
                            Log.d("RxJava", "Val: " + i);
                            Log.d("RxJava", "onNext thread exit: " + Utils.getThreadName());
                        },
                        // onError
                        (t) -> {
                            t.printStackTrace();
                        },
                        // onCompleted
                        () -> {
                            Log.d("RxJava", "Completed Sequence");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        // ex - 5:
        Log.d("RxJava", "Example 5");
        Log.d("RxJava", "Current Thread - " + Utils.getThreadName());

        Observable<Integer> observable2 = Observable.just(1, 2, 3, 4, 5, 6);
        observable2
                .filter((i) -> (i % 2) == 1)
                .subscribe(
                        // onNext
                        (i) -> {
                            Log.d("RxJava", "onNext thread enter: " + Utils.getThreadName());
                            Log.d("RxJava", "Val: " + i);
                            Log.d("RxJava", "onNext thread exit: " + Utils.getThreadName());
                        },
                        // onError
                        (t) -> {
                            t.printStackTrace();
                        },
                        // onCompleted
                        () -> {
                            Log.d("RxJava", "Completed Sequence");
                        }
                );


        // ex -6:
        Log.d("RxJava", "Example 6");
        Log.d("RxJava", "Current Thread - " + Utils.getThreadName());

        UserService service = new UserService();

        Log.d("RxJava", " { \"userList\" : [ ");

        Observable.from(service.fetchUserList())
                .flatMap((val) -> Observable.just(val)
                        .subscribeOn(Schedulers.computation())
                        .filter((user) -> user.getSecurityStatus() != UserSecurityStatus.ADMINISTRATOR)

                )
                .toSortedList((user1, user2) -> {
                    return Integer.valueOf(user1.getSecurityStatus()).compareTo(Integer.valueOf(user2.getSecurityStatus()));
                })
                .observeOn(Schedulers.io())
                .subscribe(
                        (userList) -> {
                            Log.d("RxJava", "onNext thread enter: " + Utils.getThreadName());
                            for (User user : userList) {
                                Log.d("RxJava", "[ " + user.getUserName() + " " + user.getSecurityStatus() + " ]");
                            }
                            Log.d("RxJava", "onNext thread exit: " + Utils.getThreadName());

                        },
                        (t) -> {

                        },
                        () -> {
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        Log.d("RxJava", " ] } ");

        // Ex -7: convert synchronous APIs to Observables
        Log.d("RxJava", "Example 7");
        fetchContents(service)
                .observeOn(Schedulers.newThread())
                .subscribe((userList) -> {
                    Log.d("RxJava", "onNext thread enter: " + Utils.getThreadName());

                    for (User user : userList) {
                        Log.d("RxJava", "[ " + user.getUserName() + " " + user.getSecurityStatus() + " ]");
                    }

                    Log.d("RxJava", "onNext thread exit: " + Utils.getThreadName());

                });


        // Ex -8: Retrofit 2
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.gitHub.com/")
                .build();

        GitHubService gitHubService = retrofit.create(GitHubService.class);
        Observable<GitHub> githubUser = gitHubService.getGitHubUSer("imeraj");
        githubUser.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(errors ->
                        errors.zipWith(Observable.range(1, MAX_RETRIES), (n, i) -> i)
                                .flatMap(retryCount -> {
                                    Log.d("RxJava", "retryCount - " + retryCount);
                                    return Observable.timer(retryCount, TimeUnit.SECONDS);
                                })
                )
                .subscribe(
                        (user) -> {
                            Log.d("RxJava", "Github username: " + user.getLogin() + "\nid:" + " " + user.getId());
                        },
                        (error) -> {
                            Log.e("RxJava", "Error - " + error.getMessage());
                        },
                        () -> {
                            Log.d("RxJava", "GitHub completed");
                        }
                );

        sleep(3000);

        // Ex -9: convert asynchronous APIs to Observables
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Ex - 10: one emission at a time to observer
        Observable<Integer> source = Observable.range(1, 10);

        source.map(i -> i * 100)
                .doOnNext(i -> {
                    sleep(500);
                    System.out.println("Emitting " + i + " on thread " + Thread.currentThread().getName());
                })
                .observeOn(Schedulers.newThread())
                .map(i -> i * 10)
                .subscribe(i -> System.out.println("Received " + i + " on thread "
                        + Thread.currentThread().getName()));

        sleep(2000);

        // Ex - 11: Multiple subscribers (correct: if you can subscribe at once)
        ConnectableObservable<Long> obs1 =
                Observable.fromCallable(() -> System.nanoTime())
                        .publish();

        obs1.subscribe((l) -> Log.i("RxJava", "1: " + l));
        obs1.subscribe((l) -> Log.i("RxJava", "2: " + l));

        obs1.connect();

        // Ex - 12: Multiple subscribers (incorrect)
        Observable<Long> obs2 =
                Observable.fromCallable(() -> System.nanoTime())
                        .publish()
                        .refCount();

        obs2.subscribe((l) -> Log.i("RxJava", "1: " + l));
        obs2.subscribe((l) -> Log.i("RxJava", "2: " + l));

        // Ex - 13: Multiple subscribers (correct)
        Observable<Long> obs =
                Observable.fromCallable(() -> System.nanoTime())
                        .share()
                        .replay()
                        .autoConnect();

        obs.subscribe((l) -> Log.i("RxJava", "1: " + l));
        obs.subscribe((l) -> Log.i("RxJava", "2: " + l));

        sleep(2000);

        // Error handling: onErrorReturn
        Observable.just(1, 2, 3, 0, 4)
                .map(i -> 12/i)
                .onErrorReturn(error -> 99)
                .subscribe(System.out::println);

        // Error handling: onErrorResumeNext
        Observable.just(1, 2, 3, 0, 4)
                .map(i -> 12/i)
                .onErrorResumeNext(observable.range(1,5))
                .subscribe(System.out::println);

        // Error handling: onExceptionResumeNext/onErrorResumeNext
        Observable.just(1, 2, 3, 0, 4)
                .flatMap(i -> Observable.defer(() -> Observable.just(12 / i))
                        .onExceptionResumeNext(Observable.empty()))
                .subscribe(System.out::println);
    }


    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    protected void onResume() {
//        super.onResume();
//        sensorChangedSubscription = observerSernsorChanged(sensorManager, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
//                .subscribe(
//                        (sensorEvent) -> {
//                            Log.d("RxJava", "sensorEvent.timestamp=" + sensorEvent.timestamp + ", sensorEvent.values=" + Arrays.toString(sensorEvent.values));
//                        },
//                        (sensorError) -> {
//                            Log.e("RxJava", "Sensor Error");
//                        },
//                        () -> {
//                        }
//                );
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        sensorChangedSubscription.unsubscribe();
//    }

    public Observable<List<User>> fetchContents(final UserService service) {
        Log.d("RxJava", "fetchContents thread enter: " + Utils.getThreadName());
        return Observable.fromCallable(() -> service.fetchUserList());
    }

    public Observable<SensorEvent> observerSernsorChanged(final SensorManager sensorManager, final Sensor sensor, final int periodUs) {
        return Observable.fromEmitter((emitter) -> {
            final SensorEventListener sensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {
                    emitter.onNext(sensorEvent);
                }

                @Override
                public void onAccuracyChanged(Sensor originSensor, int i) {
                    // ignored for this example
                }
            };

            // (1) - unregister listener when unsubscribed
            emitter.setCancellation(() ->
                    sensorManager.unregisterListener(sensorListener, sensor));

            sensorManager.registerListener(sensorListener, sensor, periodUs);
        },AsyncEmitter.BackpressureMode.BUFFER);
    }
}
