package net.edge.content.commands.impl;

import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.world.World;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.actor.player.assets.Rights;

@CommandSignature(alias = {"mute"}, rights = {Rights.ADMINISTRATOR, Rights.ADMINISTRATOR, Rights.MODERATOR}, syntax = "Use this command as ::mute username")
public final class MutingCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		Player mute = World.get().getPlayer(cmd[1].replaceAll("_", " ")).orElse(null);
		if(mute != null && (mute.getRights().less(Rights.MODERATOR) || player.getRights().equals(Rights.ADMINISTRATOR)) && mute != player) {
			player.message("Successfully muted " + mute.getFormatUsername() + ".");
			mute.message("@red@You have been muted by " + player.getFormatUsername() + ".");
			mute.setMuted(true);
		}
	}
}
