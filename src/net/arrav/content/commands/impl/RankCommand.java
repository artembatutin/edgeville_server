package net.arrav.content.commands.impl;

import net.arrav.content.commands.Command;
import net.arrav.content.commands.CommandSignature;
import net.arrav.world.World;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.actor.player.assets.Rights;

@CommandSignature(alias = {"rank"}, rights = {Rights.ADMINISTRATOR}, syntax = "Sets an user rank, ::rank playername rank")
public final class RankCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		Player p = World.get().getPlayer(cmd[1].replaceAll("_", " ")).orElse(null);
		if(p == null)
			return;
		String rank = cmd[2].toUpperCase();
		Rights rights = Rights.valueOf(rank);
		p.setRights(rights);
		player.message("You've successfully set " + p.getFormatUsername() + " to " + rights.name() + ".");
		p.message("You have been ranked by " + player.getFormatUsername() + " to " + rights.name() + ".");
	}
	
}
