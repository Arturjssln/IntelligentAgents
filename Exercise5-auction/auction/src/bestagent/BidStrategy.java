package bestagent;

//import logist.plan.Plan;
//import java.util.List;

public abstract class BidStrategy {

    final protected double MIN_BID = 600;

    public double maxRatio;     // Maximum value for risk ratio
    public double minRatio;     // Minimum value for risk ratio 
    public double riskRatio;    // Value of risk ratio
    public double opponentRatio;// Estimation of opponentRatio
    protected double epsilon;   // Value to uptade opponent ratio

    // Constructor
    BidStrategy(double minRatio, double maxRatio, double riskRatio, double opponentRatio, double epsilon) {
        this.minRatio = minRatio; 
        this.maxRatio = maxRatio; 
        this.riskRatio = riskRatio; 
        this.opponentRatio = opponentRatio;
        this.epsilon = epsilon;
    }

    // Compute bid using only marginal costs and risk ratios
    public Long computeBid(double ourMarginalCost, double opponentMarginalCost, double ourDistributionRatio, double opponentDistributionRatio) {
        double ourBid = riskRatio*ourMarginalCost; 
        double opponentBid = opponentRatio*opponentMarginalCost-1; 
        return (long) Math.floor(Math.max(Math.max(ourBid, opponentBid), MIN_BID));
    }

    // Update risk ratios
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourReward, long opponentBid, double opponentCost, double opponentReward, double opponentMarginalCost) {}
}
