package bestagent;

public class MainAuction {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			logist.LogistPlatform.main(args);
		} else {
			String[] defaultArgs = { "config/auction.xml", "auction-honnest", "auction-honnest"};
			logist.LogistPlatform.main(defaultArgs);
		}
	}
}
