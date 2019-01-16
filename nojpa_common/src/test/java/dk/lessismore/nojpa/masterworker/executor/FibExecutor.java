package dk.lessismore.nojpa.masterworker.executor;

public class FibExecutor  extends Executor<FibInData, FibOutData> {

    int counter = 0;

    @Override
    public FibOutData run(FibInData input) {
        FibOutData out = new FibOutData();
        out.setText(input.getText() + "::" + input.getText());
        return out;

    }


}
