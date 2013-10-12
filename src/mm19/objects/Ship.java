package mm19.objects;

import mm19.testclient.sam9000.TestClientSam9000;

import org.json.JSONException;
import org.json.JSONObject;

public class Ship {
	public enum ShipType {
		Pilot, Destroyer, Main
	};

	public int ID;
	public int health;
	public ShipType type;
	public int xCoord;
	public int yCoord;
	public String orientation;

	public Ship(int i, int h, ShipType t, int x, int y, String o) {
		ID = i;
		health = h;
		type = t;
		xCoord = x;
		yCoord = y;
		orientation = o;
	}

	public Ship(JSONObject obj) throws JSONException {
		ID = obj.getInt("ID");
		health = obj.getInt("health");

		String t = obj.getString("type");
		if (t.equals("P")) {
			type = ShipType.Pilot;
		} else if (t.equals("D")) {
			type = ShipType.Destroyer;
		} else if (t.equals("M")) {
			type = ShipType.Main;
		}

		xCoord = obj.getInt("xCoord");
		yCoord = obj.getInt("yCoord");
		orientation = obj.getString("orientation");
	}

	public void takeDamage(int damage) {
		health -= damage;
	}

	public void move(int x, int y) {
		xCoord = x;
		yCoord = y;
	}

	public ShipAction fire(int x, int y) {
		return new ShipAction(this.ID, x, y, ShipAction.Action.Fire, 0);
	}

	public boolean contains(int x, int y) {
		int size = 0;
		boolean result = false;

		switch (this.type) {
		case Main:
			size = TestClientSam9000.MAIN_LENGTH;
			break;
		case Pilot:
			size = TestClientSam9000.PILOT_LENGTH;
			break;
		case Destroyer:
			size = TestClientSam9000.DESTROYER_LENGTH;
			break;
		default:
			break;
		}
		System.out.println("Size: " + size + "\tType: " + this.type
				+ "\tOrientation: " + this.orientation);
		int head_x = this.xCoord, head_y = this.yCoord;
		System.out.println("Head X: " + head_x + "\tHead Y: " + head_y
				+ "\tx: " + x + "\ty: " + y);
		if (this.orientation.equalsIgnoreCase("H")) {
			if (x >= head_x) {
				if (x <= head_x + size) {
					if (y == head_y) {
						result = true;
					}
				}
			}
		} else {
			if (x == head_x) {
				if (y >= head_y) {
					if (y <= head_y + size) {
						result = true;
					}
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (type.equals("M")) {
			sb.append("Main ");
		} else if (type.equals("D")) {
			sb.append("Destroyer ");
		} else if (type.equals("P")) {
			sb.append("Pilot ");
		}

		sb.append("Ship " + ID + " at (" + xCoord + ", " + yCoord + ")\n");
		sb.append('\t' + "Health : " + health + '\n');
		sb.append('\t' + "Orientation: " + orientation + '\n');

		return sb.toString();

	}
}
