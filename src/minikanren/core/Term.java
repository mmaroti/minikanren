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

public abstract class Term {
	/**
	 * Finds the term associated with this term (variable) under the given
	 * substitution map.
	 */
	abstract Term walk(IntMap<Term> map);

	/**
	 * Checks if the given variable occurs in this term
	 */
	abstract boolean occurs(int var);

	public IntMap<Term> unify(IntMap<Term> map, Term other) {
		Term t1 = walk(map);
		Term t2 = other.walk(map);

		if (t1 instanceof Var) {
			Var v1 = (Var) t1;
			if (t2 instanceof Var) {
				Var v2 = (Var) t2;
				if (v2.index == v1.index)
					return map;
			}
			if (t2.occurs(v1.index))
				return null;
			else
				return map.set(v1.index, t2);
		} else if (t2 instanceof Var) {
			Var v2 = (Var) t2;
			if (t1.occurs(v2.index))
				return null;
			else
				return map.set(v2.index, t1);
		}

		if (t1 instanceof Atom && t2 instanceof Atom) {
			Atom<?> a1 = (Atom<?>) t1;
			Atom<?> a2 = (Atom<?>) t2;
			if (a1.value.equals(a2.value))
				return map;
		}

		if (t1 instanceof Op && t2 instanceof Op) {
			Op o1 = (Op) t1;
			Op o2 = (Op) t2;

			if (o1.symbol.equals(o2.symbol)
					&& o1.subs.length == o2.subs.length) {
				for (int i = 0; map != null && i < o1.subs.length; i++)
					map = o1.subs[i].unify(map, o2.subs[i]);
				return map;
			}
		}

		return null;
	}

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object other);

	public static class Var extends Term {
		public final int index;

		public Var(int index) {
			assert index >= 0;
			this.index = index;
		}

		@Override
		Term walk(IntMap<Term> map) {
			Var v = this;
			for (;;) {
				Term t = map.get(v.index);
				if (t == null)
					return v;

				if (t instanceof Var)
					v = (Var) t;
				else
					return t;
			}
		}

		@Override
		boolean occurs(int var) {
			return index == var;
		}

		@Override
		public String toString() {
			return "v" + index;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Var) {
				Var o = (Var) other;
				return index == o.index;
			} else
				return false;
		}
	}

	public static class Op extends Term {
		public final String symbol;
		public final Term[] subs;

		public Op(String operation, Term... subs) {
			this.symbol = operation;
			this.subs = subs;
		}

		@Override
		Term walk(IntMap<Term> map) {
			return this;
		}

		@Override
		boolean occurs(int var) {
			for (int i = 0; i < subs.length; i++)
				if (subs[i].occurs(var))
					return true;
			return false;
		}

		@Override
		public String toString() {
			String s = symbol;
			s += '(';
			for (int i = 0; i < subs.length; i++) {
				if (i > 0)
					s += ',';
				s += subs[i].toString();
			}
			s += ')';
			return s;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Op))
				return false;

			Op o = (Op) other;
			if (!symbol.equals(o.symbol)
					|| subs.length != o.subs.length)
				return false;

			for (int i = 0; i < subs.length; i++)
				if (!subs[i].equals(o.subs[i]))
					return false;

			return true;
		}
	}

	public static class Atom<VALUE> extends Term {
		public final VALUE value;

		public Atom(VALUE value) {
			assert value != null;
			this.value = value;
		}

		@Override
		Term walk(IntMap<Term> map) {
			return this;
		}

		@Override
		boolean occurs(int var) {
			return false;
		}

		@Override
		public String toString() {
			return value.toString();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Atom))
				return false;

			Atom<?> o = (Atom<?>) other;
			return value.equals(o.value);
		}
	}
}
