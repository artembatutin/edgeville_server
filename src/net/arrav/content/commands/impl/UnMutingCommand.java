package net.arrav.content.commands.impl;

import net.arrav.content.commands.Command;
import net.arrav.content.commands.CommandSignature;
import net.arrav.world.World;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.actor.player.assets.Rights;

@CommandSignature(alias = {"unmute"}, rights = {Rights.ADMINISTRATOR, Rights.SENIOR_MODERATOR, Rights.MODERATOR, Rights.HELPER}, syntax = "Use this command as ::unmute username")
public final class UnMutingCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		Player mute = World.get().getPlayer(cmd[1].replaceAll("_", " ")).orElse(null);
		if(mute != null && mute != player) {
			player.message("Successfully un-muted " + mute.getFormatUsername() + ".");
			mute.message("@red@You have been un-muted by " + player.getFormatUsername() + ".");
			mute.muted = false;
		} else {
			player.message("Can't find " + cmd[1].replaceAll("_", " ") + ".");
		}
	}
}
