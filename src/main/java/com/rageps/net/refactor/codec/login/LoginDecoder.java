package com.rageps.net.refactor.codec.login;

import com.google.common.net.InetAddresses;
import com.rageps.GameConstants;
import com.rageps.net.refactor.NetworkConstants;
import com.rageps.net.refactor.security.IsaacRandom;
import com.rageps.net.refactor.security.IsaacRandomPair;
import com.rageps.util.BufferUtil;
import com.rageps.util.StatefulFrameDecoder;
import com.rageps.world.entity.actor.player.PlayerCredentials;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Logger;

/**
 * A {@link StatefulFrameDecoder} which decodes the login request frames.
 *
 * @author Graham
 */
public final class LoginDecoder extends StatefulFrameDecoder<LoginDecoderState> {

	/**
	 * The logger for this class.
	 */
	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

	/**
	 * The secure random number generator.
	 */
	private static final SecureRandom RANDOM = new SecureRandom();

	/**
	 * The login packet length.
	 */
	private int loginLength;

	/**
	 * The reconnecting flag.
	 */
	private boolean reconnecting;

	/**
	 * The server-side session key.
	 */
	private long serverSeed;

	/**
	 * The username hash.
	 */
	private int usernameHash;

	private int clientBuild;

	/**
	 * Creates the login decoder with the default initial state.
	 */
	public LoginDecoder() {
		super(LoginDecoderState.LOGIN_HANDSHAKE);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, LoginDecoderState state) {
		System.out.println("decode state:"+state+" size:"+in.readableBytes());
		switch (state) {
			case LOGIN_HANDSHAKE:
				decodeHandshake(ctx, in, out);
				break;
			case LOGIN_HEADER:
				decodeHeader(ctx, in, out);
				break;
			case LOGIN_PAYLOAD:
				decodePayload(ctx, in, out);
				break;
			default:
				throw new IllegalStateException("Invalid login decoder state: " + state);
		}
	}

	/**
	 * Decodes in the handshake state.
	 *
	 * @param ctx The channel handler context.
	 * @param buffer The buffer.
	 * @param out The {@link List} of objects to pass forward through the pipeline.
	 */
	private void decodeHandshake(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
		if (buffer.isReadable()) {

			int build = buffer.readByte();
			if(build != GameConstants.CLIENT_BUILD) {
				writeResponseCode(ctx, LoginConstants.STATUS_GAME_UPDATED);
				return;
			}

			//usernameHash = buffer.readUnsignedByte();
			serverSeed = RANDOM.nextLong();

			ByteBuf response = ctx.alloc().buffer(9);
			response.writeByte(LoginConstants.STATUS_EXCHANGE_DATA);
			//response.writeLong(0);
			response.writeLong(serverSeed);
			ctx.channel().write(response);

			setState(LoginDecoderState.LOGIN_HEADER);
		}
	}

	/**
	 * Decodes in the header state.
	 *
	 * @param ctx The channel handler context.
	 * @param buffer The buffer.
	 * @param out The {@link List} of objects to pass forward through the pipeline.
	 */
	private void decodeHeader(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
		//System.out.println("readable:"+buffer.readableBytes());
		if (buffer.readableBytes() >= 2) {
			int type = buffer.readUnsignedByte();
			System.out.println(type);

			if (type != LoginConstants.TYPE_STANDARD && type != LoginConstants.TYPE_RECONNECTION) {
				logger.info("Failed to decode login header.");
				writeResponseCode(ctx, LoginConstants.STATUS_LOGIN_SERVER_REJECTED_SESSION);
				return;
			}

			reconnecting = type == LoginConstants.TYPE_RECONNECTION;
			loginLength = buffer.readUnsignedByte();

			setState(LoginDecoderState.LOGIN_PAYLOAD);
		}
	}

	/**
	 * Decodes in the payload state.
	 *
	 * @param ctx The channel handler context.
	 * @param buffer The buffer.
	 * @param out The {@link List} of objects to pass forward through the pipeline.
	 */
	private void decodePayload(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) {
		if (buffer.readableBytes() >= loginLength) {
			ByteBuf payload = buffer.readBytes(loginLength);
			//int version = 255 - payload.readUnsignedByte();

			//clientBuild = payload.readUnsignedShort();

			//int memoryStatus = payload.readUnsignedByte();
			//if (memoryStatus != 0 && memoryStatus != 1) {
			//	logger.info("Login memoryStatus (" + memoryStatus + ") not in expected range of [0, 1].");
			//	writeResponseCode(ctx, LoginConstants.STATUS_LOGIN_SERVER_REJECTED_SESSION);
			//	return;
			//}
//
			//boolean lowMemory = memoryStatus == 1;

			//int[] crcs = new int[9];
			//for (int index = 0; index < 9; index++) {
			//	crcs[index] = payload.readInt();
			//}

			int length = payload.readUnsignedByte();
			if (length != loginLength - 41) {
				logger.info("Login packet unexpected length (" + length + ")");
				writeResponseCode(ctx, LoginConstants.STATUS_LOGIN_SERVER_REJECTED_SESSION);
				return;
			}

			ByteBuf secure = payload.readBytes(length);

			BigInteger value = new BigInteger(secure.array());
			value = value.modPow(NetworkConstants.RSA_EXPONENT, NetworkConstants.RSA_MODULUS);
			secure = Unpooled.wrappedBuffer(value.toByteArray());

			int id = secure.readUnsignedByte();
			if (id != 10) {
				logger.info("Unable to read id from secure payload.");
				writeResponseCode(ctx, LoginConstants.STATUS_LOGIN_SERVER_REJECTED_SESSION);
				return;
			}

			long clientSeed = secure.readLong();
			long reportedSeed = secure.readLong();
			if (reportedSeed != serverSeed) {
				logger.info("Reported seed differed from server seed.");
				writeResponseCode(ctx, LoginConstants.STATUS_LOGIN_SERVER_REJECTED_SESSION);
				return;
			}

			String mac = String.valueOf(secure.readInt());//todo - make this read real mac
			String username = BufferUtil.readString(secure);
			String password = BufferUtil.readString(secure);
			InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			String hostAddress = InetAddresses.toAddrString(socketAddress.getAddress());

			if (password.length() < 6 || password.length() > 20 || username.isEmpty() || username.length() > 12) {
				logger.info("Username ('" + username + "') or password did not pass validation.");
				writeResponseCode(ctx, LoginConstants.STATUS_INVALID_CREDENTIALS);
				return;
			}

			int[] seed = new int[4];
			seed[0] = (int) (clientSeed >> 32);
			seed[1] = (int) clientSeed;
			seed[2] = (int) (serverSeed >> 32);
			seed[3] = (int) serverSeed;

			IsaacRandom decodingRandom = new IsaacRandom(seed);
			for (int index = 0; index < seed.length; index++) {
				seed[index] += 50;
			}

			IsaacRandom encodingRandom = new IsaacRandom(seed);

			PlayerCredentials credentials = new PlayerCredentials(username, password, usernameHash, mac, hostAddress);
			IsaacRandomPair randomPair = new IsaacRandomPair(encodingRandom, decodingRandom);

			out.add(new LoginRequest(credentials, randomPair, reconnecting, clientBuild));
		}
	}

	/**
	 * Writes a response code to the client and closes the current channel.
	 *
	 * @param ctx The context of the channel handler.
	 * @param response The response code to write.
	 */
	private void writeResponseCode(ChannelHandlerContext ctx, int response) {
		ByteBuf buffer = ctx.alloc().buffer(Byte.BYTES);
		buffer.writeByte(response);
		ctx.writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE);
	}

}