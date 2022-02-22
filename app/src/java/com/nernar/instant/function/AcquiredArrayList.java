package com.nernar.instant.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class AcquiredArrayList<E extends Object> extends ArrayList<E> {
	protected boolean acquired;
	
	public AcquiredArrayList() {
		super();
	}
	
    public AcquiredArrayList(int initialCapacity) {
		super(initialCapacity);
	}
	
    public AcquiredArrayList(Collection<? extends E> c) {
		super(c);
	}
	
	public boolean isAcquired() {
		return acquired;
	}
	
	public void acquire() {
		acquired = true;
	}
	
	@Override
	public E set(int index, E element) {
		if (!acquired) {
			return super.set(index, element);
		}
		return null;
	}
	
	@Override
    public boolean add(E e) {
		if (!acquired) {
			return super.add(e);
		}
		return false;
	}
	
	@Override
    public void add(int index, E element) {
		if (!acquired) {
			super.add(index, element);
		}
	}
	
	@Override
    public E remove(int index) {
		if (!acquired) {
			return super.remove(index);
		}
		return null;
	}
	
	@Override
    public boolean remove(Object o) {
		if (!acquired) {
			return super.remove(o);
		}
		return false;
	}
	
	@Override
    public void clear() {
		if (!acquired) {
			super.clear();
		}
	}
	
	@Override
    public boolean addAll(Collection<? extends E> c) {
		if (!acquired) {
			return super.addAll(c);
		}
		return false;
	}
	
	@Override
    public boolean addAll(int index, Collection<? extends E> c) {
		if (!acquired) {
			return super.addAll(index, c);
		}
		return false;
	}
	
	@Override
    protected void removeRange(int fromIndex, int toIndex) {
		if (!acquired) {
			super.removeRange(fromIndex, toIndex);
		}
	}
	
	@Override
    public boolean removeAll(Collection<?> c) {
		if (!acquired) {
			return super.removeAll(c);
		}
		return false;
	}
	
	@Override
    public boolean retainAll(Collection<?> c) {
		if (!acquired) {
			return super.retainAll(c);
		}
		return false;
	}
	
	@Override
	public Stream<E> parallelStream() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Stream<E> stream() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public <T extends Object> T[] toArray(IntFunction<T[]> function) {
		throw new UnsupportedOperationException();
	}
}
