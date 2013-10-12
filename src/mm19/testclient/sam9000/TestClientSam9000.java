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

	public int DESTROYERS = 8;
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

	int i_iter = 1;
	int j_iter = 0;
	
	int backward_i_iter = 1;
	int backward_j_iter = 0;

	int oldResources;
	int newResources;
	int netIncome;
	int resources;
	int pingX;
	int pingY;
	int turns;
	boolean pingHit;
	String playerToken;

	private ArrayList<Tuple<Integer, Integer>> hitList = new ArrayList<TestClientSam9000.Tuple<Integer, Integer>>();
	private ArrayList<Ship> moveList = new ArrayList<Ship>();

	private PingReport[] pingReport = new PingReport[0];

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
			oldResources = 0;

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
		moveList.clear();

		// Hit report detection
		for (HitReport hr : sr.hitReport) {
			if (hr.hit) {
				hitList.add(new Tuple<Integer, Integer>(hr.xCoord, hr.yCoord));
			}
		}

		System.out.println("PING REPORT SIZE = " + sr.pingReport.length);
		this.pingReport = sr.pingReport;

	}

	/**
	 * Preparing my incredible offense
	 */
	@Override
	public JSONObject prepareTurn(ServerResponse sr) {
		turns++;

		Random rand = new Random();
		ArrayList<Ship> availableShips = new ArrayList<Ship>(
				Arrays.asList(sr.ships));

		// Ping report detection
		System.out.println("PING LIST SIZE = " + this.pingReport.length);
		for (PingReport pr : sr.pingReport) {
			System.out.println("SHIP LIST SIZE = " + sr.ships.length);
			for (Ship ship : sr.ships) {
				System.out.println(ship.ID + " == " + pr.shipID);
				if (ship.ID == pr.shipID) {
					System.out.println("Ship added to move list");
					moveList.add(ship);
				}
			}
		}

		JSONObject turnObj = new JSONObject();
		try {
			Collection<JSONObject> actions = new ArrayList<JSONObject>();
			ShipAction tempAction = null;
			boolean specialUsed = false;

			// Check hit for MAIN and move
			for (HitReport hr : sr.hitReport) {
				if (hr.hit) {
					for (Ship _ship : sr.ships) {
						oldResources = sr.resources;
						if (_ship.type == ShipType.Main
								&& _ship.contains(hr.xCoord, hr.yCoord)) {
							System.out.println("X:" + hr.xCoord + "\tY:"
									+ hr.yCoord);
							if (rand.nextInt(2) == 1) {
								Tuple<Integer, Integer> dest = findEmptySpot(
										MAIN_LENGTH, "H", sr.ships);
								tempAction = new ShipAction(_ship.ID, dest.x,
										dest.y, ShipAction.Action.MoveH, 0);
							} else {
								Tuple<Integer, Integer> dest = findEmptySpot(
										MAIN_LENGTH, "V", sr.ships);
								tempAction = new ShipAction(_ship.ID, dest.x,
										dest.y, ShipAction.Action.MoveV, 0);
							}
						}
						if (tempAction != null) {
							specialUsed = true;
							actions.add(tempAction.toJSONObject());
							break;
						}
					}
					if (specialUsed) {
						break;
					}
				}
			}

			// Check hit for Ships and move
			if (!specialUsed) {
				for (HitReport hr : sr.hitReport) {
					if (hr.hit) {
						for (Ship _ship : sr.ships) {
							if (_ship.contains(hr.xCoord, hr.yCoord)) {
								if (rand.nextInt(2) == 1) {
									Tuple<Integer, Integer> dest = findEmptySpot(
											MAIN_LENGTH, "H", sr.ships);
									tempAction = new ShipAction(_ship.ID,
											dest.x, dest.y,
											ShipAction.Action.MoveH, 0);
								} else {
									Tuple<Integer, Integer> dest = findEmptySpot(
											MAIN_LENGTH, "V", sr.ships);
									tempAction = new ShipAction(_ship.ID,
											dest.x, dest.y,
											ShipAction.Action.MoveV, 0);
								}
							}
							if (tempAction != null) {
								specialUsed = true;
								actions.add(tempAction.toJSONObject());
								break;
							}
						}
						if (specialUsed) {
							break;
						}
					}
				}
			}

			// Move ping'd units
			if (!specialUsed) {
				for (Ship _ship : moveList) {
					System.out.println("In movelist!");
					if (rand.nextInt(2) == 1) {
						System.out.println("Moving V!");
						Tuple<Integer, Integer> dest = findEmptySpot(
								MAIN_LENGTH, "H", sr.ships);
						tempAction = new ShipAction(_ship.ID, dest.x, dest.y,
								ShipAction.Action.MoveH, 0);
					} else {
						System.out.println("Moving H!");
						Tuple<Integer, Integer> dest = findEmptySpot(
								MAIN_LENGTH, "V", sr.ships);
						tempAction = new ShipAction(_ship.ID, dest.x, dest.y,
								ShipAction.Action.MoveV, 0);
					}
					if (tempAction != null) {
						actions.add(tempAction.toJSONObject());
						specialUsed = true;
						System.out.println("Special used on evading ping!");
						break;
					}
				}
			}

			// Rehitting enemy ships
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

			// Burst shotting
			if (!(availableShips.size() == 0) && !specialUsed
					&& oldResources > 9000) {
				int x_burst = backward_i_iter - 1;
				int y_burst = backward_j_iter - 1;
				
				backward_i_iter -= 4;

				if (backward_i_iter < 0) {
					backward_j_iter -= 4;
					backward_i_iter = TestClient.BOARD_WIDTH;
					
					if (backward_j_iter < 0) {
						backward_i_iter = TestClient.BOARD_WIDTH;
						backward_j_iter = TestClient.BOARD_WIDTH;
					}
				}
				
				ship = getNextDestroyer(availableShips);
				tempAction = new ShipAction(ship.ID, x_burst, y_burst,
						ShipAction.Action.BurstShot, 0);
				actions.add(tempAction.toJSONObject());
				specialUsed = true;
			}

			boolean Shot = false;
			do {
				int xCoord = i_iter;
				int yCoord = j_iter;
				Shot = shoot(availableShips, xCoord, yCoord, actions); // Removes
				// available
				// attacking
				// ships

				if (Shot) {
					// Horizontal iteration
					i_iter += 2;

					if (i_iter > TestClient.BOARD_WIDTH) {
						j_iter++;
						i_iter = (j_iter + 1) % 2;
						if (j_iter > TestClient.BOARD_WIDTH) {
							i_iter = 1;
							j_iter = 0;
						}
					}
				}

			} while (Shot);

			// // Sonar function
			// if (!specialUsed && oldResources > 10000) { // Resources?
			// int xCoord = i_iter;
			// int yCoord = j_iter;
			//
			// i_iter += 2;
			//
			// if (i_iter > TestClient.BOARD_WIDTH) {
			// j_iter++;
			// i_iter = (j_iter + 1) % 2;
			// if (j_iter > TestClient.BOARD_WIDTH) {
			// i_iter = 1;
			// j_iter = 0;
			// }
			// }
			//
			// for (Ship free_ship : availableShips) {
			// if (free_ship.type == ShipType.Pilot) {
			// tempAction = new ShipAction(free_ship.ID, xCoord,
			// yCoord, ShipAction.Action.Sonar, 0);
			// pingX = xCoord;
			// pingY = yCoord;
			//
			// if (tempAction != null) {
			// actions.add(tempAction.toJSONObject());
			// specialUsed = true;
			// break;
			// }
			// }
			// }
			// }

			turnObj.put("playerToken", playerToken);
			turnObj.put("shipActions", actions);

			if (turns % 2 == 1) {
				newResources = sr.resources;
				netIncome = newResources - oldResources;
			} else {
				oldResources = sr.resources;
			}
			System.err.println("Resources is " + newResources);
			System.err.println("Net Income is " + netIncome);
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

	/**
	 * Finds an attacking ship and remove sit from AvailableShips.
	 * 
	 * @param ships
	 * @param x
	 * @param y
	 * @param actions
	 * @return
	 */
	private boolean shoot(ArrayList<Ship> ships, int x, int y,
			Collection<JSONObject> actions) {
		Ship ship;
		boolean fired = false;
		for (int i = 0; i < 2; i++) {
			ship = getNextDestroyer(ships);
			if (ship != null) {
				fired = true;
				ShipAction action = ship.fire(x, y);
				if (action != null) {
					actions.add(action.toJSONObject());
				}
			} else {
				break;
			}
		}
		return fired;
	}

	private Ship getNextDestroyer(ArrayList<Ship> ships) {
		Ship ship = null;
		for (int i = 0; i < ships.size(); i++) {
			if (ships.get(i).type == ShipType.Destroyer
					|| ships.get(i).type == ShipType.Main) {
				ship = ships.get(i);
				ships.remove(i);
				break;
			}
		}
		return ship;
	}

	private Tuple<Integer, Integer> findEmptySpot(int length, String ori,
			Ship[] ships) {
		Random rand = new Random();
		Tuple<Integer, Integer> dest = null;
		int x, y;
		boolean shipFound; // return new Tuple<Integer, Integer> (0,0);

		do {
			shipFound = false;
			x = rand.nextInt(99 - MAIN_LENGTH);
			y = rand.nextInt(99 - MAIN_LENGTH);

			if (ori == "H") {
				for (int i = 0; i < length; i++) {
					boolean conflict = false;
					for (Ship ship : ships) {
						if (ship.contains(x + i, y)
								|| x + length > TestClient.BOARD_WIDTH) {
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
						if (ship.contains(x, y + i)
								|| y + length > TestClient.BOARD_WIDTH) {
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
