package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.statement.*;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public class Context {
	public final JavaVersion version;
	public final TypeCreator typeCreator;
	public final ExpressionCreator expressionCreator;
	int neededBreaks = 0;
	int neededContinues = 0; // should always be 0
	int breakTargets = 0;
	int continueTargets = 0;

	public Context(JavaVersion version, TypeCreator typeCreator, ExpressionCreator expressionCreator) {
		this.version = version;
		this.typeCreator = typeCreator;
		this.expressionCreator = expressionCreator;
	}

	public Context mustBreak(){
		this.neededBreaks++;
		this.breakTargets++;
		return this;
	}

	public Context canBreak() {
		this.breakTargets++;
		return this;
	}

	public Context canContinue() {
		this.continueTargets++;
		return this;
	}
	SimpleSingleNoFallThroughStatement createBreak(RandomGenerator randomGenerator, VarsEntry varsEntry) {
		if (this.neededBreaks > 0 && this.neededContinues > 0) {
			throw new IllegalStateException("Can't both break and continue in the same statement");
		} else if (this.neededBreaks > 1) {
			throw new IllegalStateException("Can't have more than one break in the same statement");
		} else if (this.neededContinues > 1) {
			throw new IllegalStateException("Can't have more than one continue in the same statement");
		} else if (this.neededBreaks > 0) {
			return new Break(randomGenerator.nextBoolean(), varsEntry);
		} else if (this.neededContinues > 0) {
			return new Continue(randomGenerator.nextBoolean(), varsEntry);
		} else {
			int count = this.breakTargets + this.continueTargets + 2;
			int target = randomGenerator.nextInt(count) - 2;
			if (target == -2) {
				return new Return(varsEntry);
			} else if (target == -1) {
				return new Throw(varsEntry);
			} else if (target < this.breakTargets) {
				return new Break(randomGenerator.nextBoolean(), varsEntry);
			} else {
				return new Continue(randomGenerator.nextBoolean(), varsEntry);
			}
		}
	}

	boolean canBeSingle(boolean completesNormally) {
		if (completesNormally) {
			return this.neededBreaks == 0 && this.neededContinues == 0;
		} else {
			return this.neededBreaks + this.neededContinues <= 1;
		}
	}

	public long cache() {
		return (long) this.neededBreaks << 32 | this.neededContinues;
	}

	long partial(RandomGenerator rng, double i) {
		long cache = this.cache();

		assert i > 0;

		// splits the breaks and continues
		if (i > 1) {
			this.neededBreaks = split(rng, this.neededBreaks, i);
			this.neededContinues = split(rng, this.neededContinues, i);
		}

		return cache;
	}

	public long restore(long cache) {
		this.neededBreaks = (int) (cache >> 32);
		this.neededContinues = (int) cache;

		return cache;
	}

	public void applyBreakOuts(List<? extends Statement> breakOuts) {
		for (Statement breakOut : breakOuts) {
			if (WrappedBreakOutStatement.isDead(breakOut)) {
				continue;
			}
			var base = WrappedBreakOutStatement.base(breakOut);
			if (base instanceof Break && this.neededBreaks > 0) {
				this.neededBreaks--;
			} else if (base instanceof Continue && this.neededContinues > 0) {
				this.neededContinues--;
			}
		}
	}

	// binomial distribution
	private static int split(RandomGenerator rng, int x, double i) {
		int res = 0;
		while (x-- > 0) {
			if (rng.nextDouble(i) < 1) {
				res++;
			}
		}

		return res;
	}

	@SuppressWarnings("unchecked")
	public List<? extends SimpleSingleNoFallThroughStatement>[] splitBreakOuts(
		RandomGenerator random,
		List<? extends SimpleSingleNoFallThroughStatement> breakOuts,
		boolean needsBreak,
		boolean canHaveBreaks,
		boolean canHaveContinues) {
		if (!canHaveBreaks && needsBreak) {
			throw new IllegalStateException("Can't have break in this statement");
		}

		if (breakOuts == null) {
			if (needsBreak) {
				throw new IllegalStateException("Can't have a break statement without breakOuts");
			}

			if (canHaveBreaks) {
				this.breakTargets--;
			}

			if (canHaveContinues) {
				this.continueTargets--;
			}

			return new List[0];
		} else {
			List<SimpleSingleNoFallThroughStatement> breaks = new ArrayList<>();
			List<SimpleSingleNoFallThroughStatement> continues = new ArrayList<>();
			List<SimpleSingleNoFallThroughStatement> remaining = new ArrayList<>();

			// for assert at the end
			int neededBreaks = this.neededBreaks;


			for (SimpleSingleNoFallThroughStatement s : breakOuts) {
				if (s instanceof Break) {
					breaks.add(s);
				} else if (s instanceof Continue) {
					continues.add(s);
				} else if (s instanceof WrappedBreakOutStatement wrapped) {
					if (wrapped.isBreak()) {
						breaks.add(wrapped);
					} else if (wrapped.isContinue()) {
						continues.add(wrapped);
					} else {
						remaining.add(wrapped);
					}
				} else {
					remaining.add(s);
				}
			}

			if (canHaveBreaks) {
				List<SimpleSingleNoFallThroughStatement> fakeBreaks = new ArrayList<>();
				breaks.removeIf(s ->
					WrappedBreakOutStatement.isDead(s) && fakeBreaks.add(s) // add always returns true
				);
				assert breaks.size() >= this.neededBreaks;
				if (needsBreak) {
					neededBreaks--; // update for assert at the end
					this.neededBreaks--;
					int x = random.nextInt(breaks.size() - this.neededBreaks); // TODO: this is not great
					for (int i = 0; i < breaks.size() - this.neededBreaks; ) {
						if (i == x || random.nextInt(this.breakTargets) == 0) {
							i++;
						} else {
							// yeah i know this isn't optimal
							final SimpleSingleNoFallThroughStatement stat = breaks.remove(i);
							remaining.add(stat);
							WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
							if (this.neededBreaks > 0) {
								this.neededBreaks--;
								// x += random.nextInt(2);
							}
							x--;
						}
						assert neededBreaks == 0 ||
							   remaining.stream()
								   .filter(s -> WrappedBreakOutStatement.base(s) instanceof Break &&
												!WrappedBreakOutStatement.isDead(s))
								   .count() >= neededBreaks - this.neededBreaks; // this.neededBreaks are the ones still needed
					}
					for (; this.neededBreaks > 0; this.neededBreaks--) {
						final SimpleSingleNoFallThroughStatement stat = breaks.remove(breaks.size() - 1);
						remaining.add(stat);
						WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
						assert neededBreaks == 0 ||
							   remaining.stream()
								   .filter(s -> WrappedBreakOutStatement.base(s) instanceof Break &&
												!WrappedBreakOutStatement.isDead(s))
								   .count() >= neededBreaks - this.neededBreaks; // this.neededBreaks are the ones still needed
					}
					assert breaks.stream().anyMatch(stat -> !WrappedBreakOutStatement.isDead(stat));
				} else {
					for (int i = 0; i < breaks.size() - this.neededBreaks; ) {
						if (random.nextInt(this.breakTargets) == 0) {
							i++;
						} else {
							// yeah i know this isn't optimal
							final SimpleSingleNoFallThroughStatement stat = breaks.remove(i);
							remaining.add(stat);
							WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
							if (this.neededBreaks > 0) {
								this.neededBreaks--;
							}
						}
						assert neededBreaks == 0 ||
							   remaining.stream()
								   .filter(s -> WrappedBreakOutStatement.base(s) instanceof Break &&
												!WrappedBreakOutStatement.isDead(s))
								   .count() >= neededBreaks - this.neededBreaks; // this.neededBreaks are the ones still needed
					}
					for (; this.neededBreaks > 0; this.neededBreaks--) {
						final SimpleSingleNoFallThroughStatement stat = breaks.remove(breaks.size() - 1);
						remaining.add(stat);
						WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
						assert neededBreaks == 0 ||
							   remaining.stream()
								   .filter(s -> WrappedBreakOutStatement.base(s) instanceof Break &&
												!WrappedBreakOutStatement.isDead(s))
								   .count() >= neededBreaks - this.neededBreaks; // this.neededBreaks are the ones still needed
					}
				}
				fakeBreaks.removeIf(s ->
					{
						if (random.nextInt(this.breakTargets) != 0) {
							remaining.add(s);
							WrappedBreakOutStatement.<Break>baseAs(s).setSimple(false);
							return true;
						}
						return false;
					} // add always returns true
				);
				breaks.addAll(fakeBreaks);

				this.breakTargets--;
			} else {
				remaining.addAll(breaks);
				breaks.forEach(stat ->
					WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false));
				breaks.clear();
			}

			if (canHaveContinues) {
				for (int i = 0; i < continues.size(); ) {
					if (random.nextInt(this.continueTargets) == 0) {
						i++;
					} else {
						// yeah i know this isn't optimal
						final SimpleSingleNoFallThroughStatement stat = continues.remove(i);
						remaining.add(stat);
						WrappedBreakOutStatement.<Continue>baseAs(stat).setSimple(false);
					}
				}

				assert neededContinues == 0; // can't be bothered to implement this
				this.continueTargets--;
			} else {
				remaining.addAll(continues);
				continues.clear();
			}

			assert !needsBreak || breaks.stream().anyMatch(stat -> !WrappedBreakOutStatement.isDead(stat));
			assert neededBreaks == 0 || remaining.stream().filter(s -> WrappedBreakOutStatement.base(s) instanceof Break && !WrappedBreakOutStatement.isDead(s)).count() >= neededBreaks;
			assert breakTargets > 0 || remaining.stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Break);
			assert continueTargets > 0 || remaining.stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Continue);
			return new List[]{remaining, breaks, continues};
		}
	}

	public boolean needsBreakOuts() {
		return this.neededBreaks > 0 || this.neededContinues > 0;
	}
}
