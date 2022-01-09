package com.rageps.command.impl;

import com.rageps.command.Command;
import com.rageps.command.CommandSignature;
import com.rageps.content.skill.prayer.PrayerBook;
import com.rageps.content.trivia.TriviaTask;
import com.rageps.world.entity.actor.player.Player;
import com.rageps.world.entity.actor.player.assets.Rights;

/**
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 * @since 3-6-2017.
 */
@CommandSignature(alias = {"pbook"}, rights = {Rights.ADMINISTRATOR}, syntax = "Switches prayer book, ::pbook")
public final class PrayerBookCommand implements Command {
	
	/**
	 * The functionality to be executed as soon as this command is called.
	 * @param player the player we are executing this command for.
	 * @param cmd the command that we are executing for this player.
	 */
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		PrayerBook book = player.getPrayerBook() == PrayerBook.NORMAL ? PrayerBook.CURSES : PrayerBook.NORMAL;
		PrayerBook.convert(player, book);
		player.message("@red@Changed prayer book to: "+book.toString());
	}
}
