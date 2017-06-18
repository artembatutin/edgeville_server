package net.edge.content.skill.summoning;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.edge.event.impl.ButtonEvent;
import net.edge.event.impl.ItemEvent;
import net.edge.util.rand.RandomUtils;
import net.edge.content.minigame.MinigameHandler;
import net.edge.content.pets.Pet;
import net.edge.content.skill.summoning.familiar.Familiar;
import net.edge.content.skill.summoning.familiar.FamiliarAbility;
import net.edge.content.skill.summoning.familiar.FamiliarContainer;
import net.edge.content.skill.summoning.familiar.ability.Teleporter;
import net.edge.content.skill.summoning.familiar.impl.*;
import net.edge.content.skill.summoning.familiar.impl.MinotaurFamiliar.BronzeMinotaur;
import net.edge.content.skill.summoning.familiar.impl.MinotaurFamiliar.IronMinotaur;
import net.edge.locale.Position;
import net.edge.world.World;
import net.edge.world.node.NodeType;
import net.edge.world.node.entity.EntityNode;
import net.edge.world.Graphic;
import net.edge.world.node.entity.npc.Npc;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * Holds functionality for summoning familiars for a player and everything else that
 * is required for summoning such as prerequisites.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class Summoning {
	
	/**
	 * Attempts to call the pet.
	 * @param player the player to call the pet for.
	 */
	public static void callPet(Player player) {
		Optional<Pet> has_pet = player.getPetManager().getPet();
		
		if(!has_pet.isPresent()) {
			return;
		}
		
		Pet pet = has_pet.get();
		
		if(pet.getPosition().withinDistance(player.getPosition(), 3)) {
			//pet is already close enough.
			return;
		}
		ObjectList<Position> pos = World.getTraversalMap().getSurroundedTraversableTiles(player.getPosition(), player.size(), pet.size());
		if(pos.size() > 0) {
			Position p = RandomUtils.random(pos);
			pet.move(p);
		}
	}
	
	/**
	 * Attempts to call the familiar.
	 * @param player the player to call the familiar for.
	 */
	public static void callFamiliar(Player player) {
		if(!player.getFamiliar().isPresent()) {
			return;
		}
		Familiar familiar = player.getFamiliar().get();
		
		if(familiar.getPosition().withinDistance(player.getPosition(), 3)) {
			//familiar is already close enough.
			return;
		}
		ObjectList<Position> pos = World.getTraversalMap().getSurroundedTraversableTiles(player.getPosition(), player.size(), familiar.size());
		if(pos.size() > 0) {
			Position p = RandomUtils.random(pos);
			familiar.move(p);
			familiar.graphic(familiar.size() > 1 ? new Graphic(1315) : new Graphic(1314));
		}
	}
	
	/**
	 * Attempts to withdraw an item in a beast of burden or forager.
	 * @param player the player attempting to store the item.
	 * @param slot   the slot this player is storing from.
	 * @param amount the amount this player is storing.
	 */
	public static void withdraw(Player player, int slot, int amount) {
		Optional<Familiar> has_familiar = player.getFamiliar();
		
		if(!has_familiar.isPresent()) {
			return;
		}
		
		Familiar familiar = has_familiar.get();
		
		if(!familiar.getAbilityType().isHoldableContainer()) {
			player.message("This familiar cannot hold items.");
			return;
		}
		
		FamiliarContainer storage = (FamiliarContainer) familiar.getAbilityType();
		
		if(storage.getContainer().size() < 1) {
			player.message("This familiar is not holding any items.");
			return;
		}
		
		Item fromSlot = storage.getContainer().get(slot);
		
		if(amount == -1) {
			amount = storage.getContainer().computeAmountForId(fromSlot.getId());
		}
		if(!storage.canWithdraw(player, fromSlot)) {
			return;
		}
		
		storage.withdraw(player, new Item(fromSlot.getId(), amount));
		return;
	}
	
	/**
	 * Attempts to store an item in a beast of burden.
	 * @param player the player attempting to store the item.
	 * @param slot   the slot this player is storing from.
	 * @param amount the amount this player is storing.
	 */
	public static void store(Player player, int slot, int amount) {
		Optional<Familiar> has_familiar = player.getFamiliar();
		
		if(!has_familiar.isPresent()) {
			player.getMessages().sendCloseWindows();
			return;
		}
		
		Familiar familiar = has_familiar.get();
		
		if(!familiar.getAbilityType().isHoldableContainer()) {
			player.getMessages().sendCloseWindows();
			return;
		}
		
		FamiliarContainer storage = (FamiliarContainer) familiar.getAbilityType();
		Item item = player.getInventory().get(slot);
		
		if(!storage.canStore(player, item)) {
			return;
		}
		
		storage.store(player, new Item(item.getId(), amount), slot);
	}
	
	/**
	 * The method which should be overriden if this familiar has an
	 * ability where an item can be used on it.
	 * @param player the player whom is using an item on the npc.
	 * @param npc    the npc whom's being interacted by a player.
	 * @param item   the item being used on the npc.
	 * @return <true> if theres an action, <false> otherwise.
	 */
	public static boolean itemOnNpc(Player player, Npc npc, Item item) {
		Optional<Familiar> has_familiar = player.getFamiliar();
		
		if(!has_familiar.isPresent()) {
			return false;
		}
		
		Familiar familiar = has_familiar.get();
		
		if(familiar.getId() != npc.getId()) {
			return false;
		}
		
		if(!familiar.itemOnNpc(player, npc, item)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Attempts to openShop the beast of burden for the specified {@code player}.
	 * @param player the player we're opening this bob for.
	 * @return <true> if the interface could be opened, <false> otherwise.
	 */
	public static boolean openBeastOfBurden(Player player, Npc npc) {
		Optional<Familiar> has_familiar = player.getFamiliar();
		
		if(!has_familiar.isPresent()) {
			return false;
		}
		
		Familiar familiar = has_familiar.get();
		
		if(familiar.getId() != npc.getId()) {
			return false;
		}
		
		FamiliarAbility ability = familiar.getAbilityType();
		
		if(!ability.isHoldableContainer()) {
			return false;
		}
		
		player.getAttr().get("bob").set(true);
		FamiliarContainer storage = (FamiliarContainer) ability;
		player.getMessages().sendItemsOnInterface(2702, storage.getContainer().getItems());
		player.getMessages().sendInventoryInterface(2700, 5063);
		player.getMessages().sendItemsOnInterface(5064, player.getInventory().getItems());
		return true;
	}
	
	/**
	 * Attempts to withdraw all items from this familiar.
	 * @param player the player we're withdrawing items for.
	 * @param button the button this method is meant for.
	 * @return <true> if the player could withdraw an item, <false> otherwise.
	 */
	public static boolean withdrawAll(Player player, int button) {
		if(button != 10146) {
			return false;
		}
		Optional<Familiar> has_familiar = player.getFamiliar();
		
		if(!has_familiar.isPresent()) {
			return false;
		}
		
		Familiar familiar = has_familiar.get();
		
		if(!familiar.getAbilityType().isHoldableContainer()) {
			player.message("This familiar cannot hold items.");
			return false;
		}
		
		FamiliarContainer ability = (FamiliarContainer) familiar.getAbilityType();
		
		if(ability.getContainer().size() < 1) {
			player.message("This familiar is not holding any items.");
			return false;
		}
		
		ability.getContainer().forEach(item -> {
			if(!player.getInventory().canAdd(item)) {
				player.message("Your inventory is currently full.");
				return;
			}
			ability.getContainer().remove(item);
		});
		ability.getContainer().shift();
		player.getMessages().sendItemsOnInterface(2702, ability.getContainer().getItems());
		player.getMessages().sendItemsOnInterface(5064, player.getInventory().getItems());
		return true;
	}
	
	/**
	 * Attempts to teleport the summoner safely away when in combat.
	 * @param player the player whom is teleporting.
	 * @return <true> if the player was teleported, <false> otherwise.
	 */
	public static boolean combatTeleport(Player player) {
		if(!player.getFamiliar().isPresent()) {
			return false;
		}
		
		Familiar familiar = player.getFamiliar().get();
		
		if(!familiar.getAbilityType().getType().equals(FamiliarAbility.FamiliarAbilityType.TELEPORTER)) {
			return false;
		}
		
		Teleporter ability = (Teleporter) familiar.getAbilityType();
		
		if(!MinigameHandler.execute(player, m -> m.canTeleport(player, ability.getDestination()))) {
			player.message("This ability will not work in minigames where you cannot teleport.");
			return false;
		}
		
		if(!ability.combatTeleport(player)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Attempts the familiar to attack the {@code victim}.
	 * @param player the player trying to submit the attack for the familiar.
	 * @param victim the victim being attacked by the familiar.
	 * @return <true> if the familiar attacked the victim, <false> otherwise.
	 */
	public static boolean attack(Player player, EntityNode victim) {
		if(!player.getFamiliar().isPresent()) {
			return false;
		}
		Familiar familiar = player.getFamiliar().get();
		if(!player.isWildernessInterface() && victim.getType().equals(NodeType.PLAYER)) {
			player.message("This player is not in the wilderness!");
			return false;
		}
		if(!player.isMulticombatInterface()) {
			player.message("You can only do this in multi-combat areas!");
			return false;
		}
		
		if(!familiar.getDefinition().isAttackable()) {
			player.message("This familiar is not combat capable.");
			return false;
		}
		
		if(!familiar.isCombatic()) {
			player.message("This follower will only fight in self-defense.");
			return false;
		}
		
		if(!player.getCombatBuilder().inCombat()) {
			player.message("Your familiar cannot fight whilst you are not in combat.");
			return false;
		}
		
		familiar.getCombatBuilder().attack(victim);
		return true;
	}
	
	/**
	 * Attemps to summon familiar on login.
	 * @param player the player logging in.
	 */
	public static void login(Player player) {
		Optional<Familiar> familiar = player.getFamiliar();
		familiar.ifPresent(familiar1 -> familiar1.summon(player, true));
	}
	
	public static void event() {
		for(Familiar f : FAMILIARS) {
			ItemEvent e = new ItemEvent() {
				@Override
				public boolean click(Player player, Item item, int container, int slot, int click) {
					if(click == 3) {
						Optional<Familiar> hasFamiliar = player.getFamiliar();
						if(hasFamiliar.isPresent()) {
							player.message("You already have a familiar summoned.");
							return false;
						}
						
						Optional<Pet> hasPet = player.getPetManager().getPet();
						if(hasPet.isPresent()) {
							player.message("You already have a pet spawned.");
							return false;
						}
						
						if(!f.canSummon(player)) {
							return false;
						}
						
						f.summon(player, false);
					}
					return true;
				}
			};
			e.registerInventory(f.getData().getPouchId());
		}
	}
	
	/**
	 * Interacts with the familiar and sends it's respective dialogue.
	 * @param player the player interacting with the {@code npc}.
	 * @param npc    the npc being interacted by the {@code player}.
	 * @param id     the action id being interacted with.
	 * @return <true> if the dialogue was sent, <false> otherwise.
	 */
	public static boolean interact(Player player, Npc npc, int id) {
		Optional<Familiar> familiar = player.getFamiliar();
		
		if(npc.isFamiliar() && !familiar.isPresent()) {
			player.message("This is not your familiar.");
			return false;
		}
		
		if(!familiar.isPresent()) {
			return false;
		}
		
		if(familiar.get().getId() != npc.getId()) {
			return false;
		}
		
		familiar.get().interact(player, npc, id);
		return true;
	}
	
	/**
	 * Attempts to dismiss this familiar for the {@code player} dependant on the
	 * {@code login} flag.
	 * @param player the player we're dismissing this familiar for.
	 * @param button the button this player clicked.
	 * @param logout checks if we're dismissing because of logout.
	 * @return <true> if the player successfully dismissed this npc, <false> otherwise.
	 */
	public static boolean dismiss(Player player, int button, boolean logout) {
		if(logout) {
			Optional<Familiar> familiar = player.getFamiliar();
			
			if(!familiar.isPresent()) {
				return false;
			}
			
			familiar.get().dismiss(player, true);
			return true;
		}
		
		Optional<Familiar> familiar = player.getFamiliar();
		
		if(!familiar.isPresent()) {
			return false;
		}
		
		familiar.get().dismiss(player, false);
		return true;
	}
	
	/**
	 * The collection of familiars a player can summon.
	 */
	public static final ImmutableList<Familiar> FAMILIARS = ImmutableList.of(new SpiritWolf(), new DreadFowl(), new SpiritSpider(), new ThornySnail(), new GraniteCrab(), new SpiritMosquito(), new DesertWyrm(), new SpiritScorpion(), new SpiritTzKih(), new AlbinoRat(), new SpiritKalphite(), new CompostMound(), new GiantChinchompa(), new VampyreBat(), new HoneyBadger(), new Beaver(), new VoidFamiliar.VoidRavager(), new VoidFamiliar.VoidSpinner(), new VoidFamiliar.VoidShifter(), new VoidFamiliar.VoidTorcher(), new BronzeMinotaur(), new BullAnt(), new Macaw(), new SpiritTriceFamiliar.SpiritCockatrice(), new SpiritTriceFamiliar.SpiritGuthatrice(), new SpiritTriceFamiliar.SpiritSaratrice(), new SpiritTriceFamiliar.SpiritZamatrice(), new SpiritTriceFamiliar.SpiritPengatrice(), new SpiritTriceFamiliar.SpiritCoraxatrice(), new SpiritTriceFamiliar.SpiritVulatrice(), new IronMinotaur());
}
