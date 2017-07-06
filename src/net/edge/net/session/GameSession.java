package net.edge.net.session;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import net.edge.net.NetworkConstants;
import net.edge.net.codec.GameBuffer;
import net.edge.net.codec.IncomingMsg;
import net.edge.net.codec.IsaacCipher;
import net.edge.world.World;
import net.edge.world.node.entity.player.Player;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A {@link Session} implementation that handles networking for a {@link Player} during gameplay.
 * @author lare96 <http://github.org/lare96>
 */
public class GameSession extends Session {
	/**
	 * The capacity of the stream.
	 */
	private static final int STREAM_CAP = 5000;
	
	/**
	 * The player assigned to this {@code GameSession}.
	 */
	private final Player player;
	
	/**
	 * The message encryptor.
	 */
	private final IsaacCipher encryptor;
	
	/**
	 * The message decryptor.
	 */
	private final IsaacCipher decryptor;

	/**
	 * The game stream.
	 */
	private final GameBuffer stream;

	/**
	 * Creates a new {@link GameSession}.
	 * @param channel   The channel for this session.
	 * @param encryptor The message encryptor.
	 * @param decryptor The message decryptor.
	 */
	GameSession(Player player, Channel channel, IsaacCipher encryptor, IsaacCipher decryptor) {
		super(channel);
		this.player = player;
		this.encryptor = encryptor;
		this.decryptor = decryptor;
		this.stream = new GameBuffer(channel.alloc().buffer(STREAM_CAP), encryptor);
	}
	
	@Override
	public void onDispose() {
		World.get().queueLogout(player);

		stream.release();
	}
	
	@Override
	public void handleUpstreamMessage(Object msg) {
		if(msg instanceof IncomingMsg) {
			IncomingMsg packet = (IncomingMsg) msg;

			World.get().run(() -> {
				NetworkConstants.MESSAGES[packet.getOpcode()].handle(player, packet.getOpcode(), packet.getSize(), packet);

				packet.getBuffer().release();
			});
		}
	}

	/**
	 * Flushes all pending {@link IncomingMsg}s within the channel's queue. Repeated calls to this method are relatively
	 * expensive, which is why messages should be queued up with {@code queue(MessageWriter)} and flushed once at the end of
	 * the cycle.
	 */
	public void flushQueue() {
		Channel channel = getChannel();
		if(channel.isActive()) {
			channel.eventLoop().execute(() -> {
				channel.writeAndFlush(stream.retain(), channel.voidPromise());
				stream.clear();
			});
		}
	}
	
	/**
	 * @return The message encryptor.
	 */
	public IsaacCipher getEncryptor() {
		return encryptor;
	}
	
	/**
	 * @return The message decryptor.
	 */
	public IsaacCipher getDecryptor() {
		return decryptor;
	}

	/**
	 * @return The game stream.
	 */
	public GameBuffer getStream() {
		return stream;
	}
	
	/**
	 * Getting a {@link ByteBufAllocator} to allocate buffers.
	 * @return allocator.
	 */
	public ByteBufAllocator alloc() {
		return getChannel().alloc();
	}

}