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