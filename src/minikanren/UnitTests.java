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

public class UnitTests {
	public static void verify(boolean cond) {
		if (!cond)
			throw new IllegalStateException();
	}

	public static <V> void print(IntMap<V> map) {
		System.out.println("map of size " + map.size());
		for (IntMap.Entry<V> entry : map)
			System.out.println("" + entry.key + "\t" + entry.value);
		System.out.println();
	}

	public static void testIntMap1() {
		IntMap<String> map = IntMap.create();
		print(map);

		map = map.set(0, "a");
		print(map);

		map = map.set(1, "b");
		map = map.set(2, "c");
		print(map);

		map = map.set(1, null);
		print(map);
	}

	public static void testIntMap2() {
		HashMap<Integer, String> map1 = new HashMap<Integer, String>();
		IntMap<String> map2 = IntMap.create();

		for (int i = 0; i < 100000; i++) {
			int key = (int) (100 * Math.random());
			int val = (int) (5 * Math.random());

			if (val == 0) {
				map1.remove(key);
				map2 = map2.set(key, null);
			} else {
				map1.put(key, Integer.toString(val));
				map2 = map2.set(key, Integer.toString(val));
			}

			verify(map1.size() == map2.size());
		}
	}

	public static void main(String[] args) {
		testIntMap2();
	}
}
