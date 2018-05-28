package com.pixplicity.cryptogram.events

import com.pixplicity.cryptogram.models.Puzzle

abstract class PuzzleEvent(val puzzle: Puzzle?) {

    class PuzzleStartedEvent(puzzle: Puzzle) : PuzzleEvent(puzzle)

    class PuzzleCompletedEvent(puzzle: Puzzle) : PuzzleEvent(puzzle)

    class PuzzleProgressEvent(puzzle: Puzzle) : PuzzleEvent(puzzle)

    class PuzzleResetEvent(puzzle: Puzzle?) : PuzzleEvent(puzzle)

    class KeyboardInputEvent(val keyCode: Int) : PuzzleEvent(null)

}
