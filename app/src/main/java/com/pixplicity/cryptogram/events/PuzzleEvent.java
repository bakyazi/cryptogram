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

    public static class PuzzleProgressEvent extends PuzzleEvent {
        public PuzzleProgressEvent(Puzzle puzzle) {
            super(puzzle);
        }
    }

    public static class PuzzleResetEvent extends PuzzleEvent {
        public PuzzleResetEvent(Puzzle puzzle) {
            super(puzzle);
        }
    }

    public static class PuzzleStyleChangedEvent extends PuzzleEvent {
        public PuzzleStyleChangedEvent() {
            super(null);
        }
    }

    public static class KeyboardInputEvent extends PuzzleEvent {
        private final int mKeyCode;

        public KeyboardInputEvent(int keyCode) {
            super(null);
            mKeyCode = keyCode;
        }

        public int getKeyCode() {
            return mKeyCode;
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
