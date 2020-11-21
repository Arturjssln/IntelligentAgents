package bestagent;

public class HonnestStrategy extends BidStrategy {

    public HonnestStrategy(){
        this.minRatio = 1.0; 
        this.maxRatio = 1.0; 
        this.riskRatio = 1.0; 
        this.opponentRatio = 0.0;
    }
}
