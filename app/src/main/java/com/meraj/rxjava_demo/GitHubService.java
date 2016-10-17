package com.meraj.rxjava_demo;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface GitHubService {
    @GET("users/{username}")
    Observable<GitHub> getGitHubUSer(@Path("username") String userName);
}
