package bestagent;

public class CatchingUpStrategy extends BidStrategy {

    public CatchingUpStrategy(){
        this.minRatio = 1; 
        this.maxRatio = Double.MAX_VALUE; 
        this.riskRatio = 1.15; 
        this.opponentRatio = 1.1; 
    }

    @Override
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourReward, long opponentBid, double opponentCost, double opponentReward, double opponentMarginalCost) {
        double opponentPotentialRatio = opponentBid / opponentMarginalCost;
        if (winner) {
            riskRatio = Math.min(maxRatio, riskRatio * (1+EPSILON)); 
            double newOpponentRatio = opponentPotentialRatio * (1 - (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }
        else {
            riskRatio =  Math.max(minRatio, riskRatio * (1-EPSILON)); 
            double newOpponentRatio = opponentPotentialRatio * (1 + (opponentPotentialRatio - opponentRatio)*0.25);
            opponentRatio = Math.max(Math.min(maxRatio, newOpponentRatio), minRatio);
        }
    }
}
