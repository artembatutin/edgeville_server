package net.edge.content.commands.impl;

import net.edge.World;
import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.world.node.entity.npc.Npc;
import net.edge.world.node.entity.npc.impl.DefaultNpc;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.entity.player.assets.Rights;

@CommandSignature(alias = {"dummy"}, rights = {Rights.ADMINISTRATOR, Rights.DEVELOPER}, syntax = "Use this command as ::dummy npcId")
public final class DummyCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		int npcId = Integer.parseInt(cmd[1]);
		Npc npc = new DefaultNpc(npcId, player.getPosition().copy());
		npc.setCurrentHealth(1000);
		npc.setSpawnedFor(player.getUsername());
		npc.getMovementQueue().setLockMovement(true);
		World.get().getNpcs().add(npc);
	}
	
}