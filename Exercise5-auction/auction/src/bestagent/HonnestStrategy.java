package bestagent;

public class HonnestStrategy extends BidStrategy {

    public HonnestStrategy(double epsilon) {
        super(1.0, 1.0, 1.0, 0.0, epsilon);
    }
}
