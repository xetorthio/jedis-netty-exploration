package com.github.xetorthio.jedis.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;
import java.util.Queue;

import com.github.xetorthio.jedis.result.BulkStringResultPromise;

public class RedisProtocolDecoder extends ReplayingDecoder<Void> {
	public static final byte DOLLAR_BYTE = '$';
	public static final byte ASTERISK_BYTE = '*';
	public static final byte PLUS_BYTE = '+';
	public static final byte MINUS_BYTE = '-';
	public static final byte COLON_BYTE = ':';
	public static byte[] CrLf = "\r\n".getBytes();

	private Queue<BulkStringResultPromise> queue;

	public RedisProtocolDecoder(Queue<BulkStringResultPromise> queue) {
		this.queue = queue;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> arg2) throws Exception {
		final byte b = in.readByte();
		if (b == PLUS_BYTE) {
			queue.poll().setSuccessString(processStatusCodeReply(in));
		} else if (b == DOLLAR_BYTE) {
			queue.poll().setSuccessString(processBulkReply(in));
		} else if (b == ASTERISK_BYTE) {
			throw new RuntimeException("Not implemented");
			// TODO: Implement
		} else if (b == COLON_BYTE) {
			queue.poll().setSuccessLong(processInteger(in));
		} else if (b == MINUS_BYTE) {
			throw new RuntimeException("Not implemented");
		} else {
			throw new RuntimeException("Don't know what to do here yet!");
		}
	}

	private static Long processInteger(ByteBuf in) {
		String nbs = in.readSlice(findCrLf(in)).toString();
		in.readByte();
		in.readByte();

		Long nb = Long.parseLong(nbs);
		return nb;
	}

	private static byte[] processBulkReply(ByteBuf in) {
		ByteBuf buf = in.readSlice(findCrLf(in));

		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		String nbs = new String(bytes);

		in.readBytes(2);

		Integer nb = Integer.parseInt(nbs);
		bytes = new byte[nb];
		in.readBytes(bytes);

		in.readBytes(2);

		return bytes;
	}

	private static byte[] processStatusCodeReply(ByteBuf in) {
		ByteBuf buf = in.readSlice(findCrLf(in));
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		in.readBytes(2);

		return bytes;
	}

	private static int findCrLf(final ByteBuf buffer) {
		final int n = buffer.writerIndex();
		for (int i = buffer.readerIndex(); i < n; i++) {
			final byte b = buffer.getByte(i);
			if (b == '\r' && i < n - 1 && buffer.getByte(i + 1) == '\n') {
				return i - 1; // \r\n
			}
		}
		return -1; // Not found.
	}
}
