package net.edge.net.session;

import io.netty.channel.Channel;
import net.edge.net.NetworkConstants;
import net.edge.net.codec.ByteMessage;
import net.edge.net.codec.IsaacCipher;
import net.edge.net.message.GameMessage;
import net.edge.net.message.InputMessageListener;
import net.edge.world.World;
import net.edge.world.node.entity.player.Player;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A {@link Session} implementation that handles networking for a {@link Player} during gameplay.
 * @author lare96 <http://github.org/lare96>
 */
public final class GameSession extends Session {
	
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
	 * A bounded queue of inbound {@link GameMessage}s.
	 */
	private final Queue<GameMessage> inboundQueue = new ArrayBlockingQueue<>(NetworkConstants.MESSAGE_LIMIT);
	
	/**
	 * Creates a new {@link GameSession}.
	 * @param channel   The channel for this session.
	 * @param encryptor The message encryptor.
	 * @param decryptor The message decryptor.
	 */
	public GameSession(Player player, Channel channel, IsaacCipher encryptor, IsaacCipher decryptor) {
		super(channel);
		this.player = player;
		this.encryptor = encryptor;
		this.decryptor = decryptor;
	}
	
	@Override
	public void onDispose() {
		World.queueLogout(player);
	}
	
	@Override
	public void handleUpstreamMessage(Object msg) {
		if(msg instanceof GameMessage) {
			inboundQueue.offer((GameMessage) msg);
		}
	}
	
	/**
	 * Writes {@code msg} to the underlying channel; The channel is not flushed.
	 * @param msg The message to queue.
	 */
	public void queue(ByteMessage msg) {
		Channel channel = getChannel();
		if(channel.isActive()) {
			if(msg.getOpcode() == 53)
				channel.writeAndFlush(new GameMessage(msg.getOpcode(), msg.getType(), msg));
			else
				channel.write(new GameMessage(msg.getOpcode(), msg.getType(), msg), channel.voidPromise());
		}
	}
	
	/**
	 * Flushes all pending {@link GameMessage}s within the channel's queue. Repeated calls to this method are relatively
	 * expensive, which is why messages should be queued up with {@code queue(MessageWriter)} and flushed once at the end of
	 * the cycle.
	 */
	public void flushQueue() {
		Channel channel = getChannel();
		if(channel.isActive()) {
			channel.flush();
		}
	}
	
	/**
	 * Dequeues the inbound queue, handling all logic accordingly.
	 */
	public void dequeue() {
		for(; ; ) {
			GameMessage msg = inboundQueue.poll();
			if(msg == null) {
				break;
			}
			try {
				InputMessageListener listener = NetworkConstants.MESSAGES[msg.getOpcode()];
				listener.handleMessage(player, msg.getOpcode(), msg.getSize(), msg.getPayload());
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				ByteMessage payload = msg.getPayload();
				if(payload.refCnt() > 0) {
					payload.release();
				}
			}
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
}