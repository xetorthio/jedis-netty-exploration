package com.github.xetorthio.jedis.result;

public interface ResultPromise {
	public void setSuccessString(byte[] result);
	public void setSuccessLong(Long result);
}
