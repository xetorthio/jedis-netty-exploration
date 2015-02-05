package com.github.xetorthio.jedis.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

public class RedisProtocolEncoder extends MessageToByteEncoder<List<byte[]>> {
	public static final byte DOLLAR_BYTE = '$';
	public static final byte ASTERISK_BYTE = '*';
	public static final byte PLUS_BYTE = '+';
	public static final byte MINUS_BYTE = '-';
	public static final byte COLON_BYTE = ':';
	public static byte[] CrLf = "\r\n".getBytes();

	@Override
	protected void encode(ChannelHandlerContext ctx, List<byte[]> msg,
			ByteBuf buffer) throws Exception {
		buffer.writeByte(ASTERISK_BYTE);
		buffer.writeBytes(("" + msg.size()).getBytes());
		buffer.writeBytes(CrLf);

		for (byte[] bytes : msg) {
			buffer.writeByte(DOLLAR_BYTE);

			buffer.writeBytes(("" + bytes.length).getBytes());
			buffer.writeBytes(CrLf);

			buffer.writeBytes(bytes);
			buffer.writeBytes(CrLf);
		}

		ctx.writeAndFlush(buffer);
	}
}