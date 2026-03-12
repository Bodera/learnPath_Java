# Introduction

So far, we have been playing with threads directly. Our intention was to learn how things are working behind the scenes, but in actual applications, we will not want to deal with the low-level thread objects. Instead, we need a high-level concurrency framework. That is what the executor service is.

## What is Executor Service?

`ExecutorService`, in a high-level, abstracts the thread management and provides a simple interface for developers like us to handle the task. Executor service was introduced long back as part of Java 5. So far, we have had only platform threads, right? And this executor service as part of the thread management it does thread pooling. So remember that because platform threads are expensive to create. So it will try to reuse the existing threads whatever it had created.

### Use Case: Finding the Best Deal

Let us consider this use case. We have three airline services: `Delta Airline`, `Frontier Airline`, and `Southwest Airline`. We have to find the best deal. How will you know the best deal? So we have to make a call to all these services, right? Only after that, we have to get the Delta price, Frontier price, and Southwest airline price. Only after making all these calls, then we can compare their price and define the best deal. So these all could be I/O calls.

```java
deltaAirline.getPrice(...);
frontierAirline.getPrice(...);
southwestAirline.getPrice(...);
```

So instead of making all these calls sequentially one by one as part of a task, we can divide that into multiple smaller subtasks because these look like they are completely independent.

Then we can divide that into multiple smaller subtasks and virtual thread is very cheap. So what we can do here is we can create three different virtual threads. And one thread will be doing `Delta Airline`, the other thread will be doing `Frontier`, and so on. So we can do these in parallel to improve the overall response time you are getting. So in scenarios like this the executor service is going to help us.

```java
Thread.ofVirtual().start(() -> deltaAirline.getPrice(...));
Thread.ofVirtual().start(() -> frontierAirline.getPrice(...));
Thread.ofVirtual().start(() -> southwestAirline.getPrice(...));
```

## Executor, Executor Service, and Virtual Threads

We might hear a lot of similar sounding words, so it's better to discuss. We have something called the `Executor`. It's a functional interface. We have something called the `ExecutorService`, which is another interface which **extends** the `Executor` interface. It has a few additional methods. We have few implementations for this `ExecutorService` for example, `ForkJoinPool` is one such implementation. Then we have executors. The `Executor` is simply a utility class with many factory methods to create the instance of this `ExecutorService` implementation.

## Virtual Threads and Executor Service

Now this is going to be interesting. Virtual threads are not supposed to be pulled. It's not designed for pulling. Remember that virtual threads are tasks, they supposed to be disposable. We have to treat this like an "use and throw" type of object. The Java documentation says this. The Oracle Java Architect has explicitly mentioned in one of the presentations that people are already misusing executor service with a virtual thread, so we have to be careful.

> A _thread pool_ is a group of preconstructed platform threads that are reused when they become available. Some thread pools have a fixed number of threads while others create new threads as needed. 
>
> Don't pool virtual threads. Create one for every application task. Virtual threads are short-lived and have shallow call stacks. They don't need the additional overhead or the functionality of thread pools.

If virtual threads are not going to be pooled, then what is the point of using `ExecutorService` with virtual threads? Because `ExecutorService` is thread pool, right? Actually, no, it's not like that. It's simply thread per task creation management. We still need virtual thread on demand. Someone has to create virtual thread and start the thread for us, right? That someone is `ExecutorService`.

## Understanding Executor Service Types in Java

### Why Should We Care About ExecutorService?

Even though we're diving deep into the world of virtual threads in this course, we can't just skip over the classic `ExecutorService` types. Why? Because understanding how traditional executors work will help us appreciate the challenges and trade-offs that virtual threads address. One particular challenge we'll explore is choosing the right executor for your workload—and understanding when virtual threads change the game entirely.

Think of it as learning to drive a manual car before jumping into an automatic—you'll understand what's happening under the hood much better!

### The Classic `ExecutorService` Types

Let's break down the different types of executor services that have been our go-to tools for concurrent programming in Java.

#### 1. Fixed Thread Pool

**What is it?** A thread pool with a predetermined, fixed number of threads that never changes.

**Real-world example:** A Spring Boot web application using Tomcat typically comes configured with a fixed thread pool of 200 threads dedicated to handling incoming HTTP requests. No matter how many requests arrive, you're working with those same 200 threads—no more, no less.

**When to use it:** When you know your workload patterns and want predictable, controlled resource usage. Great for web servers handling HTTP requests where you want to limit concurrent connections.

#### 2. Single Thread Executor

**What is it?** A thread pool with just one worker thread—no parallelism here!

**Real-world example:** Imagine a bank vault that only one person can enter at a time. Tasks are processed one after another, in strict order.

**When to use it:** Perfect for mission-critical tasks that must be executed sequentially. If order matters and you can't risk race conditions, this is your friend. Think of processing financial transactions where order is crucial.

#### 3. Cached Thread Pool

**What is it?** An elastic, on-demand thread pool that grows and shrinks dynamically based on your workload.

**How it works:**
- Starts with zero threads
- Creates new platform threads on demand when tasks arrive
- Reuses idle threads for new tasks
- Automatically terminates threads that have been idle for 60 seconds (configurable)
- **Important:** No upper limit on thread creation—can be dangerous under heavy load!

**Real-world example:** Like an Uber surge system—more drivers appear when demand spikes, and they go offline when things are quiet.

**When to use it:** Perfect for unpredictable, short-lived tasks where you don't know how many concurrent operations you'll need. Be careful with unbounded growth—monitor it in production!

⚠️ **Caution:** Since there's no cap on thread creation, a sudden spike in tasks could create thousands of threads and overwhelm your system. Use with care!

#### 4. Scheduled Thread Pool

**What is it?** A specialized thread pool designed to run tasks at regular intervals or with specific delays.

**Real-world example:** Like setting recurring alarms—you need to poll a remote service every minute to check for updates, or run a cache cleanup job every hour, or send a heartbeat signal every 30 seconds.

**When to use it:** Any time you need periodic or delayed task execution. Perfect for background maintenance tasks, polling operations, or scheduled health checks.

#### 5. Work Stealing Pool (Optional Deep Dive)

**What is it?** Creates a `ForkJoinPool` that uses work-stealing algorithms where idle threads can "steal" work from busy threads' queues.

**Quick note:** Designed for recursive, divide-and-conquer algorithms (like parallel sorting, tree traversal, or parallel stream operations). Most typical web applications won't need this—it shines in computational tasks that can be broken into smaller subtasks.

**Note:** We won't focus much on this since it's specialized for specific computational patterns rather than general-purpose concurrency.

### Enter Java 21: The Thread-Per-Task Executor

Here's where things get exciting! Java 21 introduces a game-changer: **the thread-per-task executor**.

#### What makes it special?

- Creates a new virtual thread for each task on demand
- Behind the scenes, it uses the virtual thread builder factory
- No more worrying about thread pool sizes or queue backlogs!

This is the bridge between traditional `ExecutorService` and the virtual thread revolution.

### How `ExecutorService` Works Under the Hood

#### The Traditional Way (Platform Threads)

Let's peek behind the curtain of how classic executor services operate:

1. **The Submit Method:** All `ExecutorService` implementations have a `submit()` method that accepts `Runnable` or `Callable` tasks.
2. **Thread Safety:** These implementations are thread-safe, meaning multiple threads can safely submit tasks simultaneously without coordination.
3. **The Queue System:** Here's the key architecture:

```
Task Submission → Internal Queue → Worker Threads → Execution → Results
```

All traditional implementations (fixed, single, cached, scheduled, even `ForkJoinPool`) follow this pattern:

- Tasks are submitted via `submit()`
- Tasks are added to an internal queue (waiting area)
- A pool of worker threads sits idle, waiting for work
- When tasks arrive, worker threads pick them up from the queue
- Threads execute the tasks and return results
- If all threads are busy, new tasks wait in the queue

#### Visual representation:

```
[Task 1] ──┐
[Task 2] ──┼─→ [Queue] ─→ [Thread 1] ──┐
[Task 3] ──┘              [Thread 2] ──┼─→ Results
[Task 4] (waiting...)     [Thread 3] ──┘
```

**The bottleneck:** With platform threads, you're limited by the number of threads. If all threads are busy doing blocking I/O (waiting for database responses, API calls, etc.), new tasks must wait in the queue—even though the CPU might be sitting idle!

### The Virtual Thread Way (Thread-Per-Task Executor)

Now here's where it gets interesting. The new thread-per-task executor fundamentally changes the architecture:

#### What's different?

- **No internal queue!** Tasks go directly to execution
- Each task gets its own virtual thread immediately
- Virtual threads are so lightweight that millions can exist simultaneously
- Carrier threads (platform threads) handle the actual execution behind the scenes

**Why no queue?** Because virtual threads are incredibly cheap to create (unlike platform threads). We don't need to queue tasks waiting for available threads—we just create a new virtual thread for each task instantly!

#### Visual representation:

```
[Task 1] ─→ [Virtual Thread 1] ──┐
[Task 2] ─→ [Virtual Thread 2] ──┼─→ Carrier Threads ─→ Results
[Task 3] ─→ [Virtual Thread 3] ──┤    (Platform Threads)
[Task 4] ─→ [Virtual Thread 4] ──┘
```

**The magic of carrier threads:** When a virtual thread hits blocking I/O (like waiting for a database query), it doesn't waste a platform thread sitting idle. The carrier thread (platform thread) is freed up to execute other virtual threads. This is the "non-blocking benefit" of virtual threads!

**In simpler terms:** Imagine you have 3 phone operators (carrier threads) handling calls from thousands of customers (virtual threads). When a customer is put on hold waiting for information, the operator doesn't just sit there—they switch to help another customer. This way, 3 operators can effectively handle thousands of concurrent calls!

### The Beautiful Part: Same API, Better Performance

Here's what makes virtual threads so elegant: **your code doesn't change!**

Because the thread-per-task executor implements the same `ExecutorService` interface, your application code looks identical:

```java
// This works the same way whether you're using
// a traditional executor or virtual threads!
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

executor.submit(() -> {
    // Your task here - maybe a database call
    // or an API request
});
```

#### What you get:

- ✅ Same familiar API you already know
- ✅ No code changes needed to migrate
- ✅ Non-blocking execution—carrier threads aren't wasted on blocked operations
- ✅ Massive scalability—handle millions of concurrent tasks
- ✅ Better resource utilization—your CPU stays busy while tasks wait for I/O

#### What "non-blocking benefits" really means:

- Platform threads doing blocking I/O = wasted resources (thread sits idle, doing nothing)
- Virtual threads doing blocking I/O = carrier thread is freed to do other work
- Result: Same hardware can handle exponentially more concurrent operations!

#### Understanding the Challenge

So what's the challenge with virtual threads that we mentioned at the start?

The challenge is knowing **when** and **how** to use them effectively:

- Traditional executors are still perfectly fine for many use cases
- Virtual threads excel when you have high concurrency with lots of I/O blocking
- You need to understand both models to choose the right tool
- Some patterns that worked with platform threads need rethinking with virtual threads (we'll explore this in future lectures!)

### Key Takeaway

Understanding traditional `ExecutorService` types isn't just academic—it's essential for appreciating how virtual threads solve real problems. The thread-per-task executor with virtual threads gives us the best of both worlds: the familiar `ExecutorService` API we know and love, with the massive scalability and efficiency of lightweight virtual threads.

The shift from queuing tasks (waiting for threads) to instantly creating virtual threads (no waiting needed) is a fundamental paradigm shift in how we think about concurrency in Java.

Ready to see this in action? Let's start playing with virtual executors in the next section!

_💡 Pro Tip: Don't think of virtual threads as replacing everything—think of them as a powerful new tool in your concurrent programming toolkit. Traditional executors still have their place, but virtual threads shine when you have high-concurrency, I/O-heavy workloads!_

---

## Guide: ExecutorService and AutoCloseable in Java 21

### 1. The Big Change: Why it matters

In Java 21, `ExecutorService` now extends `AutoCloseable`.

* **Before Java 21:** You had to manually call `.shutdown()` in a `finally` block to ensure your application didn't hang.
* **Java 21+:** You can use **Try-with-Resources**. When the `try` block finishes, Java automatically calls `.close()`, which triggers a shutdown.

### 2. Shutdown vs. ShutdownNow

Understanding how an executor stops is crucial for preventing data loss or "zombie" threads.

| Method | Behavior | New Task Submission |
| --- | --- | --- |
| **`shutdown()`** | Graceful. Waits for submitted tasks to finish. | Rejected (Throws `RejectedExecutionException`) |
| **`shutdownNow()`** | Forceful. Attempts to stop active tasks immediately. | Rejected |
| **`close()`** | Called by Try-with-Resources. In Java 21, it acts like `shutdown()` and waits. | Rejected |

---

### 3. Implementation Comparison

#### The "Old" Way (Manual Management)

This approach is prone to memory leaks if you forget the shutdown call.

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.submit(() -> System.out.println("Task running..."));

executor.shutdown(); // Must remember to do this!

```

#### The "Modern" Way (Try-with-Resources)

```java
// The executor is automatically closed at the end of the curly brace
try (var executor = Executors.newSingleThreadExecutor()) {
    executor.submit(() -> {
        Thread.sleep(1000);
        System.out.println("Task 1 Complete");
    });
    executor.submit(() -> System.out.println("Task 2 Complete"));
} 
// .close() is called automatically here. 
// The program waits for tasks to finish before moving past this line.

```

---

### 4. When to use Try-with-Resources?

You shouldn't use this *everywhere*. It depends on the **lifecycle** of your application:

* **✅ Use it for: Short-lived tasks.** Scripting, batch processing, or CLI tools where the work has a clear beginning and end.
* **❌ Avoid it for: Long-lived applications.** Spring Boot microservices or Web Servers. In these cases, the Executor should stay alive as a **Bean** to handle requests throughout the day. If you put it in a `try` block, it will die as soon as the first request finishes!

> **Key Takeaway:** `AutoCloseable` makes code cleaner and prevents the application from hanging, but only use it when you actually *want* the executor to stop after the block of code runs.

---

### Quiz time

Q. In Java 21, which interface was added to the 'ExecutorService' hierarchy to enable its use in try-with-resources blocks?

A. AutoCloseable -> Implementing AutoCloseable allows the JVM to automatically call the .close() method when exiting a try-with-resources block.

Q. When an 'ExecutorService' is used within a try-with-resources block in Java 21, what happens internally when the block finishes?

A. The `.close()` method is called, which internally triggers `.shutdown()`. -> Java 21's implementation ensures that `.close()` initiates a graceful shutdown and waits for tasks to complete.

Q. What is the primary difference between '.shutdown()' and '.shutdownNow()'?

A. `.shutdown()` waits for existing tasks; `.shutdownNow()` attempts to stop them immediately -> Shutdown is 'graceful' because it respects already submitted work, while shutdownNow is 'forceful'.

Q. Why is it generally NOT recommended to use try-with-resources for an 'ExecutorService' in a Spring Boot web application bean?

A. It would close the executor after a single use, making it unavailable for future requests. -> In web apps, we need the executor to stay 'alive' to handle multiple incoming requests over time.

Q. If you call `.shutdown()` and then immediately try to call `.submit(newTask)`, what will happen?

A 'RejectedExecutionException' will be thrown -> This is the standard runtime exception used to signal that the executor is no longer accepting tasks.

Q. Which utility class is commonly used to create different types of 'ExecutorService' implementations, such as a single-threaded executor?

A. Executors -> The 'Executors' class provides static factory methods for creating pre-configured thread pools.

Q. What happens to tasks that are currently waiting in the queue (but haven't started yet) when `.shutdownNow()` is called?

A. They are drained from the queue and returned as a list of Runnables. -> The `.shutdownNow()` method returns the list of tasks that were never started so the caller can handle them.

Q. In the context of the lecture, what is the 'short-lived application' where try-with-resources is most useful?

A. A command-line tool that performs a specific calculation and exits. -> In 'one-and-done' tools, ensuring the executor closes allows the JVM to exit without hanging.

Q. If you don't use try-with-resources or call `.shutdown()`, why might your Java application fail to exit even after 'main' finishes?

A. Active non-daemon threads in the executor's pool keep the JVM alive. -> The JVM only exits when all non-daemon threads (like those in a standard executor) have finished.

Q. What is the result of using 'var' with an 'ExecutorService' in a try-with-resources block, as shown in the lecture?

A. The compiler infers the type as 'ExecutorService', keeping the code clean -> Using 'var' reduces boilerplate code while maintaining the full functionality of the inferred type.

### Demo

```java
public class Lec01AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        Lec01AutoCloseable.class
    );

    public static void main(String[] args) {

        var executorService = Executors.newSingleThreadExecutor();
        executorService.submit(Lec01AutoCloseable::task);
        LOGGER.info("submitted");
        exe
    }

    private static void task() {
        ThreadUtils.sleep(Duration.ofSeconds(1));
        LOGGER.info("task executed");
    }
}
```

Ouput:
```
02:20:30.689 [main] INFO section07.Lec01AutoCloseable -- submitted
02:20:31.692 [pool-1-thread-1] INFO section07.Lec01AutoCloseable -- task executed
```

We can nottice a couple of things here:

1. The `main` thread submitted the task
2. The `pool-1-thread-1` thread executed the task - a single thread executor
3. The application still runs after the task is completed

if you modify it a bit like so

```java
public class Lec01AutoCloseable {
//...
    public static void main(String[] args) {

        var executorService = Executors.newSingleThreadExecutor();
        executorService.submit(Lec01AutoCloseable::task);
        LOGGER.info("submitted");
        executorService.shutdown();
    }
//...
}
```

Ouput:
```
02:26:18.885 [main] INFO section07.Lec01AutoCloseable -- submitted
02:26:19.885 [pool-1-thread-1] INFO section07.Lec01AutoCloseable -- task executed

Process finished with exit code 0
```

and if you modify it a bit like so

```java
public class Lec01AutoCloseable {
//...
    public static void main(String[] args) {

        var executorService = Executors.newSingleThreadExecutor();
        executorService.submit(Lec01AutoCloseable::task);
        LOGGER.info("submitted");
        executorService.shutdownNow();
    }
//...
}
```

Ouput:
```
02:28:27.981 [main] INFO section07.Lec01AutoCloseable -- submitted

Process finished with exit code 0
```

---

## 🚀 Lecture Notes: Java ExecutorService Types

The `ExecutorService` simplifies asynchronous execution by decoupling task submission from thread management.

### 1. Common Utility Method

To test these executors, we use a shared `execute` method. Note the use of **Try-with-Resources**, which ensures `executor.close()` is called, preventing resource leaks.

```java
private static void execute(ExecutorService executorService, int taskCount) {
    try (executorService) { // Automatically shuts down after tasks complete
        for (int i = 0; i < taskCount; i++) {
            int j = i;
            executorService.execute(() -> ioTask(j));
        }
    }
}
```

---

### 2. Standard Thread Pools (Platform Threads)

| Type | Factory Method | Behavior | Best Use Case |
| --- | --- | --- | --- |
| **Single** | `newSingleThreadExecutor()` | One thread, executes tasks **sequentially**. | Mission-critical tasks where order matters and thread safety is a concern. |
| **Fixed** | `newFixedThreadPool(n)` | A pool with a fixed number of threads. | Predictable load; prevents system exhaustion by limiting thread count. |
| **Cached** | `newCachedThreadPool()` | Starts with 0 threads; creates new ones as needed. Reuses idle threads. | Short-lived asynchronous tasks with fluctuating volume. |
| **Scheduled** | `newScheduledThreadPool(n)` | Supports delayed or periodic execution. | Heartbeats, cleaning caches, or polling services. |

> **💡 Pro Tip:** The difference between a `SingleThreadExecutor` and a `FixedThreadPool(1)` is that the Single version cannot be reconfigured or resized later in the code, providing a "guaranteed" sequential behavior.

---

### 3. The Modern Era: Virtual Threads (Project Loom)

Introduced in recent Java versions, these are lightweight threads that don't map 1:1 to OS threads.

* **Method:** `Executors.newVirtualThreadPerTaskExecutor()`
* **How it works:** It doesn't use a "pool" because virtual threads are "cheap" to create. It simply spawns a new virtual thread for every single task.
* **Scale:** Can handle millions of tasks (e.g., 10,000+ IO tasks) without throwing `OutOfMemoryError`.
* **When to use:** Blocking IO operations (API calls, Database queries). **Avoid** for heavy CPU calculations.

---

### 4. Scheduled Executor Details

Used for tasks that need to run repeatedly.

* **`scheduleAtFixedRate`**: Starts the next task based on the **start time** of the previous one (ignores how long the task took).
* **`scheduleWithFixedDelay`**: Starts the next task only after a specific delay **following the completion** of the previous one.

```java
// Example: Running every 1 second after a 5-second sleep
executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
```

---

### 🔑 Key Study Questions

* **Q:** Why use `CachedThreadPool` for 200 tasks instead of `Fixed(5)`?
* *A: Cached will run all 200 in parallel (if resources allow), whereas Fixed(5) will process them 5 at a time in a queue.

* **Q:** Can I use Virtual Threads for Scheduling?
* *A: Currently, `newSingleThreadScheduledExecutor` uses platform threads. We will explore workarounds for virtual-thread scheduling in future lectures.*

---

Since you are diving into the world of Java Concurrency, there is one specific concept that usually trips people up: **The Task Queue.** In your notes, you mentioned that `FixedThreadPool` and `CachedThreadPool` behave differently. This is because of how they manage their internal "waiting room" for tasks.

* **FixedThreadPool:** Uses a **LinkedBlockingQueue** (infinite capacity). If your threads are busy, tasks just pile up in the queue.
* **CachedThreadPool:** Uses a **SynchronousQueue** (zero capacity). It doesn't hold tasks; it either hands the task to an idle thread or creates a new one immediately.

---

## 🛠️ Executor Selection Cheat Sheet

| Scenario | Recommended Executor | Why? |
| --- | --- | --- |
| **Microservice REST API** (High volume, many blocking I/O calls) | `newVirtualThreadPerTaskExecutor()` | It handles thousands of concurrent requests without the overhead of heavy OS threads. |
| **Heavy Image/Video Processing** (CPU Intensive) | `newFixedThreadPool(n)` | Set  to the number of CPU cores. Using more threads than cores here actually slows you down due to context switching. |
| **Financial Transaction Processor** (Strict order required) | `newSingleThreadExecutor()` | Ensures Task A finishes before Task B starts. No race conditions on the sequence. |
| **Background Mailer / Notification Service** (Spiky traffic) | `newCachedThreadPool()` | Scales up rapidly when you have 1,000 emails to send, then shrinks to 0 threads when the queue is empty to save RAM. |
| **Database Cleanup / "Heartbeat" Monitor** | `newScheduledThreadPool(n)` | Perfect for tasks that need to run "Every 10 minutes" or "1 hour from now." |

---

### 🎨 Visualizing the Choice

To understand why we choose one over the other, it's all about how the "Queue" and the "Workers" interact.

* **The Queue:** If your pool is "Fixed," the queue holds the overflow.
* **The Workers:** If your pool is "Virtual," there is effectively no queue—you just spawn a new worker for every task.

---

### ⚠️ The "Golden Rule" of Virtual Threads

While you're studying, keep this in mind: **Virtual threads are for waiting, not for working.**

* If your task is waiting for a database (I/O), use **Virtual Threads**.
* If your task is calculating complex math (CPU), use **Fixed Platform Threads**.

---

Testing concurrent code requires a shift in perspective. To benchmark `ExecutorService` types properly, you usually choose between **JMeter** (for testing the system as a whole) or **JMH** (for testing specific Java methods).

Since you are testing a single Java class, **JMH (Java Microbenchmark Harness)** is actually the "industry standard" for accuracy, as it accounts for JVM warm-up and optimization. However, **JMeter** is better if you wrap your code in a small web service.

---

## 🏗️ Option 1: Testing with JMH (Recommended for Code)

JMH is built by the OpenJDK team specifically to avoid "benchmarking pitfalls" like the JVM optimizing away your code because it thinks it's a "dead loop."

### How to set it up:

1. **Add Dependency:** Add `jmh-core` and `jmh-generator-annprocess` to your `pom.xml`.
2. **Write the Benchmark:**

```java
@BenchmarkMode(Mode.Throughput) // Measure operations per second
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class ExecutorBenchmark {

    private static final int TASKS = 1_000;

    @Benchmark
    public void testFixedThreadPool() {
        try (var executor = Executors.newFixedThreadPool(10)) {
            runTasks(executor);
        }
    }

    @Benchmark
    public void testVirtualThreads() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            runTasks(executor);
        }
    }

    private void runTasks(ExecutorService executor) {
        for (int i = 0; i < TASKS; i++) {
            executor.execute(() -> {
                // Simulate I/O work
                LockSupport.parkNanos(1_000_000); // 1ms sleep
            });
        }
    }
}
```

---

## 🧪 Option 2: Testing with JMeter (System Level)

If you want to see how these executors handle real "traffic," you can wrap your logic in a simple Spring Boot controller and use JMeter to "bombard" it.

### Step-by-Step JMeter Setup:

1. **Thread Group:** Create a "Thread Group" (simulating 500 users).
2. **Ramp-up:** Set this to 10 seconds (so it doesn't crash your PC immediately).
3. **Sampler:** Add an **HTTP Request** pointing to your Java app's endpoint.
4. **Listeners:** Add a **Summary Report** and **Response Times Over Time**.

### What to watch for:

* **Platform Threads:** You’ll likely see the "Response Time" spike as soon as your pool is full.
* **Virtual Threads:** You should see a flat, consistent response time even as you increase the number of users to 1,000+.

---

## 📊 Comparison Summary Table

| Feature | JMH (Java Microbenchmark) | JMeter (Load Testing) |
| --- | --- | --- |
| **Focus** | Method-level performance | System-level throughput |
| **Precision** | Very High (nanoseconds) | Low (milliseconds) |
| **Complexity** | Requires writing more Java code | GUI-based, easy to scale |
| **Best for...** | Comparing `Fixed` vs `Virtual` in code. | Testing a real API using Virtual Threads. |

---

**Would you like me to help you generate the Maven `pom.xml` file needed to run the JMH benchmark on your machine?**

[Comparing Virtual Threads vs Platform Threads in Spring Boot using JMeter Load Test](https://www.youtube.com/watch?v=LDgriPNWCjY)

This video provides a side-by-side comparison of how Virtual Threads and Platform Threads behave under high load in a real application using JMeter, which is exactly what you need to visualize the performance difference.

### One Final "Gotcha" for your Notes: **Thread Local Variables**

As you study further, keep an eye out for **ThreadLocals**.

* **Platform Threads:** We often use ThreadLocals because we only have a few threads.
* **Virtual Threads:** Since you might have 1,000,000 virtual threads, using heavy ThreadLocal data can eat up your memory fast! This is a common "trap" in modern Java interviews.

---

## 🚀 Simulating external services

The goal is to simulate a **Microservices environment** using a single provided `.jar` file.

* **Execution Command:** 
```bash
java -jar external-services.jar
```

* **Custom Port (Optional):** If `7070` is taken, use:

```bash
java -jar external-services.jar --server.port=8080
```

* **Accessing the UI:** Once running, navigate to: `http://localhost:7070/swagger-ui/`
* **Compatibility:** Built with Java 17, but fully compatible with your Java 21 setup.


### 🛠️ The Simulation Environment

Even though it is one file, you are instructed to **visualize it as two distinct microservices**. This simulates network latency and I/O overhead—the "bread and butter" use case for Virtual Threads.

### 1. Product Microservice (`/product`)

* **Input:** Product ID (up to 50).
* **Output:** A random String representing a product name.
* **Behavior:** Simulates a slow service with a **~1-second delay** to mimic network/database calls.

### 2. Rating Microservice (`/rating`)

* **Input:** Product ID.
* **Output:** A random integer (1–5).
* **Behavior:** Similar to the product service, used to simulate an independent data source for the same ID.

### 💡 Key Takeaways for your Course

* **Why are we doing this?** To stop using `Thread.sleep()` and start practicing **blocking I/O calls**. Virtual threads shine when a thread is "waiting" for a response from a service like this.
* **Data Format:** Currently, these return simple **Strings**, but the course will transition to **JSON** payloads once you start using Spring Boot Web.
* **Focus:** For now, ignore Sections 02 and 03 in the Swagger UI; focus strictly on **Section 01**.

---

This is a great follow-up. In this lecture, you've transitioned from just running the "server" (the JAR file) to building the **Client**—the actual Java code that will interact with it.

Since the goal of the course is to study **Virtual Threads**, this client is designed to be "blocking" on purpose so you can see how Virtual Threads handle those pauses.

---

## 🛠️ The Client Implementation Summary

```java
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            Client.class
    );
    private static final String PRODUCT_REQUEST_FORMAT = "http://localhost:7070/sec01/product/%d";
    private static final String RATING_REQUEST_FORMAT = "http://localhost:7070/sec01/rating/%d";

    public static String getProduct(int id) {
        return callExternalService(PRODUCT_REQUEST_FORMAT.formatted(id));
    }

    public static Integer getRating(int id) {
        return Integer.parseInt(callExternalService(RATING_REQUEST_FORMAT.formatted(id)));
    }

    private static String callExternalService(String url) {
        LOGGER.info("Calling externa service at {}", url);
        try (var stream = URI.create(url).toURL().openStream()) {
            return new String(stream.readAllBytes());
        } catch (Exception e)  {
            LOGGER.error("Error calling externa service", e);
            throw new RuntimeException(e);
        }
    }
}
```

You’ve built a utility class called `Client` that handles the communication between your "playground" project and the external microservice.

### 1. Request Architecture

Instead of using a complex `HttpClient` immediately, the instructor has you using a simpler, lower-level approach to demonstrate the basics of a network call.

* **URL Templates:** You defined constants using `%d` placeholders. This allows you to dynamically inject the `productId` into the URL using `.formatted(id)`.
* **Target API:** It specifically targets `sec01` endpoints on `localhost:7070`.

### 2. The Core Logic: `callExternaServic`

This private method is the engine of the class. It handles three critical steps:

* **Connection:** `URI.create(url).toURL().openStream()` initiates the TCP connection and sends the HTTP GET request.
* **Reading:** `readAllBytes()` pulls the entire response into memory.
> **Note:** The instructor mentioned this is fine here because the responses (product names and ratings) are very small.


* **Resource Management:** You are using a **try-with-resources** block `try (var stream = ...)` which ensures the input stream is closed automatically, preventing memory leaks.

### 3. Public API Methods

You exposed two specific methods to make the client easy to use:

* **`getProduct(int id)`**: Returns a `String` (the product name).
* **`getRating(int id)`**: Returns an `Integer`. It internally calls the string-based service and parses the result using `Integer.parseInt()`.

---

## 📝 Key Observations for Virtual Threads

* **Blocking I/O:** Every time you call `openStream()` or `readAllBytes()`, the thread executing that code **blocks** (it sits idle waiting for the network).
* **The "Playground" Setup:** This is the perfect environment to test Virtual Threads because they are designed to "unmount" from the CPU while these blocking network calls are waiting for a response.

---

This lecture is a major milestone: you are moving from simply "running" code to orchestrating **parallel tasks** using Java's modern concurrency model.

Here is the summary of how you are now accessing responses using `Future` and Virtual Threads.

The code:

```java
public class Lec03AccessResponseUsingFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(Lec03AccessResponseUsingFuture.class);

    static void main(String[] args) throws InterruptedException, ExecutionException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Future<String> product41 = executor.submit(() -> Client.getProduct(41));
            Future<String> product42 = executor.submit(() -> Client.getProduct(42));
            Future<String> product43 = executor.submit(() -> Client.getProduct(43));

            LOGGER.info("product-41: {}", product41.get());
            LOGGER.info("product-42: {}", product42.get());
            LOGGER.info("product-43: {}", product43.get());
        }
    }
}
```

Output: 

```bash
22:29:23.925 [virtual-41] INFO section07.externalservices.Client -- Calling externa service at http://localhost:7070/sec01/product/43
22:29:23.925 [virtual-37] INFO section07.externalservices.Client -- Calling externa service at http://localhost:7070/sec01/product/41
22:29:23.925 [virtual-39] INFO section07.externalservices.Client -- Calling externa service at http://localhost:7070/sec01/product/42
22:29:24.947 [main] INFO section07.Lec03AccessResponseUsingFuture -- product-41: Durable Marble Clock
22:29:24.947 [main] INFO section07.Lec03AccessResponseUsingFuture -- product-42: Mediocre Aluminum Hat
22:29:24.947 [main] INFO section07.Lec03AccessResponseUsingFuture -- product-43: Practical Marble Bag


Process finished with exit code 0
```

---

## 🛰️ From Runnable to Callable

Previously, you likely used `executor.submit(() -> { ... })` for tasks that just "did work" without returning anything.

* **The Shift:** By using `Client.getProduct(id)`, your task now returns a value.
* **The Mechanism:** When you submit a task that returns a value (a `Callable`), the executor returns a **`Future<T>`**.
* **The Metaphor:** Think of a `Future` as a **claim check** or a placeholder. You don't have the product yet, but you have a ticket you can exchange for it once it's ready.

---

## ⚡ Parallelism vs. Sequential Execution



This is where the power of Virtual Threads becomes obvious. In a traditional sequential program, three requests with a 1-second delay would take **3 seconds**.

* **Concurrency in Action:** In your logs, you can see all three "Calling external service" messages appear at almost the exact same millisecond (`22:27:54.815`).
* **The Result:** Even though each call takes 1 second, because they run in parallel on separate virtual threads, you get all three results back in **~1 second total**.
* **Logs Reveal the Threads:** Notice the names in your logs: `[virtual-37]`, `[virtual-39]`, and `[virtual-41]`. These are the individual virtual threads doing the heavy lifting.

---

## 🛑 The Role of `future.get()`

The method `future.get()` is a **blocking call**.

* **The "Old" Problem:** In the past, blocking a thread was "expensive" because it tied up a limited platform thread.
* **The Virtual Thread Solution:** When you call `.get()`, the Virtual Thread "unmounts." It doesn't hog the CPU; it just waits efficiently.
* **Developer Experience:** As the instructor noted, Java 21 encourages this "blocking style" because it’s easier to read than complex asynchronous/reactive code, but it performs just as well under the hood.

---

## 🔍 Troubleshooting Tips

If your code fails, the instructor suggests this checklist:

1. **Server Check:** Is the `.jar` still running?
2. **Connectivity:** Can you reach `http://localhost:7070/sec01/product/1` in your browser?
3. **Code Match:** Ensure your `Client` matches the one you built in the previous step.

---

# Concurrency vs Parallelism

## 1️⃣ Big Picture

* **Concurrency** is the broader concept.
* **Parallelism** is a specific type of concurrency.

So every parallel program is concurrent — but not every concurrent program is parallel.

---

# 🔹 What Is Concurrency?

Concurrency is about **dealing with multiple tasks over a period of time**.

It does **not** necessarily mean tasks run at the exact same time.

It means the system can manage multiple tasks and make progress on all of them.

---

## 🖥 Example: One CPU, Two Applications

Imagine:

* You have **Chrome**
* You have **IntelliJ**
* You have **one CPU core**

The CPU works like this:

```
Run Chrome for a bit
Run IntelliJ for a bit
Run Chrome again
Run IntelliJ again
...
```

It switches very fast between them.

To you, it *looks* like both are running at the same time.

But in reality:

- Only one task runs at any given instant.
- The CPU just switches quickly between them.

This is **concurrency**.

---

## 👨‍🍳 Chef Example (Very Important)

Imagine one chef preparing two dishes.

He:

* Chops vegetables for Dish A
* Switches to stirring Dish B
* Goes back to Dish A
* Returns to Dish B

It looks like both dishes are being prepared simultaneously.

But:

- The chef can only do one thing at a time.
- He just switches between tasks.

That is concurrency.

---

# 🔹 Concurrency in a Java Web Application

Let’s imagine:

* You built a Spring Boot web application.
* A user from the US sends a request.
* A user from the UK sends another request.

Your server:

* Assigns **Thread 1** to Request 1
* Assigns **Thread 2** to Request 2

Now both threads are making progress.

Even if you have only one CPU core, the system switches between threads quickly.

This allows your application to **handle multiple requests efficiently**.

Java provides tools for this in:

```
java.util.concurrent
```

Concurrency is about:

* Managing multiple tasks
* Coordinating them
* Making progress on all of them

---

# 🔹 What Is Parallelism?

Parallelism is about **doing multiple tasks at the exact same time**.

To achieve this, you need:

* Multiple CPU cores

---

## 🧮 Example: Sorting 6 Million Items

Imagine:

* You receive an array with **6 million elements**
* You need to sort it

If you use:

* One thread
* One CPU core

It will take time.

Instead, you could:

1. Split the array into 6 smaller arrays (1 million each)
2. Use 6 threads
3. Run them on 6 CPU cores

Now:

- All 6 threads execute **simultaneously**
- Work happens at the same time
- Total processing time decreases

This is **parallelism**.

---

## 👨‍🍳 Six Chefs Example

Instead of one chef switching between dishes:

Now you have **six chefs**, each preparing one dish at the same time.

That is parallelism.

---

# 🧠 Key Difference (The Core Idea)

Here’s the simplest mental model:

```
Concurrency  = Dealing with many tasks
Parallelism  = Doing many tasks at the same time
```

Or even simpler:

```
Concurrency = Switching
Parallelism = Simultaneous execution
```

---

# 📊 Visual Summary

## Concurrency (Single Core)

```
Time →
[Task A][Task B][Task A][Task B][Task A]
```

Only one runs at a time — but they all make progress.

---

## Parallelism (Multiple Cores)

```
Core 1: [Task A][Task A][Task A]
Core 2: [Task B][Task B][Task B]
```

Tasks truly run at the same time.

---

# ⚖️ Comparison Table

| Concurrency                         | Parallelism                  |
| ----------------------------------- | ---------------------------- |
| Broader concept                     | Specific type of concurrency |
| May use single CPU                  | Requires multiple CPUs/cores |
| Tasks make progress over time       | Tasks run simultaneously     |
| Focused on structure & coordination | Focused on performance       |

---

# 💡 Final Mental Model for Your Future Self

When you think:

* “How can I handle many users at once?” → **Concurrency**
* “How can I make this computation faster?” → **Parallelism**

---

# 🔥 One Last Important Insight

You can have:

* Concurrency without parallelism (1 core switching)
* Parallelism always implies concurrency

---

# Working with `Future` in Java

When using an **Executor (including a virtual thread executor)**, every time you submit a task, you get back a **`Future`**.

Think of a `Future` as:

> 📦 A placeholder for a result that will be available later.

---

## 1️⃣ Submitting a Task

When you submit a `Callable` to an executor:

```java
Future<String> future = executor.submit(() -> getUserInfo());
```

If `getUserInfo()` returns a `String`, then:

```java
Future<String>
```

Because `Future<T>` holds whatever the `Callable<T>` returns.

---

# 🔹 What Does `Future` Actually Do?

A `Future` lets you:

* Wait for the result
* Get the result
* Cancel the task
* Check if it’s done

---

# 2️⃣ `future.get()` — Wait for Result

```java
String result = future.get();
```

What happens here?

* The current thread **blocks**
* It waits until the task finishes
* Then it returns the result

So this:

```java
future.get();
```

means:

> “I’m willing to wait as long as it takes.”

⚠️ If the task takes 20 seconds… you wait 20 seconds.

---

# 3️⃣ `future.get(timeout)` — Wait, But Not Forever

Sometimes you don’t want to wait forever.

You can say:

```java
future.get(2, TimeUnit.SECONDS);
```

This means:

> “Wait at most 2 seconds.”

If the task doesn’t finish within 2 seconds:

* A `TimeoutException` is thrown

So you typically wrap it in a `try-catch`:

```java
try {
    String result = future.get(2, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    // fallback logic
}
```

This allows you to:

* Provide a default value
* Return partial data
* Log and move on
* Retry

Very useful in real-world systems.

---

# 4️⃣ `future.cancel(true)` — Stop the Task

You can also cancel a running task:

```java
future.cancel(true);
```

What does this do?

* Sends an **interrupt signal** to the thread
* If the task responds to interruption, it stops

Important:

> Cancellation works only if the task cooperates with interruption.

If your code ignores `InterruptedException`, cancellation may not work properly.

---

# 🧠 Mental Model

Think of `Future` like ordering food at a restaurant:

* You place the order → `submit()`
* You get a token → `Future`
* You can:

  * Wait until it's ready → `get()`
  * Wait only 2 minutes → `get(timeout)`
  * Cancel the order → `cancel()`

---

# ⚠️ Important Limitation of `Future`

`Future` is:

* Blocking
* Imperative
* Not composable
* Not elegant for chaining tasks

That’s why Java introduced:

👉 `CompletableFuture`

---

# 🔹 Why `CompletableFuture` Is Better

With `Future`, you write:

```java
String result = future.get();
process(result);
```

With `CompletableFuture`, you can write:

```java
future.thenApply(this::process)
      .thenAccept(System.out::println);
```

More:

* Declarative
* Functional style
* Non-blocking
* Easier composition
* Better error handling

We’ll go deeper into timeouts and chaining later.

---

# 📊 Quick Comparison

| Feature          | `Future` | `CompletableFuture` |
| ---------------- | -------- | ------------------- |
| Blocking         | Yes      | Optional            |
| Timeout support  | Yes      | Yes                 |
| Cancellation     | Yes      | Yes                 |
| Chaining         | No       | Yes                 |
| Functional style | No       | Yes                 |

---

# 🔥 Core Takeaway for Future You

`Future` is:

> A handle to a background computation.

It lets you:

* Wait
* Timeout
* Cancel

But it’s somewhat basic.

For modern async programming in Java:

👉 Use `CompletableFuture` whenever possible.

---

# 🧩 Aggregator Pattern (API Composition Pattern)

In real-world systems, we often have multiple backend services:

* 🛍 Product Service
* ⭐ Rating Service
* 💰 Pricing Service
* 📦 Inventory Service
* etc.

A frontend (browser/mobile app) should NOT call all of them individually.

Instead, we introduce an **Aggregator Service**.

This pattern is known as:

* **Gateway Aggregator Pattern**
* **API Composition Pattern**

---

# 🖼 Big Picture Architecture

```
Client (Browser)
        |
        v
  Aggregator Service
      /        \
     v          v
Product API   Rating API
```

The aggregator:

1. Receives a product ID
2. Calls multiple backend services
3. Combines their responses
4. Returns a single unified response

---

# 🧠 The Goal

If someone requests:

```
GET /product/42
```

The aggregator should internally call:

```
GET product-service/42
GET rating-service/42
```

Then return:

```json
{
  "id": 42,
  "description": "Heavy Duty Wool Bottle",
  "rating": 4
}
```

---

# 🏗 Step 1 — Product DTO

We create a simple immutable record:

```java
public record ProductDto(
    int id,
    String description,
    int rating
) {}
```

This represents the **combined response**.

---

# 🏗 Step 2 — Aggregator Service

This class:

* Accepts an `ExecutorService`
* Calls multiple services in parallel
* Combines results

```java
public class AggregatorService {

    private final ExecutorService executor;

    public AggregatorService(ExecutorService executor) {
        this.executor = executor;
    }

    public ProductDto getProduct(int id)
            throws ExecutionException, InterruptedException {

        var product = executor.submit(() -> Client.getProduct(id));
        var rating  = executor.submit(() -> Client.getRating(id));

        return new ProductDto(
                id,
                product.get(),
                rating.get()
        );
    }
}
```

---

# 🔥 What Is Important Here?

## ✅ These two lines run in parallel:

```java
var product = executor.submit(() -> Client.getProduct(id));
var rating  = executor.submit(() -> Client.getRating(id));
```

Because you're using:

```java
Executors.newVirtualThreadPerTaskExecutor()
```

Each task runs in its own **virtual thread**.

So:

* Product service call runs independently
* Rating service call runs independently
* Both execute at the same time

---

# ⏱ Visual Timeline

Instead of:

```
Call product (1s)
THEN call rating (1s)
Total = 2s
```

You get:

```
Call product  ────── 1s
Call rating   ────── 1s
Total ≈ 1s
```

Parallel execution.

---

# 🧪 Demo Code

```java
var executor = Executors.newVirtualThreadPerTaskExecutor();
var aggregator = new AggregatorService(executor);

LOGGER.info("product-42: {}", aggregator.getProduct(42));
```

---

# 📊 Output Analysis

```
[virtual-37] Calling product service
[virtual-39] Calling rating service
```

Notice:

* Two different virtual threads
* Both start at the same time

Then:

```
[main] product-42: ProductDto[id=42, description=..., rating=4]
```

The main thread waits for both `.get()` calls to complete.

---

# 🧠 Important Concept Here

You are combining:

### Concurrency

You submit multiple tasks and manage them.

### Parallelism

They actually run simultaneously (virtual threads).

### Aggregation

You merge multiple backend responses into one.

---

# ⚠️ One Subtle But Important Detail

Even though calls run in parallel, this line:

```java
product.get()
rating.get()
```

Is still **blocking**.

The main thread waits until both complete.

But because they started in parallel, you still gain performance.

---

# 💡 Why Virtual Threads Are Powerful Here

With traditional platform threads:

* Thousands of concurrent service calls = heavy

With virtual threads:

* Lightweight
* Cheap
* Perfect for I/O calls
* Ideal for microservice aggregation

This is a **very modern Java architecture style (Project Loom era).**

---

# 🧠 Mental Model for Future You

Think of the aggregator as:

> A waiter collecting dishes from multiple kitchens before serving the customer.

Each kitchen works independently.

The waiter waits until all dishes are ready.

Then serves one combined plate.

---

# 🔎 Where This Pattern Is Used

* API Gateways
* Backend-for-Frontend (BFF)
* Microservices architecture
* GraphQL resolvers
* E-commerce systems

---

# 🧩 If You Want to Improve This Later

Future improvements:

* Handle timeouts individually
* Add fallback values
* Use `CompletableFuture` instead
* Add error isolation
* Use structured concurrency (Java 21+)

---

# 🔥 Final Takeaway

This lecture demonstrates:

* Real-world use of concurrency
* Practical parallel service calls
* Aggregator pattern
* Virtual thread usage

This is not just “thread theory” anymore — this is production-style backend design.

---

This is exactly how smart studying looks:
You’re not just copying code — you’re extracting the **pattern + mental model**.

Let’s restructure this into something your future self will understand instantly.

---

# 🚀 New Requirement: Fetch 50 Products at Once

Previously:

* Client requested **1 product**
* Aggregator called:

  * Product service
  * Rating service
* Returned combined result

Now the requirement changed:

> Client sends **one request** asking for product IDs 1–50.

---

# 🧠 The Problem

Our backend services **do NOT support batch APIs**.

We CANNOT do this:

```http
GET /products?ids=1,2,3,...50 ❌
```

Instead, we must call:

```http
GET /product/1
GET /product/2
...
GET /product/50
```

And the same for ratings.

That means:

* 50 product calls
* 50 rating calls
* 100 total service calls

---

# ❌ If We Do This Sequentially

```text
Get product 1
Get product 2
...
Get product 50
```

If each takes 1 second:

```text
50 seconds total 😬
```

Not acceptable.

---

# ✅ The Strategy: Parallelize Everything

We use:

```java
Executors.newVirtualThreadPerTaskExecutor()
```

We submit **50 tasks in parallel**.

---

# 🖼 Big Architecture Picture

For 1 request from client:

```text
Client
   |
   v
Aggregator
   |
   |---- getProduct(1)
   |---- getProduct(2)
   |---- getProduct(3)
   |---- ...
   |---- getProduct(50)
```

But each `getProduct(id)` itself does:

```text
Product Service
Rating Service
```

So what really happens is:

```text
50 aggregator tasks
   ×
2 backend calls each
=
100 virtual threads running
```

🔥 And that’s totally fine with virtual threads.

---

# 🏗 Step 1 — Submit 50 Tasks

```java
var futures = IntStream.rangeClosed(1, 50)
        .mapToObj(id -> executor.submit(() -> aggregator.getProduct(id)))
        .toList();
```

### What This Does

1. Generates numbers 1–50
2. For each ID:

   * Submits a task
   * That task calls `aggregator.getProduct(id)`
3. Collects everything into:

```java
List<Future<ProductDto>>
```

So now we have:

```text
50 Future objects
```

Each one represents:

> “The product will be ready soon.”

---

# 🖼 Visual Timeline

Instead of this (sequential):

```text
[1][2][3][4]...[50]
```

We get this:

```text
[1][2][3][4]...[50]
 |  |  |  |
All running simultaneously
```

And inside each:

```text
Product Call   Rating Call
      \         /
       Combined Result
```

---

# 🏗 Step 2 — Collect Results

Now we convert:

```java
List<Future<ProductDto>>
```

into:

```java
List<ProductDto>
```

You wrote:

```java
List<ProductDto> products = futures.stream()
        .map(Lec04AggregatorDemo::toProductDto)
        .toList();
```

And the helper:

```java
private static ProductDto toProductDto(Future<ProductDto> future) {
    try {
        return future.get();
    } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
    }
}
```

---

# 🧠 Why This Helper Method Is Smart

Instead of messy try/catch inside lambda:

❌ Ugly:

```java
.map(f -> {
   try {
       return f.get();
   } catch(...) { ... }
})
```

✅ Clean:

```java
.map(Lec04AggregatorDemo::toProductDto)
```

This keeps your stream pipeline elegant.

That’s good engineering taste.

---

# 🧵 What’s Actually Happening with Threads?

Look at your logs:

```text
[virtual-59]
[virtual-88]
[virtual-113]
[virtual-164]
...
```

You see dozens of virtual threads created instantly.

This matches what we discussed earlier:

### Main thread:

* Submits 50 tasks

### Each aggregator task:

* Creates 2 more virtual threads (product + rating)

So:

```text
Main Thread
   └── 50 virtual threads
           └── each creates 2 virtual threads
```

Total ≈ 100 service calls in parallel.

---

# ⏱ Timing Observation

From your logs:

```text
23:25:15 → requests start
23:25:16 → all 50 results returned
```

That means:

👉 50 products fetched in ~1 second
👉 Instead of 50 seconds

This is the power of:

* Parallelism
* Virtual threads
* Aggregator pattern

---

# 🔥 Core Mental Model

Think of it like this:

Instead of:

> One delivery guy picking up 50 packages one by one

You now have:

> 50 delivery guys picking up 50 packages at the same time.

And each delivery guy:

> Visits two shops (product + rating) before delivering.

---

# 🧠 Important Concept You Just Demonstrated

This example combines:

* API Composition
* Concurrency
* Parallelism
* Virtual Threads
* Nested Task Submission
* Stream processing

This is real production-level backend design.

---

# ⚠️ One Subtle Detail

Even though tasks are parallel:

```java
future.get()
```

is still blocking.

But since all 50 tasks are already running:

* `get()` just waits for completion
* It doesn’t slow down total time significantly

---

# 📦 Final Abstraction Summary

## Problem

Need 50 products, no batch API.

## Solution

Fan out requests in parallel.

## Mechanism

* Virtual thread per task
* Submit 50 tasks
* Each task calls 2 services
* Collect results

## Result

~1 second instead of ~50 seconds.

---

# 🏁 What You Just Built

You implemented a:

> Concurrent fan-out / fan-in aggregator

Fan-out:

* Send many requests

Fan-in:

* Collect results into one list

This is a fundamental distributed systems pattern.

---

# 🚀 Parallel Task Execution: Common Pitfalls with Virtual Threads

## TL;DR (Too Long; Didn't Read)

When using `ExecutorService` to parallelize tasks, **submit ALL I/O operations to the executor at the same time**. Don't execute some synchronously while others run async. This defeats the purpose of parallelization.

```java
// ❌ WRONG - Sequential execution hidden in parallel code
var product = executor.submit(() -> Client.getProduct(id));
var rating = Client.getRating(id);  // Waits synchronously!
return new ProductDto(id, product.get(), rating);

// ✅ CORRECT - All tasks parallelized
var product = executor.submit(() -> Client.getProduct(id));
var rating = executor.submit(() -> Client.getRating(id));
return new ProductDto(id, product.get(), rating.get());
```

---

## The Problem: Logical But Inefficient Code

### Scenario: Aggregator Service

You're building a service that needs to fetch product information from two sources:

1. **Product details** from an API (takes 1 second)
2. **Rating information** from another API (takes 1 second)

You want to fetch them **in parallel** to get total response in ~1 second instead of 2.

---

## Version 1: ✅ CORRECT Approach

```java
public ProductDto getProduct(int id) throws ExecutionException, InterruptedException {
    // Submit BOTH tasks immediately
    var product = executor.submit(() -> Client.getProduct(id));
    var rating = executor.submit(() -> Client.getRating(id));

    // Then wait for both results
    return new ProductDto(
        id, 
        product.get(),  // Wait for product
        rating.get()    // Wait for rating
    );
}
```

### Timeline Visualization

```
Time →

Task 1 (getProduct):     |----1s----|
Task 2 (getRating):      |----1s----|

Total Time: ~1 second (parallel!)

Virtual Thread:
  submit(getProduct) → submit(getRating) → wait for both
```

### Execution Details

```
Thread Timeline:
├─ 0ms:   VT submits getProduct → assigned to PT-1
├─ 1ms:   VT submits getRating → assigned to PT-2
├─ 2ms:   VT calls product.get() → blocks waiting
│         (but can be paused and other VTs can run!)
├─ 1000ms: PT-1 finishes getProduct
├─ 1001ms: VT resumes, continues
├─ 1002ms: VT calls rating.get() → blocks waiting
└─ 2000ms: PT-2 finishes, VT gets result, returns
```

**Total elapsed time: ~1000ms** ✅

---

## Version 2: ❌ WRONG Approach (First Refactoring)

Someone thinks: "Why create two child threads if one thread can just wait? Let's optimize!"

```java
public ProductDto getProduct(int id) throws ExecutionException, InterruptedException {
    // Submit only ONE task
    var product = executor.submit(() -> Client.getProduct(id));
    
    // Execute rating synchronously (on the calling thread!)
    var rating = Client.getRating(id);

    return new ProductDto(
        id, 
        product.get(), 
        rating  // Already have the result
    );
}
```

### Timeline Visualization

```
Time →

Task 1 (getProduct):     |----1s----|
Task 2 (getRating):                    |----1s----|

Total Time: ~2 seconds (NOT parallel!)

Virtual Thread:
  submit(getProduct) → getRating() [BLOCKS HERE] → wait for product
```

### The Problem Illustrated

```
Timeline:
├─ 0ms:    VT submits getProduct → assigned to PT-1
├─ 1ms:    VT calls Client.getRating(id) → BLOCKS
│          (waiting for API response, wasting time!)
│          Meanwhile PT-1 is getting the product...
├─ 1000ms: PT-1 finishes getProduct
│          BUT VT is still blocked in getRating!
├─ 1001ms: getRating finally completes
├─ 1002ms: VT calls product.get() → immediately returns
└─ 2000ms: Method returns
```

**Total elapsed time: ~2000ms** ❌

### Why This Seems Fine at First

In the logs, you might see:
```
[2131] Submitted product request
[2131] Got rating response      ← Looks quick!
[2132] Got product response
```

It looks like everything happened fast, but that's **misleading**! The actual API calls took 2 seconds sequentially.

---

## Version 3: ❌ EVEN WORSE Approach (Second Refactoring)

Someone thinks: "Why store the rating in a variable? Let's inline it!"

```java
public ProductDto getProduct(int id) throws ExecutionException, InterruptedException {
    var product = executor.submit(() -> Client.getProduct(id));

    return new ProductDto(
        id, 
        product.get(), 
        Client.getRating(id)  // ← Called AFTER product.get()!
    );
}
```

### Timeline Visualization

```
Time →

Task 1 (getProduct):     |----1s----|
Task 2 (getRating):                 |----1s----|

Total Time: ~2 seconds (SEQUENTIAL!)

Virtual Thread:
  submit(getProduct) → wait for product → THEN getRating()
```

### The Problem Is Even Worse!

```
Timeline:
├─ 0ms:    VT submits getProduct → assigned to PT-1
├─ 1ms:    VT calls product.get() → blocks
├─ 1000ms: PT-1 finishes, VT resumes
├─ 1001ms: VT calls Client.getRating(id) → blocks
│          (NOW we start the rating request!)
│          Meanwhile PT-1 is idle...
└─ 2000ms: getRating completes, method returns
```

**Total elapsed time: ~2000ms** ❌❌

Notice: **Ratings request only starts AFTER product.get() completes!**

---

## Visual Comparison: All Three Versions

### Version 1: CORRECT ✅

```
Virtual Thread A (calling thread):
├─ submit(product) ──────┐
├─ submit(rating) ───────┤
├─ product.get() ────────┼────────→ (wait in parallel)
└─ rating.get() ─────────┼────────→ (wait in parallel)

Platform Thread 1: [getProduct ___1000ms___]
Platform Thread 2:                [getRating ___1000ms___]

Timeline: 0ms ──────── 1000ms ──────── 2000ms
                   ✅ ~1000ms total
```

### Version 2: WRONG (Sequential Hidden) ❌

```
Virtual Thread A (calling thread):
├─ submit(product) ──────────────┐
├─ getRating() [BLOCKS HERE] ────┤
├─ product.get() ────────────────┤
└─ return ──────────────────────┘

Platform Thread 1: [getProduct ___1000ms___]
Platform Thread 2:                [getRating ___1000ms___]

Timeline: 0ms ──────── 1000ms ──────── 2000ms
                   ❌ ~2000ms total (but looks fast in logs!)
```

### Version 3: WORST (Sequential Clear) ❌❌

```
Virtual Thread A (calling thread):
├─ submit(product) ──────────────────┐
├─ product.get() [BLOCKS] ───────────┤
├─ getRating() [BLOCKS] ──────────────┤
└─ return ──────────────────────────┘

Platform Thread 1: [getProduct ___1000ms___]
Platform Thread 2:                [getRating ___1000ms___]

Timeline: 0ms ──────── 1000ms ──────── 2000ms
                   ❌ ~2000ms total (sequential!)
```

---

## Why This Matters in Real Life

### Development Environment (Visible Problem)

```
Local machine with fast network (1ms latency):
- Expected time: ~1ms
- Version 1: ✅ ~1ms (correct)
- Version 2: ❌ ~2ms (slightly slower, might not notice)
- Version 3: ❌ ~2ms (slightly slower, might not notice)

→ You push to production thinking it's fine
```

### Production Environment (Hidden Problem)

```
Production with slow network (500ms latency):
- Expected time: ~500ms
- Version 1: ✅ ~500ms (correct)
- Version 2: ❌ ~1000ms (2x slower!)
- Version 3: ❌ ~1000ms (2x slower!)

→ Now you have timeout issues, customer complaints
→ Only then do you realize the mistake
```

### Under Heavy Load (Catastrophic)

```
Version 1 (Correct): Can handle 10,000 concurrent users
Version 2/3 (Wrong): Threads get blocked waiting, context switching increases,
                     cascading failures, performance degradation
```

---

## The Core Lesson: Eager Submission

### Golden Rule

> **Submit all independent I/O tasks to the executor IMMEDIATELY, before waiting for any result.**

### Pattern: Eager Submission

```java
// ✅ GOOD PATTERN
Future<T1> result1 = executor.submit(task1);  // Submit
Future<T2> result2 = executor.submit(task2);  // Submit
Future<T3> result3 = executor.submit(task3);  // Submit
// ← Now all 3 are running in parallel!

T1 value1 = result1.get();  // Wait
T2 value2 = result2.get();  // Wait
T3 value3 = result3.get();  // Wait
// ← Now all results are available
```

### Anti-Pattern: Lazy Submission

```java
// ❌ BAD PATTERN
Future<T1> result1 = executor.submit(task1);
T2 value2 = blockingCall();          // ← Blocks here!
                                      // task1 is running, but task2 started late
T3 value3 = blockingCall();          // ← Blocks here!
                                      // task2 is running, but task3 started even later
```

---

## Real-World Example: Ecommerce API

### Scenario

You're building an endpoint that returns:
- Product details
- User reviews
- Inventory status
- Recommendation suggestions

Each call takes 500ms.

### ❌ WRONG: Sequential Calls

```java
@GetMapping("/product/{id}")
public ProductResponse getProduct(@PathVariable int id) {
    var productDetails = apiClient.getProductDetails(id);      // 500ms
    var reviews = apiClient.getReviews(id);                    // 500ms
    var inventory = apiClient.getInventory(id);                // 500ms
    var recommendations = apiClient.getRecommendations(id);    // 500ms
    
    // Total: 2000ms! 😱
    return new ProductResponse(productDetails, reviews, inventory, recommendations);
}
```

### ✅ CORRECT: Parallel with ExecutorService

```java
@GetMapping("/product/{id}")
public ProductResponse getProduct(@PathVariable int id) throws ExecutionException, InterruptedException {
    // STEP 1: Submit ALL tasks immediately
    var productDetails = executor.submit(() -> apiClient.getProductDetails(id));
    var reviews = executor.submit(() -> apiClient.getReviews(id));
    var inventory = executor.submit(() -> apiClient.getInventory(id));
    var recommendations = executor.submit(() -> apiClient.getRecommendations(id));
    
    // STEP 2: Wait for all results
    return new ProductResponse(
        productDetails.get(),
        reviews.get(),
        inventory.get(),
        recommendations.get()
    );
    
    // Total: ~500ms! ✅
}
```

### Performance Comparison

```
Sequential (❌):    [-----500ms-----][-----500ms-----][-----500ms-----][-----500ms-----]
                    Total: 2000ms

Parallel (✅):      [-----500ms-----]
                    [-----500ms-----]
                    [-----500ms-----]
                    [-----500ms-----]
                    Total: ~500ms

Speedup: 4x faster! 🚀
```

---

## Common Refactoring Mistakes

### Mistake 1: Removing "Unnecessary" Variables

```java
// Before (correct)
var product = executor.submit(() -> Client.getProduct(id));
var rating = executor.submit(() -> Client.getRating(id));
return new ProductDto(id, product.get(), rating.get());

// After (WRONG - but looks like optimization!)
var product = executor.submit(() -> Client.getProduct(id));
return new ProductDto(id, product.get(), Client.getRating(id));
```

**Why this breaks:** The second `executor.submit()` is removed, so `getRating()` runs synchronously and sequentially!

### Mistake 2: Inlining for "Cleaner" Code

```java
// WRONG: Inlining makes it sequential!
return new ProductDto(
    id,
    executor.submit(() -> Client.getProduct(id)).get(),  // Wait
    Client.getRating(id)                                  // THEN call this
);
```

### Mistake 3: Mixing Executor and Direct Calls

```java
// WRONG: Confusing pattern
var product = executor.submit(() -> Client.getProduct(id));
var userDetails = userService.getDetails(id);  // ← Direct call?
return product.get() + userDetails;
```

**Question:** Is `userService.getDetails()` an I/O call? Should it also be in executor?

---

## When to Use ExecutorService

### Perfect For:
```
✅ Multiple independent I/O operations
✅ API calls to different services
✅ Database queries
✅ File I/O operations
✅ External service calls
```

### Not Ideal For:
```
❌ Single I/O operation
❌ CPU-bound operations (parallelism won't help much)
❌ Operations with dependencies (Task B must wait for Task A)
```

### Example with Dependencies

```java
// Dependencies: Can't parallelize everything
// Task 2 depends on Task 1's result

// Wrong attempt:
var task1 = executor.submit(() -> apiCall1());
var task2 = executor.submit(() -> apiCall2(task1.get()));  // ← Defeats purpose!

// Better approach:
var task1Result = executor.submit(() -> apiCall1()).get();
var task2 = executor.submit(() -> apiCall2(task1Result));
```

---

## Best Practices Checklist

### Before You Submit Code

- [ ] **Are all independent I/O tasks submitted to executor immediately?**
  ```java
  ✅ submit() → submit() → submit() → get() → get() → get()
  ❌ submit() → get() → submit() → get()
  ```

- [ ] **Are synchronous calls made after all submissions?**
  ```java
  ✅ // All submissions first
     var t1 = executor.submit(...);
     var t2 = executor.submit(...);
     // Then all waits
     t1.get(); t2.get();
  
  ❌ // Mixing submit and sync calls
     var t1 = executor.submit(...);
     syncCall();  // ← Wrong order!
     t1.get();
  ```

- [ ] **Did you avoid inlining executor.submit() calls in method arguments?**
  ```java
  ✅ var result = executor.submit(...).get();
  
  ❌ new DTO(executor.submit(...).get(), syncCall());
  ```

- [ ] **Do all tasks run in parallel, or are some sequential?**
  ```
  ✅ All tasks: submit, submit, submit, wait, wait, wait
  ❌ Task 1: submit → wait, Task 2: sync call
  ```

---

## Virtual Threads Make This Easier (But Don't Ignore It!)

### Why Virtual Threads Are Great

Virtual threads are **lightweight**, so you can:
- Create many of them
- Let them block (waiting for I/O)
- The scheduler handles parking/unparking automatically

```java
// With VT, you can create one per request without worry
Thread.ofVirtual().start(() -> handleRequest());
```

### But You Still Need Proper Parallelization Logic!

Even with virtual threads, **submitting tasks eagerly is crucial**:

```java
// ✅ Still the right approach with VT
var product = executor.submit(() -> Client.getProduct(id));
var rating = executor.submit(() -> Client.getRating(id));
return new ProductDto(id, product.get(), rating.get());

// ❌ Still wrong with VT
var product = executor.submit(() -> Client.getProduct(id));
var rating = Client.getRating(id);  // ← Defeats parallelization!
return new ProductDto(id, product.get(), rating);
```

---

## Summary: Side-by-Side Comparison

| Aspect | Version 1 ✅ | Version 2 ❌ | Version 3 ❌❌ |
|--------|------------|------------|-------------|
| **Code** | Both tasks via executor | One task via executor | One task via executor |
| **Parallelization** | Full ✅ | Partial ❌ | None ❌ |
| **Time (ideal)** | ~1s | ~1s | ~1s |
| **Time (realistic)** | ~1s ✅ | ~2s ❌ | ~2s ❌ |
| **Production behavior** | Scales well ✅ | Degrades under load ❌ | Degrades under load ❌ |
| **Code clarity** | Crystal clear ✅ | Confusing ⚠️ | Very confusing ❌ |

---

## Key Takeaways

1. **Submit first, wait later:** Get all tasks started before waiting for any
2. **Avoid "optimizations" that break parallelization:** Extra variables and submissions are cheap
3. **Test with realistic latencies:** Development might hide the issue
4. **Virtual threads are great, but don't replace proper parallelization logic**
5. **Clear code > "optimized" code that's actually sequential**

---

## Practice Exercise

Fix this code to properly parallelize all 4 API calls:

```java
public UserProfile getUserProfile(String userId) throws ExecutionException, InterruptedException {
    var user = executor.submit(() -> userService.getUser(userId));
    var posts = postService.getUserPosts(userId);
    var friends = executor.submit(() -> friendService.getFriends(userId));
    var preferences = preferenceService.getPreferences(userId);
    
    return new UserProfile(
        user.get(),
        posts,
        friends.get(),
        preferences
    );
}
```

**Answer:**
```java
public UserProfile getUserProfile(String userId) throws ExecutionException, InterruptedException {
    // Step 1: Submit ALL tasks
    var user = executor.submit(() -> userService.getUser(userId));
    var posts = executor.submit(() -> postService.getUserPosts(userId));
    var friends = executor.submit(() -> friendService.getFriends(userId));
    var preferences = executor.submit(() -> preferenceService.getPreferences(userId));
    
    // Step 2: Wait for ALL results
    return new UserProfile(
        user.get(),
        posts.get(),
        friends.get(),
        preferences.get()
    );
}
```

---

## Further Reading

- [Java ExecutorService Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html)
- [Java CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) (alternative approach)
- [Virtual Threads in Java 21](https://openjdk.org/jeps/444)

---

**Remember:** Virtual threads make concurrency easier, but they don't make parallelization logic disappear. Always submit independent tasks eagerly! 🚀

---

# 🧹 AutoCloseable & ExecutorService Lifecycle: Virtual Threads vs Platform Threads

## TL;DR

**Platform Threads:** Require explicit `shutdownNow()` or the app keeps running (busy threads block exit)  
**Virtual Threads:** Don't require shutdown in many cases (they're lightweight and the main thread's blocking calls naturally halt the app)

```java
// ❌ WITHOUT try-with-resources (manual cleanup)
var executor = Executors.newSingleThreadExecutor();
executor.submit(() -> doWork());
executor.shutdownNow();  // ← Must do this manually

// ✅ WITH try-with-resources (automatic cleanup)
try (var executor = Executors.newSingleThreadExecutor()) {
    executor.submit(() -> doWork());
}  // ← Automatically calls shutdown()
```

---

## The Problem: Why Do We Need Shutdown?

### What Happens Without Shutdown?

When you submit tasks to an `ExecutorService`, you're creating **background threads**. These threads can prevent your application from exiting!

```java
public static void withoutAutoCloseable() {
    var executorService = Executors.newSingleThreadExecutor();
    executorService.submit(Lec01AutoCloseable::task);
    LOGGER.info("submitted");
    // ❌ Application does NOT exit here!
    // The executor thread is still running in the background
}

// Main thread reaches the end of main()
// But the executor thread is still alive
// → Application hangs indefinitely
```

### Why Threads Block Application Exit

In Java, an application exits when:

```
All non-daemon threads have finished
```

If you have a thread pool with active threads:
```
main() {
    executor = new ThreadPool()
    executor.submit(task)  // Creates a NON-DAEMON thread
    println("done")       
}  // ← Main thread finishes here

// But executor thread is still running!
// Application waits for it to finish
// → Application never exits
```

### Visualization

```
Main Thread:  [main] → submit() → println("done") → END
                                        ↓
Executor Thread: ────[execute task]─────────→ (still running!)
                                        ↓
Application Status: ⏸️  WAITING (won't exit)
```

---

## The Solution: AutoCloseable & Try-With-Resources

### Understanding AutoCloseable

The `ExecutorService` interface extends `AutoCloseable`:

```java
public interface ExecutorService extends Executor, AutoCloseable {
    // ... other methods
    
    @Override
    void close();  // ← Closes resources
}
```

### The Try-With-Resources Pattern

Java provides automatic resource management:

```java
try (var executorService = Executors.newSingleThreadExecutor()) {
    executorService.submit(Lec01AutoCloseable::task);
    executorService.submit(Lec01AutoCloseable::task);
    executorService.submit(Lec01AutoCloseable::task);
    LOGGER.info("submitted");
}  // ← Automatically calls executorService.close()
```

### What Happens

```
Step 1: Enter try block
    └─ Create executor
    └─ Submit tasks

Step 2: Exit try block (explicitly or via exception)
    └─ Automatically call close()
    └─ Which calls shutdown() internally
    └─ Waits for all tasks to complete
    └─ Terminates threads

Step 3: Application can now exit
```

### How `close()` Works for ExecutorService

```java
// Internally, ExecutorService.close() does something like:
public void close() {
    shutdown();  // Stop accepting new tasks
    try {
        if (!awaitTermination(long timeout, TimeUnit unit)) {
            shutdownNow();  // Force stop if timeout
        }
    } catch (InterruptedException e) {
        shutdownNow();
        Thread.currentThread().interrupt();
    }
}
```

---

## Comparison: With vs Without AutoCloseable

### Without AutoCloseable ❌

```java
public static void withoutAutoCloseable() {
    var executorService = Executors.newSingleThreadExecutor();
    executorService.submit(Lec01AutoCloseable::task);
    LOGGER.info("submitted");
    // ❌ Forgot to shutdown!
}

// Timeline:
// 0ms:  Task submitted
// 1ms:  "submitted" logged
// 3ms:  Main method returns
// 4ms:  Main thread finishes
// ⏸️   Application HANGS - executor thread still running!
// Eventually: Timeout or manual kill
```

### With AutoCloseable ✅

```java
public static void withAutoCloseable() {
    try (var executorService = Executors.newSingleThreadExecutor()) {
        executorService.submit(Lec01AutoCloseable::task);
        executorService.submit(Lec01AutoCloseable::task);
        executorService.submit(Lec01AutoCloseable::task);
        LOGGER.info("submitted");
    }  // ← Auto shutdown happens here
}

// Timeline:
// 0ms:  All tasks submitted
// 1ms:  "submitted" logged
// 3ms:  Exit try block
// 4ms:  close() called automatically
// 5ms:  shutdown() waits for tasks
// 1005ms: All tasks complete
// 1006ms: shutdown() returns
// 1007ms: Main thread finishes
// ✅    Application exits cleanly
```

---

## The Key Difference: Platform Threads vs Virtual Threads

### Platform Threads (Heavy & Expensive)

Platform threads are **daemon-aware** but default to **non-daemon**:

```java
public class ExecutorThread {
    public ExecutorThread() {
        Thread t = new Thread(...);
        t.setDaemon(false);  // ← Non-daemon by default!
    }
}

// Non-daemon threads prevent JVM exit
```

#### Without Shutdown: Application Hangs

```
Main thread:    [code] → [end]
                          ↓
Platform thread: [work................................] (still going!)
                          ↓
JVM Status: ⏸️ WAITING FOR NON-DAEMON THREADS
```

#### With Shutdown: Application Exits

```
Main thread:    [code] → [shutdown()] → [end]
                          ↓
Platform thread: [work] → [interrupted] → [stopped]
                          ↓
JVM Status: ✅ ALL THREADS DONE - EXIT
```

---

### Virtual Threads (Lightweight & Daemon-Like)

Virtual threads behave **more like daemon threads** in practice:

```
Main thread:    [code] → [blocking I/O] → [end]
                          ↓
Virtual threads: [work] → [parked] → [resumed]
                          ↓
JVM Status: ✅ MAIN THREAD BLOCKED (not waiting for VTs)
```

**Key insight:** Since the main thread is **blocking on `future.get()`**, the JVM waits for that to complete. Once the main thread's `future.get()` returns, it can exit.

---

## Real-World Example: The AggregatorDemo

### The Code

```java
static void main(String[] args) throws InterruptedException, ExecutionException {
    var executor = Executors.newVirtualThreadPerTaskExecutor();
    var aggregator = new AggregatorService(executor);

    LOGGER.info("product-42: {}", aggregator.getProduct(42));

    var futures = IntStream.rangeClosed(1, 50)
            .mapToObj(id -> executor.submit(() -> aggregator.getProduct(id)))
            .toList();

    List<ProductDto> products = futures.stream()
        .map(Lec04AggregatorDemo::toProductDto)
        .toList();
    
    LOGGER.info("list: {}", products);
    // ✅ Application exits here (NO shutdown needed!)
}
```

### Why It Works Without Shutdown

```
Execution Timeline:

1. Submit task for product-42
2. Wait for result (future.get() - blocking!)
3. Submit 50 more tasks
4. Store futures in list
5. Process futures with toProductDto()
   └─ For each future:
      └─ Call future.get() - BLOCKING WAIT
      └─ Virtual thread yields, main thread blocks
      └─ Virtual thread completes
      └─ Main thread resumes with result
6. Log results
7. Main method ends
   └─ Main thread finishes
   └─ All virtual threads already finished (via future.get())
   └─ ✅ JVM can exit (no non-daemon threads left)
```

### Visual Timeline

```
Thread 1 (Main):
  ├─ aggregator.getProduct(42)
  │  └─ future.get() ┐
  │                  ├─ [BLOCKING WAIT]
  │                  │  (while VT1 runs)
  │                  └─ ✅ Result returned
  │
  ├─ Create futures for 50 products
  │
  └─ futures.stream().map(toProductDto)
     │
     ├─ future.get() ┐
     │               ├─ [BLOCKING WAIT]
     │               │  (while VT2-51 run)
     │               └─ ✅ Result returned
     │
     └─ ... repeat for all 50

Virtual Threads 1-51:
  ├─ VT1: [execute getProduct(42)]
  ├─ VT2: [execute getProduct(1)]
  ├─ VT3: [execute getProduct(2)]
  └─ ... VT51: [execute getProduct(50)]

Final: All VTs complete → Main thread continues → Exits cleanly ✅
```

### Why This Works With Virtual Threads

1. **No daemon/non-daemon distinction matters** - VTs are lightweight
2. **Main thread blocks** on `future.get()` explicitly
3. **By the time main thread finishes**, all VTs have already completed
4. **No background threads left** to block the exit

---

## Contrast: If We Used Platform Threads

```java
static void main(String[] args) {
    var executor = Executors.newFixedThreadPool(10);  // ← Platform threads
    
    var futures = IntStream.rangeClosed(1, 50)
            .mapToObj(id -> executor.submit(() -> aggregator.getProduct(id)))
            .toList();

    List<ProductDto> products = futures.stream()
        .map(Lec04AggregatorDemo::toProductDto)
        .toList();
    
    LOGGER.info("list: {}", products);
    
    // ❌ DANGER: Without shutdown, application hangs!
    // The 10 platform threads are still alive
    // JVM won't exit until executor.shutdownNow() is called
    
    // ✅ Must do this:
    executor.shutdown();  // or shutdownNow()
}
```

---

## When Do You Need Shutdown?

### Short-Lived Applications (One-Off Tasks)

```
Apps that:
├─ Start
├─ Do work
└─ Exit immediately

❌ WITHOUT shutdown: Application hangs
✅ WITH shutdown: Cleans up properly

Example: CLI tools, batch jobs, scripts
```

### Long-Running Applications (Servers)

```
Apps that:
├─ Start (e.g., Spring Boot web server)
├─ Accept requests indefinitely
└─ Graceful shutdown on signal (Ctrl+C)

ℹ️  Usually the framework handles shutdown
   (Spring manages ExecutorService lifecycle)

Example: REST APIs, web servers, microservices
```

### Virtual Threads Special Case

```
Because virtual threads are lightweight:

✅ Try-with-resources still recommended
   (explicit resource management)
   
❌ But if you forget, it might not hang
   (because of how main thread blocks on future.get())
   
⚠️  STILL: Don't rely on this!
   Always use try-with-resources for clarity
```

---

## Best Practices

### ✅ ALWAYS Use Try-With-Resources

```java
public static void bestPractice() {
    try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
        var future = executorService.submit(() -> doWork());
        var result = future.get();
        System.out.println(result);
    }  // ← Automatically shutdown
}
```

**Benefits:**
- Explicit resource cleanup
- Works whether you forget or not
- Clear intent (resource used in this block)
- Exception-safe (cleanup even if exception occurs)
- Readable and maintainable

### ✅ Use Try-With-Resources + Proper Timeout

```java
public static void withTimeout() {
    try (var executorService = Executors.newVirtualThreadPerTaskExecutor()) {
        var future = executorService.submit(() -> doWork());
        var result = future.get(5, TimeUnit.SECONDS);  // ← Timeout!
        System.out.println(result);
    } catch (TimeoutException e) {
        LOGGER.error("Task took too long!");
    }
}
```

### ✅ Spring Boot Example (Framework Manages It)

```java
@Configuration
public class ExecutorConfig {
    
    @Bean
    public ExecutorService executorService() {
        // Spring will call close() on shutdown
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

@Service
public class MyService {
    private final ExecutorService executor;
    
    public MyService(ExecutorService executor) {
        this.executor = executor;  // ← Injected, managed by Spring
    }
    
    public void doWork() {
        executor.submit(() -> heavyWork());
        // No manual shutdown needed (Spring handles it)
    }
}
```

---

## Common Mistakes

### ❌ Mistake 1: Forgetting Shutdown (Platform Threads)

```java
public static void bad() {
    var executor = Executors.newFixedThreadPool(10);
    executor.submit(() -> doWork());
    LOGGER.info("submitted");
    // ❌ No shutdown! Application hangs
}
```

**Fix:**
```java
public static void good() {
    try (var executor = Executors.newFixedThreadPool(10)) {
        executor.submit(() -> doWork());
        LOGGER.info("submitted");
    }  // ✅ Auto shutdown
}
```

### ❌ Mistake 2: Shutdown Waits Forever

```java
public static void problematic() {
    var executor = Executors.newFixedThreadPool(10);
    executor.submit(() -> {
        while (true) {  // ← Infinite loop!
            doWork();
        }
    });
    executor.shutdown();
    executor.awaitTermination(5, TimeUnit.SECONDS);
    // ⏱️ Waits 5 seconds, then times out
}
```

**Fix:**
```java
public static void better() {
    try (var executor = Executors.newFixedThreadPool(10)) {
        executor.submit(this::finiteTask);
        // Task completes normally
    }  // ✅ Shutdown works immediately
}

private void finiteTask() {
    // Do work and return (not infinite)
}
```

### ❌ Mistake 3: Creating Executor in Loop

```java
public static void terrible() {
    for (int i = 0; i < 100; i++) {
        var executor = Executors.newFixedThreadPool(10);
        executor.submit(() -> doWork());
        // ❌ Forgot to shutdown! 100 executors × 10 threads = 1000 threads!
        // Application definitely hangs
    }
}
```

**Fix:**
```java
public static void good() {
    try (var executor = Executors.newFixedThreadPool(10)) {
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> doWork());
        }
    }  // ✅ One executor, reused 100 times, properly shutdown
}
```

---

## Understanding the AggregatorDemo More Deeply

### Why Blocking `.get()` is Key

```java
// This is the crucial part:
List<ProductDto> products = futures.stream()
    .map(future -> future.get())  // ← BLOCKING WAIT
    .toList();

// What happens:
// 1. Main thread calls future.get()
// 2. Main thread BLOCKS waiting for virtual thread to complete
// 3. Virtual thread executes the task
// 4. Virtual thread completes
// 5. Main thread UNBLOCKS and continues
// 6. Main thread processes next future

// Because main thread is BLOCKED:
// - JVM won't exit until main finishes
// - All virtual threads complete before main finishes
// - No need for explicit shutdown
```

### The flow

```
Main Thread Timeline:

Start
├─ Submit VT1, VT2, VT3, ..., VT50
├─ Store futures in list
├─ futures.stream().map(f -> f.get())
│  ├─ VT1: future.get() → [BLOCK] ← Main thread waits
│  │         ...VT1 executes...
│  │         ✅ VT1 done
│  │  → [UNBLOCK] Continue with next
│  ├─ VT2: future.get() → [BLOCK]
│  │         ...VT2 executes...
│  │         ✅ VT2 done
│  │  → [UNBLOCK] Continue with next
│  └─ ... repeat for all 50 VTs
├─ Log results
└─ End of main() → Exit cleanly ✅
```

### If We Didn't Block (Different Scenario)

```java
// Hypothetical scenario without blocking:
var futures = IntStream.rangeClosed(1, 50)
    .mapToObj(id -> executor.submit(() -> aggregator.getProduct(id)))
    .toList();

LOGGER.info("submitted 50 tasks");
// ❌ Don't call future.get() here!
// ❌ Just log and return

// What happens:
// Main thread: 0ms - submit tasks
// Main thread: 1ms - log
// Main thread: 2ms - END (main method returns)
// Virtual threads: still running in background
// ⏸️ JVM behavior: ???
//    (VTs might be daemon-like, might not)
//    (Unpredictable - AVOID THIS!)
```

---

## Summary Table

| Aspect | Platform Threads | Virtual Threads |
|--------|-----------------|-----------------|
| **Thread Weight** | Heavy (1-2 MB stack) | Light (~KB) |
| **Shutdown Required** | ✅ YES (else hangs) | ℹ️ RECOMMENDED (for clarity) |
| **Without Shutdown** | ❌ App hangs | ⚠️ Might work (but unclear) |
| **Blocking get()** | Blocks main thread | Blocks main thread |
| **Best Practice** | Always use try-with-resources | Always use try-with-resources |
| **Complexity** | Pool management needed | Can be more casual |
| **Memory Footprint** | High | Low |

---

## Key Takeaways

1. **AutoCloseable + Try-With-Resources = Automatic Cleanup**
   - No manual shutdown calls
   - Exception-safe
   - Clear code intent

2. **Platform Threads Hang Without Shutdown**
   - Non-daemon by default
   - Keep JVM alive
   - Will cause application to hang indefinitely

3. **Virtual Threads Are Lighter**
   - But still need cleanup (for resource management)
   - Main thread blocking on `future.get()` naturally ensures proper exit
   - Don't rely on this behavior - be explicit!

4. **Best Practice: Always Use Try-With-Resources**
   ```java
   try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
       // Use executor
   }  // Auto cleanup
   ```

5. **For Long-Running Apps**
   - Frameworks (Spring, Jakarta) handle shutdown gracefully
   - Don't need to worry about manual cleanup

6. **For Short-Lived Apps**
   - Use try-with-resources
   - Ensures clean exit
   - No hanging processes

---

## Visual Cheat Sheet

### When to Shutdown?

```
✅ ALWAYS:
  └─ Explicitly managed executor in your code
     try (var executor = ...) { ... }

ℹ️ SOMETIMES:
  └─ Framework-managed (Spring handles it)

❌ NEVER:
  └─ Rely on implicit behavior or hope
```

### The Rule

```
Create Executor
    ↓
Use Try-With-Resources
    ↓
Auto Shutdown Happens
    ↓
Resources Cleaned Up
    ↓
Application Exits Cleanly ✅
```

---

**Remember:** Virtual threads make concurrency easier, but resource management is still important. Use try-with-resources and let Java do the cleanup automatically! 🧹✨

---

# 📝 Virtual Thread Naming & ThreadFactory: Observability and Debugging

## TL;DR

By default, virtual threads have **no meaningful names**. Use `ThreadFactory` to give them descriptive names for better observability and debugging.

```java
// ❌ Default: Cryptic names like [virtual-41]
var executor = Executors.newVirtualThreadPerTaskExecutor();

// ✅ Better: Descriptive names like [aggregator-demo-1]
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("aggregator-demo-", 1)
        .factory()
);
```

---

## The Problem: Virtual Threads Are Invisible by Default

### Default Behavior: Unnamed Threads

When you create an executor without custom naming:

```java
var executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> Client.getProduct(43));
executor.submit(() -> Client.getProduct(41));
executor.submit(() -> Client.getProduct(42));
```

**Output:**
```
22:29:23.925 [virtual-41] INFO Client -- Calling external service for product 43
22:29:23.925 [virtual-37] INFO Client -- Calling external service for product 41
22:29:23.925 [virtual-39] INFO Client -- Calling external service for product 42
22:29:24.947 [main] INFO -- product-41: Durable Marble Clock
22:29:24.947 [main] INFO -- product-42: Mediocre Aluminum Hat
22:29:24.947 [main] INFO -- product-43: Practical Marble Bag
```

### The Problem

```
Thread names like [virtual-41], [virtual-37], [virtual-39]...
                        ↓
❌ Not meaningful
❌ Hard to correlate in logs
❌ Difficult to debug
❌ No semantic meaning
❌ Next time app runs, numbers change
```

---

## Understanding ThreadFactory

### What is ThreadFactory?

A `ThreadFactory` is responsible for **creating thread instances**. It's a simple interface:

```java
public interface ThreadFactory {
    Thread newThread(Runnable r);
}
```

**Purpose:**
- Customize thread creation
- Add naming conventions
- Set thread properties (priority, daemon status, etc.)
- Apply organization-wide standards

### How Executors Use ThreadFactory

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
// Internally uses a default ThreadFactory that creates threads with generic names
```

### Creating a Custom ThreadFactory

The traditional way (for platform threads):

```java
public class MyThreadFactory implements ThreadFactory {
    private int counter = 0;
    private final String namePrefix;
    
    public MyThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(namePrefix + "-" + (++counter));
        return t;
    }
}

// Usage:
ExecutorService executor = Executors.newFixedThreadPool(
    10, 
    new MyThreadFactory("worker")
);
```

---

## Virtual Thread Fluent API

### The Elegant Way: Thread.ofVirtual()

Java 21+ provides a fluent API to create virtual thread factories:

```java
Thread.Builder.OfVirtual factory = Thread.ofVirtual()
    .name("aggregator-demo-", 1);  // "aggregator-demo-1", "aggregator-demo-2", ...

ThreadFactory threadFactory = factory.factory();
```

### Breaking It Down

```java
Thread.ofVirtual()
    .name("prefix-", 1)  // Name prefix + starting number
    .factory()           // Convert to ThreadFactory
```

### What `.name("prefix-", 1)` Does

```
First thread:  "prefix-1"
Second thread: "prefix-2"
Third thread:  "prefix-3"
...

The second parameter (1) is the STARTING NUMBER
```

---

## Side-by-Side Comparison

### Version 1: Default (No Custom Names) ❌

```java
public class DefaultNaming {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        
        executor.submit(() -> {
            System.out.println("Running task 1 on: " + Thread.currentThread().getName());
            sleep(100);
        });
        
        executor.submit(() -> {
            System.out.println("Running task 2 on: " + Thread.currentThread().getName());
            sleep(100);
        });
        
        executor.submit(() -> {
            System.out.println("Running task 3 on: " + Thread.currentThread().getName());
            sleep(100);
        });
    }
}
```

**Output:**
```
Running task 1 on: virtual-41
Running task 2 on: virtual-37
Running task 3 on: virtual-39
```

**Problems:**
- ❌ Names don't indicate purpose
- ❌ Random-looking numbers (actually sequential, but not obvious)
- ❌ Hard to trace in logs
- ❌ No context

---

### Version 2: Custom Names with ThreadFactory ✅

```java
public class CustomNaming {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Create a custom ThreadFactory
        ThreadFactory factory = Thread.ofVirtual()
            .name("aggregator-demo-", 1)  // Descriptive name!
            .factory();
        
        var executor = Executors.newVirtualThreadPerTaskExecutor(factory);
        
        executor.submit(() -> {
            System.out.println("Running task 1 on: " + Thread.currentThread().getName());
            sleep(100);
        });
        
        executor.submit(() -> {
            System.out.println("Running task 2 on: " + Thread.currentThread().getName());
            sleep(100);
        });
        
        executor.submit(() -> {
            System.out.println("Running task 3 on: " + Thread.currentThread().getName());
            sleep(100);
        });
    }
}
```

**Output:**
```
Running task 1 on: aggregator-demo-1
Running task 2 on: aggregator-demo-2
Running task 3 on: aggregator-demo-3
```

**Benefits:**
- ✅ Names are descriptive
- ✅ Easy to identify purpose
- ✅ Consistent numbering (1, 2, 3, ...)
- ✅ Clear in logs

---

## Real-World Example: Service-Based Naming

### Scenario: Microservices Architecture

You have multiple services running, each using an executor:

```java
// Order Service
var orderExecutor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("order-service-", 1)
        .factory()
);

// Payment Service
var paymentExecutor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("payment-service-", 1)
        .factory()
);

// Inventory Service
var inventoryExecutor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("inventory-service-", 1)
        .factory()
);
```

### Log Output (Easy to Identify Service)

```
22:30:23.925 [order-service-1] INFO OrderService -- Processing order #1001
22:30:23.926 [payment-service-1] INFO PaymentService -- Processing payment for $99.99
22:30:23.927 [inventory-service-1] INFO InventoryService -- Checking stock
22:30:24.001 [order-service-2] INFO OrderService -- Processing order #1002
22:30:24.050 [inventory-service-2] INFO InventoryService -- Checking stock
```

**Without custom naming:**
```
22:30:23.925 [virtual-1001] INFO OrderService -- Processing order #1001
22:30:23.926 [virtual-1002] INFO PaymentService -- Processing payment for $99.99
22:30:23.927 [virtual-1003] INFO InventoryService -- Checking stock
```

❌ Which thread is for which service? Unclear!

---

## The AggregatorDemo Example

### Original Code (Default Naming)

```java
public class Lec03AccessResponseUsingFuture {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var aggregator = new AggregatorService(executor);
        
        System.out.println("Product 41: " + aggregator.getProduct(41));
        System.out.println("Product 42: " + aggregator.getProduct(42));
        System.out.println("Product 43: " + aggregator.getProduct(43));
    }
}
```

**Output:**
```
22:29:23.925 [virtual-41] INFO Client -- Calling external service for product 43
22:29:23.925 [virtual-37] INFO Client -- Calling external service for product 41
22:29:23.925 [virtual-39] INFO Client -- Calling external service for product 42
22:29:24.947 [main] INFO -- Product 41: Durable Marble Clock
22:29:24.947 [main] INFO -- Product 42: Mediocre Aluminum Hat
22:29:24.947 [main] INFO -- Product 43: Practical Marble Bag
```

### Improved Code (Custom Naming)

```java
public class Lec03AccessResponseUsingFuture {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Create executor with custom naming
        ThreadFactory factory = Thread.ofVirtual()
            .name("aggregator-demo-", 1)
            .factory();
        
        var executor = Executors.newVirtualThreadPerTaskExecutor(factory);
        var aggregator = new AggregatorService(executor);
        
        System.out.println("Product 41: " + aggregator.getProduct(41));
        System.out.println("Product 42: " + aggregator.getProduct(42));
        System.out.println("Product 43: " + aggregator.getProduct(43));
    }
}
```

**Output:**
```
22:30:23.925 [aggregator-demo-1] INFO Client -- Calling external service for product 43
22:30:23.925 [aggregator-demo-2] INFO Client -- Calling external service for product 41
22:30:23.925 [aggregator-demo-3] INFO Client -- Calling external service for product 42
22:30:24.947 [main] INFO -- Product 41: Durable Marble Clock
22:30:24.947 [main] INFO -- Product 42: Mediocre Aluminum Hat
22:30:24.947 [main] INFO -- Product 43: Practical Marble Bag
```

✅ Much clearer! Thread names show the purpose immediately.

---

## Virtual Thread Fluent API Deep Dive

### Full Thread.ofVirtual() API

```java
Thread.Builder.OfVirtual builder = Thread.ofVirtual();

// Configure naming
builder = builder.name("prefix-", 1);        // Name with counter
builder = builder.name("fixed-name");        // Fixed name (no counter)

// Configure other properties
builder = builder.daemon(true);              // Daemon thread
builder = builder.inheritInheritableThreadLocals(false);  // Thread locals

// Finally, get the factory
ThreadFactory factory = builder.factory();
```

### Naming Options

#### Option 1: Prefix with Counter

```java
Thread.ofVirtual()
    .name("worker-", 1)
    .factory()
// Creates: worker-1, worker-2, worker-3, ...
```

#### Option 2: Fixed Name (All Same)

```java
Thread.ofVirtual()
    .name("fixed-name")
    .factory()
// Creates: fixed-name, fixed-name, fixed-name, ...
// (Usually not recommended - loses identity)
```

#### Option 3: Complex Naming

```java
Thread.ofVirtual()
    .name("service-", 0, "-task-", 1)
    .factory()
// Creates: service-0-task-1, service-0-task-2, ...
// (Multiple counters possible)
```

---

## Complete Example: Multiple Named Executors

```java
public class MultiServiceDemo {
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Different executor for each service
        var userService = new UserService(
            Executors.newVirtualThreadPerTaskExecutor(
                Thread.ofVirtual()
                    .name("user-service-", 1)
                    .factory()
            )
        );
        
        var orderService = new OrderService(
            Executors.newVirtualThreadPerTaskExecutor(
                Thread.ofVirtual()
                    .name("order-service-", 1)
                    .factory()
            )
        );
        
        var paymentService = new PaymentService(
            Executors.newVirtualThreadPerTaskExecutor(
                Thread.ofVirtual()
                    .name("payment-service-", 1)
                    .factory()
            )
        );
        
        // Execute tasks
        var userFuture = userService.getUser(123);
        var orderFuture = orderService.getOrder(456);
        var paymentFuture = paymentService.processPayment(789);
        
        // Wait for results
        User user = userFuture.get();
        Order order = orderFuture.get();
        Payment payment = paymentFuture.get();
        
        System.out.println("User: " + user);
        System.out.println("Order: " + order);
        System.out.println("Payment: " + payment);
    }
}
```

**Logs:**
```
[user-service-1] INFO UserService -- Fetching user 123
[order-service-1] INFO OrderService -- Fetching order 456
[payment-service-1] INFO PaymentService -- Processing payment 789
[user-service-2] INFO UserService -- Fetching related orders
[order-service-2] INFO OrderService -- Checking inventory
[payment-service-2] INFO PaymentService -- Verifying card
```

✅ **Crystal clear** which service each thread belongs to!

---

## Why This Matters: Debugging Scenario

### Scenario: Production Issue with Default Names

```
ERROR: TimeoutException in transaction
Stack trace: [virtual-1241] transaction timeout

Now what?
- Which service?
- What was it doing?
- Was it a database call? API call?
- Unclear! 😞
```

### With Custom Names

```
ERROR: TimeoutException in transaction
Stack trace: [payment-service-5] transaction timeout

Now you know:
✅ It's the payment service
✅ The 5th concurrent payment being processed
✅ Easy to correlate with other logs
✅ Easy to debug! 😊
```

---

## Observability Benefits

### What Custom Naming Enables

```
1. LOG CORRELATION
   ├─ [aggregator-demo-1] calls 3 APIs
   ├─ [aggregator-demo-2] calls 3 APIs
   └─ Easy to see concurrent requests

2. PERFORMANCE MONITORING
   ├─ Which service's threads are slow?
   ├─ Pattern matching: grep for "service-name-"
   └─ Identify bottlenecks

3. DISTRIBUTED TRACING
   ├─ Thread name + request ID = full trace
   ├─ Follow request through all services
   └─ Find where delay happened

4. DEBUGGING
   ├─ See which thread caused the error
   ├─ Reproduce the scenario
   └─ Faster root cause analysis

5. MONITORING DASHBOARDS
   ├─ Thread pool metrics per service
   ├─ Identify over-utilization
   └─ Capacity planning
```

---

## Step-by-Step: Converting Default to Custom Names

### Step 1: Understand Current Naming

```java
// Before
var executor = Executors.newVirtualThreadPerTaskExecutor();
```

Output:
```
[virtual-41], [virtual-37], [virtual-39]
```

### Step 2: Create ThreadFactory

```java
// Step 1: Create builder
Thread.Builder.OfVirtual builder = Thread.ofVirtual();

// Step 2: Add naming
builder = builder.name("aggregator-demo-", 1);

// Step 3: Get factory
ThreadFactory factory = builder.factory();
```

### Step 3: Pass to Executor

```java
// Before
var executor = Executors.newVirtualThreadPerTaskExecutor();

// After
ThreadFactory factory = Thread.ofVirtual()
    .name("aggregator-demo-", 1)
    .factory();
var executor = Executors.newVirtualThreadPerTaskExecutor(factory);
```

### Step 4: Run and Verify

```
[aggregator-demo-1], [aggregator-demo-2], [aggregator-demo-3]
✅ Much better!
```

---

## Common Patterns

### Pattern 1: Application Name

```java
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("myapp-", 1)
        .factory()
);
```

### Pattern 2: Service Name

```java
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("user-service-", 1)
        .factory()
);
```

### Pattern 3: Hierarchical

```java
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("api-", 0, "-handler-", 1)
        .factory()
);
// Creates: api-0-handler-1, api-0-handler-2, ...
```

### Pattern 4: With Timestamp (Discouraged)

```java
// NOT RECOMMENDED (names change on each run)
String timestamp = System.currentTimeMillis();
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("app-" + timestamp + "-", 1)
        .factory()
);
```

---

## Best Practices

### ✅ DO: Use Meaningful Names

```java
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("payment-processor-", 1)
        .factory()
);
```

### ✅ DO: Match Your Architecture

```java
// Microservices naming
ThreadFactory orderThreads = Thread.ofVirtual()
    .name("order-service-", 1)
    .factory();

ThreadFactory paymentThreads = Thread.ofVirtual()
    .name("payment-service-", 1)
    .factory();
```

### ✅ DO: Be Consistent

```java
// Consistent naming across your codebase
"service-name-" prefix + incremental number
```

### ❌ DON'T: Use Generic Names

```java
// Bad - not descriptive
Thread.ofVirtual()
    .name("thread-", 1)  // Too vague
    .factory()
```

### ❌ DON'T: Use Random/Dynamic Names

```java
// Bad - names change on each run
Thread.ofVirtual()
    .name("vt-" + UUID.randomUUID(), 1)
    .factory()
```

### ❌ DON'T: Use Very Long Names

```java
// Bad - too verbose
Thread.ofVirtual()
    .name("order-processing-service-main-executor-task-", 1)
    .factory()
```

---

## Practical Refactoring Checklist

When improving observability, use this checklist:

- [ ] **Identify all ExecutorServices** in your code
- [ ] **Determine purpose** of each executor
- [ ] **Choose descriptive names** (service-name, handler-type, etc.)
- [ ] **Create ThreadFactory** with custom names
- [ ] **Pass factory to executor** creation
- [ ] **Test and verify** names appear in logs
- [ ] **Document naming convention** for team
- [ ] **Update monitoring/dashboards** to use new names
- [ ] **Add examples** to project documentation

---

## Summary Table

| Aspect | Default | Custom |
|--------|---------|--------|
| **Thread Name** | [virtual-41] | [aggregator-demo-1] |
| **Clarity** | ❌ Cryptic | ✅ Descriptive |
| **Consistency** | ❌ Varies | ✅ Predictable |
| **Debugging** | ❌ Hard | ✅ Easy |
| **Logging** | ❌ Generic | ✅ Identifiable |
| **Monitoring** | ❌ Difficult | ✅ Simple |
| **Performance** | ✅ Same | ✅ Same |
| **Memory** | ✅ Same | ✅ Same |
| **Setup Complexity** | ✅ Simple | ℹ️ 3 lines extra |

---

## Key Takeaways

1. **Virtual threads are numbered by default** - Names like `[virtual-41]` don't mean anything

2. **Use ThreadFactory for custom names** - 3 lines of code for massive clarity improvement

3. **Naming convention** - Use format: `service-name-` with incrementing counter

4. **Observable code is maintainable code** - Invest in good naming

5. **Thread naming doesn't impact performance** - Pure observability benefit, no cost

6. **Document your convention** - Help teammates understand the naming pattern

7. **Use in all executors** - Platform threads AND virtual threads benefit equally

---

## Quick Reference

```java
// Quick template for custom-named executor:
var executor = Executors.newVirtualThreadPerTaskExecutor(
    Thread.ofVirtual()
        .name("your-service-name-", 1)  // Change this!
        .factory()
);
```

**Before:** `[virtual-1], [virtual-2], [virtual-3]`  
**After:** `[your-service-name-1], [your-service-name-2], [your-service-name-3]`

✅ Done! Your logs are now clear and maintainable. 🎉

---

## Further Reading

- [Java Thread.Builder Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.Builder.html)
- [Virtual Threads Overview](https://openjdk.org/jeps/444)
- [ThreadFactory Interface](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadFactory.html)

---

**Remember:** Good thread naming costs almost nothing and helps tremendously with debugging and monitoring. Always name your threads! 📝✨

---

# ⚠️ Virtual Threads Limitations: Beyond newVirtualThreadPerTaskExecutor()

## TL;DR

Virtual threads are **great for I/O-heavy, one-off tasks**, but Java doesn't provide built-in support for:
- Scheduled execution (periodic tasks)
- Resource pooling and management
- Complex executor patterns (cache, scheduled, fork-join)

This lesson explores the **gap between what we have and what we need**.

```
✅ What Works Great: One-off I/O tasks
❌ What's Missing: Scheduled/periodic execution
❌ What's Missing: Complex executor patterns
❌ What's Missing: Standard APIs for these scenarios

Solution: We'll learn workarounds and custom implementations in upcoming lectures
```

---

## The Current State: Virtual Thread Support

### What We Have ✅

```java
// ✅ STANDARD API - Works great!
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Use case: "Execute this task, and I don't care when"
executor.submit(() -> doWork());
```

**Characteristics:**
- One task per virtual thread
- Fire-and-forget
- Perfect for I/O-bound work
- Simple and effective

### What We Don't Have ❌

```java
// ❌ NO STANDARD API
// "Execute this task every 5 seconds"
ScheduledExecutorService scheduledExecutor = 
    Executors.newScheduledVirtualThreadExecutor();  // ← Doesn't exist!

// "Use a pool of virtual threads to run many tasks"
ExecutorService cachedExecutor = 
    Executors.newCachedVirtualThreadExecutor();  // ← Doesn't exist!
```

**Missing features:**
- Scheduled/periodic execution
- Fixed thread pool for virtual threads
- Cached thread pool for virtual threads
- Fork-join pool for virtual threads
- Timed tasks

---

## The Problem Scenario

### Real-World Use Case: Periodic Health Checks

Imagine you're building a microservices application where you need to:

```
Every 10 seconds:
├─ Check database connection health
├─ Check external API availability
├─ Verify cache status
└─ Report metrics
```

### Solution With Platform Threads ✅ (But Limited)

```java
public class HealthCheckService {
    
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(1);
    
    public void startHealthChecks() {
        // Schedule periodic task
        scheduler.scheduleAtFixedRate(
            this::performHealthCheck,
            0,           // initial delay
            10,          // period
            TimeUnit.SECONDS
        );
    }
    
    private void performHealthCheck() {
        LOGGER.info("Performing health check at {}", 
            LocalDateTime.now());
        
        // Check database
        checkDatabase();
        
        // Check APIs
        checkExternalAPIs();
        
        // Check cache
        checkCache();
        
        LOGGER.info("Health check completed");
    }
}
```

### The Problem: Can't Use Virtual Threads Here ❌

```java
public class HealthCheckServiceWithVT {
    
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(1);  // ← Platform threads!
    
    public void startHealthChecks() {
        // We WANT to use virtual threads, but there's no API for it!
        // No: Executors.newScheduledVirtualThreadExecutor()
        
        scheduler.scheduleAtFixedRate(
            this::performHealthCheck,
            0, 10, TimeUnit.SECONDS
        );
        // ❌ Still using platform threads (expensive, limited)
    }
}
```

---

## Understanding The Gap

### Platform Thread Executor Ecosystem

```
Executors class provides:
├─ newSingleThreadExecutor()
│  └─ 1 thread, sequential execution
├─ newFixedThreadPool(int nThreads)
│  └─ Fixed number of threads, task queue
├─ newCachedThreadPool()
│  └─ Dynamic, creates threads on demand, reuses idle
├─ newScheduledThreadPool(int corePoolSize)
│  └─ Periodic and delayed execution
├─ newSingleThreadScheduledExecutor()
│  └─ 1 thread, periodic execution
└─ newForkJoinPool()
   └─ Divide-and-conquer, CPU-bound tasks
```

### Virtual Thread Executor Ecosystem (Limited)

```
Executors class provides:
└─ newVirtualThreadPerTaskExecutor()
   └─ One task per virtual thread (only!)

Missing:
├─ ❌ newScheduledVirtualThreadExecutor()
├─ ❌ newFixedVirtualThreadPool()
├─ ❌ newCachedVirtualThreadPool()
├─ ❌ newSingleVirtualThreadExecutor()
└─ ❌ Any variant with scheduling
```

### Comparison Table

| Feature | Platform Threads | Virtual Threads |
|---------|-----------------|-----------------|
| **One-off execution** | ✅ newFixedThreadPool() | ✅ newVirtualThreadPerTaskExecutor() |
| **Scheduled execution** | ✅ newScheduledThreadPool() | ❌ NOT AVAILABLE |
| **Periodic tasks** | ✅ scheduleAtFixedRate() | ❌ NOT AVAILABLE |
| **Thread pooling** | ✅ Multiple variants | ❌ Only per-task |
| **Resource control** | ✅ Pool size management | ❌ Unlimited VT creation |
| **Java version** | Since Java 5 | Java 21+ |

---

## Why This Gap Exists

### Design Decision: Virtual Threads Philosophy

The Java team decided:

> "Virtual threads should be created per task and discarded. Don't pool them."

This makes sense for **simple I/O tasks**, but breaks down for **scheduled work**.

### The Dilemma

```
Option A: Force VT pooling (defeats purpose)
├─ Violates the "don't pool VT" principle
├─ Loses memory efficiency benefits
└─ Not the intended usage

Option B: Require periodic task to create new VT each time
├─ Yes, this is wasteful compared to reusing a thread
├─ BUT: VTs are so cheap that it might not matter!
└─ Trade-off: Simplicity vs optimal resource usage

Option C: Provide new APIs (what Java chose)
├─ Let framework/library authors handle it
├─ Allows flexibility
├─ But leaves standard users without built-in solutions
└─ State of Java 21 (for now)
```

---

## Real-World Scenarios Lacking VT Support

### Scenario 1: Periodic Health Checks

```java
// Want to do this:
var scheduler = Executors.newScheduledVirtualThreadExecutor();
scheduler.scheduleAtFixedRate(
    this::healthCheck,
    0, 10, TimeUnit.SECONDS
);

// What we actually have to do:
var scheduler = Executors.newScheduledThreadPool(1);  // Platform threads
```

**Trade-offs:**
- ❌ Uses expensive platform thread (1-2 MB stack)
- ✅ Works reliably
- ❌ Not scalable if multiple periodic tasks
- ✅ Standard, familiar API

### Scenario 2: Periodic Cache Refresh

```java
public class CacheRefreshService {
    
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(3);  // 3 refresh tasks
    
    public void initializeRefreshTasks() {
        // Refresh user cache every 5 minutes
        scheduler.scheduleAtFixedRate(
            this::refreshUserCache,
            0, 5, TimeUnit.MINUTES
        );
        
        // Refresh product cache every 10 minutes
        scheduler.scheduleAtFixedRate(
            this::refreshProductCache,
            0, 10, TimeUnit.MINUTES
        );
        
        // Refresh recommendation cache every 15 minutes
        scheduler.scheduleAtFixedRate(
            this::refreshRecommendationCache,
            0, 15, TimeUnit.MINUTES
        );
    }
    
    private void refreshUserCache() { /* ... */ }
    private void refreshProductCache() { /* ... */ }
    private void refreshRecommendationCache() { /* ... */ }
}
```

**The Problem:**
```
3 platform threads reserved just for periodic tasks
Each consuming 1-2 MB of memory continuously
Even if idle between executions!
```

**Ideal Solution (Not Available):**
```java
// This doesn't exist (yet):
var scheduler = Executors.newScheduledVirtualThreadExecutor();
// Would allow lightweight scheduled execution
```

### Scenario 3: Request Timeout Handling

```java
public class TimeoutHandler {
    
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(5);  // Platform threads
    
    public <T> T executeWithTimeout(
        Callable<T> task, 
        long timeoutMillis) {
        
        // Schedule a cancellation if task takes too long
        var future = scheduler.submit(task);
        scheduler.schedule(
            future::cancel,
            timeoutMillis,
            TimeUnit.MILLISECONDS
        );
        
        return future.get();
    }
}
```

**The Problem:**
```
Platform threads used for timeout management
Expensive resource for a simple "cancel after N ms" operation
```

---

## The Workaround Approaches

### Approach 1: Use Platform Threads (For Now)

```java
public class CurrentApproach {
    
    // For periodic tasks, we have no choice
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(1);
    
    // For one-off I/O, use virtual threads
    private final ExecutorService ioExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    public void handleRequest(Request req) {
        // I/O operations: virtual threads
        ioExecutor.submit(() -> {
            var userService = callUserService(req.userId);
            var orderService = callOrderService(req.orderId);
            return combineResults(userService, orderService);
        });
        
        // Periodic tasks: platform threads (for now)
        scheduler.scheduleAtFixedRate(
            this::periodicHealthCheck,
            0, 10, TimeUnit.SECONDS
        );
    }
}
```

**Status:** ✅ Works, ❌ Not optimal

### Approach 2: Custom Implementation (Preview of Future Lectures)

```java
// Pseudo-code: Custom scheduled virtual thread executor
public class CustomScheduledVirtualThreadExecutor {
    
    private final ExecutorService virtualExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    private final ScheduledExecutorService platformScheduler = 
        Executors.newScheduledThreadPool(1);  // Just for scheduling!
    
    public ScheduledFuture<?> scheduleAtFixedRate(
        Runnable command,
        long initialDelay,
        long period,
        TimeUnit unit) {
        
        // Platform thread only handles scheduling (lightweight)
        // Actual work runs on virtual threads
        return platformScheduler.scheduleAtFixedRate(
            () -> virtualExecutor.submit(command),  // ← VT per execution!
            initialDelay,
            period,
            unit
        );
    }
}
```

**Status:** 🔄 Workaround, works but not ideal

### Approach 3: Wait for Standard APIs (Future)

```java
// Hypothetical future Java version:
var scheduler = Executors.newScheduledVirtualThreadExecutor();
scheduler.scheduleAtFixedRate(...);

// Status: 🎯 Ideal, but not yet available (Java 21)
```

---

## Decision Tree: Which Executor to Use?

```
"I need to execute some work..."

├─ "...one time, and it does I/O"
│  └─ ✅ newVirtualThreadPerTaskExecutor()
│
├─ "...many times, and they do I/O"
│  ├─ "...concurrently"
│  │  └─ ✅ newVirtualThreadPerTaskExecutor()
│  └─ "...sequentially"
│     └─ ? (No good option - see Lecture X)
│
├─ "...periodically, and it does I/O"
│  └─ ⚠️ newScheduledThreadPool(1) for now
│     (Workaround discussed in later lectures)
│
├─ "...on a schedule with delays"
│  └─ ⚠️ newScheduledThreadPool(n) for now
│     (Workaround discussed in later lectures)
│
├─ "...but I want to limit concurrent count"
│  └─ ? (Limited VT support - see Lecture X)
│
└─ "...for CPU-intensive work"
   └─ ✅ newForkJoinPool() (not related to VT)
```

---

## The Gap in Numbers: Java 21 Status

### Executor Types Available

| Executor Type | Platform Threads | Virtual Threads | Recommendation |
|---|---|---|---|
| One-off async | ✅ ThreadPool | ✅ PerTask | Use VT |
| Many concurrent | ✅ FixedPool | ⚠️ PerTask only | Use VT |
| Reuse idle | ✅ CachedPool | ❌ Not available | Use Cached |
| Scheduled | ✅ ScheduledPool | ❌ Not available | Use Scheduled (PT) |
| Periodic | ✅ ScheduledPool | ❌ Not available | Use Scheduled (PT) |
| Delayed execution | ✅ ScheduledPool | ❌ Not available | Use Scheduled (PT) |
| CPU work | ✅ ForkJoinPool | ❌ Not VT based | Use ForkJoin |

**Takeaway:** ~50% of executor patterns lack native VT support

---

## Visual Overview: Executor Universe (Java 21)

```
JAVA EXECUTORS LANDSCAPE (Java 21)

Platform Threads (Mature)
├─ newSingleThreadExecutor()
├─ newFixedThreadPool()
├─ newCachedThreadPool()
├─ newScheduledThreadPool()
├─ newSingleThreadScheduledExecutor()
├─ newWorkStealingPool()
└─ newForkJoinPool()
   ↑ 20+ years of API

Virtual Threads (New)
└─ newVirtualThreadPerTaskExecutor()
   ↑ Only 1 API!

Missing Bridges:
├─ ❌ newScheduledVirtualThreadExecutor()
├─ ❌ newFixedVirtualThreadPool()
├─ ❌ newCachedVirtualThreadPool()
└─ ❌ Other variants
   ↑ To be covered in upcoming lectures
```

---

## Why This Lesson Matters

### Reason 1: Awareness

Not every code scenario can use virtual threads directly. Understanding the limitations helps you:
- Know when VT is the right choice
- Know when you're forced to use platform threads
- Understand trade-offs in your architecture

### Reason 2: Future-Proofing

```
Java 21: Limited VT APIs
Java 22: More APIs (maybe)
Java 23: More APIs (maybe)
...

By understanding the gap now, you can:
✅ Write code that's easy to migrate later
✅ Avoid painting yourself into a corner
✅ Make informed architectural decisions
```

### Reason 3: Understanding The Workarounds

The next lectures explore **creative solutions** to problems like:
- How do you schedule periodic tasks with VT?
- How do you limit concurrent VT execution?
- How do you manage VT lifecycle in long-running apps?

These are valuable patterns even if Java adds new APIs later.

---

## What's Coming in Next Lectures

### Lecture Topics (Spoiler Alert)

```
├─ Scheduled Execution with Virtual Threads
│  └─ Creating your own scheduler
│
├─ Managing VT Concurrency Limits
│  └─ Semaphore patterns
│
├─ VT + Structured Concurrency
│  └─ Better resource management
│
└─ Real-world Workarounds
   └─ Production-ready patterns
```

### What You'll Learn

By the end of the next few lectures, you'll understand:
1. **Why** the gap exists (design decisions)
2. **What** to do in the meantime (workarounds)
3. **How** to implement workarounds correctly
4. **When** to use each pattern

---

## Current Best Practices (Until Gap is Closed)

### For One-Off I/O Tasks ✅

```java
var executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> doIoWork());
```

**Status:** Perfect, use this!

### For Scheduled Tasks ⚠️

```java
// Temporary solution
var scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(
    this::periodicTask,
    0, 10, TimeUnit.SECONDS
);
```

**Status:** Works, but not ideal. Workarounds coming.

### For Mixed Scenarios

```java
public class MixedExecutorStrategy {
    
    // One-off I/O: Use VT
    private final ExecutorService ioExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    // Scheduled tasks: Use Platform threads (for now)
    private final ScheduledExecutorService scheduler = 
        Executors.newScheduledThreadPool(1);
    
    public void handleRequest(Request req) {
        // I/O: fast, lightweight
        ioExecutor.submit(() -> processRequest(req));
        
        // Scheduled: overhead justified by infrequency
        scheduler.scheduleAtFixedRate(
            this::maintenanceTask,
            0, 1, TimeUnit.HOURS
        );
    }
}
```

**Status:** Pragmatic approach for current Java versions

---

## Remember: This is Temporary

```
Current State (Java 21):
├─ Virtual threads are new ✨
├─ API coverage is limited
├─ Gaps are expected
└─ Solution: Workarounds and patience

Future State (Java 22+):
├─ More VT-related APIs (expected)
├─ Scheduled execution support (hoped for)
├─ Smoother migration path
└─ Better standard library support
```

---

## Key Takeaways

1. **Virtual threads are great for I/O tasks** - but only for one-off, immediate execution

2. **Scheduled execution is a gap** - No standard API for periodic VT tasks

3. **Platform threads are still relevant** - For many patterns, they're still the only option

4. **Workarounds exist** - We'll learn them in upcoming lectures (patience!)

5. **This is temporary** - Java will likely fill these gaps in future versions

6. **Be pragmatic** - Use VT where it works, use platform threads where it doesn't (yet)

7. **Stay aware** - Understand the tradeoffs you're making when mixing executor types

---

## Real-World Advice

### For Production Code Today

```java
// ✅ DO THIS
ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor();
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

// For I/O tasks
ioExecutor.submit(() -> slowApiCall());

// For scheduled maintenance
scheduler.scheduleAtFixedRate(
    this::maintenanceTasks,
    0, 5, TimeUnit.MINUTES
);
```

### What NOT to Do

```java
// ❌ DON'T try to force VT where it doesn't fit
try {
    Executors.newScheduledVirtualThreadExecutor();  // Doesn't exist!
} catch (NoSuchMethodError e) {
    // Of course it doesn't work
}

// ❌ DON'T give up on VT
// Virtual threads are still valuable for I/O!
```

### Mindset

```
"Virtual threads solve I/O concurrency scaling.
They don't solve all executor problems... yet.

Be patient.
Learn the workarounds.
Understand the tradeoffs.
Contribute feedback to the Java community."
```

---

## A Note on Patience

> This lecture acknowledges that the next few lectures might seem frustrating or tedious. You're learning about **limitations and workarounds** rather than exciting new features.

**Why it matters:**
- Understanding problems deepens understanding of solutions
- Knowing the "why" helps you design better code
- Learning workarounds makes you a better engineer
- This knowledge becomes irrelevant once APIs improve (which is a good thing!)

**Keep the faith:** 
The journey through these limitations makes you understand virtual threads at a deeper level. The next few lectures build toward practical, production-ready solutions. 🚀

---

## Quick Reference: Current State (Java 21)

```
Virtual Threads:
├─ ✅ Great for: One-off I/O tasks
├─ ✅ Lightweight: ~KB per thread
├─ ✅ Scalable: Thousands of concurrent tasks
├─ ❌ Scheduled: No standard API (yet)
├─ ❌ Pooling: Not the design intent
└─ ❌ Fixed capacity: Can't limit easily

Next Steps:
├─ Understand the gap (this lecture)
├─ Learn workarounds (next lectures)
├─ Make pragmatic choices (your code)
└─ Stay tuned for improvements (future Java versions)
```

---

**Next Lecture Preview:** We'll dive into creating our own scheduled virtual thread executor and explore the creative solutions available today! 🎯

---

**Remember:** Being aware of limitations makes you a better architect. Let's embrace the journey! 💪

---

# 🚦 Concurrency Limits: Why VT Pooling is Wrong & How to Fix It

## TL;DR

**Virtual threads should NEVER be pooled.** Even though `FixedThreadPool` accepts a `ThreadFactory`, passing a virtual thread factory defeats the purpose and violates VT design principles.

```java
// ❌ DON'T: Pools virtual threads (violates design)
var factory = Thread.ofVirtual().name("vt-", 1).factory();
var executor = Executors.newFixedThreadPool(3, factory);  // WRONG!

// ✅ DO: Use Semaphore for concurrency limits
var semaphore = new Semaphore(3);
var executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> {
    semaphore.acquire();
    try {
        doWork();
    } finally {
        semaphore.release();
    }
});
```

---

## The Problem: Rate Limiting External Services

### Real-World Scenario

Your application needs to call an external product API:

```
API Rate Limit: "Maximum 3 concurrent calls"
         ↓
Your requirement: Submit 20 tasks, but respect the limit
         ↓
Goal: Process all 20, but only 3 at a time
```

### The Code Example

```java
public class Lec05ConcurrencyLimit {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        Lec05ConcurrencyLimit.class
    );

    static void main() {
        execute(Executors.newCachedThreadPool(), 20);
    }

    private static void printProductInfo(int id) {
        LOGGER.info("{} => {}", id, Client.getProduct(id));
    }

    private static void execute(ExecutorService executor, int taskCount) {
        try (executor) {
            for (int i = 1; i <= taskCount; i++) {
                int j = i;
                executor.execute(() -> printProductInfo(j));
            }
            LOGGER.info("task submitted");
        }
    }
}
```

---

## Version 1: Unlimited Concurrency ❌ (Too Fast)

### The Code

```java
static void main() {
    execute(Executors.newCachedThreadPool(), 20);
}
```

### What Happens

The `CachedThreadPool` creates **as many threads as needed**:

```
Task 1  → Thread 1 → API Call
Task 2  → Thread 2 → API Call
Task 3  → Thread 3 → API Call
...
Task 20 → Thread 20 → API Call

All 20 calls run simultaneously! 🚀
```

### Output

```
00:26:46.761 [pool-1-thread-1] INFO Client -- Calling http://localhost:7070/product/1
00:26:46.761 [pool-1-thread-2] INFO Client -- Calling http://localhost:7070/product/2
00:26:46.761 [pool-1-thread-3] INFO Client -- Calling http://localhost:7070/product/3
00:26:46.761 [pool-1-thread-4] INFO Client -- Calling http://localhost:7070/product/4
... (all 20 at once!)
```

### The Problem

```
You: "Make 20 API calls"
API: "Sure, but only 3 at a time!"
You: "OK" (submits all 20)
Reality: All 20 hit the API simultaneously
Result: ❌ API rate limit exceeded!
        ❌ Requests fail or get throttled
        ❌ Service returns 429 Too Many Requests
```

### Visualization

```
Timeline: 0ms ─────── 1000ms ─────── 2000ms

Concurrent Calls at any given moment:
│
20 ├─────────────────────────────────────
   │  ████████████████████ (all 20!)
   │
15 ├─────────────────────────────────────
   │
10 ├─────────────────────────────────────
   │
5  ├─────────────────────────────────────
   │
0  └─────────────────────────────────────
   
Status: ❌ Violates API contract (max 3 concurrent)
```

---

## Version 2: Limited Concurrency with FixedThreadPool ✅ (But Wrong For VT!)

### The Code

```java
static void main() {
    execute(Executors.newFixedThreadPool(3), 20);
}
```

### What Happens

`FixedThreadPool(3)` creates exactly **3 platform threads**:

```
Initial State:
├─ Thread 1: idle
├─ Thread 2: idle
└─ Thread 3: idle

Task 1-3:  → [Threads 1-3 run immediately]
Task 4-20: → [Queued, waiting for threads to free up]

As tasks complete:
├─ Thread 1 finishes → picks Task 4 from queue
├─ Thread 2 finishes → picks Task 5 from queue
└─ Thread 3 finishes → picks Task 6 from queue
```

### Output

```
00:26:46.761 [pool-1-thread-1] INFO Client -- Calling product/1
00:26:46.761 [pool-1-thread-2] INFO Client -- Calling product/2
00:26:46.761 [pool-1-thread-3] INFO Client -- Calling product/3
(pause... waiting for one to complete)
00:26:47.761 [pool-1-thread-1] INFO Client -- Calling product/4
00:26:47.761 [pool-1-thread-2] INFO Client -- Calling product/5
00:26:47.761 [pool-1-thread-3] INFO Client -- Calling product/6
...
```

### The Solution (For Platform Threads)

```
Status: ✅ Works correctly!
        ✅ Respects rate limit (max 3 concurrent)
        ❌ But uses expensive platform threads
```

### Visualization

```
Timeline: 0ms ────── 1s ────── 2s ────── 3s ─────

Concurrent Calls at any given moment:
│
5  ├─────────────────────────────────────
   │
3  ├──███───███───███───███───███───────
   │  (always exactly 3 or less)
1  ├─────────────────────────────────────
   │
0  └─────────────────────────────────────
   
Status: ✅ Respects API contract
```

---

## Version 3: The Tempting BUT WRONG Approach ❌

### The Attempted "Fix"

Someone thinks: "Virtual threads are lightweight, let me pass a VT factory to FixedThreadPool!"

```java
static void main() {
    var factory = Thread.ofVirtual().name("bodera-virtual", 1).factory();
    execute(Executors.newFixedThreadPool(3, factory), 20);
}
```

### Output

```
00:26:46.761 [bodera-virtual-1] INFO Client -- Calling product/1
00:26:46.761 [bodera-virtual-2] INFO Client -- Calling product/2
00:26:46.761 [bodera-virtual-3] INFO Client -- Calling product/3
00:26:46.761 [bodera-virtual-1] INFO Client -- Calling product/4
00:26:46.761 [bodera-virtual-2] INFO Client -- Calling product/5
...
```

### Why This LOOKS Right But Is Actually WRONG

```
It seems to work:
✅ Only 3 threads in use
✅ Rate limit respected
✅ Virtual thread names visible

But underneath:
❌ FixedThreadPool creates 3 VTs and REUSES them (pooling!)
❌ VTs are supposed to be disposable, per-task
❌ Violates VT design principles
❌ Ignores Oracle's explicit recommendation
```

### The Core Issue

```
FixedThreadPool's Behavior:

for (int i = 0; i < poolSize; i++) {
    Thread thread = factory.newThread(...);  // Create thread via factory
    threads[i] = thread;                     // Store in array
}

// For each task:
for (Task task : allTasks) {
    availableThread.execute(task);  // REUSE the same thread
    // ← This is pooling!
}
```

### The Violation

```
What FixedThreadPool Does:
├─ Create 3 threads via factory
├─ Store them in pool
└─ Reuse them forever
   └─ Thread 1 runs Task 1, then Task 4, then Task 7, etc.
   └─ Thread 2 runs Task 2, then Task 5, then Task 8, etc.
   └─ Thread 3 runs Task 3, then Task 6, then Task 9, etc.

What Virtual Threads Are Designed For:
├─ Create 1 thread per task
├─ Run it once
└─ Discard it
```

### Oracle's Explicit Recommendation

> **"Don't pool virtual threads. Create one for every application task. Virtual threads are short-lived and have shallow call stacks. They don't need the additional overhead or the functionality of thread pools."**
> 
> — Oracle Java Documentation

---

## Version 4: The CORRECT Approach with Semaphore ✅

### Understanding Semaphore

A `Semaphore` is a synchronization primitive that:
- Maintains a **permit count**
- `acquire()`: Wait until a permit is available, then take it
- `release()`: Give back a permit
- Perfect for **rate limiting**

```java
Semaphore semaphore = new Semaphore(3);
// ↑ Allows 3 concurrent operations
```

### How It Works

```
Initialization: Semaphore(3)
├─ Available permits: 3

Task 1 arrives:
├─ acquire() → Takes 1 permit
├─ Available: 2
└─ Runs

Task 2 arrives:
├─ acquire() → Takes 1 permit
├─ Available: 1
└─ Runs

Task 3 arrives:
├─ acquire() → Takes 1 permit
├─ Available: 0
└─ Runs

Task 4 arrives:
├─ acquire() → BLOCKS (no permits available!)
├─ Waits...

Task 1 finishes:
├─ release() → Returns 1 permit
├─ Available: 1
└─ Task 4 can now acquire()
```

### The Solution

```java
public class Lec05ConcurrencyLimitFixed {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        Lec05ConcurrencyLimitFixed.class
    );
    
    private static final int CONCURRENCY_LIMIT = 3;
    private static final Semaphore semaphore = new Semaphore(CONCURRENCY_LIMIT);

    static void main() throws InterruptedException {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try (executor) {
            for (int i = 1; i <= 20; i++) {
                int id = i;
                executor.submit(() -> executeWithLimit(id));
            }
            LOGGER.info("All 20 tasks submitted");
        }
    }

    private static void executeWithLimit(int id) {
        try {
            semaphore.acquire();  // ← Request permission
            try {
                printProductInfo(id);
            } finally {
                semaphore.release();  // ← Release permission
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void printProductInfo(int id) {
        LOGGER.info("{} => {}", id, Client.getProduct(id));
    }
}
```

### Output

```
00:26:46.761 [virtual-1] INFO Client -- Calling product/1
00:26:46.761 [virtual-2] INFO Client -- Calling product/2
00:26:46.761 [virtual-3] INFO Client -- Calling product/3
(pause... waiting for permits)
00:26:47.761 [virtual-4] INFO Client -- Calling product/4
00:26:47.761 [virtual-5] INFO Client -- Calling product/5
00:26:47.761 [virtual-6] INFO Client -- Calling product/6
(pause... waiting for permits)
...
All 20 tasks submitted
```

### Why This Is CORRECT ✅

```
✅ Uses virtual threads (lightweight, per-task)
✅ Respects Semaphore limit (max 3 concurrent)
✅ Each VT is disposable (created, used, discarded)
✅ No pooling (honors VT design)
✅ Clean separation: VT creation vs rate limiting
✅ Easy to adjust limit (change Semaphore parameter)
```

---

## Visual Comparison: All Approaches

### Approach 1: CachedThreadPool (Unlimited)

```
Timeline: 0ms ────── 1s ────── 2s

Concurrent API Calls:
20 ├█████████████████████████████
   │ (All 20 at once!)
   │
0  └─────────────────────────────

Status: ❌ API rate limit exceeded
```

### Approach 2: FixedThreadPool(3) with Platform Threads

```
Timeline: 0ms ────── 1s ────── 2s ────── 3s

Concurrent API Calls:
3  ├──███───███───███───███───███
   │ (Always 3)
   │
0  └──────────────────────────────

Status: ✅ Rate limit respected
        ❌ Expensive platform threads
```

### Approach 3: FixedThreadPool(3) with VT Factory (WRONG)

```
Timeline: 0ms ────── 1s ────── 2s ────── 3s

Concurrent API Calls:
3  ├──███───███───███───███───███
   │ (Always 3)
   │
0  └──────────────────────────────

Status: ✅ Rate limit respected
        ❌ Violates VT design (pooling)
        ❌ Oracle explicitly recommends against
```

### Approach 4: newVirtualThreadPerTaskExecutor() + Semaphore (CORRECT)

```
Timeline: 0ms ────── 1s ────── 2s ────── 3s

Concurrent API Calls:
3  ├──███───███───███───███───███
   │ (Always 3)
   │
0  └──────────────────────────────

Status: ✅ Rate limit respected
        ✅ Uses VT correctly (per-task)
        ✅ Follows Oracle recommendations
        ✅ Lightweight and scalable
```

---

## Semaphore Deep Dive

### What is a Semaphore?

A `Semaphore` is like a bouncer at an exclusive club:

```
Club Capacity: 3 people max

Person 1 arrives:
├─ "Can I enter?"
├─ Bouncer: "Yes, we have 3 spots"
├─ Spots left: 2
└─ Enters

Person 2 arrives:
├─ "Can I enter?"
├─ Bouncer: "Yes, we have 2 spots"
├─ Spots left: 1
└─ Enters

Person 3 arrives:
├─ "Can I enter?"
├─ Bouncer: "Yes, we have 1 spot"
├─ Spots left: 0
└─ Enters

Person 4 arrives:
├─ "Can I enter?"
├─ Bouncer: "No, we're full"
└─ WAIT outside

Person 1 leaves:
├─ Bouncer: "You can leave, thanks"
├─ Spots left: 1
└─ Person 4: "Can I enter now?"
   ├─ Bouncer: "Yes!"
   └─ Enters
```

### Java Semaphore API

```java
// Create with N permits
Semaphore sem = new Semaphore(3);

// Acquire a permit (blocks if unavailable)
sem.acquire();        // Waits if needed
sem.acquireUninterruptibly();  // Can't be interrupted
sem.tryAcquire();     // Non-blocking, returns boolean
sem.tryAcquire(1, TimeUnit.SECONDS);  // Timeout

// Release a permit
sem.release();        // Give back 1 permit
sem.release(2);       // Give back 2 permits

// Query state
int available = sem.availablePermits();
```

---

## Real-World Example: Multiple Rate Limits

### Scenario: Managing Multiple External Services

```java
public class RateLimitedServiceAggregator {
    
    private final Semaphore userServiceLimit = new Semaphore(5);
    private final Semaphore orderServiceLimit = new Semaphore(3);
    private final Semaphore paymentServiceLimit = new Semaphore(2);
    
    private final ExecutorService executor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    public UserDto getUser(int userId) {
        return executeWithLimit(
            userServiceLimit,
            () -> userService.getUser(userId)
        );
    }
    
    public OrderDto getOrder(int orderId) {
        return executeWithLimit(
            orderServiceLimit,
            () -> orderService.getOrder(orderId)
        );
    }
    
    public PaymentDto processPayment(PaymentRequest req) {
        return executeWithLimit(
            paymentServiceLimit,
            () -> paymentService.process(req)
        );
    }
    
    private <T> T executeWithLimit(Semaphore limit, Callable<T> task) {
        try {
            limit.acquire();
            try {
                return task.call();
            } finally {
                limit.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Output Flow

```
User Service: Can make 5 concurrent calls
Order Service: Can make 3 concurrent calls  
Payment Service: Can make 2 concurrent calls

Request 1: User + Order + Payment
├─ User call: acquire (4 left)
├─ Order call: acquire (2 left)
├─ Payment call: acquire (1 left)
└─ All proceed in parallel (but rate-limited)

Request 2: User + Order + Payment
├─ User call: acquire (3 left)
├─ Order call: acquire (1 left)
├─ Payment call: BLOCKS (0 left)
│  └─ Waits for existing payment to complete
├─ Existing payment finishes: release
└─ Payment call: now acquires
```

---

## Common Patterns

### Pattern 1: Simple Rate Limiting

```java
private static final Semaphore rateLimiter = new Semaphore(3);

public void processItem(Item item) {
    executor.submit(() -> {
        try {
            rateLimiter.acquire();
            try {
                expensiveOperation(item);
            } finally {
                rateLimiter.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    });
}
```

### Pattern 2: Try-Acquire with Timeout

```java
public boolean tryProcessWithTimeout(Item item) {
    try {
        if (rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            try {
                expensiveOperation(item);
                return true;
            } finally {
                rateLimiter.release();
            }
        } else {
            LOGGER.warn("Rate limit exceeded, dropping request");
            return false;
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return false;
    }
}
```

### Pattern 3: Batch Operations

```java
public void processBatch(List<Item> items) {
    var futures = items.stream()
        .map(item -> executor.submit(() -> {
            try {
                rateLimiter.acquire();
                try {
                    return processItem(item);
                } finally {
                    rateLimiter.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }))
        .toList();
    
    // Wait for all to complete
    futures.forEach(future -> {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("Batch processing failed", e);
        }
    });
}
```

---

## Key Mistakes to Avoid

### ❌ Mistake 1: Pooling Virtual Threads

```java
// WRONG - violates VT design
var factory = Thread.ofVirtual().factory();
var executor = Executors.newFixedThreadPool(3, factory);
executor.submit(() -> doWork());
```

**Why it's wrong:**
- VTs are designed to be created per-task
- Pooling defeats the lightweight advantage
- Violates Oracle's explicit recommendation

### ❌ Mistake 2: Forgetting release() in Exception

```java
// WRONG - if exception occurs, permit is never released
semaphore.acquire();
doWork();  // ← If this throws, release() never called
semaphore.release();
```

**Fix:**
```java
semaphore.acquire();
try {
    doWork();
} finally {
    semaphore.release();  // ← Always called, even on exception
}
```

### ❌ Mistake 3: Not Handling InterruptedException

```java
// WRONG - ignores interruption
try {
    semaphore.acquire();
    doWork();
} catch (InterruptedException e) {
    // Silently ignored!
}
```

**Fix:**
```java
try {
    semaphore.acquire();
    doWork();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // ← Restore interrupt flag
    throw new RuntimeException(e);
}
```

---

## Semaphore vs Other Approaches

| Approach | Works | VT-Friendly | Rate-Limited | Complexity |
|----------|-------|-----------|--------------|-----------|
| CachedThreadPool | ✅ | ❌ | ❌ | Low |
| FixedThreadPool(PT) | ✅ | ❌ | ✅ | Low |
| FixedThreadPool(VT) | ✅ | ❌ | ✅ | Low (**Wrong!**) |
| VT + Semaphore | ✅ | ✅ | ✅ | Medium |
| VT + Custom Limiter | ✅ | ✅ | ✅ | High |

---

## Best Practices

### ✅ DO: Use Semaphore for Rate Limiting

```java
var semaphore = new Semaphore(3);
var executor = Executors.newVirtualThreadPerTaskExecutor();

executor.submit(() -> {
    semaphore.acquire();
    try {
        doWork();
    } finally {
        semaphore.release();
    }
});
```

### ✅ DO: Extract to Helper Method

```java
public <T> T withRateLimit(Semaphore limit, Callable<T> task) {
    try {
        limit.acquire();
        try {
            return task.call();
        } finally {
            limit.release();
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

// Usage:
executor.submit(() -> withRateLimit(semaphore, () -> {
    doWork();
    return null;
}));
```

### ❌ DON'T: Pool Virtual Threads

```java
// WRONG
var factory = Thread.ofVirtual().factory();
var executor = Executors.newFixedThreadPool(3, factory);
```

### ❌ DON'T: Use Platform Thread Pool for VT

```java
// WRONG - conceptually inconsistent
var executor = Executors.newFixedThreadPool(3);
// Now you've limited to 3 threads, but you're using platform threads
// Defeats the purpose of lightweight VT
```

---

## When to Use Each

### Use Semaphore When:
- ✅ You have VT per-task executor
- ✅ You need rate limiting
- ✅ You want to respect external service limits
- ✅ You need fine-grained concurrency control

### Use FixedThreadPool When:
- ✅ You want simple, familiar API
- ✅ You're okay with platform threads (for now)
- ✅ You have CPU-bound work
- ✅ You need traditional thread pool semantics

### Never Use:
- ❌ FixedThreadPool with VT factory
- ❌ CachedThreadPool for unlimited calls (without limits)
- ❌ Semaphore with negative permits
- ❌ Pooling pattern with virtual threads

---

## Summary Table

| Scenario | Solution | Why |
|----------|----------|-----|
| Call API with rate limit (3 max) | Semaphore(3) + VT executor | Respects limit, uses VT correctly |
| Need fixed 10 concurrent workers | FixedThreadPool(10) | Simple, clear intent |
| I/O with no constraints | newVirtualThreadPerTaskExecutor() | Lightweight, per-task |
| Periodic scheduled tasks | newScheduledThreadPool(1) | No VT API yet |
| CPU-bound divide-and-conquer | ForkJoinPool | Designed for CPU work |

---

## Key Takeaways

1. **Never pool virtual threads** - Even though FixedThreadPool accepts ThreadFactory, it's wrong for VT

2. **Use Semaphore for rate limiting** - The correct way to limit concurrent VT execution

3. **VTs are disposable** - Create one per task, don't reuse them in a pool

4. **Oracle is explicit** - "Don't pool virtual threads. Create one for every application task."

5. **Semaphore is simple** - Just wrap your work with acquire/release

6. **Try-finally is essential** - Always release, even if exception occurs

7. **This pattern is reusable** - Use it everywhere you need rate limiting with VT

---

## Quick Reference

### The Wrong Way (Don't Do This)

```java
var factory = Thread.ofVirtual().factory();
var executor = Executors.newFixedThreadPool(3, factory);  // ❌ WRONG
```

### The Right Way

```java
var semaphore = new Semaphore(3);
var executor = Executors.newVirtualThreadPerTaskExecutor();

executor.submit(() -> {
    semaphore.acquire();
    try {
        doWork();
    } finally {
        semaphore.release();
    }
});
```

**Remember:** Rate limiting != Thread pooling. Use the right tool for the job! 🚦

---

## Further Reading

- [Java Semaphore Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html)
- [Virtual Threads JEP 444](https://openjdk.org/jeps/444)
- [Project Loom: Virtual Threads](https://wiki.openjdk.org/display/loom/Main)

---

**Next Concept:** Now that you understand why NOT to pool VTs, let's explore alternative patterns for scenarios where you need more control! 🚀

---

# 🔐 Semaphores Explained: Beyond Basic Rate Limiting

## TL;DR

**Semaphore** = A synchronization primitive that controls access to a shared resource using **permits**.

```java
// Create a semaphore with 3 permits (max 3 concurrent)
Semaphore semaphore = new Semaphore(3);

// Thread acquires a permit (blocks if none available)
semaphore.acquire();
try {
    // Critical section - only 3 threads here at a time
    doWork();
} finally {
    // Release the permit for waiting threads
    semaphore.release();
}
```

**Key insight:** Unlike `synchronized` or `ReentrantLock`, semaphores don't pin virtual threads and can be released by any thread!

---

## What is a Semaphore?

### High-Level Concept

Imagine a **nightclub with a capacity of 3 people**:

```
Club Capacity: 3

Person A arrives → Bouncer: "You have 3 permits, enter"
Person B arrives → Bouncer: "You have 2 permits, enter"
Person C arrives → Bouncer: "You have 1 permit, enter"
Person D arrives → Bouncer: "No permits left, wait outside"

Person A leaves → Bouncer: "Thank you, here's a permit back"
Person D: "Can I enter now?" → Bouncer: "Yes! 1 permit available"
```

### In Java Terms

```java
Semaphore semaphore = new Semaphore(3);  // 3 permits

// Thread acquires permit (equivalent to Person entering)
semaphore.acquire();      // Permits now: 2

// Thread does work (Person is inside)
doWork();

// Thread releases permit (Person leaves)
semaphore.release();      // Permits now: 3
```

---

## Semaphore vs Lock vs synchronized

### Conceptual Differences

```
synchronized (Binary - exclusive access)
├─ Lock acquired by thread A
├─ Thread B BLOCKED
├─ Thread A releases lock
└─ Thread B can acquire

ReentrantLock (Binary - exclusive access)
├─ Lock acquired by thread A
├─ Thread B BLOCKED  
├─ ONLY thread A can release
└─ Thread B can acquire

Semaphore (N-permits - shared access)
├─ 3 permits available
├─ Threads A, B, C acquire (1 each)
├─ Thread D BLOCKED
├─ ANY of A, B, C can release
└─ Thread D can acquire when permit available
```

### Visual Comparison Diagram

```
LOCK (Binary):                 SEMAPHORE (N-permits):
┌────────────────┐            ┌─────────────────────┐
│   LOCK = 1     │            │  PERMITS = 3        │
└────────────────┘            └─────────────────────┘
       │                              │ │ │
   ┌───┴────┐                    ┌────┼─┼─┴───────┐
   │        │                    │    │ │         │
Thread1  Thread2               Thread1 Thread2  Thread3
(locked) (waiting)             (acquire)(acquire)(acquire)
                               
Thread4 must WAIT              Thread4 must WAIT
(no other permits)             (no permits left)
```

---

## How Semaphore Works (Detailed)

### The Permit Pool Mechanism

```
INITIALIZATION:
┌─────────────────────────────┐
│ Semaphore(3)                │
│ ├─ Permit 1: available      │
│ ├─ Permit 2: available      │
│ └─ Permit 3: available      │
│                             │
│ Total: 3 available permits  │
└─────────────────────────────┘

THREAD A ACQUIRES:
┌─────────────────────────────┐
│ Semaphore(3)                │
│ ├─ Permit 1: TAKEN by A     │
│ ├─ Permit 2: available      │
│ └─ Permit 3: available      │
│                             │
│ Total: 2 available permits  │
└─────────────────────────────┘

THREAD B ACQUIRES:
┌─────────────────────────────┐
│ Semaphore(3)                │
│ ├─ Permit 1: TAKEN by A     │
│ ├─ Permit 2: TAKEN by B     │
│ └─ Permit 3: available      │
│                             │
│ Total: 1 available permit   │
└─────────────────────────────┘

THREAD C ACQUIRES:
┌─────────────────────────────┐
│ Semaphore(3)                │
│ ├─ Permit 1: TAKEN by A     │
│ ├─ Permit 2: TAKEN by B     │
│ └─ Permit 3: TAKEN by C     │
│                             │
│ Total: 0 available permits  │
└─────────────────────────────┘

THREAD D ARRIVES:
┌─────────────────────────────┐
│ Semaphore(3)                │
│ ├─ Permit 1: TAKEN by A     │
│ ├─ Permit 2: TAKEN by B     │
│ └─ Permit 3: TAKEN by C     │
│                             │
│ D is BLOCKED (waiting queue)│
└─────────────────────────────┘

THREAD A RELEASES:
┌─────────────────────────────┐
│ Semaphore(3)                │
│ ├─ Permit 1: available      │
│ ├─ Permit 2: TAKEN by B     │
│ └─ Permit 3: TAKEN by C     │
│                             │
│ D acquired Permit 1!        │
│ Total: 0 available permits  │
└─────────────────────────────┘
```

---

## The Key Difference: Who Can Release?

### ReentrantLock (Strict)

```
Lock Requirements:
├─ ONLY the thread that acquired the lock can release it
├─ Violating this = IllegalMonitorStateException
└─ Prevents accidents but limits flexibility

Example:
    Thread A acquires lock
    Thread A enters critical section
    Thread A MUST release (no one else can)
```

### Semaphore (Flexible)

```
Semaphore Permission:
├─ ANY thread can release a permit
├─ Even a thread that never acquired it!
└─ Enables advanced patterns

Example:
    Thread A acquires permit
    Thread A enters critical section
    Thread B, C, or even new Thread D can release
```

### Diagram: Lock vs Semaphore Release

```
REENTRANT LOCK:
Thread A: [acquire] → [execute] → [MUST release]
                                         ↑
                               Only Thread A can do this

Thread B: Cannot release (will throw exception)
Thread C: Cannot release (will throw exception)

SEMAPHORE:
Thread A: [acquire] → [execute] → [release] ✅
Thread B: Can also release ✅
Thread C: Can also release ✅
Thread D: Can also release ✅
```

---

## The Crazy Use Case: Deferred Release

### The Scenario

Imagine you want to:
1. Acquire a permit in Thread A
2. Do some work
3. Spawn a background task (Virtual Thread)
4. Let the background task release the permit later

```java
public class DeferredReleaseExample {
    
    private static final Semaphore semaphore = new Semaphore(3);
    
    public void processWithDeferredRelease() {
        try {
            semaphore.acquire();
            LOGGER.info("Acquired permit, processing...");
            
            // Do some immediate work
            doImmediateWork();
            
            // Now spawn a background task to handle cleanup
            // and permit release
            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(Duration.ofSeconds(5));  // Simulate async work
                    LOGGER.info("Background task releasing permit");
                    semaphore.release();  // Any thread can release!
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Original thread continues without waiting
            LOGGER.info("Original thread continuing...");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Timeline Diagram

```
Time →

Thread A:
  0ms:  [acquire permit]
        permits = 2
  
  10ms: [do immediate work]
  
  20ms: [spawn VT, return immediately]
        (doesn't wait for VT!)
        
  
Virtual Thread (background):
  0ms:  [idle]
  
  5000ms: [wake up]
  
  5010ms: [release permit]
           permits = 3

Thread B (waiting):
  0ms:    [trying to acquire]
           BLOCKED (no permits)
  
  5010ms: [finally acquires permit]
           (after VT releases)
```

---

## The Exclusive Access Use Case

### Scenario: Some Threads Want ALL Permits

Imagine in our nightclub:
- 3 VIP people can coexist (each takes 1 permit)
- But sometimes a celebrity arrives and wants the club ALL to themselves
- Celebrity acquires ALL 3 permits at once!

### Implementation

```java
public class ExclusiveAccessExample {
    
    private static final Semaphore semaphore = new Semaphore(3);
    
    // Normal thread: acquire 1 permit
    public void normalThreadAccess() {
        try {
            semaphore.acquire(1);  // Get 1 permit
            try {
                doWork();
            } finally {
                semaphore.release(1);  // Release 1 permit
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    // Celebrity thread: acquire ALL 3 permits
    public void exclusiveAccess() {
        try {
            semaphore.acquire(3);  // Get ALL permits!
            try {
                LOGGER.info("Celebrity: I have exclusive access!");
                doExclusiveWork();
            } finally {
                semaphore.release(3);  // Release all 3
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Timeline Diagram

```
Time →

Initial State:
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [1][2][3]  │
└─────────────────────┘

Thread A (normal): acquires(1)
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [A][2][3]  │
└─────────────────────┘

Thread B (normal): acquires(1)
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [A][B][3]  │
└─────────────────────┘

Thread C (normal): acquires(1)
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [A][B][C]  │
└─────────────────────┘

Thread D (normal): tries to acquire(1)
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [A][B][C]  │
│ D: BLOCKED          │
└─────────────────────┘

Later: Thread A releases
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [1][B][C]  │
└─────────────────────┘

Thread E (celebrity): tries to acquire(3)
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [1][B][C]  │
│ E: BLOCKED          │
│ (needs all 3!)      │
└─────────────────────┘

Thread B releases:
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [1][2][C]  │
│ E: STILL BLOCKED    │
└─────────────────────┘

Thread C releases:
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [1][2][3]  │
│ E: NOW ACQUIRES!    │
└─────────────────────┘

Thread E has exclusive access
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [E][E][E]  │
│ A, B, C, D: BLOCKED │
└─────────────────────┘

Thread E releases(3):
┌─────────────────────┐
│ Semaphore(3)        │
│ Permits: [1][2][3]  │
│ Others can now use  │
└─────────────────────┘
```

---

## Virtual Threads + Semaphore: The Game Changer

### Why Semaphore Works Better with VT

#### The Problem with synchronized/Lock

```
synchronized blocks PIN virtual threads:

Platform Thread (carrier):
├─ Virtual Thread A enters synchronized block
│  └─ VT A gets PINNED to this carrier thread
│     (carrier thread can't be reassigned!)
│
├─ Virtual Thread B tries to enter
│  └─ VT B BLOCKS the entire carrier thread
│     (even though VT B could run on a different carrier!)
│
└─ Result: Loss of scalability benefits
```

#### The Solution with Semaphore

```
Semaphore does NOT pin:

Platform Thread 1 (carrier):
├─ Virtual Thread A: semaphore.acquire()
│  └─ VT A is NOT pinned!
│     (can be unmounted if needed)
│
├─ Virtual Thread B: semaphore.acquire()
│  └─ VT B BLOCKS but doesn't block carrier
│     (carrier can run other VTs!)
│
Platform Thread 2 (carrier):
├─ Virtual Thread C: semaphore.acquire()
│  └─ Can run freely on this carrier
│
└─ Result: Full scalability maintained ✅
```

### Diagram: Pinning vs Non-Pinning

```
WITH synchronized (PINS - BAD):
┌──────────────────────────────────┐
│ Platform Thread (OS Resource)    │
├──────────────────────────────────┤
│ VT-A: [in synchronized block]    │
│       └─ PINNED HERE! 📌         │
│ VT-B: [waiting for lock]         │
│       └─ Blocks entire PT        │
│ VT-C: [queued]                   │
│       └─ Must wait               │
└──────────────────────────────────┘
❌ Wasted capacity on Platform Thread

WITH Semaphore (NO PINNING - GOOD):
┌──────────────────────────────────┐
│ Platform Thread 1                │
├──────────────────────────────────┤
│ VT-A: [acquired permit]          │
│       └─ Not pinned, can unmount │
│ VT-D: [acquired permit]          │
│       └─ Running independently   │
│ VT-G: [acquired permit]          │
│       └─ Running independently   │
└──────────────────────────────────┘
✅ Full platform thread utilization

┌──────────────────────────────────┐
│ Platform Thread 2                │
├──────────────────────────────────┤
│ VT-B: [waiting for permit]       │
│       └─ Not pinned              │
│ VT-E: [running other task]       │
│       └─ Different work          │
│ VT-H: [running other task]       │
│       └─ Different work          │
└──────────────────────────────────┘
✅ Full platform thread utilization
```

---

## Semaphore State Transitions

### State Diagram

```
START
  │
  ├─ new Semaphore(3) ─→ [Available: 3]
  │
  ├─ Thread-A.acquire() ─→ [Available: 2]
  │                            │
  │                   Thread-B.acquire()
  │                            │
  │                       [Available: 1]
  │                            │
  │                   Thread-C.acquire()
  │                            │
  │                       [Available: 0]
  │                            │
  │                   Thread-D.acquire()
  │                            │
  ├─────────────────── BLOCKED ─────────────
  │                   (waiting queue)
  │                            │
  │                   Thread-A.release()
  │                            │
  │                       [Available: 1]
  │                            │
  │                   D wakes up and acquires
  │                            │
  │                       [Available: 0]
  │                            │
  ├─ Thread-B.release() ─→ [Available: 1]
  ├─ Thread-C.release() ─→ [Available: 2]
  ├─ Thread-D.release() ─→ [Available: 3]
  │
  END
```

---

## Platform Threads vs Virtual Threads: When to Use Semaphore

### With Platform Threads (Traditional Use Case)

```java
public class DatabaseConnectionPoolExample {
    
    // Database can only handle 10 concurrent connections
    private static final Semaphore dbConnections = new Semaphore(10);
    
    // But we have a thread pool with 100 threads
    private static final ExecutorService executor = 
        Executors.newFixedThreadPool(100);
    
    public void executeQuery(String sql) {
        executor.submit(() -> {
            try {
                dbConnections.acquire();  // Max 10 threads accessing DB
                try {
                    // Only 10 threads can be here at once
                    // Even though we have 100 threads total
                    executeAgainstDatabase(sql);
                } finally {
                    dbConnections.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

**Timeline:**
```
Time →

100 threads available
    │
    ├─ Threads 1-10: acquire permit, query DB
    ├─ Threads 11-100: WAIT (no permits)
    │
    ├─ (1 second later)
    │
    ├─ Thread 1 finishes: release
    ├─ Thread 11 acquires permit, queries DB
    │
    └─ (cycle continues)
```

### With Virtual Threads (New Capability)

```java
public class VirtualThreadWithSemaphoreExample {
    
    // External API rate limit: max 5 concurrent calls
    private static final Semaphore apiRateLimit = new Semaphore(5);
    
    // Virtual thread per task - can handle thousands!
    private static final ExecutorService executor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    public void callExternalAPI(Request request) {
        executor.submit(() -> {
            try {
                apiRateLimit.acquire();  // Respect rate limit
                try {
                    // Only 5 VTs calling API at once
                    // But we could have 10,000 VTs total!
                    response = callAPI(request);
                } finally {
                    apiRateLimit.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

**Advantage:**
```
With Platform Threads: 
  Max 200 concurrent tasks (thread pool size)
  
With Virtual Threads:
  Can handle 10,000+ concurrent tasks
  Respects API limit of 5 concurrent calls
  No pinning, full scalability
```

---

## Semaphore API Reference

### Basic API

```java
// Create semaphore with N permits
Semaphore sem = new Semaphore(3);

// Acquire 1 permit (blocks if unavailable)
sem.acquire();

// Acquire N permits (blocks if unavailable)
sem.acquire(2);

// Release 1 permit
sem.release();

// Release N permits
sem.release(3);

// Try to acquire without blocking
boolean acquired = sem.tryAcquire();        // Try 1 permit
boolean acquired = sem.tryAcquire(2);       // Try 2 permits

// Try to acquire with timeout
boolean acquired = sem.tryAcquire(1, TimeUnit.SECONDS);
boolean acquired = sem.tryAcquire(2, 5, TimeUnit.SECONDS);

// Get available permit count
int available = sem.availablePermits();

// Get queued threads count
int waiting = sem.getQueueLength();
```

### Fair Semaphore (FIFO Order)

```java
// Regular semaphore (no order guarantee)
Semaphore unfair = new Semaphore(3);

// Fair semaphore (FIFO - first come, first served)
Semaphore fair = new Semaphore(3, true);

// Fair guarantees:
// ├─ If Thread A waits before Thread B
// ├─ Then Thread A gets permit before Thread B
// └─ Prevents thread starvation
```

---

## Complete Example: Multi-Service Rate Limiting

```java
public class MultiServiceRateLimiter {
    
    // Different services have different rate limits
    private static final Semaphore userServiceLimit = new Semaphore(5);
    private static final Semaphore orderServiceLimit = new Semaphore(3);
    private static final Semaphore paymentServiceLimit = new Semaphore(2);
    
    private static final ExecutorService executor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    // Get user (max 5 concurrent)
    public void getUser(int userId, Consumer<User> callback) {
        executor.submit(() -> {
            try {
                userServiceLimit.acquire();
                try {
                    User user = userService.fetch(userId);
                    callback.accept(user);
                } finally {
                    userServiceLimit.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    // Get order (max 3 concurrent)
    public void getOrder(int orderId, Consumer<Order> callback) {
        executor.submit(() -> {
            try {
                orderServiceLimit.acquire();
                try {
                    Order order = orderService.fetch(orderId);
                    callback.accept(order);
                } finally {
                    orderServiceLimit.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    // Process payment (max 2 concurrent - expensive!)
    public void processPayment(Payment payment, Consumer<Receipt> callback) {
        executor.submit(() -> {
            try {
                paymentServiceLimit.acquire();
                try {
                    Receipt receipt = paymentService.process(payment);
                    callback.accept(receipt);
                } finally {
                    paymentServiceLimit.release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

### Timeline Visualization

```
User Service (limit: 5)
├─ VT-1: [acquire] ─ 4 left
├─ VT-2: [acquire] ─ 3 left
├─ VT-3: [acquire] ─ 2 left
├─ VT-4: [acquire] ─ 1 left
├─ VT-5: [acquire] ─ 0 left
├─ VT-6: [BLOCK] (waiting)
└─ VT-7: [BLOCK] (waiting)

Order Service (limit: 3)
├─ VT-8: [acquire] ─ 2 left
├─ VT-9: [acquire] ─ 1 left
├─ VT-10: [acquire] ─ 0 left
├─ VT-11: [BLOCK] (waiting)
└─ VT-12: [BLOCK] (waiting)

Payment Service (limit: 2)
├─ VT-13: [acquire] ─ 1 left
├─ VT-14: [acquire] ─ 0 left
├─ VT-15: [BLOCK] (waiting)
├─ VT-16: [BLOCK] (waiting)
└─ VT-17: [BLOCK] (waiting)
```

---

## Semaphore vs Other Synchronization Tools

### Comparison Table

| Tool | Purpose | Key Feature | Best For |
|------|---------|------------|----------|
| **synchronized** | Mutual exclusion | Simple, keyword-based | Simple critical sections |
| **ReentrantLock** | Mutual exclusion | More flexible than synchronized | Exclusive access |
| **Semaphore** | Shared resource limiting | Multiple permits | Rate limiting, resource pooling |
| **CountDownLatch** | Wait for N tasks | One-time synchronization | Waiting for multiple tasks |
| **CyclicBarrier** | Synchronization point | Reusable barrier | Synchronizing thread batches |

### Decision Tree

```
Do you need to:

├─ Limit concurrent access to N resources?
│  └─ Use Semaphore(N)
│
├─ Ensure only 1 thread executes critical section?
│  └─ Use synchronized or ReentrantLock
│
├─ Wait for multiple tasks to complete?
│  └─ Use CountDownLatch or CompletableFuture
│
├─ Synchronize N threads at a barrier point?
│  └─ Use CyclicBarrier
│
└─ Rate limit virtual thread execution?
   └─ Use Semaphore (preferred over Lock!)
```

---

## Best Practices

### ✅ DO: Use Try-Finally with Semaphore

```java
try {
    semaphore.acquire();
    try {
        doWork();
    } finally {
        semaphore.release();  // Always release!
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### ✅ DO: Use Fair Semaphore for Critical Resources

```java
// Prevents thread starvation
Semaphore fair = new Semaphore(3, true);
```

### ✅ DO: Use Semaphore Instead of Lock with Virtual Threads

```java
// With VT, prefer Semaphore to avoid pinning
Semaphore semaphore = new Semaphore(1);  // Acts like a lock, but non-pinning
```

### ❌ DON'T: Forget to Handle InterruptedException

```java
// WRONG - ignores interruption
semaphore.acquire();
doWork();
semaphore.release();

// RIGHT - respects interruption
try {
    semaphore.acquire();
    try {
        doWork();
    } finally {
        semaphore.release();
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new RuntimeException(e);
}
```

### ❌ DON'T: Use Semaphore When You Mean Lock

```java
// WRONG - using Semaphore(1) as a lock
Semaphore sem = new Semaphore(1);
sem.acquire();
doExclusiveWork();
// Any thread can release! (wrong semantics)

// RIGHT - use ReentrantLock for exclusive access
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    doExclusiveWork();
} finally {
    lock.unlock();  // Only this thread can call this
}
```

---

## Key Takeaways

1. **Semaphore = Permit System** - Controls shared resource access through permits

2. **Not Just for Rate Limiting** - Can acquire/release multiple permits for flexible access patterns

3. **Any Thread Can Release** - Unlike locks, enables deferred release patterns

4. **Virtual Thread Friendly** - Doesn't pin threads, maintains scalability

5. **Platform Thread Compatible** - Works great with platform threads too (resource pooling)

6. **Always Use Try-Finally** - Ensures permits are released even on exception

7. **Fair vs Unfair** - Use fair semaphores to prevent thread starvation

8. **Better Than Lock for VT** - Prefer Semaphore over synchronized/ReentrantLock with virtual threads to avoid pinning

---

## Quick Reference

### Simple Rate Limiting (Most Common)

```java
Semaphore limit = new Semaphore(3);

executor.submit(() -> {
    limit.acquire();
    try {
        apiCall();
    } finally {
        limit.release();
    }
});
```

### Exclusive Access (Like a Lock)

```java
Semaphore exclusive = new Semaphore(1);
// Acts like a lock, but non-pinning with VT
```

### Acquire Multiple Permits

```java
Semaphore sem = new Semaphore(10);
sem.acquire(5);  // Take 5 permits at once
try {
    heavyWork();
} finally {
    sem.release(5);  // Release all 5
}
```

### Fair FIFO Order

```java
Semaphore fair = new Semaphore(3, true);
// FIFO order: first waiter gets first permit
```

---

## Historical Context

- **Introduced:** Java 5 (same as ExecutorService, CountDownLatch, CyclicBarrier)
- **Not New Technology** - Well-tested, production-proven for 15+ years
- **Renewed Interest** - Virtual threads make them the preferred synchronization tool for I/O-heavy workloads
- **Future-Proof** - Works with platform threads AND virtual threads

---

## Further Reading

- [Java Semaphore Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html)
- [Virtual Threads and Thread Pinning](https://wiki.openjdk.org/display/loom/Pinning)
- [Java Concurrency in Practice - Chapter 5](https://jcip.net/)
- [Semaphore vs Lock](https://en.wikipedia.org/wiki/Semaphore_%28programming%29)

---

**Remember:** Semaphores are not new, but virtual threads finally make them the ideal choice for rate-limiting and resource pooling in Java! 🔐✨

---

# 🎁 ConcurrencyLimiter: Building a Reusable Wrapper

## TL;DR

**ConcurrencyLimiter** is a wrapper around `ExecutorService` that automatically applies semaphore-based concurrency limiting to every submitted task.

```java
// Without wrapper: Manual semaphore management in every task
executor.submit(() -> {
    semaphore.acquire();
    try {
        doWork();
    } finally {
        semaphore.release();
    }
});

// With wrapper: Clean, reusable abstraction
var limiter = new ConcurrencyLimiter(executor, 3);
limiter.submit(() -> doWork());  // Concurrency limit applied automatically!
```

---

## The Problem: Repetitive Semaphore Code

### Without a Wrapper

```java
Semaphore semaphore = new Semaphore(3);
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// Every single task submission looks like this:
executor.submit(() -> {
    try {
        semaphore.acquire();
        try {
            task1();
        } finally {
            semaphore.release();
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

executor.submit(() -> {
    try {
        semaphore.acquire();
        try {
            task2();
        } finally {
            semaphore.release();
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

executor.submit(() -> {
    try {
        semaphore.acquire();
        try {
            task3();
        } finally {
            semaphore.release();
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
});

// ... repeat for all 20 tasks 😫
```

**Problems:**
- ❌ Boilerplate code repeated everywhere
- ❌ Easy to make mistakes (forget try-finally, semaphore.release())
- ❌ Hard to maintain (change limit? update everywhere!)
- ❌ Unclear intent (semaphore logic buries the actual work)

### With a Wrapper (Clean!)

```java
var limiter = new ConcurrencyLimiter(executor, 3);

limiter.submit(() -> task1());
limiter.submit(() -> task2());
limiter.submit(() -> task3());
// ... repeat for all 20 tasks

// Clean, simple, intent is clear!
```

---

## The ConcurrencyLimiter Class

### Complete Implementation

```java
public class ConcurrencyLimiter implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ConcurrencyLimiter.class
    );

    private final ExecutorService executor;
    private final Semaphore semaphore;

    // Constructor: Initialize with executor and concurrency limit
    public ConcurrencyLimiter(ExecutorService executor, int limit) {
        this.executor = executor;
        this.semaphore = new Semaphore(limit);
    }

    // Public API: Submit a callable task (returns Future)
    public <T> Future<T> submit(Callable<T> callable) {
        return executor.submit(() -> wrapCallable(callable));
    }

    // Private helper: Wrap callable with semaphore logic
    private <T> T wrapCallable(Callable<T> callable) {
        try {
            semaphore.acquire();           // Get permission
            return callable.call();         // Execute task
        } catch (Exception e) {
            LOGGER.error("Exception in callable", e);
            throw new RuntimeException(e);  // Propagate error
        } finally {
            semaphore.release();            // Always release
        }
    }

    // AutoCloseable: Clean shutdown
    @Override
    public void close() {
        this.executor.close();
    }
}
```

### What Each Part Does

```
ConcurrencyLimiter
├─ executor: The underlying ExecutorService (VT or Platform threads)
├─ semaphore: Controls concurrency (e.g., max 3 tasks)
│
├─ Constructor(executor, limit)
│  └─ Sets up the semaphore with N permits
│
├─ submit(callable)
│  └─ Public API - user-facing method
│     └─ Wraps the callable with semaphore logic
│        └─ Delegates to executor
│
├─ wrapCallable(callable)
│  └─ Private helper - does the actual wrapping
│     ├─ acquire() - wait for permission
│     ├─ call() - execute the task
│     ├─ catch - log errors
│     └─ finally - always release
│
└─ close()
   └─ Shutdown the executor (AutoCloseable)
```

---

## Design Pattern: Decorator Pattern

### What is the Decorator Pattern?

The **Decorator Pattern** wraps an object to add behavior without modifying the original.

```
Before (without decoration):
┌──────────────────┐
│  ExecutorService │
└──────────────────┘

After (with decoration):
┌────────────────────────────┐
│   ConcurrencyLimiter       │
│  ┌──────────────────────┐  │
│  │ ExecutorService      │  │
│  └──────────────────────┘  │
│  + Adds semaphore logic    │
└────────────────────────────┘
```

### Flow Diagram

```
User Code:
  limiter.submit(callable)
           ↓
ConcurrencyLimiter:
  ├─ Wraps callable with semaphore
  │  ├─ semaphore.acquire()
  │  ├─ callable.call()
  │  └─ semaphore.release()
  │
  └─ executor.submit(wrappedCallable)
           ↓
ExecutorService (VT or PT):
  ├─ Creates thread/VT
  └─ Executes wrappedCallable
           ↓
Task Execution:
  ├─ Waits for semaphore permit (max 3 concurrent)
  ├─ Executes actual work
  └─ Releases permit
```

---

## Step-by-Step: How It Works

### Step 1: User Submits Task

```java
var limiter = new ConcurrencyLimiter(executor, 3);
limiter.submit(() -> Client.getProduct(1));
```

### Step 2: ConcurrencyLimiter.submit() is Called

```java
public <T> Future<T> submit(Callable<T> callable) {
    // callable = () -> Client.getProduct(1)
    
    return executor.submit(() -> wrapCallable(callable));
    //                       ↑
    //                   New callable that:
    //                   1. Acquires semaphore
    //                   2. Runs original callable
    //                   3. Releases semaphore
}
```

### Step 3: Executor Receives Wrapped Callable

```
Executor sees:
() -> {
    semaphore.acquire();
    try {
        callable.call();  // Client.getProduct(1)
    } finally {
        semaphore.release();
    }
}
```

### Step 4: Execution Timeline

```
Task 1 submitted: submit(() -> product(1))
  └─ ConcurrencyLimiter.submit() called
     └─ Executor.submit(wrapped) called
        └─ Creates virtual thread
           └─ VT starts execution
              ├─ semaphore.acquire() → Success (permits: 2)
              ├─ callable.call() → product(1) executes
              ├─ (API call takes 1 second)
              └─ (VT still holding semaphore)

Task 2 submitted: submit(() -> product(2))
  └─ Same flow
     └─ VT-2: semaphore.acquire() → Success (permits: 1)

Task 3 submitted: submit(() -> product(3))
  └─ Same flow
     └─ VT-3: semaphore.acquire() → Success (permits: 0)

Task 4 submitted: submit(() -> product(4))
  └─ ConcurrencyLimiter.submit() called
     └─ Executor.submit(wrapped) called
        └─ Creates virtual thread
           └─ VT starts execution
              ├─ semaphore.acquire() → BLOCKED! (no permits)
              └─ (waits for one of the first 3 to release)

(1 second passes)

Task 1 finishes: product(1) returns
  └─ VT-1: semaphore.release() → Permits: 1
     └─ VT-4: semaphore.acquire() now succeeds!
        └─ VT-4 can now execute product(4)
```

---

## Usage: Before and After

### Version 1: Without Wrapper ❌

```java
public class Lec05ConcurrencyLimitWithoutWrapper {

    static void main() {
        Semaphore semaphore = new Semaphore(3);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        
        try (executor) {
            for (int i = 1; i <= 20; i++) {
                int id = i;
                executor.submit(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            printProductInfo(id);
                        } finally {
                            semaphore.release();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
    }

    private static void printProductInfo(int id) {
        var product = Client.getProduct(id);
        LOGGER.info("{} => {}", id, product);
    }
}
```

**Problems:**
- ❌ Repetitive try-catch-finally for semaphore
- ❌ Error handling mixed in
- ❌ 15 lines per task submission
- ❌ Easy to forget InterruptedException handling

---

### Version 2: With Wrapper ✅

```java
public class Lec06ConcurrencyLimitWithWrapper {

    static void main() {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        var limiter = new ConcurrencyLimiter(executor, 3);
        
        try (limiter) {
            for (int i = 1; i <= 20; i++) {
                int id = i;
                limiter.submit(() -> printProductInfo(id));
            }
            LOGGER.info("tasks submitted");
        }
    }

    private static String printProductInfo(int id) {
        var product = Client.getProduct(id);
        LOGGER.info("{} => {}", id, product);
        return product;
    }
}
```

**Benefits:**
- ✅ Clean, readable code
- ✅ Intent is clear (submit task with limit)
- ✅ One line per task submission
- ✅ Error handling hidden (but still works!)
- ✅ Reusable across the codebase

---

## Output Comparison

### Without Wrapper (Manual Semaphore)

```
00:26:05.677 [virtual-48] Calling product/10
00:26:05.677 [virtual-49] Calling product/11
00:26:05.677 [virtual-47] Calling product/9
(3 concurrent calls as expected, but code was messy)
```

### With Wrapper (Clean!)

```
00:26:05.677 [bodera-virtual15] Calling product/15
00:26:05.677 [bodera-virtual13] Calling product/13
00:26:05.677 [bodera-virtual16] Calling product/16
(Same behavior, but code is much cleaner!)
```

---

## With Custom Thread Names

### Code

```java
static void main() {
    var factory = Thread.ofVirtual()
        .name("bodera-virtual", 1)
        .factory();
    
    var executor = Executors.newThreadPerTaskExecutor(factory);
    var limiter = new ConcurrencyLimiter(executor, 3);
    execute(limiter, 20);
}
```

### Output

```
00:27:24.017 [bodera-virtual15] Calling product/15
00:27:24.017 [bodera-virtual13] Calling product/13
00:27:24.017 [bodera-virtual16] Calling product/16
00:27:25.030 [bodera-virtual16] 16 => Durable Aluminum Car
00:27:25.030 [bodera-virtual15] 15 => Small Aluminum Bag
00:27:25.030 [bodera-virtual13] 13 => Enormous Bronze Bag
00:27:25.030 [bodera-virtual17] Calling product/17
00:27:25.030 [bodera-virtual18] Calling product/18
00:27:25.030 [bodera-virtual19] Calling product/19
(... 3 more tasks in parallel ...)
```

**Observation:**
- ✅ Exactly 3 API calls at a time (15, 13, 16)
- ✅ When they complete, next 3 start (17, 18, 19)
- ✅ Thread names are descriptive
- ✅ Concurrency limit is respected perfectly!

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│  User Application                                       │
│  └─ limiter.submit(callable)                            │
│     └─ limiter.submit(callable)                         │
│        └─ limiter.submit(callable) × 20                 │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│  ConcurrencyLimiter                                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │ public <T> Future<T> submit(Callable<T> callable)│  │
│  │   return executor.submit(() ->                  │  │
│  │     wrapCallable(callable))                     │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │ private <T> T wrapCallable(Callable<T> callable)│  │
│  │   semaphore.acquire()                           │  │
│  │   try:                                          │  │
│  │     return callable.call()                      │  │
│  │   finally:                                      │  │
│  │     semaphore.release()                        │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  Members:                                               │
│  ├─ ExecutorService executor                            │
│  └─ Semaphore semaphore (limit=3)                       │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│  ExecutorService (VT or Platform)                       │
│  ├─ Creates virtual threads                             │
│  ├─ Executes wrapped callables                          │
│  └─ Manages thread lifecycle                            │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│  Execution                                              │
│  ├─ VT-1: semaphore.acquire() → execute → release       │
│  ├─ VT-2: semaphore.acquire() → execute → release       │
│  ├─ VT-3: semaphore.acquire() → execute → release       │
│  ├─ VT-4: semaphore.acquire() [BLOCKED] → ...           │
│  └─ ... (repeats as permits become available)          │
└─────────────────────────────────────────────────────────┘
```

---

## Generic Implementation: Understanding `<T>`

### Why Generics?

The wrapper should work with **any type** of callable return value:

```java
// Callable that returns String
limiter.submit(() -> Client.getProduct(1));  // Future<String>

// Callable that returns Integer
limiter.submit(() -> calculateTotal());      // Future<Integer>

// Callable that returns User
limiter.submit(() -> userService.getUser()); // Future<User>

// Callable that returns Void
limiter.submit(() -> {
    System.out.println("done");
    return null;
});                                          // Future<Void>
```

### Generic Method Signature

```java
public <T> Future<T> submit(Callable<T> callable) {
    //      ↑ Type parameter
    //      └─ "T can be any type"
    
    return executor.submit(() -> wrapCallable(callable));
    //     ↑ Returns Future<T>
}

private <T> T wrapCallable(Callable<T> callable) {
    //      ↑ Type parameter (same as above)
    try {
        semaphore.acquire();
        return callable.call();  // Returns T
        //     ↑ Type T
    } finally {
        semaphore.release();
    }
}
```

### Type Safety Example

```java
// Compile-time type checking
var limiter = new ConcurrencyLimiter(executor, 3);

// ✅ Correct - submit returns Callable<String>
Future<String> future1 = limiter.submit(
    () -> Client.getProduct(1)  // Returns String
);
String product = future1.get();

// ❌ Compile error - type mismatch
Future<Integer> future2 = limiter.submit(
    () -> Client.getProduct(1)  // Returns String, not Integer!
);
```

---

## AutoCloseable Implementation

### Why Implement AutoCloseable?

```java
public class ConcurrencyLimiter implements AutoCloseable {
    
    @Override
    public void close() {
        this.executor.close();
    }
}
```

### Enables Try-With-Resources

```java
// Before: Manual cleanup
var limiter = new ConcurrencyLimiter(executor, 3);
try {
    limiter.submit(() -> task1());
    limiter.submit(() -> task2());
} finally {
    limiter.close();  // ← Manual cleanup
}

// After: Automatic cleanup
try (var limiter = new ConcurrencyLimiter(executor, 3)) {
    limiter.submit(() -> task1());
    limiter.submit(() -> task2());
}  // ← Automatic shutdown
```

---

## Error Handling Design

### Current Implementation

```java
private <T> T wrapCallable(Callable<T> callable) {
    try {
        semaphore.acquire();
        return callable.call();
    } catch (Exception e) {
        LOGGER.error("Exception in callable", e);
        throw new RuntimeException(e);  // Wrap and throw
    } finally {
        semaphore.release();
    }
}
```

### Why This Approach?

1. **Checked Exception Handling**
   - `Callable.call()` throws `Exception`
   - We need to handle it

2. **Logging**
   - Log the error for debugging

3. **Propagation**
   - Wrap in RuntimeException for caller

4. **Finally Block**
   - ALWAYS release the semaphore, even on error!

### Timeline with Error

```
Task execution with error:

0ms:   semaphore.acquire() → Success (permits: 2)
1ms:   callable.call() → Throws exception!
2ms:   LOGGER.error() → Log the error
3ms:   CATCH block executes
4ms:   THROW RuntimeException
       ↓
       FINALLY block still executes!
       ├─ semaphore.release()
       └─ Permits: 3 (restored!)
       ↓
5ms:   Exception propagates to caller
       └─ future.get() throws RuntimeException
```

**Key:** Even with exception, semaphore is released!

---

## Extensibility: Enhancing the Wrapper

### Version 1: Basic (Current)

```java
public <T> Future<T> submit(Callable<T> callable)
```

### Version 2: With Timeout

```java
public <T> Future<T> submit(Callable<T> callable, 
                            long timeout, TimeUnit unit) {
    return executor.submit(() -> {
        if (!semaphore.tryAcquire(timeout, unit)) {
            throw new TimeoutException("Couldn't acquire permit");
        }
        try {
            return callable.call();
        } finally {
            semaphore.release();
        }
    });
}
```

### Version 3: With Fallback

```java
public <T> Future<T> submit(Callable<T> callable,
                            T fallbackValue) {
    return executor.submit(() -> {
        if (!semaphore.tryAcquire()) {
            LOGGER.warn("Returning fallback value");
            return fallbackValue;  // Return default instead of blocking
        }
        try {
            return callable.call();
        } finally {
            semaphore.release();
        }
    });
}
```

### Version 4: Full ExecutorService Delegation

```java
public class FullConcurrencyLimiter implements ExecutorService {
    // Implement ALL ExecutorService methods
    // ├─ submit(Runnable)
    // ├─ submit(Callable)
    // ├─ invokeAll()
    // ├─ invokeAny()
    // ├─ execute()
    // └─ ... etc
    
    // Each method wraps with semaphore logic
}
```

---

## Real-World Usage Scenarios

### Scenario 1: Database Connection Limiting

```java
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
var dbLimiter = new ConcurrencyLimiter(executor, 10);  // Max 10 DB connections

// Can submit thousands of tasks, but only 10 access DB concurrently
for (User user : millionUsers) {
    dbLimiter.submit(() -> updateUserInDatabase(user));
}
```

### Scenario 2: External API Rate Limiting

```java
var executor = Executors.newVirtualThreadPerTaskExecutor();
var apiLimiter = new ConcurrencyLimiter(executor, 5);  // API allows 5/sec

// Submit all tasks, but respect rate limit
for (Request request : allRequests) {
    apiLimiter.submit(() -> externalAPI.call(request));
}
```

### Scenario 3: Multiple Rate Limits

```java
// Different limits for different services
var userServiceLimiter = new ConcurrencyLimiter(executor, 5);
var orderServiceLimiter = new ConcurrencyLimiter(executor, 3);
var paymentServiceLimiter = new ConcurrencyLimiter(executor, 2);

// Use each as needed
userServiceLimiter.submit(() -> userService.getUser(id));
orderServiceLimiter.submit(() -> orderService.getOrder(id));
paymentServiceLimiter.submit(() -> paymentService.charge(amount));
```

---

## Key Design Insights

### 1. Separation of Concerns

```
ConcurrencyLimiter:
├─ Responsibility: Apply concurrency limit
└─ Does NOT care about: Actual work being done

ExecutorService:
├─ Responsibility: Thread management
└─ Does NOT care about: Concurrency limits

Callable:
├─ Responsibility: Actual work
└─ Does NOT care about: How it's executed
```

### 2. Composability

```java
// Can combine multiple wrappers!
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
var timeoutExecutor = new TimeoutDecorator(executor);
var loggingExecutor = new LoggingDecorator(timeoutExecutor);
var limitedExecutor = new ConcurrencyLimiter(loggingExecutor, 3);

// Each layer adds behavior!
```

### 3. Reusability

```java
// Create once, use many times
var limiter = new ConcurrencyLimiter(executor, 3);

limiter.submit(task1);
limiter.submit(task2);
limiter.submit(task3);
// ... 100 more times

// Logic centralized in one place
```

---

## Testing the Concurrency Limiter

### Test: Verify Concurrency Limit

```java
@Test
public void testConcurrencyLimit() throws InterruptedException {
    AtomicInteger currentConcurrency = new AtomicInteger(0);
    AtomicInteger maxConcurrency = new AtomicInteger(0);
    
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    var limiter = new ConcurrencyLimiter(executor, 3);
    
    for (int i = 0; i < 20; i++) {
        limiter.submit(() -> {
            int current = currentConcurrency.incrementAndGet();
            maxConcurrency.updateAndGet(max -> Math.max(max, current));
            
            Thread.sleep(Duration.ofSeconds(1));
            currentConcurrency.decrementAndGet();
            
            return null;
        });
    }
    
    Thread.sleep(Duration.ofSeconds(5));
    
    // Verify max concurrency was never exceeded
    assertTrue(maxConcurrency.get() <= 3, 
        "Max concurrency was " + maxConcurrency.get());
}
```

---

## Summary Table

| Aspect | Without Wrapper | With Wrapper |
|--------|-----------------|--------------|
| **Code per task** | 15 lines | 1 line |
| **Boilerplate** | High | None |
| **Error handling** | Inline | Centralized |
| **Maintainability** | Low | High |
| **Reusability** | None | Full |
| **Clarity** | Mixed concerns | Clear intent |
| **Performance** | Same | Same |
| **Safety** | Easy to break | Hard to break |

---

## Best Practices

### ✅ DO: Use for External Service Calls

```java
var limiter = new ConcurrencyLimiter(executor, rateLimitFromAPI);
for (Request req : requests) {
    limiter.submit(() -> externalAPI.call(req));
}
```

### ✅ DO: Use for Database Operations

```java
var limiter = new ConcurrencyLimiter(executor, dbPoolSize);
for (User user : users) {
    limiter.submit(() -> database.save(user));
}
```

### ✅ DO: Use Try-With-Resources

```java
try (var limiter = new ConcurrencyLimiter(executor, 3)) {
    // Submit tasks
}  // Auto-cleanup
```

### ❌ DON'T: Ignore Returned Futures

```java
// WRONG - fire and forget
limiter.submit(() -> importantTask());

// RIGHT - handle results or errors
Future<Result> future = limiter.submit(() -> importantTask());
try {
    Result result = future.get();
} catch (ExecutionException e) {
    // Handle error
}
```

### ❌ DON'T: Mix Multiple Limiters Incorrectly

```java
// WRONG - each limiter has its own semaphore
var limiter1 = new ConcurrencyLimiter(executor, 3);
var limiter2 = new ConcurrencyLimiter(executor, 3);
// Total: 6 concurrent (not 3!)

// RIGHT - share executor, use single limiter
var limiter = new ConcurrencyLimiter(executor, 3);
// Total: exactly 3 concurrent
```

---

## Key Takeaways

1. **ConcurrencyLimiter solves a real problem** - Repetitive semaphore wrapping code

2. **Decorator pattern in action** - Adds behavior without modifying original

3. **Separation of concerns** - Concurrency logic separate from business logic

4. **Reusability matters** - Write once, use everywhere

5. **Error handling still works** - Even if something goes wrong, semaphore is released

6. **Try-with-resources friendly** - Implements AutoCloseable for clean shutdown

7. **Works with any executor** - Platform threads, virtual threads, custom executors

8. **Generic implementation** - Handles any return type

---

## Quick Reference

### Basic Usage

```java
var executor = Executors.newVirtualThreadPerTaskExecutor();
var limiter = new ConcurrencyLimiter(executor, 3);

try (limiter) {
    for (int i = 1; i <= 20; i++) {
        int id = i;
        limiter.submit(() -> doWork(id));
    }
}
```

### With Custom Thread Names

```java
var factory = Thread.ofVirtual()
    .name("task-", 1)
    .factory();

var executor = Executors.newThreadPerTaskExecutor(factory);
var limiter = new ConcurrencyLimiter(executor, 3);

limiter.submit(() -> doWork());
```

### With Different Limits per Service

```java
var userLimiter = new ConcurrencyLimiter(executor, 5);
var orderLimiter = new ConcurrencyLimiter(executor, 3);
var paymentLimiter = new ConcurrencyLimiter(executor, 2);
```

---

**Next Concept:** Building on ConcurrencyLimiter, you can extend it with timeout handling, fallback values, and more advanced patterns! 🚀

---

## Further Reading

- [Decorator Pattern](https://en.wikipedia.org/wiki/Decorator_pattern)
- [Java Executor Framework](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
- [Virtual Threads Documentation](https://openjdk.org/jeps/444)
- [Semaphore API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html)

---

**Remember:** Good abstractions hide complexity and make code more maintainable. ConcurrencyLimiter is a perfect example! ✨