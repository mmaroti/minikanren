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
	public Term walk(IntMap<Term> map) {
		return this;
	}

	public abstract IntMap<Term> unify(Term other, IntMap<Term> map);

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
				else if (!(t instanceof Variable))
					return t;
				else
					v = (Variable) t;
			}
		}

		@Override
		public IntMap<Term> unify(Term other, IntMap<Term> map) {
			// TODO Auto-generated method stub
			return null;
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

	public static class FreeOperation extends Term {
		public final String operation;
		public final Term[] subterms;

		public FreeOperation(String operation, Term... subterms) {
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
			if (other instanceof FreeOperation) {
				FreeOperation o = (FreeOperation) other;
				if (!operation.equals(o.operation)
						|| subterms.length != o.subterms.length)
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
