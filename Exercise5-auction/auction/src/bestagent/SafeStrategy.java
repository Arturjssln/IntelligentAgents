package bestagent;

public class SafeStrategy extends BidStrategy {

    // todo : add opponent 

    public SafeStrategy(){
        this.minRatio = 1; 
        this.maxRatio = Double.MAX_VALUE; 
        this.riskRatio = 1; 
        this.opponentRatio = 1; 
    }

    @Override
    public Long computeBid(double ourMarginalCost, double opponentMarginalCost, double ourDistributionRatio, double opponentDistributionRatio) {
        double ourBid = ourDistributionRatio*riskRatio*ourMarginalCost; 
        double opponentBid = opponentDistributionRatio*opponentRatio*opponentMarginalCost-1; 
        return Math.round(Math.max(Math.max(ourBid, opponentBid), MIN_BID));
    }

    @Override
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourMarginalCost, long opponentBid, double opponentMarginalCost) {
        /**
         * avg of the rewards 
         * utility = tout ce qu' on a gagné = reward - cost 
         * plus utility plus risque 
         * marginal < totalCost/nbTasks -> really want it => decreases the ratio
         * marginal > totalCost/nbTasks -> bofpas du tout  => increases or null
        */

        // Cost less than usual 
        if (ourMarginalCost < ourCost/nbTasks){ 
            riskRatio =  Math.max(minRatio, riskRatio * (1-EPSILON));
                          
        } 
        else {
            riskRatio = Math.min(maxRatio, riskRatio * (1+EPSILON)); 
        }

        double opponentPotentialRatio = opponentBid / opponentMarginalCost;

        if (winner) {
            double newOpponentRatio = opponentPotentialRatio * (1 - (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }
        else {
            double newOpponentRatio = opponentPotentialRatio * (1 + (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }

        
    } 

}
