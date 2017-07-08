package com.pixplicity.cryptogram.events;

import com.pixplicity.cryptogram.models.Puzzle;

public abstract class PuzzleEvent {

    public static class PuzzleStartedEvent extends PuzzleEvent {
        public PuzzleStartedEvent(Puzzle puzzle) {
            super(puzzle);
        }
    }

    public static class PuzzleCompletedEvent extends PuzzleEvent {
        public PuzzleCompletedEvent(Puzzle puzzle) {
            super(puzzle);
        }
    }

    private final Puzzle mPuzzle;

    public PuzzleEvent(Puzzle puzzle) {
        mPuzzle = puzzle;
    }

    public Puzzle getPuzzle() {
        return mPuzzle;
    }

}
