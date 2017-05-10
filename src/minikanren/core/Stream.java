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

package minikanren.core;

public abstract class Stream<VALUE> {
	/**
	 * Takes two streams and merges them by interleaving values and work on lazy
	 * streams.
	 */
	public abstract Stream<VALUE> merge(Stream<VALUE> other);

	public static abstract class Fun<VALUE> {
		public abstract Stream<VALUE> calculate(VALUE state);
	}

	/**
	 * Maps each element to a lazy stream and merges them into a single stream.
	 */
	public abstract Stream<VALUE> mapcat(Fun<VALUE> fun);

	/**
	 * Creates a lazy stream of values
	 */
	@SafeVarargs
	public static <VALUE> Stream<VALUE> create(VALUE... values) {
		Stream<VALUE> stream = new Nill<VALUE>();
		for (int i = values.length - 1; i >= 0; i--)
			stream = new Cons<VALUE>(values[i], stream);
		return stream;
	}

	static class Nill<VALUE> extends Stream<VALUE> {
		@Override
		public Stream<VALUE> merge(Stream<VALUE> other) {
			return other;
		}

		@Override
		public Stream<VALUE> mapcat(Fun<VALUE> map) {
			return this;
		}
	}

	static class Cons<VALUE> extends Stream<VALUE> {
		public final VALUE head;
		public Stream<VALUE> tail;

		public Cons(VALUE head, Stream<VALUE> tail) {
			this.head = head;
			this.tail = tail;
		}

		@Override
		public Stream<VALUE> merge(Stream<VALUE> other) {
			return new Cons<VALUE>(head, other.merge(tail));
		}

		@Override
		public Stream<VALUE> mapcat(Fun<VALUE> fun) {
			return fun.calculate(head).merge(tail.mapcat(fun));
		}
	}

	public static abstract class Lazy<VALUE> extends Stream<VALUE> {
		public abstract Stream<VALUE> work();

		@Override
		public Stream<VALUE> merge(Stream<VALUE> other) {
			return other.merge(work());
		}

		@Override
		public Stream<VALUE> mapcat(Fun<VALUE> fun) {
			return work().mapcat(fun);
		}
	}
}
