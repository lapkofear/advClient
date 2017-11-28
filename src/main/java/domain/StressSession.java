package domain;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static org.springframework.http.HttpStatus.OK;

public class StressSession {
	private static final Logger LOG = Logger.getLogger(StressSession.class.getName());
	private static final String ADV_PROVIDER_URL = "http://localhost:8080/advertisement?timeout=";

	private static RestTemplate restTemplate = new RestTemplate();

	private AtomicLong successfulRequestsCount = new AtomicLong(0);
	private ExecutorService executors;
	private CountDownLatch doneSignal;
	private long startTime;
	private Integer threadsCount;
	private Integer requestsToSend;
	private String singleRequestTimeout;


	public StressSession(Integer requestsToSend, Integer threadsCount, String singleRequestTimeout) {
		this.threadsCount = threadsCount;
		this.requestsToSend = requestsToSend;
		this.singleRequestTimeout = singleRequestTimeout;

		this.executors = Executors.newFixedThreadPool(threadsCount);
		this.doneSignal = new CountDownLatch(requestsToSend);
	}

	public void start(long timeoutInSeconds) {
		try {
			startTime = System.currentTimeMillis();

			startTasks();

			doneSignal.await(timeoutInSeconds, TimeUnit.SECONDS);

			printResults();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void startTasks() {
		for (int i = 0; i < threadsCount; i++) {
			executors.submit(() -> {
				while (true) {
					ResponseEntity<String> entity = restTemplate.getForEntity(ADV_PROVIDER_URL + singleRequestTimeout, String.class);
					if (entity.getStatusCode() == OK && entity.getBody() != null) {
						successfulRequestsCount.incrementAndGet();
					}
					doneSignal.countDown();
				}
			});
		}
	}

	private void printResults() {
		LOG.info("Done in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
		LOG.info("requests remains          :  " + doneSignal.getCount());
		LOG.info("successfulRequestsCount   :  " + successfulRequestsCount.get() + ",  " + getPercentage());
	}

	private String getPercentage() {
		if (successfulRequestsCount.get() == 0) {
			return "0%";
		}
		return successfulRequestsCount.get() / (double)requestsToSend * 100 + "%";
	}
}
