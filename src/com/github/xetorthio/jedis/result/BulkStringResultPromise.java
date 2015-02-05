package com.github.xetorthio.jedis.result;

import io.netty.channel.EventLoop;
import io.netty.util.concurrent.DefaultPromise;

public class BulkStringResultPromise extends DefaultPromise<String> implements
		ResultPromise {
	public BulkStringResultPromise(EventLoop loop) {
		super(loop);
	}

	public void setSuccessString(byte[] result) {
		this.setSuccess(new String(result));
	}

	public void setSuccessLong(Long result) {
		this.setSuccess(result.toString());
	}
}
