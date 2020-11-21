package bestagent;

public class RiskyStrategy extends BidStrategy {

    public RiskyStrategy(){
        this.minRatio = 0.5; 
        this.maxRatio = 1; 
        this.riskRatio = 0.75;
        this.opponentRatio = 0.0;
    }

    @Override
    public Long computeBid(double ourMarginalCost, double opponentMarginalCost, double ourDistributionRatio, double opponentDistributionRatio) {
        double ourBid = ourDistributionRatio*riskRatio*ourMarginalCost; 
        double opponentBid = opponentDistributionRatio*opponentRatio*opponentMarginalCost-1; 
        return Math.round(Math.max(Math.max(ourBid, opponentBid), MIN_BID));
    }

    @Override
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourMarginalCost, long opponentBid, double opponentMarginalCost) {
        if (nbTasks > round/2) {
            // nice : increases
            riskRatio = Math.min(maxRatio, riskRatio * (1 + EPSILON * (1.0 - ((double)nbTasks)/round)));
        } else { 
            // aggressive : decreases
            riskRatio = Math.max(minRatio, riskRatio * (1 - EPSILON * (1.0 - ((double)nbTasks)/round)));
        }
    } 


}
