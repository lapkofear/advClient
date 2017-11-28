import domain.StressSession;

public class Main {


	public static void main(String[] args) throws InterruptedException {
		if (args.length < 3) {
			throw new IllegalArgumentException("Wrong parameters count. Please, specify requests count, threads count and timeout.");
		}

		new StressSession(Integer.valueOf(args[0]), Integer.valueOf(args[1]), args[2]).start(300);
	}
}
