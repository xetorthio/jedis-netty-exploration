import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.github.xetorthio.jedis.AsyncJedis;

public class Main {
	public static void main(String[] args) throws InterruptedException,
			ExecutionException {
		AsyncJedis jedis = new AsyncJedis("localhost", 6379);
		jedis.connect().sync();

		jedis.set("foo", "lalala");
		Future<String> foo = jedis.get("foo");

		System.out.println(foo.get());

		jedis.disconnect().sync();
	}
}
