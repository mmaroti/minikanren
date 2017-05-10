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

public abstract class Term {
	/**
	 * Finds the term associated with this term (variable) under the given
	 * substitution map.
	 */
	abstract Term walk(IntMap<Term> map);

	public IntMap<Term> unify(IntMap<Term> map, Term other) {
		Term t1 = walk(map);
		Term t2 = other.walk(map);

		if (t1 instanceof Variable) {
			Variable v1 = (Variable) t1;
			if (t2 instanceof Variable) {
				Variable v2 = (Variable) t2;
				if (v2.index == v1.index)
					return map;
			}
			return map.set(v1.index, t2);
		} else if (t2 instanceof Variable) {
			Variable v2 = (Variable) t2;
			return map.set(v2.index, t1);
		}

		if (t1 instanceof Atom && t2 instanceof Atom) {
			Atom<?> a1 = (Atom<?>) t1;
			Atom<?> a2 = (Atom<?>) t2;
			if (a1.value.equals(a2.value))
				return map;
		}

		if (t1 instanceof FreeOp && t2 instanceof FreeOp) {
			FreeOp o1 = (FreeOp) t1;
			FreeOp o2 = (FreeOp) t2;

			if (o1.symbol.equals(o2.symbol) && o1.subterms.length == o2.subterms.length) {
				for (int i = 0; map != null && i < o1.subterms.length; i++)
					map = o1.subterms[i].unify(map, o2.subterms[i]);
				return map;
			}
		}

		return null;
	}

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object other);

	public static class Variable extends Term {
		public final int index;

		public Variable(int index) {
			assert index >= 0;
			this.index = index;
		}

		@Override
		Term walk(IntMap<Term> map) {
			Variable v = this;
			for (;;) {
				Term t = map.get(v.index);
				if (t == null)
					return v;

				if (t instanceof Variable)
					v = (Variable) t;
				else
					return t;
			}
		}

		@Override
		public String toString() {
			return "v" + index;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Variable) {
				Variable o = (Variable) other;
				return index == o.index;
			} else
				return false;
		}
	}

	public static class FreeOp extends Term {
		public final String symbol;
		public final Term[] subterms;

		public FreeOp(String operation, Term... subterms) {
			this.symbol = operation;
			this.subterms = subterms;
		}

		@Override
		Term walk(IntMap<Term> map) {
			return this;
		}

		@Override
		public String toString() {
			String s = symbol;
			s += '(';
			for (int i = 0; i < subterms.length; i++) {
				if (i > 0)
					s += ',';
				s += subterms[i].toString();
			}
			s += ')';
			return s;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof FreeOp))
				return false;

			FreeOp o = (FreeOp) other;
			if (!symbol.equals(o.symbol)
					|| subterms.length != o.subterms.length)
				return false;

			for (int i = 0; i < subterms.length; i++)
				if (!subterms[i].equals(o.subterms[i]))
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
