package dk.lessismore.nojpa.flow;

import dk.lessismore.nojpa.db.methodquery.MQL;
import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import dk.lessismore.nojpa.utils.MaxSizeMap;
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

    int sameCount = 0;
    int currentEnd = 0;
    public void runFlow() {
        final int totalCount = getTotalCount();

        while (currentCount < totalCount && currentEnd < totalCount) {
            log.debug("WorkQueue-fill-up-check: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount );
            if(getCurrentQueueSize() < getMinimumQueueSize()) {
                log.debug("WorkQueue-will-fill-up: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
                int start = startFromBeginning() ? 0 : currentEnd;
                currentEnd = start + getSplitLimitSize();
                query().limit(start, start + getSplitLimitSize()).visit(new AbstractCountingVisitor<T>() {
                    @Override
                    public void process(T t) {
                        if (currentCount % (getSplitLimitSize() / 10) == 0) {
                            log.debug("WorkQueue-PROCESS: currentCount(" + currentCount + ")");
                        }
                        currentCount++;
                        sameCount = 0;
                        getExecutor().execute(() -> doWork(t));
                    }
                });
            }

            log.debug("WorkQueue-is-full: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
            int beforeQSize = getCurrentQueueSize();


            for (int i = 0; i < 60; i++) {
                log.debug("WorkQueue-checking: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
                if(getCurrentQueueSize() < getMinimumQueueSize()){
                    break;
                }
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                }
                if(beforeQSize == getCurrentQueueSize()){
                    log.debug("WorkQueue-has-not-changed: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
                    sameCount++;
                } else {
                    sameCount = 0;
                    i = 0;
                }
            }
            if(sameCount > 110){
                log.debug("WorkQueue-has-not-changed-for-long-time: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
                log.error("Will now try to force a shutdown... ");
                break;

            }
        }

        //Waiting for 60 secs more ....
        int beforeQSize = getCurrentQueueSize();
        for (int i = 0; i < 60; i++) {
            log.debug("WorkQueue-WAITING-FOR-FINISH: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
            }
            if(beforeQSize == getCurrentQueueSize()){
                log.debug("WorkQueue-has-not-changed: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
                sameCount++;
            } else {
                log.debug("WorkQueue-HAS-changed: getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
                sameCount = 0;
                i = 0;
                beforeQSize = getCurrentQueueSize();
            }
        }


        // closing things
        try{
            log.debug("We will close: currentCount("+ currentCount +"), totalCount("+ totalCount +") currentEnd("+ currentEnd +") getCurrentQueueSize("+ getCurrentQueueSize() +"), getMinimumQueueSize("+ getMinimumQueueSize() +") sameCount: " + sameCount);
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
