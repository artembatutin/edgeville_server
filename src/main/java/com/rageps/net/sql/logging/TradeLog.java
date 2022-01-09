package com.rageps.net.sql.logging;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.rageps.net.sql.DatabaseTransaction;
import com.rageps.net.sql.TableRepresentation;
import com.rageps.net.sql.statement.NamedPreparedStatement;
import com.rageps.util.DateTimeUtil;
import com.rageps.world.entity.actor.player.Player;
import com.rageps.world.entity.item.Item;
import com.rageps.world.locale.Position;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Created by Ryley Kimmel on 11/7/2016.
 */
public final class TradeLog extends DatabaseTransaction {
	private static final Gson GSON = new Gson();

	private final Player player;

	private final String other;

	private final Position position;

	private final List<Item> gave;

	private final List<Item> received;

	private final Timestamp timestamp = Timestamp.from(Instant.now(DateTimeUtil.CLOCK));

	public TradeLog(Player player, String other, List<Item> gave, List<Item> received) {
		super(TableRepresentation.LOGGING);
		this.player = player;
		this.other = other;
		this.position = player.getPosition().copy();
		this.gave = ImmutableList.copyOf(gave);
		this.received = ImmutableList.copyOf(received);
	}

	@Override
	public void execute(Connection connection) throws SQLException {
		try (NamedPreparedStatement statement = NamedPreparedStatement.create(connection,
		 "INSERT INTO trades (session_id, username, ip_address, uid, other, other_session_id, items_gave, items_received, x, y, z, timestamp) "
			+ "VALUES (session_id, :username, :ip_address, :uid, :other, :other_session_id, :items_gave, :items_received, :x, :y, :z, :timestamp);")) {
			statement.setLong("session_id", player.getSession().getSessionId());
			statement.setString("username", player.credentials.username);
			statement.setString("ip_address", player.credentials.getHostAddress());
			statement.setString("uid", player.credentials.getUid());
			statement.setString("other", other);
			statement.setLong("other_session_id", player.getSession().getSessionId());
			statement.setString("items_gave", GSON.toJson(gave));
			statement.setString("items_received", GSON.toJson(received));
			statement.setInt("x", position.getX());
			statement.setInt("y", position.getY());
			statement.setInt("z", position.getZ());
			statement.setTimestamp("timestamp", timestamp);
			statement.executeUpdate();
			connection.commit();
		}
	}
}
