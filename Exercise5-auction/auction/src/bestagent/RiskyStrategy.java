package bestagent;

public class RiskyStrategy extends BidStrategy {

    public RiskyStrategy(double epsilon) {
        super(0.5, 1.0, 0.75, 0.0, epsilon);    
    }

    // Compute using marginal costs, risk ratios and distribution ratios
    @Override
    public Long computeBid(double ourMarginalCost, double opponentMarginalCost, double ourDistributionRatio, double opponentDistributionRatio) {
        double ourBid = ourDistributionRatio*riskRatio*ourMarginalCost; 
        double opponentBid = opponentDistributionRatio*opponentRatio*opponentMarginalCost-1; 
        return Math.round(Math.max(Math.max(ourBid, opponentBid), MIN_BID));
    }

    @Override
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourReward, long opponentBid, double opponentCost, double opponentReward, double opponentMarginalCost) {
        if (nbTasks > round/2) {
            // less aggressive : increases
            riskRatio = Math.min(maxRatio, riskRatio * (1 + epsilon * (1.0 - ((double)nbTasks)/round)));
        } else { 
            // aggressive : decreases
            riskRatio = Math.max(minRatio, riskRatio * (1 - epsilon * (1.0 - ((double)nbTasks)/round)));
        }
    } 


}
