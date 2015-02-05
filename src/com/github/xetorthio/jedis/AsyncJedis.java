package com.github.xetorthio.jedis;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import com.github.xetorthio.jedis.codec.RedisProtocolDecoder;
import com.github.xetorthio.jedis.codec.RedisProtocolEncoder;
import com.github.xetorthio.jedis.result.BulkStringResultPromise;

public class AsyncJedis {
	private String host;
	private int port;
	private ChannelFuture f;
	private final ConcurrentLinkedQueue<BulkStringResultPromise> outputs = new ConcurrentLinkedQueue<>();
	private EventLoopGroup workerGroup;

	public AsyncJedis(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public ChannelFuture connect() {
		Bootstrap b = new Bootstrap();
		this.workerGroup = new NioEventLoopGroup();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new RedisProtocolEncoder(),
						new RedisProtocolDecoder(outputs));
			}
		});
		this.f = b.connect(host, port);
		return f;
	}

	public ChannelFuture disconnect() {
		workerGroup.shutdownGracefully();
		return this.f.channel().closeFuture();
	}

	public Future<String> set(String key, String value) {
		BulkStringResultPromise promise = new BulkStringResultPromise(f
				.channel().eventLoop());
		List<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add("SET".getBytes());
		cmd.add(key.getBytes());
		cmd.add(value.getBytes());
		f.channel().write(cmd);
		outputs.add(promise);
		return promise;
	}

	public Future<String> get(String key) {
		BulkStringResultPromise promise = new BulkStringResultPromise(f
				.channel().eventLoop());
		List<byte[]> cmd = new ArrayList<byte[]>();
		cmd.add("GET".getBytes());
		cmd.add(key.getBytes());
		f.channel().write(cmd);
		outputs.add(promise);
		return promise;
	}
}
