package bestagent;

import logist.plan.Plan;
import java.util.ArrayList;
import java.util.List;

public class HonnestStrategy extends BidStrategy {

    @Override
    public Long computeBid(double ourMarginalCost, double opponentMarginalCost) {
        return Math.round(ourMarginalCost);
    }


}
