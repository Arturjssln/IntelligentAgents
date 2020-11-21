package bestagent;

//import logist.plan.Plan;
//import java.util.List;

public abstract class BidStrategy {

    final protected double EPSILON = 0.05; //TODO test that
    final protected double MIN_BID = 500;

    public double maxRatio; 
    public double minRatio; 
    public double riskRatio;
    public double opponentRatio;

    public Long computeBid(double ourMarginalCost, double opponentMarginalCost, double ourDistributionRatio, double opponentDistributionRatio) {
        double ourBid = riskRatio*ourMarginalCost; 
        double opponentBid = opponentRatio*opponentMarginalCost-1; 
        return (long) Math.floor(Math.max(Math.max(ourBid, opponentBid), MIN_BID));
    }

    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourReward, long opponentBid, double opponentCost, double opponentReward, double opponentMarginalCost) {}
}
