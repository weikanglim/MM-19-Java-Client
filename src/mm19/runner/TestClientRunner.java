package mm19.runner;

import mm19.testclient.TestClient;
import mm19.testclient.TestClientException;
import mm19.testclient.alex.TestClientAlex;
import mm19.testclient.sam.TestClientSam;
import mm19.testclient.sam9000.TestClientSam9000;

public class TestClientRunner {

	public static void main(String args[]) {
		String name = "Sam The Trickster";
		if (args.length >= 1) {
			name = args[0];
		}
		TestClient tc1 = new TestClientSam9000(name);

		try {
			tc1.connect();

		} catch (TestClientException e) {
			e.printStackTrace();
		}
	}
}
