/*
 * (c) Copyright 2025 Ryan Yeats. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;
import org.example.DeterministicExecutor;
import org.example.SchedulableVirtualThreadFactory;

public class HttpSimExample {

  private static ScheduledExecutorService simStub = Executors.newScheduledThreadPool(1);

  //Unsuccessfully Trying to do a more complex example deterministically with the simulation loop run in a background thread
  public static void main(String... args)
      throws IOException, InterruptedException, ExecutionException {
    long seed = new SecureRandom().nextLong();
    RandomGenerator rand = new Random();
    System.out.println("Seed: "+ seed);
    DeterministicExecutor scheduler = new DeterministicExecutor(rand);
    ThreadFactory threadFactory = new SchedulableVirtualThreadFactory(scheduler);
    //        ThreadFactory threadFactory = Thread.ofVirtual().factory();
    //        Executor executor = Executors.newSingleThreadExecutor(threadFactory);
    ExecutorService executor = Executors.newThreadPerTaskExecutor(threadFactory);
    //        simStub.scheduleAtFixedRate(scheduler::runInCurrentQueueOrder, 1000, 1000,
    // TimeUnit.MILLISECONDS);
    simStub.schedule(scheduler::runInCurrentQueueOrder, 100, TimeUnit.MILLISECONDS);
    JdkHttpServer server = new JdkHttpServer(executor);
    HttpClient client =
        HttpClient.newBuilder().executor(executor).connectTimeout(Duration.ofSeconds(5)).build();
    Thread.sleep(100);

    scheduler.runInCurrentQueueOrder();

    HttpRequest request =
        HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString("start:"))
            .uri(URI.create("http://localhost:8080/file"))
            .build();
    CompletableFuture<HttpResponse<String>> resp =
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    while (!resp.isDone()) {
      scheduler.runInCurrentQueueOrder();
      Thread.sleep(10);
    }

    Thread.sleep(1000);
    //        Collection<Callable<CompletableFuture<HttpResponse<String>>>> tasks = new
    // ArrayList<>();
    //        tasks.add(() -> putRequest(client, "1"));
    //        tasks.add(() -> getRequest(client));
    //        tasks.add(() -> putRequest(client, "2"));
    //        tasks.add(() -> getRequest(client));
    //        tasks.add(() -> putRequest(client, "3"));
    //        tasks.add(() -> getRequest(client));
    //        tasks.add(() -> putRequest(client, "4"));
    //        tasks.add(() -> getRequest(client));
    //        tasks.add(() -> putRequest(client, "5"));
    //        tasks.add(() -> getRequest(client));
    Collection<CompletableFuture<HttpResponse<String>>> tasks = new ArrayList<>();
    tasks.add(putRequest(client, "1"));
    tasks.add(getRequest(client));
    tasks.add(putRequest(client, "2"));
    tasks.add(getRequest(client));
    tasks.add(putRequest(client, "3"));
    tasks.add(getRequest(client));
    tasks.add(putRequest(client, "4"));
    tasks.add(getRequest(client));
    tasks.add(putRequest(client, "5"));
    tasks.add(getRequest(client));
    //        while(!responses.stream().filter(Future::isDone).toList().isEmpty()) {
    while (!tasks.stream().filter(Predicate.not(Future::isDone)).toList().isEmpty()) {
      System.out.println(tasks.stream().filter(Predicate.not(Future::isDone)).toList().size());
      scheduler.drain();
      Thread.sleep(100);
    }
    List<String> responses =
        tasks.stream()
            .map(
                (f) -> {
                  try {
                    return f.get().body();
                  } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toList();
    System.out.println(responses);

    server.close();
    simStub.shutdownNow();
  }

  public static CompletableFuture<HttpResponse<String>> putRequest(
      HttpClient client, String contents) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(contents))
            .uri(URI.create("http://localhost:8080/file"))
            .build();
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }

  public static CompletableFuture<HttpResponse<String>> getRequest(HttpClient client) {
    HttpRequest request =
        HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/file")).build();
    return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }
}
