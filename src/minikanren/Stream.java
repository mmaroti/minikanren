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

public abstract class Stream<VALUE> {
	/**
	 * Takes two streams and merges them by interleaving values and work on lazy
	 * streams.
	 */
	public abstract Stream<VALUE> merge(Stream<VALUE> other);

	/**
	 * Maps each element to a lazy stream and merges them into a single stream.
	 */
	public abstract Stream<VALUE> mapcat(Map<VALUE> map);

	public static abstract class Map<V> {
		public abstract Stream<V> map(V value);
	}

	public static class Nill<V> extends Stream<V> {
		@Override
		public Stream<V> merge(Stream<V> other) {
			return other;
		}

		@Override
		public Stream<V> mapcat(Map<V> map) {
			return this;
		}
	}

	public static class Cons<V> extends Stream<V> {
		public final V head;
		public Stream<V> tail;

		public Cons(V head, Stream<V> tail) {
			this.head = head;
			this.tail = tail;
		}

		@Override
		public Stream<V> merge(Stream<V> other) {
			return new Cons<V>(head, other.merge(tail));
		}

		@Override
		public Stream<V> mapcat(Map<V> map) {
			return map.map(head).merge(tail);
		}
	}

	public static abstract class Lazy<V> extends Stream<V> {
		public abstract Stream<V> work();

		@Override
		public Stream<V> merge(Stream<V> other) {
			return other.merge(work());
		}

		@Override
		public Stream<V> mapcat(Map<V> map) {
			return work().mapcat(map);
		}
	}
}
