package bestagent;

public class RiskyStrategy extends BidStrategy {

    public RiskyStrategy(){
        this.minRatio = 0.25; 
        this.maxRatio = 0.75; 
        this.riskRatio = 0.5;
        this.opponentRatio = 0.0;
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
