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
	public abstract Term walk(IntMap<Term> map);

	public IntMap<Term> unify(IntMap<Term> map, Term other) {
		Term term = walk(map);
		other = other.walk(map);

		if (term.equals(other))
			return map;
		else if (term instanceof Variable) {
			int i = ((Variable) term).index;
			assert map.get(i) == null;
			return map.set(i, other);
		} else if (other instanceof Variable) {
			int i = ((Variable) other).index;
			assert map.get(i) == null;
			return map.set(i, term);
		}

		return null;
	}

	protected abstract Variable getVariable();

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
		public Term walk(IntMap<Term> map) {
			Variable v = this;
			for (;;) {
				Term t = map.get(v.index);
				if (t == null)
					return v;

				v = t.getVariable();
				if (v == null)
					return t;
			}
		}

		@Override
		protected Variable getVariable() {
			return this;
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

	public abstract static class Value extends Term {
		@Override
		public Term walk(IntMap<Term> map) {
			return this;
		}

		@Override
		protected Variable getVariable() {
			return null;
		}
	}

	public static class FreeOp extends Value {
		public final String operation;
		public final Term[] subterms;

		public FreeOp(String operation, Term... subterms) {
			this.operation = operation;
			this.subterms = subterms;
		}

		@Override
		public String toString() {
			String s = operation;
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
			if (other instanceof FreeOp) {
				FreeOp o = (FreeOp) other;
				if (!operation.equals(o.operation) || subterms.length != o.subterms.length)
					return false;

				for (int i = 0; i < subterms.length; i++)
					if (!subterms[i].equals(o.subterms[i]))
						return false;

				return true;
			} else
				return false;
		}
	}
}
