package loadbalancer.com.component;

import loadbalancer.com.service.RateLimiter;

import java.util.concurrent.atomic.AtomicLong;

public class LeakyBucketRateLimiter implements RateLimiter {

    private final long capacity;
    private final long ratePerSecond;
    private final AtomicLong lastRequestTime;
    private final AtomicLong currentBucketSize;

    public LeakyBucketRateLimiter(long capacity, long ratePerSecond) {
        this.capacity = capacity;
        this.ratePerSecond = ratePerSecond;
        this.lastRequestTime = new AtomicLong(System.currentTimeMillis());
        this.currentBucketSize = new AtomicLong(0);
    }


    @Override
    public boolean isAllowed() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastRequestTime.getAndSet(currentTime);

        long leakedTokens = elapsedTime * ratePerSecond / 1000;
        currentBucketSize.updateAndGet(bucketSize ->
                Math.max(0, Math.min(bucketSize + leakedTokens, capacity)));


        if (currentBucketSize.get() > 0) {
            currentBucketSize.decrementAndGet();
            return true;
        }

        return false;
    }
}
