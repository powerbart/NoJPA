package dk.lessismore.nojpa.flow;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public abstract class FlowVisitor<T extends ModelObjectInterface> {


    private final ThreadPoolTaskExecutor executor;

    public FlowVisitor() {
        executor = new ThreadPoolTaskExecutor();
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix(getThreadNamePrefix());
        executor.setCorePoolSize(getNumberOfThreads());
        executor.setMaxPoolSize(getNumberOfThreads());
    }

    abstract int getNumberOfThreads();

    abstract int getMinimumQueueSize();

    abstract int getSplitLimitSize();


    abstract String getThreadNamePrefix();

    public void runFlow() {
        int totalCount = query().getCount();

        int queueSize = executor.getThreadPoolExecutor().getQueue().size();

        int currentCount = 0;
        while (totalCount < getSplitLimitSize() + currentCount)
            while (queueSize < getMinimumQueueSize()) {
                // TODO do something with totalCount

                query().limit(currentCount, getSplitLimitSize())
                        .visit(new AbstractCountingVisitor<T>() {
                            @Override
                            public void process(T t) {
                                executor.execute(() -> process(t));
                            }
                        });
            }


    }

    public abstract MQL.SelectQuery<T> query();


    public abstract void process(T t);

}
