package dk.lessismore.nojpa.masterworker.worker;

import dk.lessismore.nojpa.masterworker.client.JobHandle;
import dk.lessismore.nojpa.masterworker.client.MasterService;
import dk.lessismore.nojpa.masterworker.executor.Executor;
import dk.lessismore.nojpa.masterworker.messages.observer.UpdateMessage;

/**
 * Created with IntelliJ IDEA.
 * User: seb
 */
public class FunWorker extends Executor<FunWorker.SomeData, FunWorker.ResultData>{




    public static void main(String[] args) {


        UpdateMessage status = MasterService.getStatus();
        System.out.println("status.getObserverWorkerMessages().size() = " + status.getObserverWorkerMessages().size());

        SomeData d = new SomeData();
        d.setData("Hello my friend");
        d.setTitle("Dotti rocks!!!");
        System.out.println("Making handle ....");
        JobHandle<ResultData> jobHandle = MasterService.runJob(FunWorker.class, d);
        ResultData result = jobHandle.getResult();
        System.out.println("result = " + result);


//        SomeData d = new SomeData();
//        d.setData("Hello my friend");
//        d.setTitle("Dotti rocks!!!");
//        System.out.println("Making handle ....");
//        JobHandle<ResultData> jobHandle = MasterService.runJob(FunWorker.class, d);
//        jobHandle.addJobListener(new JobListener<ResultData>() {
//            @Override
//            public void onStatus(JobStatus status) {
//                System.out.println("&&&&&&& JobListener.onStatus("+ status +")");
//            }
//
//            @Override
//            public void onProgress(double progress) {
//                System.out.println("&&&&&&& JobListener.onProgress("+ progress +")");
//            }
//
//            @Override
//            public void onResult(ResultData result) {
//                System.out.println("&&&&&&& JobListener.onResult("+ result +")");
//            }
//
//            @Override
//            public void onRunMethodRemoteResult(RunMethodRemoteResultMessage runMethodRemoteResultMessage) {
//                System.out.println("&&&&&&& JobListener.onRunMethodRemoteResult("+ runMethodRemoteResultMessage +")");
//
//            }
//
//            @Override
//            public void onException(RuntimeException e) {
//                System.out.println("&&&&&&& JobListener.onException("+ e +")");
//
//            }
//        });
//        System.out.println("Calling jobHandle.getResult() :-) ");
//        while(jobHandle.getStatus() != JobStatus.DONE){
//            System.out.println("Waiting for result ... " + jobHandle.getStatus());
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("We are now done ... printing result");
//        ResultData result = jobHandle.getResult();
//        System.out.println("result.getNeg() = " + result.getNeg());
//


    }





    public static class ResultData {
        double neg = 0d;
        double pos = 0d;
        double neu = 0d;


        public double getNeg() {
            return neg;
        }

        public void setNeg(double neg) {
            this.neg = neg;
        }

        public double getPos() {
            return pos;
        }

        public void setPos(double pos) {
            this.pos = pos;
        }

        public double getNeu() {
            return neu;
        }

        public void setNeu(double neu) {
            this.neu = neu;
        }

        @Override
        public String toString() {
            return "pos("+ pos +")";
        }
    }
    public static class SomeData {

        String title;
        String url;
        String id;
        String data;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }





        @Override
        public ResultData run(SomeData input) {
            System.out.println("************************************************ start ****");
            setProgress(0.1d); try { Thread.sleep(100); } catch (InterruptedException e) {  e.printStackTrace();    }
            System.out.println("Hello JOB : " + input.getData());
            System.out.println("**** Working hard :-) " + getProgress());
            setProgress(0.2d); try { Thread.sleep(100); } catch (InterruptedException e) {  e.printStackTrace();    }
            ResultData rd = new ResultData();
            System.out.println("**** Working hard :-) " + getProgress());
            setProgress(0.3d); try { Thread.sleep(100); } catch (InterruptedException e) {  e.printStackTrace();    }
            rd.setNeg(Math.random());
            rd.setNeu(Math.random());
            System.out.println("**** Working hard :-) " + getProgress());
            setProgress(0.5d); try { Thread.sleep(100); } catch (InterruptedException e) {  e.printStackTrace();    }
            rd.setPos(Math.random());
            System.out.println("**** Working hard :-) " + getProgress());
            setProgress(0.9d); try { Thread.sleep(100); } catch (InterruptedException e) {  e.printStackTrace();    }
            System.out.println("**** Working hard :-) " + getProgress());
            System.out.println("************************************************ end ****");

            return rd;
        }


}
