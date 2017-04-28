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

/**
 * Persistent map from integers to objects.
 */
public class IntMap<VALUE> {
	private final VALUE val;
	private final IntMap<VALUE> sub0, sub1, sub2, sub3;

	/**
	 * Creates an empty map where all integers are mapped to <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public static <T> IntMap<T> create() {
		return (IntMap<T>) EMPTY;
	}

	private static IntMap<Object> EMPTY = new IntMap<Object>();

	private IntMap() {
		val = null;
		sub0 = this;
		sub1 = this;
		sub2 = this;
		sub3 = this;
	}

	/**
	 * Returns the value associated with the given integer.
	 */
	public VALUE get(int key) {
		IntMap<VALUE> t = this;

		while (t != EMPTY && key != 0) {
			int a = key & 0x03;
			key = (key >> 2) & Integer.MAX_VALUE;

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

		if (key == 0)
			v = value;
		else {
			int a = key & 0x3;
			key = (key >> 2) & Integer.MAX_VALUE;

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
}
