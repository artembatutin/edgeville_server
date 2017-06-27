package net.edge.content.door;

import net.edge.locale.Position;
import net.edge.world.node.entity.player.Player;
import net.edge.world.object.DynamicObject;
import net.edge.world.object.ObjectDirection;
import net.edge.world.object.ObjectNode;
import net.edge.world.object.ObjectType;

/**
 * Represents a single door.
 */
public class SimpleDoor {

	private boolean closed = false;

	private boolean appended = false;
	
	private DynamicObject current;

	private final ObjectNode original;

	public SimpleDoor(ObjectNode object) {
		current = new DynamicObject(object.getId(), object.getGlobalPos(), object.getDirection(), object.getObjectType(), false, 0, 0);
		original = current.copy();
		closed = current.getDefinition().hasAction("open");
	}

	public void append(Player player) {
		current.delete();
		current.remove();
		int xAdjustment = 0;
		int yAdjustment = 0;
		ObjectDirection direction = getDirection();
		if(!appended) {
			if(getObjectType() == ObjectType.STRAIGHT_WALL) {
				if(closed) {
					if(getDirection() == ObjectDirection.WEST) {
						xAdjustment = -1;
						direction = ObjectDirection.NORTH;
					} else if(getDirection() == ObjectDirection.NORTH) {
						yAdjustment = 1;
						direction = ObjectDirection.EAST;
					} else if(getDirection() == ObjectDirection.EAST) {
						xAdjustment = 1;
						direction = ObjectDirection.SOUTH;
					} else if(getDirection() == ObjectDirection.SOUTH) {
						yAdjustment = -1;
						direction = ObjectDirection.WEST;
					} else if(original.getDirection() != getDirection()) {
						direction = original.getDirection();
					}
				} else {
					if(getDirection() == ObjectDirection.WEST) {
						yAdjustment = 1;
						direction = ObjectDirection.SOUTH;
					} else if(getDirection() == ObjectDirection.NORTH) {
						xAdjustment = 1;
						direction = ObjectDirection.WEST;
					} else if(getDirection() == ObjectDirection.EAST) {
						yAdjustment = -1;
						direction = ObjectDirection.NORTH;
					} else if(getDirection() == ObjectDirection.SOUTH) {
						xAdjustment = -1;
						direction = ObjectDirection.EAST;
					} else if(original.getDirection() != getDirection()) {
						direction = original.getDirection();
					}
				}
			} else if(getObjectType() == ObjectType.DIAGONAL_WALL) {
				if(closed) {
					if(getDirection() == ObjectDirection.WEST) {
						xAdjustment = 1;
						direction = ObjectDirection.SOUTH;
					} else if(getDirection() == ObjectDirection.NORTH) {
						xAdjustment = 1;
						direction = ObjectDirection.EAST;
					} else if(getDirection() == ObjectDirection.EAST) {
						xAdjustment = -1;
						direction = ObjectDirection.NORTH;
					} else if(getDirection() == ObjectDirection.SOUTH) {
						xAdjustment = -1;
						direction = ObjectDirection.WEST;
					} else if(original.getDirection() != getDirection()) {
						direction = original.getDirection();
					}
				} else {
					if(getDirection() == ObjectDirection.WEST) {
						xAdjustment = 1;
						direction = ObjectDirection.SOUTH;
					} else if(getDirection() == ObjectDirection.NORTH) {
						xAdjustment = 1;
						direction = ObjectDirection.WEST;
					} else if(getDirection() == ObjectDirection.EAST) {
						xAdjustment = -1;
						direction = ObjectDirection.NORTH;
					} else if(getDirection() == ObjectDirection.SOUTH) {
						xAdjustment = -1;
						direction = ObjectDirection.EAST;
					} else if(original.getDirection() != getDirection()) {
						direction = original.getDirection();
					}
				}
			}
			if(current.getGlobalPos().same(original.getGlobalPos())) {
				change(current.getGlobalPos().move(xAdjustment, yAdjustment), direction);
			} else {
				change(original.getGlobalPos(), direction);
			}
		} else {
			change(original.getGlobalPos(), original.getDirection());
		}
		closed = !closed;
		appended = !appended;
		current.restore();
		current.publish();
	}
	
	public void change(Position position, ObjectDirection direction) {
		current = current.setPosition(position).setDirection(direction).toDynamic();
	}
	
	public ObjectDirection getDirection() {
		return current.getDirection();
	}
	
	public ObjectType getObjectType() {
		return current.getObjectType();
	}
	
	public DynamicObject getCurrent() {
		return current;
	}

}
