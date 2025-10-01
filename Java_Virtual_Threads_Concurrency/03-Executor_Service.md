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

---

_💡 Pro Tip: Don't think of virtual threads as replacing everything—think of them as a powerful new tool in your concurrent programming toolkit. Traditional executors still have their place, but virtual threads shine when you have high-concurrency, I/O-heavy workloads!_



