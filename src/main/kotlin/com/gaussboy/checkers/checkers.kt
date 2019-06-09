import java.awt.*
import java.awt.event.*
import java.util.ArrayList
import javax.swing.*

class Checkers : JPanel() {

    private var newGameButton: JButton? = null  // Button for starting a new game.
    private var restartButton: JButton? = null   // Button that a player can use to end
    // the game by restarting.

    private var message: JLabel? = null  // Label for displaying messages to the user.


    init {
        layout = null  // I will do the layout myself.
        preferredSize = Dimension(390, 290)

        background = Color(255, 255, 255)  // White background.

        /* Create the components and add them to the applet. */

        val board = Board()  // Note: The constructor for the
        //   board also creates the buttons
        //   and label.
        add(board)
        add(newGameButton)
        add(restartButton)
        add(message)

        /* Set the position and size of each component by calling
       its setBounds() method. */

        board.setBounds(20, 20, 164, 164) // Note:  size MUST be 164-by-164 !
        newGameButton!!.setBounds(260, 60, 120, 30)
        restartButton!!.setBounds(260, 120, 120, 30)
        message!!.setBounds(30, 250, 350, 30)

    } // end constructor


    private class CheckersMove internal constructor(
        internal var fromRow: Int, internal var fromCol: Int  // Position of piece to be moved.
        , internal var toRow: Int, internal var toCol: Int      // Square it is to move to.
    )// Constructor.  Just set the values of the instance variables.
    {
        internal// Test whether this move is a jump.  It is assumed that
        // the move is legal.  In a jump, the piece moves two
        // rows.  (In a regular move, it only moves one row.)
        val isJump: Boolean
            get() = fromRow - toRow == 2 || fromRow - toRow == -2
    }  // end class CheckersMove.


    private inner class Board

    internal constructor() : JPanel(), ActionListener, MouseListener {


        internal var board: CheckersData

        internal var gameInProgress: Boolean = false



        internal var currentPlayer: Int = 0

        internal var selectedRow: Int = 0
        internal var selectedCol: Int = 0

        internal var legalMoves: Array<CheckersMove?>? = null

        init {
            background = Color.BLACK
            addMouseListener(this)
            restartButton = JButton("Restart")
            restartButton!!.addActionListener(this)
            newGameButton = JButton("New Game")
            newGameButton!!.addActionListener(this)
            message = JLabel("", JLabel.CENTER)
            message!!.font = Font("Serif", Font.BOLD, 14)
            message!!.foreground = Color.black
            board = CheckersData()
            doNewGame()
        }


        override fun actionPerformed(evt: ActionEvent) {
            val src = evt.source
            if (src === newGameButton)
                doNewGame()
            else if (src === restartButton)
                doRestart()
        }


        /**
         * Start a new game
         */
        internal fun doNewGame() {
            if (gameInProgress) {
                // This should not be possible, but it doesn't hurt to check.
                message!!.text = "Finish the current game first!"
                return
            }
            board.setUpGame()   // Set up the pieces.
            currentPlayer = CheckersData.WHITE   // WHITE moves first.
            legalMoves = board.getLegalMoves(CheckersData.WHITE)  // Get WHITE's legal moves.
            selectedRow = -1   // WHITE has not yet selected a piece to move.
            message!!.text = "White:  Make your move."
            gameInProgress = true
            newGameButton!!.isEnabled = false
            restartButton!!.isEnabled = true
            repaint()
        }



        internal fun doRestart() {
            if (!gameInProgress) {  // Should be impossible.
                message!!.text = "There is no game in progress!"
                return
            }
            if (currentPlayer == CheckersData.WHITE)
                gameOver("WHITE give up. BLACK wins.")
            else
                gameOver("BLACK give up. WHITE wins.")
        }



        internal fun gameOver(str: String) {
            message!!.text = str
            newGameButton!!.isEnabled = true
            restartButton!!.isEnabled = false
            gameInProgress = false
        }



        internal fun doClickSquare(row: Int, col: Int) {
            for (i in legalMoves!!.indices)
                if (legalMoves!![i]!!.fromRow == row && legalMoves!![i]!!.fromCol == col) {
                    selectedRow = row
                    selectedCol = col
                    if (currentPlayer == CheckersData.WHITE)
                        message!!.text = "WHITE: Make your move."
                    else
                        message!!.text = "BLACK: Make your move."
                    repaint()
                    return
                }

            /* If no piece has been selected to be moved, the user must first
          select a piece.  Show an error message and return. */

            if (selectedRow < 0) {
                message!!.text = "Click the piece you want to move."
                return
            }

            /* If the user clicked on a square where the selected piece can be
          legally moved, then make the move and return. */

            for (i in legalMoves!!.indices)
                if (legalMoves!![i]!!.fromRow == selectedRow && legalMoves!![i]!!.fromCol == selectedCol
                    && legalMoves!![i]!!.toRow == row && legalMoves!![i]!!.toCol == col
                ) {
                    doMakeMove(legalMoves!![i]!!)
                    return
                }

            /* If we get to this point, there is a piece selected, and the square where
          the user just clicked is not one where that piece can be legally moved.
          Show an error message. */

            message!!.text = "Click the square you want to move to."

        }  // end doClickSquare()



        internal fun doMakeMove(move: CheckersMove) {

            board.makeMove(move)

            /* If the move was a jump, it's possible that the player has another
          jump.  Check for legal jumps starting from the square that the player
          just moved to.  If there are any, the player must jump.  The same
          player continues moving.
          */

            if (move.isJump) {
                legalMoves = board.getLegalJumpsFrom(currentPlayer, move.toRow, move.toCol)
                if (legalMoves != null) {
                    if (currentPlayer == CheckersData.WHITE)
                        message!!.text = "WHITE:  You must continue jumping."
                    else
                        message!!.text = "BLACK:  You must continue jumping."
                    selectedRow = move.toRow  // Since only one piece can be moved, select it.
                    selectedCol = move.toCol
                    repaint()
                    return
                }
            }

            /* The current player's turn is ended, so change to the other player.
          Get that player's legal moves.  If the player has no legal moves,
          then the game ends. */

            if (currentPlayer == CheckersData.WHITE) {
                currentPlayer = CheckersData.BLACK
                legalMoves = board.getLegalMoves(currentPlayer)
                when {
                    legalMoves == null -> gameOver("BLACK has no moves.  WHITE wins.")
                    legalMoves!![0]!!.isJump -> message!!.text = "BLACK:  Make your move.  You must jump."
                    else -> message!!.text = "BLACK:  Make your move."
                }
            } else {
                currentPlayer = CheckersData.WHITE
                legalMoves = board.getLegalMoves(currentPlayer)
                when {
                    legalMoves == null -> gameOver("WHITE has no moves.  BLACK wins.")
                    legalMoves!![0]!!.isJump -> message!!.text = "WHITE:  Make your move.  You must jump."
                    else -> message!!.text = "WHITE:  Make your move."
                }
            }

            /* Set selectedRow = -1 to record that the player has not yet selected
          a piece to move. */

            selectedRow = -1

            /* As a courtesy to the user, if all legal moves use the same piece, then
          select that piece automatically so the use won't have to click on it
          to select it. */

            if (legalMoves != null) {
                var sameStartSquare = true
                for (i in 1 until legalMoves!!.size)
                    if (legalMoves!![i]!!.fromRow != legalMoves!![0]!!.fromRow || legalMoves!![i]!!.fromCol != legalMoves!![0]!!.fromCol) {
                        sameStartSquare = false
                        break
                    }
                if (sameStartSquare) {
                    selectedRow = legalMoves!![0]!!.fromRow
                    selectedCol = legalMoves!![0]!!.fromCol
                }
            }

            /* Make sure the board is redrawn in its new state. */

            repaint()

        }  // end doMakeMove();


        public override fun paintComponent(g: Graphics) {

            /* Draw a two-pixel black border around the edges of the canvas. */

            g.color = Color.black
            g.drawRect(0, 0, size.width - 1, size.height - 1)
            g.drawRect(1, 1, size.width - 3, size.height - 3)

            /* Draw the squares of the checkerboard and the checkers. */

            for (row in 0..7) {
                for (col in 0..7) {
                    if (row % 2 == col % 2)
                        g.color = Color(177,137,0)
                    else
                        g.color = Color(70, 30, 0)
                    g.fillRect(2 + col * 20, 2 + row * 20, 20, 20)
                    when (board.pieceAt(row, col)) {
                        CheckersData.WHITE -> {
                            g.color = Color.WHITE
                            g.fillOval(4 + col * 20, 4 + row * 20, 15, 15)
                        }
                        CheckersData.BLACK -> {
                            g.color = Color.BLACK
                            g.fillOval(4 + col * 20, 4 + row * 20, 15, 15)
                        }
                        CheckersData.WHITE_QUEEN -> {
                            g.color = Color.WHITE
                            g.fillOval(4 + col * 20, 4 + row * 20, 15, 15)
                            g.color = Color.BLACK
                            g.drawString("Q", 7 + col * 20, 16 + row * 20)
                        }
                        CheckersData.BLACK_QUEEN -> {
                            g.color = Color.BLACK
                            g.fillOval(4 + col * 20, 4 + row * 20, 15, 15)
                            g.color = Color.WHITE
                            g.drawString("Q", 7 + col * 20, 16 + row * 20)
                        }
                    }
                }
            }

            /* If a game is in progress, highlight the legal moves.   Note that legalMoves
          is never null while a game is in progress. */

            if (gameInProgress) {
                /* First, draw a 2-pixel blue border around the pieces that can be moved. */
                g.color = Color.blue
                for (i in legalMoves!!.indices) {
                    g.drawRect(2 + legalMoves!![i]!!.fromCol * 20, 2 + legalMoves!![i]!!.fromRow * 20, 19, 19)
                    g.drawRect(3 + legalMoves!![i]!!.fromCol * 20, 3 + legalMoves!![i]!!.fromRow * 20, 17, 17)
                }
                /* If a piece is selected for moving (i.e. if selectedRow >= 0), then
                draw a 2-pixel blue border around that piece and draw cyan borders
                around each square that that piece can be moved to. */
                if (selectedRow >= 0) {
                    g.color = Color.blue
                    g.drawRect(2 + selectedCol * 20, 2 + selectedRow * 20, 19, 19)
                    g.drawRect(3 + selectedCol * 20, 3 + selectedRow * 20, 17, 17)
                    g.color = Color.cyan
                    for (i in legalMoves!!.indices) {
                        if (legalMoves!![i]!!.fromCol == selectedCol && legalMoves!![i]!!.fromRow == selectedRow) {
                            g.drawRect(2 + legalMoves!![i]!!.toCol * 20, 2 + legalMoves!![i]!!.toRow * 20, 19, 19)
                            g.drawRect(3 + legalMoves!![i]!!.toCol * 20, 3 + legalMoves!![i]!!.toRow * 20, 17, 17)
                        }
                    }
                }
            }
        }  /* end paintComponent() */



        override fun mousePressed(evt: MouseEvent) {
            if (!gameInProgress)
                message!!.text = "Click \"New Game\" to start a new game."
            else {
                val col = (evt.x - 2) / 20
                val row = (evt.y - 2) / 20
                if (col in 0..7 && row >= 0 && row < 8)
                    doClickSquare(row, col)
            }
        }


        override fun mouseReleased(evt: MouseEvent) {}
        override fun mouseClicked(evt: MouseEvent) {}
        override fun mouseEntered(evt: MouseEvent) {}
        override fun mouseExited(evt: MouseEvent) {}


    }  // end class Board

    private class CheckersData

    internal constructor() {


        internal var board: Array<IntArray> =
            Array(8) { IntArray(8) }  // board[r][c] is the contents of row r, column c.

        init {
            setUpGame()
        }
        companion object {

            /*  The following constants represent the possible contents of a square
          on the board.  The constants WHITE and BLACK also represent players
          in the game. */

            internal const val EMPTY = 0
            internal const val WHITE = 1
            internal const val WHITE_QUEEN = 2
            internal const val BLACK = 3
            internal const val BLACK_QUEEN = 4
        }

        internal fun setUpGame() {
            for (row in 0..7) {
                for (col in 0..7) {
                    if (row % 2 == col % 2) {
                        when {
                            row < 3 -> board[row][col] = BLACK
                            row > 4 -> board[row][col] = WHITE
                            else -> board[row][col] = EMPTY
                        }
                    } else {
                        board[row][col] = EMPTY
                    }
                }
            }
        }  // end setUpGame()



        internal fun pieceAt(row: Int, col: Int): Int {
            return board[row][col]
        }



        internal fun makeMove(move: CheckersMove) {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol)
        }



        internal fun makeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
            board[toRow][toCol] = board[fromRow][fromCol]
            board[fromRow][fromCol] = EMPTY
            if (fromRow - toRow == 2 || fromRow - toRow == -2) {
                // The move is a jump.  Remove the jumped piece from the board.
                val jumpRow = (fromRow + toRow) / 2  // Row of the jumped piece.
                val jumpCol = (fromCol + toCol) / 2  // Column of the jumped piece.
                board[jumpRow][jumpCol] = EMPTY
            }
            if (toRow == 0 && board[toRow][toCol] == WHITE)
                board[toRow][toCol] = WHITE_QUEEN
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_QUEEN
        }


        internal fun getLegalMoves(player: Int): Array<CheckersMove?>? {

            if (player != WHITE && player != BLACK)
                return null

            val playerQueen: Int = if (player == WHITE)
                WHITE_QUEEN
            else
                BLACK_QUEEN  // The constant representing a Queen belonging to player.

            val moves = ArrayList<CheckersMove>()  // Moves will be stored in this list.

            /*  First, check for any possible jumps.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          jump in each of the four directions from that square.  If there is
          a legal jump in that direction, put it in the moves ArrayList.
          */

            for (row in 0..7) {
                for (col in 0..7) {
                    if (board[row][col] == player || board[row][col] == playerQueen) {
                        if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2))
                            moves.add(CheckersMove(row, col, row + 2, col + 2))
                        if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2))
                            moves.add(CheckersMove(row, col, row - 2, col + 2))
                        if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2))
                            moves.add(CheckersMove(row, col, row + 2, col - 2))
                        if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2))
                            moves.add(CheckersMove(row, col, row - 2, col - 2))
                    }
                }
            }

            /*  If any jump moves were found, then the user must jump, so we don't
          add any regular moves.  However, if no jumps were found, check for
          any legal regular moves.  Look at each square on the board.
          If that square contains one of the player's pieces, look at a possible
          move in each of the four directions from that square.  If there is
          a legal move in that direction, put it in the moves ArrayList.
          */

            if (moves.size == 0) {
                for (row in 0..7) {
                    for (col in 0..7) {
                        if (board[row][col] == player || board[row][col] == playerQueen) {
                            if (canMove(player, row, col, row + 1, col + 1))
                                moves.add(CheckersMove(row, col, row + 1, col + 1))
                            if (canMove(player, row, col, row - 1, col + 1))
                                moves.add(CheckersMove(row, col, row - 1, col + 1))
                            if (canMove(player, row, col, row + 1, col - 1))
                                moves.add(CheckersMove(row, col, row + 1, col - 1))
                            if (canMove(player, row, col, row - 1, col - 1))
                                moves.add(CheckersMove(row, col, row - 1, col - 1))
                        }
                    }
                }
            }

            /* If no legal moves have been found, return null.  Otherwise, create
          an array just big enough to hold all the legal moves, copy the
          legal moves from the ArrayList into the array, and return the array. */

            return if (moves.size == 0)
                null
            else {
                val moveArray = arrayOfNulls<CheckersMove>(moves.size)
                for (i in moves.indices)
                    moveArray[i] = moves[i]
                moveArray
            }
        }  // end getLegalMoves



        internal fun getLegalJumpsFrom(player: Int, row: Int, col: Int): Array<CheckersMove?>? {
            if (player != WHITE && player != BLACK)
                return null
            val playerQueen: Int = if (player == WHITE)
                WHITE_QUEEN
            else
                BLACK_QUEEN  // The constant representing a Queen belonging to player.
            val moves = ArrayList<CheckersMove>()  // The legal jumps will be stored in this list.
            if (board[row][col] == player || board[row][col] == playerQueen) {
                if (canJump(player, row, col, row + 1, col + 1, row + 2, col + 2))
                    moves.add(CheckersMove(row, col, row + 2, col + 2))
                if (canJump(player, row, col, row - 1, col + 1, row - 2, col + 2))
                    moves.add(CheckersMove(row, col, row - 2, col + 2))
                if (canJump(player, row, col, row + 1, col - 1, row + 2, col - 2))
                    moves.add(CheckersMove(row, col, row + 2, col - 2))
                if (canJump(player, row, col, row - 1, col - 1, row - 2, col - 2))
                    moves.add(CheckersMove(row, col, row - 2, col - 2))
            }
            return if (moves.size == 0)
                null
            else {
                val moveArray = arrayOfNulls<CheckersMove>(moves.size)
                for (i in moves.indices)
                    moveArray[i] = moves[i]
                moveArray
            }
        }  // end getLegalMovesFrom()



        private fun canJump(player: Int, r1: Int, c1: Int, r2: Int, c2: Int, r3: Int, c3: Int): Boolean {
            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false  // (r3,c3) is off the board.
            if (board[r3][c3] != EMPTY)
                return false  // (r3,c3) already contains a piece.
            if (player == WHITE) {
                if (board[r1][c1] == WHITE && r3 > r1)
                    return false  // Regular white piece can only move  up.
                return !(board[r2][c2] != BLACK && board[r2][c2] != BLACK_QUEEN)  // There is no black piece to jump.
// The jump is legal.
            } else {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false  // Regular black piece can only move down.
                return !(board[r2][c2] != WHITE && board[r2][c2] != WHITE_QUEEN)  // There is no white piece to jump.
// The jump is legal.
            }
        }  // end canJump()


        private fun canMove(player: Int, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false  // (r2,c2) is off the board.
            if (board[r2][c2] != EMPTY)
                return false  // (r2,c2) already contains a piece.
            return if (player == WHITE) {
                !(board[r1][c1] == WHITE && r2 > r1)  // Regular white piece can only move down.
// The move is legal.
            } else {
                !(board[r1][c1] == BLACK && r2 < r1)  // Regular black piece can only move up.
// The move is legal.
            }
        }  // end canMove()


    } // end class CheckersData

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val window = JFrame("Checkers")
            val content = Checkers()
            window.contentPane = content
            window.pack()
            val screenSize = Toolkit.getDefaultToolkit().screenSize
            window.setLocation(
                (screenSize.width - window.width) / 2,
                (screenSize.height - window.height) / 2
            )
            window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            window.isResizable = false
            window.isVisible = true
        }
    }
} // end class Checkers