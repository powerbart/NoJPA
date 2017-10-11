package dk.lessismore.nojpa.flow;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public abstract class FlowVisitor<T extends ModelObjectInterface> {

    protected final Logger log = LoggerFactory.getLogger(getClass());


    private ThreadPoolTaskExecutor executor = null;
    int currentCount = 0;

    @Bean
    public ThreadPoolTaskExecutor getExecutor() {
        if(executor == null){
            executor = new ThreadPoolTaskExecutor();
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setThreadNamePrefix(getThreadNamePrefix());
            executor.setCorePoolSize(getNumberOfThreads());
            executor.setMaxPoolSize(getNumberOfThreads());
            executor.setQueueCapacity(Integer.MAX_VALUE);
            executor.initialize();
        }
        return executor;
    }

    protected abstract int getNumberOfThreads();

    protected abstract int getMinimumQueueSize();

    protected abstract int getSplitLimitSize();


    protected abstract String getThreadNamePrefix();

    public int getCurrentQueueSize(){
        return getExecutor().getThreadPoolExecutor().getQueue().size();
    }

    public void runFlow() {
        final int totalCount = getTotalCount();


        while (getSplitLimitSize() + currentCount < totalCount) {
            while (getCurrentQueueSize() < getMinimumQueueSize()) {
                log.debug("WorkQueue-fill-up-start: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
                MQL.SelectQuery<T> query = query();
                if(startFromBeginning()){
                    query.limit(getSplitLimitSize());
                } else {
                    query.limit(currentCount, currentCount + getSplitLimitSize());
                }
                log.debug("WorkQueue-fill-up-end: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
                int oldCount = currentCount;
                query.visit(new AbstractCountingVisitor<T>() {
                            @Override
                            public void process(T t) {
                                if(currentCount % (getSplitLimitSize() / 10) == 0){
                                    log.debug("WorkQueue-PROCESS: currentCount("+ currentCount +")");
                                }
                                currentCount++;
                                getExecutor().execute(() -> doWork(t));
                            }
                        });
                if (oldCount == currentCount) {
                    // didn't visit any
                    break;
                }
            }
            log.debug("WorkQueue-is-full: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
            int beforeQSize = getCurrentQueueSize();
            int sameCount = 0;
            for (int i = 0; i < 180; i++) {
                if (getCurrentQueueSize() == 0 && totalCount == currentCount) {
                    // we're DONE!
                    break;
                }
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                }
                if(beforeQSize == getCurrentQueueSize()){
                    log.debug("WorkQueue-has-not-changed: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
                    sameCount++;
                }
                log.debug("WorkQueue-checking: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
                if(getCurrentQueueSize() < getMinimumQueueSize()){
                    break;
                }
                log.debug("WorkQueue-is-full: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
            }
            if(sameCount == 180){
                log.debug("WorkQueue-has-not-changed-for-long-time: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +")");
                log.error("Will now try to force a shutdown... ");
                break;
            }


        }

        // closing things
        try{
            log.error("Will close... ");
            close();
        } catch (Exception e){

        } finally {
            System.exit(-1);
        }



    }

    public abstract MQL.SelectQuery<T> query();


    public abstract boolean startFromBeginning();
    public abstract void doWork(T t);
    public abstract void close();
    public abstract int getTotalCount();

}
