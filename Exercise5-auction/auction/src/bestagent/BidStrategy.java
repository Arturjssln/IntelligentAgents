package bestagent;

import logist.plan.Plan;
import java.util.List;

public abstract class BidStrategy {

    public abstract Long computeBid(double ourMarginalCost, double opponentMarginalCost); 
}
