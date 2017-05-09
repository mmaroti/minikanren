/**
 *	Copyright (C) Miklos Maroti, 2017
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package minikanren;

import java.util.*;

/**
 * Persistent map from integers to objects.
 */
public class IntMap<VALUE> implements Iterable<IntMap.Entry<VALUE>> {
	private final VALUE val;
	private final IntMap<VALUE> sub0, sub1, sub2, sub3;

	/**
	 * Creates an empty map where all integers are mapped to <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <VALUE> IntMap<VALUE> create() {
		return (IntMap<VALUE>) EMPTY;
	}

	private static IntMap<Object> EMPTY = new IntMap<Object>();

	private IntMap() {
		val = null;
		sub0 = this;
		sub1 = this;
		sub2 = this;
		sub3 = this;
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}

	/**
	 * Returns the value associated with the given integer.
	 */
	public VALUE get(int key) {
		assert key >= 0;
		IntMap<VALUE> t = this;

		while (t != EMPTY && key > 0) {
			key -= 1;
			int a = key & 0x03;
			key >>= 2;

			switch (a) {
			case 0:
				t = sub0;
				break;
			case 1:
				t = sub1;
				break;
			case 2:
				t = sub2;
				break;
			default:
				t = sub3;
				break;
			}
		}

		return t.val;
	}

	private IntMap(VALUE value, IntMap<VALUE> sub0, IntMap<VALUE> sub1, IntMap<VALUE> sub2, IntMap<VALUE> sub3) {
		this.val = value;
		this.sub0 = sub0;
		this.sub1 = sub1;
		this.sub2 = sub2;
		this.sub3 = sub3;
	}

	/**
	 * Creates a new map that keeps all previous associations but reassociates
	 * the given key with the given value.
	 */
	@SuppressWarnings("unchecked")
	public IntMap<VALUE> set(int key, VALUE value) {
		assert key >= 0;

		VALUE v = val;
		IntMap<VALUE> s0 = sub0;
		IntMap<VALUE> s1 = sub1;
		IntMap<VALUE> s2 = sub2;
		IntMap<VALUE> s3 = sub3;

		if (key <= 0)
			v = value;
		else {
			key -= 1;
			int a = key & 0x3;
			key >>= 2;

			switch (a) {
			case 0:
				s0 = sub0.set(key, value);
				break;
			case 1:
				s1 = sub1.set(key, value);
				break;
			case 2:
				s2 = sub2.set(key, value);
				break;
			default:
				s3 = sub3.set(key, value);
				break;
			}
		}

		if (v == null && s0 == EMPTY && s1 == EMPTY && s2 == EMPTY && s3 == EMPTY)
			return (IntMap<VALUE>) EMPTY;
		else
			return new IntMap<VALUE>(v, s0, s1, s2, s3);
	}

	/**
	 * Returns the number of non-null elements in the map.
	 */
	public int size() {
		if (this == EMPTY)
			return 0;

		int s = val != null ? 1 : 0;
		s += sub0.size();
		s += sub1.size();
		s += sub2.size();
		s += sub3.size();
		return s;
	}

	public static class Entry<VALUE> {
		public final int key;
		public final VALUE value;

		public Entry(int key, VALUE value) {
			assert value != null;

			this.key = key;
			this.value = value;
		}
	}

	private static class Trace<VALUE> {
		private final Trace<VALUE> last;
		private final IntMap<VALUE> map;
		private final int key;
		private final int div;

		private Trace(Trace<VALUE> last, IntMap<VALUE> node, int index, int divider) {
			this.last = last;
			this.map = node;
			this.key = index;
			this.div = divider;
		}
	}

	private static class Iter<VALUE> implements Iterator<Entry<VALUE>> {
		private Trace<VALUE> trace;

		private Iter(IntMap<VALUE> map) {
			if (map != EMPTY) {
				trace = new Trace<VALUE>(null, map, 0, 1);
				if (trace.map.val == null)
					find();
			} else
				trace = null;
		}

		private void find() {
			do {
				int key = trace.key;
				IntMap<VALUE> map = trace.map;
				int div = trace.div;

				trace = trace.last;
				if (map.sub3 != EMPTY)
					trace = new Trace<VALUE>(trace, map.sub3, key + 4 * div, div << 2);
				if (map.sub2 != EMPTY)
					trace = new Trace<VALUE>(trace, map.sub2, key + 3 * div, div << 2);
				if (map.sub1 != EMPTY)
					trace = new Trace<VALUE>(trace, map.sub1, key + 2 * div, div << 2);
				if (map.sub0 != EMPTY)
					trace = new Trace<VALUE>(trace, map.sub0, key + div, div << 2);
			} while (trace != null && trace.map.val == null);
		}

		@Override
		public boolean hasNext() {
			return trace != null;
		}

		@Override
		public Entry<VALUE> next() {
			if (trace != null) {
				int key = trace.key;
				VALUE val = trace.map.val;
				find();
				return new Entry<VALUE>(key, val);
			} else
				throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Returns an interator for the non-null elements of this map.
	 */
	@Override
	public Iterator<Entry<VALUE>> iterator() {
		return new Iter<VALUE>(this);
	}
}
