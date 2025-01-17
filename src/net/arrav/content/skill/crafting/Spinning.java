package net.arrav.content.skill.crafting;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.arrav.action.impl.ItemOnObjectAction;
import net.arrav.content.skill.SkillData;
import net.arrav.content.skill.action.impl.ProducingSkillAction;
import net.arrav.net.packet.out.SendEnterAmount;
import net.arrav.net.packet.out.SendItemModelInterface;
import net.arrav.task.Task;
import net.arrav.util.TextUtils;
import net.arrav.world.Animation;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.item.Item;
import net.arrav.world.entity.object.GameObject;

import java.util.Optional;

/**
 * Holds functionality for spinning items.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class Spinning extends ProducingSkillAction {
	
	/**
	 * The data this skill action is dependent of.
	 */
	private final SpinningData data;
	
	/**
	 * The amount of times this task should run for.
	 */
	private int amount;
	
	/**
	 * Constructs a new {@link Spinning}.
	 * @param player {@link #getPlayer()}.
	 * @param data {@link #data}.
	 * @param amount {@link #amount}.
	 */
	public Spinning(Player player, SpinningData data, int amount) {
		super(player, Optional.empty());
		this.data = data;
		this.amount = amount;
	}
	
	/**
	 * Attempts to register a certain amount of spinning products.
	 * @param player the player creating the spinning products.
	 * @param buttonId the button the player interacted with.
	 * @return {@code true} if the player created any products, {@code false} otherwise.
	 */
	public static boolean create(Player player, int buttonId) {
		if(!BUTTON_FOR_AMOUNT.containsKey(buttonId) || !player.getAttr().get("crafting_spin").getBoolean()) {
			return false;
		}
		int amount = BUTTON_FOR_AMOUNT.get(buttonId);
		if(amount == -1) {
			player.out(new SendEnterAmount("How many you would like to spin?", s -> () -> Spinning.create(player, (SpinningData) player.getAttr().get("crafting_spinning").get(), Integer.parseInt(s))));
			return true;
		}
		if(amount == -2) {
			amount = player.getInventory().computeAmountForId(((SpinningData) player.getAttr().get("crafting_spinning").get()).item.getId());
		}
		create(player, (SpinningData) player.getAttr().get("crafting_spinning").get(), amount);
		return true;
	}
	
	/**
	 * Starts the skill action.
	 * @param player the player to start the skill action for.
	 * @param data the data this skill action is dependent of.
	 * @param amount the amount of times this task should run.
	 */
	public static void create(Player player, SpinningData data, int amount) {
		Spinning crafting = new Spinning(player, data, amount);
		crafting.start();
	}
	
	public static void action() {
		for(SpinningData data : SpinningData.values()) {
			ItemOnObjectAction a = new ItemOnObjectAction() {
				@Override
				public boolean click(Player player, GameObject object, Item item, int container, int slot) {
					if(object.getId() != 2644) {
						return false;
					}
					player.text(2799, "\\n\\n\\n\\n\\n" + data.produced.getDefinition().getName());
					player.out(new SendItemModelInterface(1746, 200, data.produced.getId()));
					player.getAttr().get("crafting_spin").set(true);
					player.getAttr().get("crafting_spinning").set(data);
					player.chatWidget(4429);
					return true;
				}
			};
			a.registerObj(2644);
			a.registerItem(data.item.getId());
		}
	}
	
	@Override
	public void onProduce(Task t, boolean success) {
		if(success) {
			amount--;
			
			if(amount <= 0)
				t.cancel();
		}
	}
	
	@Override
	public Optional<Animation> animation() {
		return Optional.of(new Animation(883));
	}
	
	@Override
	public Optional<Item[]> removeItem() {
		return Optional.of(new Item[]{data.item});
	}
	
	@Override
	public Optional<Item[]> produceItem() {
		return Optional.of(new Item[]{data.produced});
	}
	
	@Override
	public int delay() {
		return 5;
	}
	
	@Override
	public boolean instant() {
		return true;
	}
	
	@Override
	public boolean init() {
		player.closeWidget();
		return checkCrafting();
	}
	
	@Override
	public boolean canExecute() {
		return checkCrafting();
	}
	
	@Override
	public double experience() {
		return data.experience;
	}
	
	@Override
	public SkillData skill() {
		return SkillData.CRAFTING;
	}
	
	@Override
	public void onStop() {
		player.getAttr().get("crafting_spin").set(false);
	}
	
	private boolean checkCrafting() {
		if(!player.getSkills()[skill().getId()].reqLevel(data.requirement)) {
			player.message("You need a crafting level of " + data.requirement + " to spin " + TextUtils.appendIndefiniteArticle(data.produced.getDefinition().getName()));
			return false;
		}
		return true;
	}
	
	public enum SpinningData {
		WOOL(1737, 1759, 1, 2.5),
		FLAX(1779, 1777, 1, 15),
		SINEW(9436, 9438, 10, 15),
		MAGIC_ROOTS(6051, 6038, 19, 30),
		HAIR(10814, 954, 30, 25);
		/**
		 * The item required to spin.
		 */
		private final Item item;
		
		/**
		 * The item produced from spinning.
		 */
		private final Item produced;
		
		/**
		 * The requirement to spin.
		 */
		private final int requirement;
		
		/**
		 * The experience gained from spinning.
		 */
		private final double experience;
		
		/**
		 * Constructs a new {@link SpinningData}.
		 * @param item {@link #item}.
		 * @param produced {@link #produced}.
		 * @param requirement {@link #requirement}.
		 * @param experience {@link #experience}.
		 */
		SpinningData(int item, int produced, int requirement, double experience) {
			this.item = new Item(item);
			this.produced = new Item(produced);
			this.requirement = requirement;
			this.experience = experience;
		}
	}
	
	private static final Int2IntArrayMap BUTTON_FOR_AMOUNT = new Int2IntArrayMap(ImmutableMap.of(10239, 1, 10238, 5, 6212, -1, 6211, -2));
}
