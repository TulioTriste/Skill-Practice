package net.skillwars.practice.util;

public interface TtlHandler<E> {

	void onExpire(E element);

	long getTimestamp(E element);

}
