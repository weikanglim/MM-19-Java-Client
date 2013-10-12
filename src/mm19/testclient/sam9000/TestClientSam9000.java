package mm19.testclient.sam9000;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import mm19.objects.HitReport;
import mm19.objects.PingReport;
import mm19.objects.Ship;
import mm19.objects.Ship.ShipType;
import mm19.objects.ShipAction;
import mm19.response.ServerResponse;
import mm19.testclient.TestClient;

import org.json.JSONException;
import org.json.JSONObject;

import sun.security.x509.AVA;

/**
 * 
 */
public class TestClientSam9000 extends TestClient {

	public int DESTROYERS = 9;
	public int PILOTS = 18 - DESTROYERS;

	public int COLUMNS = 4;
	public int ROWS = 5;
	public int COLUMN_WIDTH = TestClient.BOARD_WIDTH / COLUMNS;
	public int ROW_HEIGHT = TestClient.BOARD_WIDTH / ROWS;

	public static final int DESTROYER_LENGTH = 4;
	public static final int PILOT_LENGTH = 2;
	public static final int MAIN_LENGTH = 5;

	public static final int FIRE = 50;
	public static final int MOVE_PILOT = 100;
	public static final int MOVE_DEST = 250;
	public static final int MOVE_MAIN = 300;
	public static final int MINIMUN_RES = MOVE_MAIN;
	public static final int PING = 110;
	public static final int BURST = 250;

	int resources;
	int pingX;
	int pingY;
	boolean pingHit;
	String playerToken;

	private ArrayList<Tuple<Integer, Integer>> hitList = new ArrayList<TestClientSam9000.Tuple<Integer, Integer>>();

	public TestClientSam9000(String name) {
		super(name);
	}

	/**
	 * Making my ships impossibly hard to find.
	 */
	@Override
	public JSONObject setup() {
		JSONObject obj = new JSONObject();
		Random rand = new Random();
		try {
			obj.put("playerName", this.name);

			boolean main = false;
			int destroyer = DESTROYERS, pilots = PILOTS;
			Collection<JSONObject> ships = new ArrayList<JSONObject>();
			for (int i = 0; i < ROWS; i++) {
				for (int j = 0; j < COLUMNS; j++) {

					if (pilots <= 0 && destroyer <= 0) {
						if (!main) {
							JSONObject mainShip = new JSONObject();
							int xCoord = (int) ((j * COLUMN_WIDTH)
									+ rand.nextInt((TestClient.BOARD_WIDTH / COLUMNS)) - MAIN_LENGTH);
							int yCoord = (int) ((i * ROW_HEIGHT) + rand
									.nextInt((TestClient.BOARD_WIDTH / ROWS)
											- MAIN_LENGTH));
							mainShip.put("xCoord", xCoord);
							mainShip.put("yCoord", yCoord);
							mainShip.put("orientation", "H");
							obj.put("mainShip", mainShip);
							main = true;

							System.out.println("X:" + xCoord + "\tY:" + yCoord
									+ "\tO:H");
						}
						break;
					}

					JSONObject ship = new JSONObject();

					if (i % 2 == 0) {
						ship.put("orientation", "H");
					} else {
						ship.put("orientation", "V");
					}

					boolean empty = false;
					if (rand.nextInt() % 3 == 0) {
						if (destroyer <= 0) {
							if (pilots <= 0) {
								empty = true;
							} else {
								ship.put("type", "P");
								pilots--;
							}
						} else {
							ship.put("type", "D");
							destroyer--;
						}
					} else {
						if (pilots <= 0) {
							if (destroyer <= 0) {
								if (empty) {
									break;
								}
							} else {
								ship.put("type", "D");
								destroyer--;
							}
						} else {
							ship.put("type", "P");
							pilots--;
						}
					}

					int xCoord = (int) ((j * COLUMN_WIDTH) + rand
							.nextInt((TestClient.BOARD_WIDTH / COLUMNS)));
					int yCoord = (int) ((i * ROW_HEIGHT) + rand
							.nextInt((TestClient.BOARD_WIDTH / ROWS)));
					if (xCoord > (COLUMN_WIDTH - MAIN_LENGTH)) {
						xCoord -= MAIN_LENGTH;
					}
					if (yCoord > (ROW_HEIGHT - MAIN_LENGTH)) {
						yCoord -= MAIN_LENGTH;
					}
					ship.put("xCoord", xCoord);
					ship.put("yCoord", yCoord);
					ships.add(ship);

					String ori = "";
					if (i % 2 == 0) {
						ori = "H";
					} else {
						ori = "V";
					}

					System.out.println("X:" + xCoord + "\tY:" + yCoord + "\tO:"
							+ ori);

				}
			}

			obj.put("ships", ships);
		} catch (JSONException e) {

		}

		return obj;
	}

	/**
	 * Process my response in O(n^2) like a boss.
	 */
	@Override
	public void processResponse(ServerResponse sr) {

		hitList.clear();
		for (HitReport hr : sr.hitReport) {
			if (hr.hit) {
				hitList.add(new Tuple<Integer, Integer>(hr.xCoord, hr.yCoord));
			}
		}
	}

	/**
	 * Preparing my incredible offense
	 */
	@Override
	public JSONObject prepareTurn(ServerResponse sr) {

		ArrayList<Ship> availableShips = new ArrayList<Ship>(
				Arrays.asList(sr.ships));

		JSONObject turnObj = new JSONObject();
		try {
			Collection<JSONObject> actions = new ArrayList<JSONObject>();
			ShipAction tempAction = null;
			boolean moved = false;
			// Chech hit for MAIN
			for (HitReport hr : sr.hitReport) {
				if (hr.hit) {
					for (Ship _ship : sr.ships) {

						if (_ship.type == ShipType.Main
								&& _ship.contains(hr.xCoord, hr.yCoord)) {
							System.out.println("X:" + hr.xCoord + "\tY:"
									+ hr.yCoord);
							Tuple<Integer, Integer> dest = findEmptySpot(
									MAIN_LENGTH, _ship.orientation, sr.ships);
							tempAction = new ShipAction(_ship.ID, dest.x,
									dest.y, ShipAction.Action.MoveH, 0);
							System.out.println("TO= X:" + dest.x + "\tY:"
									+ dest.y);
						}
						if (tempAction != null) {
							moved = true;
							actions.add(tempAction.toJSONObject());
							break;
						}
					}
					if (moved) {
						break;
					}
				}
			}

			// Chech hit for Ships
			if (!moved) {
				for (HitReport hr : sr.hitReport) {
					if (hr.hit) {
						for (Ship _ship : sr.ships) {
							if (_ship.contains(hr.xCoord, hr.yCoord)) {
								Tuple<Integer, Integer> dest = findEmptySpot(
										MAIN_LENGTH, _ship.orientation,
										sr.ships);
								tempAction = new ShipAction(_ship.ID, dest.x,
										dest.y, ShipAction.Action.MoveH, 0);
							}
							if (tempAction != null) {
								actions.add(tempAction.toJSONObject());
								break;
							}
						}
						if (moved) {
							break;
						}
					}
				}
			}

			Ship ship;
			for (Tuple<Integer, Integer> hit : hitList) {
				ship = getNextDestroyer(availableShips);
				if (ship != null) {
					tempAction = ship.fire(hit.x, hit.y);
					actions.add(tempAction.toJSONObject());
				}
				if (availableShips.size() == 0) {
					break;
				}
			}

			for (Ship free_ship : availableShips) {
				tempAction = null;
				int xCoord = (int) (Math.random() * 99);
				int yCoord = (int) (Math.random() * 99);

				if (free_ship.type == ShipType.Destroyer) {
					tempAction = new ShipAction(free_ship.ID, xCoord, yCoord,
							ShipAction.Action.Fire, 0);
				}

				if (tempAction != null) {
					actions.add(tempAction.toJSONObject());
				}
			}

			/*
			 * for (Ship hit_ship : sr.ships) { int xCoord = (int)
			 * (Math.random() * 99); int yCoord = (int) (Math.random() * 99);
			 * 
			 * if (hit_ship.type == ShipType.Destroyer) { tempAction = new
			 * ShipAction(hit_ship.ID, xCoord, yCoord, ShipAction.Action.Fire,
			 * 0); }
			 * 
			 * if (tempAction != null) { actions.add(tempAction.toJSONObject());
			 * } }
			 */

			turnObj.put("playerToken", playerToken);
			turnObj.put("shipActions", actions);
			return turnObj;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return turnObj;
	}

	/**
	 * wat
	 */
	@Override
	public void handleInterrupt(ServerResponse sr) {
		System.out.println("wat");
	}

	private void shoot(ArrayList<Ship> ships, int x, int y) {
		Ship ship;
		for (int i = 0; i < 2; i++) {
			ship = getNextDestroyer(ships);
			if (ship != null) {
				ship = ships.get(0);
				ship.fire(x, y);
				ships.remove(0);
			}
		}
	}

	private Ship getNextDestroyer(ArrayList<Ship> ships) {
		Ship ship = null;
		for (int i = 0; i < ships.size(); i++) {
			if (ships.get(i).type == ShipType.Destroyer) {
				ship = ships.get(i);
				ships.remove(i);
			}
		}
		return ship;
	}

	private Tuple<Integer, Integer> findEmptySpot(int length, String ori,
			Ship[] ships) {
		Random rand = new Random();
		Tuple<Integer, Integer> dest = null;
		int x, y;
		boolean shipFound;
		do {
			shipFound = false;
			x = rand.nextInt(99 - MAIN_LENGTH);
			y = rand.nextInt(99 - MAIN_LENGTH);

			if (ori == "H") {
				for (int i = 0; i < length; i++) {
					boolean conflict = false;
					for (Ship ship : ships) {
						if (ship.contains(x + i, y)) {
							conflict = true;
							break;
						}
					}
					if (conflict) {
						shipFound = true;
						break;
					}
				}
			} else {
				for (int i = 0; i < length; i++) {
					boolean conflict = false;
					for (Ship ship : ships) {
						if (ship.contains(x, y + i)) {
							conflict = true;
							break;
						}
					}
					if (conflict) {
						shipFound = true;
						break;
					}
				}
			}
			dest = new Tuple<Integer, Integer>(x, y);
		} while (shipFound);
		return dest;
		// return new Tuple<Integer, Integer> (0,0);
	}

	public class Tuple<X, Y> {
		public final X x;
		public final Y y;

		public Tuple(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
}
