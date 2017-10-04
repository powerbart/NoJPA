package dk.lessismore.nojpa.flow;

import dk.lessismore.nojpa.reflection.db.model.ModelObjectInterface;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public abstract class FlowVisitor<T extends ModelObjectInterface> {


    private final ThreadPoolTaskExecutor executor;
    private final FlowService<T> flowService;


    public FlowVisitor(FlowService<T> flowService){
        this.flowService = flowService;
        executor = new ThreadPoolTaskExecutor();
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix(getThreadNamePrefix());
        executor.setCorePoolSize(getNumberOfThreads());
        executor.setMaxPoolSize(getNumberOfThreads());
        executor.setQueueCapacity(Integer.MAX_VALUE);

    }

    protected abstract int getNumberOfThreads();

    protected abstract String getThreadNamePrefix();

    public void runFlow(){
        int totalCount = flowService.getCount();
        for(){

        }


    }

    public interface FlowService<T> {

        int getCount();
    }


}
