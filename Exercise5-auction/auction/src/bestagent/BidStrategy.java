package bestagent;

//import logist.plan.Plan;
//import java.util.List;

public abstract class BidStrategy {

    final protected double EPSILON = 0.05; //TODO test that
    
    public double maxRatio; 
    public double minRatio; 
    public double riskRatio;
    public double opponentRatio;

    public Long computeBid(double ourMarginalCost, double opponentMarginalCost, Long minOpponentBid) {
        return Math.round(Math.max(Math.max(riskRatio*ourMarginalCost, opponentRatio*opponentMarginalCost-1), (double) minOpponentBid-1)); // TODO check the minus
    }

    public void computeRatio(boolean winner, int round, int nbTasks, double ourCost, double ourMarginalCost, long opponentBid, double opponentMarginalCost) {}
}
