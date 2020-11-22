package bestagent;

public class CatchingUpStrategy extends BidStrategy {

    public CatchingUpStrategy(double epsilon) {
        super(1.0, Double.MAX_VALUE, 1.15, 1.1, epsilon);
        
    }

    // Compute using marginal costs, risk ratios and distribution ratios
    @Override
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourReward, long opponentBid, double opponentCost, double opponentReward, double opponentMarginalCost) {
        double opponentPotentialRatio = opponentBid / opponentMarginalCost;
        if (winner) {
            // increase risk ratio
            riskRatio = Math.min(maxRatio, riskRatio * (1+epsilon)); 
            // descrease opponent risk ratio
            double newOpponentRatio = opponentRatio * (1 - (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }
        else {
            // descrease risk ratio
            riskRatio =  Math.max(minRatio, riskRatio * (1-epsilon)); 
            // increase opponent risk ratio
            double newOpponentRatio = opponentRatio * (1 + (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }
    }
}
