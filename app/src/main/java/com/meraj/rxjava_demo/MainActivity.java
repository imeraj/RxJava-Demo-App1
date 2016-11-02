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

        // ex - 1: pre-java 8
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
                        Log.d("RxJava-ex1", "Sequence complete");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RxJava-ex1", "Sequence error");
                    }

                    @Override
                    public void onNext(Double val) {
                        Log.d("RxJava-ex1", "Sqrt val: " + val);
                    }
                });

        // ex -2: using Java 8
        Log.d("RxJava", "Example 2");
        Observable.just(1, 2, 3, 4, 5, 6)
                .subscribeOn(Schedulers.computation())
                .filter(i -> {
                    Log.d("RxJava-ex2", "filter thread: " + Utils.getThreadName());
                    return (i % 2) == 1;
                })
                .map(i -> Math.sqrt(i))
                .observeOn(Schedulers.newThread())
                .subscribe(
                        (val) -> {
                            Log.d("RxJava-ex2", "onNext thread: " + Utils.getThreadName());
                            Log.d("RxJava-ex2", "Sqrt val: " + val);
                        },
                        (error) -> {
                                Log.d("RxJava-ex2", "Sequence error");
                        },
                        () -> {
                            Log.d("RxJava-ex2", "Sequence complete");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        // ex -3:
        Log.d("RxJava", "Example 3");
        Log.d("RxJava-ex3", "Current Thread - " + Utils.getThreadName());

        Observable<Integer> observable = Observable.just(1, 2, 3, 4, 5, 6);
        observable
                .observeOn(Schedulers.newThread())
                .subscribe(
                        // onNext
                        (i) -> {
                            Log.d("RxJava-ex3", "onNext thread: " + Utils.getThreadName());
                            Log.d("RxJava-ex3", "Val: " + i);
                        },
                        // onError
                        (t) -> {
                            Log.d("RxJava-ex3", "Sequence error");
                        },
                        // onCompleted
                        () -> {
                            Log.d("RxJava-ex3", "Sequence complete");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }


        // ex -4:
        Log.d("RxJava", "Example 4");
        Log.d("RxJava-ex4", "Current Thread - " + Utils.getThreadName());

        Observable<Integer> observable1 = Observable.just(1, 2, 3, 4, 5, 6);
        observable1
                .observeOn(Schedulers.computation())
                .subscribe(
                        (i) -> {
                            Log.d("RxJava-ex4", "onNext thread: " + Utils.getThreadName());
                            Log.d("RxJava-ex4", "Val: " + i);
                        },
                        // onError
                        (t) -> {
                            Log.d("RxJava-ex4", "Sequence error");
                        },
                        () -> {
                            Log.d("RxJava-ex4", "Sequence complete");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        // ex -5:
        Log.d("RxJava", "Example 5");
        Log.d("RxJava-ex5", "Current Thread - " + Utils.getThreadName());

        UserService service = new UserService();

        Observable.from(service.fetchUserList())
                .flatMap((val) -> Observable.just(val)
                        .subscribeOn(Schedulers.computation())
                        .filter((user) -> {
                            Log.d("RxJava-ex5", "filter thread: " + Utils.getThreadName());
                            return user.getSecurityStatus() != UserSecurityStatus.ADMINISTRATOR;
                        })
                )
                .toSortedList((user1, user2) -> {
                    return Integer.valueOf(user1.getSecurityStatus()).compareTo(Integer.valueOf(user2.getSecurityStatus()));
                })
                .observeOn(Schedulers.io()) // try subscribeOn()
                .subscribe(
                        (userList) -> {
                            Log.d("RxJava-ex5", "onNext thread: " + Utils.getThreadName());
                            for (User user : userList) {
                                Log.d("RxJava-ex5", "[ " + user.getUserName() + " " + user.getSecurityStatus() + " ]");
                            }
                        },
                        (t) -> {
                            Log.d("RxJava-ex5", "Sequence error");
                        },
                        () -> {
                            Log.d("RxJava-ex5", "Sequence complete");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {}
        }

        // Ex -6: Retrofit 2
        Log.d("RxJava", "Example 6");
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                /**
                 * Returns an instance which creates synchronous observables that do not operate on any scheduler
                 * by default.
                 */
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.gitHub.com/")
                .build();

        GitHubService gitHubService = retrofit.create(GitHubService.class);
        Observable<GitHub> githubUser = gitHubService.getGitHubUSer("imeraj");
        githubUser.subscribeOn(Schedulers.newThread())
                .doOnError(error -> Log.d("RxJava-ex6", error.toString()))
                .retryWhen(errors ->
                        errors.zipWith(Observable.range(1, MAX_RETRIES), (n, i) -> i)
                                .flatMap(retryCount -> {
                                    Log.d("RxJava-ex6", "retryCount - " + retryCount) ;
                                    return Observable.timer((long) Math.pow(2, retryCount), TimeUnit.SECONDS);
                                })
                )
                .subscribe(
                        (user) -> {
                            Log.d("RxJava-ex6", "onNext thread: " + Utils.getThreadName());
                            Log.d("RxJava-ex6", "Github username: " + user.getLogin() + "\nid:" + " " + user.getId());
                        },
                        (error) -> {
                            Log.e("RxJava-ex6", "Sequence error");
                        },
                        () -> {
                            Log.d("RxJava-ex6", "Sequence complete");
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                );

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }

        // Ex - 7: Multiple subscribers (correct: if you can subscribe at once)
        ConnectableObservable<Long> obs1 =
                Observable.fromCallable(() -> System.nanoTime())
                        .publish();

        obs1.subscribe((l) -> Log.i("RxJava-ex7", "1: " + l));
        obs1.subscribe((l) -> Log.i("RxJava-ex7", "2: " + l));

        obs1.connect();

        // Ex - 8: Multiple subscribers (incorrect)
        Observable<Long> obs2 =
                Observable.fromCallable(() -> System.nanoTime())
                        .publish()
                        .refCount();

        obs2.subscribe((l) -> Log.i("RxJava-ex8", "1: " + l));
        obs2.subscribe((l) -> Log.i("RxJava-ex8", "2: " + l));

        // Ex - 9: Multiple subscribers (correct)
        Observable<Long> obs =
                Observable.fromCallable(() -> System.nanoTime())
                        .share()
                        .replay()  // .share().replay() --> .publish()
                        .autoConnect();

        obs.subscribe((l) -> Log.i("RxJava-ex9", "1: " + l));
        obs.subscribe((l) -> Log.i("RxJava-ex9", "2: " + l));


        // Error handling: onErrorReturn
        Observable.just(1, 2, 3, 0, 4)
                .map(i -> 12/i)
                .onErrorReturn(error -> -1)
                .subscribe(System.out::println);

        // Error handling: onErrorResumeNext
        Observable.just(1, 2, 3, 0, 4)
                .map(i -> 12/i)
                .onErrorResumeNext(observable.range(1,5))
                .subscribe(System.out::println);

        // put example for resuming original observable


        // Ex -10: convert asynchronous APIs to Observables
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    protected void onResume() {
        super.onResume();
        sensorChangedSubscription = observerSernsorChanged(sensorManager, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
                .subscribe(
                        (sensorEvent) -> {
                            Log.d("RxJava-ex10", "sensorEvent.timestamp=" + sensorEvent.timestamp + ", sensorEvent.values=" + Arrays.toString(sensorEvent.values));
                        },
                        (sensorError) -> {
                            Log.e("RxJava-ex10", "Sensor Error");
                        },
                        () -> {
                        }
                );
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorChangedSubscription.unsubscribe();
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
        }, AsyncEmitter.BackpressureMode.BUFFER);
    }
}
