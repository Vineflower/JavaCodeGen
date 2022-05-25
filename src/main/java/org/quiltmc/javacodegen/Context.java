package org.quiltmc.javacodegen;

import org.quiltmc.javacodegen.statement.*;
import org.quiltmc.javacodegen.vars.VarsEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

class Context {
	int neededBreaks = 0;
	int neededContinues = 0; // should always be 0
	int breakTargets = 0;
	int continueTargets = 0;

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

	long partial(RandomGenerator rng, double i) {
		long cache = (long) this.neededBreaks << 32 | this.neededContinues;

		// splits the breaks and continues
		if (i > 1) {
			this.neededBreaks = split(rng, this.neededBreaks, i);
			this.neededContinues = split(rng, this.neededContinues, i);
		}

		return cache;
	}

	long restore(long cache) {
		this.neededBreaks = (int) (cache >> 32);
		this.neededContinues = (int) cache;

		return cache;
	}

	void applyBreakOuts(List<? extends Statement> breakOuts) {
		if (breakOuts != null) {
			breakOuts.forEach(this::applyBreakOut);
		}
	}

	private void applyBreakOut(Statement statement) {
		if (statement instanceof Break) {
			if (this.neededBreaks > 0) {
				this.neededBreaks--;
			}
		} else if (statement instanceof Continue) {
			if (this.neededContinues > 0) {
				this.neededContinues--;
			}
		} else if (statement instanceof WrappedBreakOutStatement stat) {
			this.applyBreakOut(stat.statement());
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
	List<? extends SimpleSingleNoFallThroughStatement>[] splitBreakOuts(
		Random random,
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


			for (SimpleSingleNoFallThroughStatement s : breakOuts) {
				if (s instanceof Break) {
					breaks.add(s);
				} else if (s instanceof Continue) {
					continues.add(s);
				} else if (s instanceof WrappedBreakOutStatement wrapped) {
					if (wrapped.isBreak()) {
						breaks.add(wrapped.statement());
					} else if (wrapped.isContinue()) {
						continues.add(wrapped.statement());
					} else {
						remaining.add(wrapped.statement());
					}
				} else {
					remaining.add(s);
				}
			}


			if (canHaveBreaks) {
				if (needsBreak) {
					List<SimpleSingleNoFallThroughStatement> fakeBreaks = new ArrayList<>();
					breaks.removeIf(s ->
						WrappedBreakOutStatement.isDead(s) && fakeBreaks.add(s) // add always returns true
					);
					this.neededBreaks--;
					int x = random.nextInt(breaks.size() - this.neededBreaks); // TODO: this is not great
					for (int i = 0; i < breaks.size() - this.neededBreaks;) {
						if (i == x || random.nextInt(this.breakTargets) == 0) {
							i++;
						} else {
							// yeah i know this isn't optimal
							final SimpleSingleNoFallThroughStatement stat = breaks.remove(i);
							remaining.add(stat);
							WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
							if(this.neededBreaks > 0) {
								this.neededBreaks--;
							}
						}
					}
					fakeBreaks.removeIf(s ->
						random.nextInt(this.breakTargets) != 0 && remaining.add(s) // add always returns true
					);
					breaks.addAll(fakeBreaks);
				} else {
					for (int i = 0; i < breaks.size() - this.neededBreaks;) {
						if ( random.nextInt(this.breakTargets) == 0) {
							i++;
						} else {
							// yeah i know this isn't optimal
							final SimpleSingleNoFallThroughStatement stat = breaks.remove(i);
							remaining.add(stat);
							WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false);
							if(this.neededBreaks > 0) {
								this.neededBreaks--;
							}
						}
					}
				}

				this.breakTargets--;
			} else {
				remaining.addAll(breaks);
				breaks.forEach(stat -> WrappedBreakOutStatement.<Break>baseAs(stat).setSimple(false));
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


			assert breakTargets > 0 || remaining.stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Break);
			assert continueTargets > 0 || remaining.stream().noneMatch(s -> WrappedBreakOutStatement.base(s) instanceof Continue);
			return new List[]{remaining, breaks, continues};
		}
	}
}
