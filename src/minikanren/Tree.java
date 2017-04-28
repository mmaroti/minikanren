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

public class Tree<VALUE> {
	private final VALUE val;
	private final Tree<VALUE> sub0, sub1, sub2, sub3;

	@SuppressWarnings("unchecked")
	public static <T> Tree<T> create() {
		return (Tree<T>) (EMPTY);
	}

	private static Tree<Object> EMPTY = new Tree<Object>();

	private Tree() {
		val = null;
		sub0 = this;
		sub1 = this;
		sub2 = this;
		sub3 = this;
	}

	public VALUE get(int index) {
		assert index >= 0;
		Tree<VALUE> t = this;

		while (t != EMPTY && --index >= 0) {
			int a = index & 0x03;
			index >>= 2;

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

	private Tree(VALUE value, Tree<VALUE> sub0, Tree<VALUE> sub1, Tree<VALUE> sub2, Tree<VALUE> sub3) {
		this.val = value;
		this.sub0 = sub0;
		this.sub1 = sub1;
		this.sub2 = sub2;
		this.sub3 = sub3;
	}

	public Tree<VALUE> set(int index, VALUE value) {
		assert index >= 0;

		VALUE v = val;
		Tree<VALUE> s0 = sub0;
		Tree<VALUE> s1 = sub1;
		Tree<VALUE> s2 = sub2;
		Tree<VALUE> s3 = sub3;

		if (--index < 0)
			v = value;
		else {
			int a = index & 0x3;
			index >>= 2;

			switch (a) {
			case 0:
				s0 = sub0.set(index, value);
				break;
			case 1:
				s1 = sub1.set(index, value);
				break;
			case 2:
				s2 = sub2.set(index, value);
				break;
			default:
				s3 = sub3.set(index, value);
				break;
			}
		}

		return new Tree<VALUE>(v, s0, s1, s2, s3);
	}
}
