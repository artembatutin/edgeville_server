package net.arrav.content.wilderness;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.arrav.action.impl.ObjectAction;
import net.arrav.task.Task;
import net.arrav.util.rand.RandomUtils;
import net.arrav.world.World;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.region.Region;
import net.arrav.world.locale.Position;
import net.arrav.world.locale.loc.SquareLocation;
import net.arrav.world.entity.object.GameObject;

import java.util.EnumSet;

import static net.arrav.content.teleport.TeleportType.OBELISK;

/**
 * Handles the wilderness obelisks.
 */
public enum Obelisk {
	LEVEL_FIFTY(14831, new SquareLocation(3307, 3916, 0, 2)),
	LEVEL_SEVENTEEN(14830, new SquareLocation(3219, 3656, 0, 2)),
	LEVEL_THIRTEEN(14829, new SquareLocation(3156, 3620, 0, 2)),
	LEVEL_THIRTY_FIVE(14828, new SquareLocation(3106, 3794, 0, 2)),
	LEVEL_TWENTY_SEVEN(14827, new SquareLocation(3035, 3732, 0, 2)),
	LEVEL_FORTY_FOUR(14826, new SquareLocation(2980, 3866, 0, 2));
	
	/**
	 * Caches our enum values.
	 */
	private static final ImmutableSet<Obelisk> VALUES = Sets.immutableEnumSet(EnumSet.allOf(Obelisk.class));
	
	/**
	 * The obelisk object id.
	 */
	private final int object;
	
	/**
	 * The obelisk's boundary in a {@link SquareLocation}.
	 */
	private final SquareLocation boundary;
	
	Obelisk(int object, SquareLocation boundary) {
		this.object = object;
		this.boundary = boundary;
	}
	
	public int getObject() {
		return object;
	}
	
	public static void action() {
		for(Obelisk ob : VALUES) {
			ObjectAction a = new ObjectAction() {
				@Override
				public boolean click(Player player, GameObject object, int click) {
					Region reg = player.getRegion();
					if(reg != null) {
						World.get().submit(new ObeliskTask(ob, reg));
					}
					return true;
				}
			};
			a.registerFirst(ob.getObject());
		}
	}
	
	/**
	 * Handles the main obelisk.
	 */
	private static class ObeliskTask extends Task {
		
		/**
		 * The obelisk data toggled.
		 */
		private final Obelisk data;
		
		/**
		 * The {@link Region} instance in which the obelisk is located.
		 */
		private final Region reg;
		
		ObeliskTask(Obelisk data, Region reg) {
			super(8, false);
			this.data = data;
			this.reg = reg;
		}
		
		@Override
		protected void onSubmit() {
			reg.interactAction(data.object, o -> {
				o.setId(14825);
				o.publish();
			});
		}
		
		@Override
		protected void execute() {
			reg.interactAction(data.object, o -> {
				o.setId(14825);
				o.publish();
			});
			Obelisk dest = null;
			while(dest == null || dest == data) {
				dest = RandomUtils.random(VALUES.asList());
			}
			int x = dest.boundary.getSwX();
			int y = dest.boundary.getSwY();
			reg.getPlayers().forEach(p -> {
				if(data.boundary.inLocation(p.getPosition())) {
					p.teleport(new Position(x + RandomUtils.inclusive(1, 3), y + RandomUtils.inclusive(1, 3)), OBELISK);
					p.message("Ancient magic teleports you somewhere in the wilderness...");
				}
			});
			this.cancel();
		}
	}
}
