package net.arrav.content.commands.impl;

import net.arrav.content.commands.Command;
import net.arrav.content.commands.CommandSignature;
import net.arrav.content.dialogue.impl.OptionDialogue;
import net.arrav.content.dialogue.impl.StatementDialogue;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.actor.player.assets.Rights;

@CommandSignature(alias = {"emptybank"}, rights = {Rights.ADMINISTRATOR, Rights.SENIOR_MODERATOR}, syntax = "Empties your bank, ::emptybank")
public final class EmptyBankCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.getDialogueBuilder().append(new StatementDialogue("Are you sure you want to empty your bank?"), new OptionDialogue(t -> {
			if(t.equals(OptionDialogue.OptionType.FIRST_OPTION)) {
				player.getBank().clear();
				player.getBank().refreshAll();
				player.closeWidget();
			} else if(t.equals(OptionDialogue.OptionType.SECOND_OPTION) || t.equals(OptionDialogue.OptionType.FOURTH_OPTION)) {
				player.closeWidget();
			} else if(t.equals(OptionDialogue.OptionType.THIRD_OPTION)) {
				player.getDialogueBuilder().previous();
			}
		}, "Yes", "No", "What was the question again?", "Nevermind"));
	}
	
}
