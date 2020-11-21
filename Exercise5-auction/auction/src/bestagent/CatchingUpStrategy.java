package bestagent;

public class CatchingUpStrategy extends BidStrategy {

    public CatchingUpStrategy(){
        this.minRatio = 1; 
        this.maxRatio = Double.MAX_VALUE; 
        this.riskRatio = 1; 
        this.opponentRatio = 1; 
    }

    @Override
    public void computeRiskRatio(boolean winner, int round, int nbTasks, double ourCost, double ourMarginalCost, long opponentBid, double opponentMarginalCost) {
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

        /**
         * 1st marginal cost ? 
         * 2nd marginal cost opponent => risk ratio opponent ? 
         * keep the maximal : useful if estimated that higher 
         * 
         * bid/marginalCost = opponentRiskRatio  +- le meme EPSILON que nous 
         *  */ 



}
