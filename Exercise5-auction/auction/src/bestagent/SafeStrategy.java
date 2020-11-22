package bestagent;

public class SafeStrategy extends BidStrategy {

    public SafeStrategy(double epsilon) {
        super(1.0, Double.MAX_VALUE, 1.25, 1.2, epsilon);
        
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
        // Compute utility values
        double ourUtility = ourReward - ourCost;
        double opponentUtility = opponentReward - opponentCost; 

        if (ourUtility > opponentUtility && winner) {
            // Increase risk ratio
            riskRatio =  Math.max(minRatio, riskRatio * (1+epsilon));
        } 
        else if (ourUtility < opponentUtility && !winner) {
            // Decrease risk ratio
            riskRatio = Math.min(maxRatio, riskRatio * (1-epsilon)); 
        }

        double opponentPotentialRatio = opponentBid / opponentMarginalCost;
        if (winner) {
            // Decrease opponent risk ration
            double newOpponentRatio = opponentPotentialRatio * (1 - (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }
        else {
            // Increase opponent risk ration
            double newOpponentRatio = opponentPotentialRatio * (1 + (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }

        
    } 

}
