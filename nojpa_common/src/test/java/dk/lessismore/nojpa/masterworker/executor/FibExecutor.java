package dk.lessismore.nojpa.masterworker.executor;

public class FibExecutor  extends Executor<FibInData, FibOutData> {

    int counter = 0;

    @Override
    public FibOutData run(FibInData input) {
        try {
            Thread.sleep(15 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FibOutData out = new FibOutData();
        out.setText(input.getText() + "::" + input.getText());
        return out;

    }


}
