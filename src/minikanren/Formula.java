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

public abstract class Formula extends Stream.Fun<Formula.State> {
	protected static class State {
		public final IntMap<Term> subs;
		public final int vars;

		public State() {
			subs = IntMap.create();
			vars = 0;
		}

		public State(IntMap<Term> subs, int vars) {
			this.subs = subs;
			this.vars = vars;
		}
	}

	/**
	 * Calculates a stream of satisfying assignments extending the given
	 * assignment of variables to terms
	 */
	public abstract Stream<State> calculate(State state);

	/**
	 * Creates fresh variables (existential quantifier)
	 */
	public static abstract class Fresh extends Formula {
		public final int count;

		public Fresh(int count) {
			assert count >= 0;
			this.count = count;
		}

		public abstract Formula create(Term.Var[] variables);

		@Override
		public Stream<State> calculate(State state) {
			Term.Var[] vars = new Term.Var[count];
			for (int i = 0; i < count; i++)
				vars[i] = new Term.Var(state.vars + i);
			return create(vars).calculate(
					new State(state.subs, state.vars + count));
		}
	}

	/**
	 * Unification of terms (equality relation)
	 */
	public static class Unify extends Formula {
		public final Term term1;
		public final Term term2;

		public Unify(Term term1, Term term2) {
			this.term1 = term1;
			this.term2 = term2;
		}

		@Override
		public Stream<State> calculate(State state) {
			IntMap<Term> subs = term1.unify(state.subs, term2);
			if (subs == null)
				return Stream.create();
			else
				return Stream.create(new State(subs, state.vars));
		}
	}

	/**
	 * Disjunction of formulae
	 */
	public static class Disj extends Formula {
		public final Formula[] subs;

		public Disj(Formula... subs) {
			this.subs = subs;
		}

		@Override
		public Stream<State> calculate(State state) {
			Stream<State> stream = Stream.create();
			for (int i = subs.length - 1; i >= 0; i--)
				stream = subs[i].calculate(state).merge(stream);
			return stream;
		}
	}

	/**
	 * Conjunction of formulae
	 */
	public static class Conj extends Formula {
		public final Formula[] subs;

		public Conj(Formula... subs) {
			this.subs = subs;
		}

		@Override
		public Stream<State> calculate(State state) {
			Stream<State> stream = Stream.create(state);
			for (int i = 0; i < subs.length; i++)
				stream = stream.mapcat(subs[i]);
			return stream;
		}
	}
}
