import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.util.*
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

class Checkers : JPanel() {

    private var newGameButton: JButton? = null
    private var restartButton: JButton? = null
    private var message: JLabel? = null


    init {
        layout = null
        preferredSize = Dimension(390, 290)
        background = Color(255, 255, 255)
        val board = Board()
        add(board)
        add(newGameButton)
        add(restartButton)
        add(message)
        board.setBounds(20, 20, 164, 164)
        newGameButton!!.setBounds(260, 60, 120, 30)
        restartButton!!.setBounds(260, 120, 120, 30)
        message!!.setBounds(30, 250, 350, 30)
    }

    private class CheckersMove constructor(
        var fromRow: Int, var fromCol: Int,
        var toRow: Int, var toCol: Int
    )
    {
        val isJump: Boolean
            get() = fromRow - toRow == 2 || fromRow - toRow == -2
    }

    private inner class Board
    internal constructor() : JPanel(), ActionListener, MouseListener {
        var board: CheckersData
        var gameInProgress: Boolean = false
        var currentPlayer: Int = 0
        var selectedRow: Int = 0
        var selectedCol: Int = 0
        var legalMoves: Array<CheckersMove?>? = null
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

        fun doNewGame() {
            if (gameInProgress) {
                message!!.text = "Finish the current game first!"
                return
            }
            board.setUpGame()
            currentPlayer = CheckersData.WHITE
            legalMoves = board.getLegalMoves(CheckersData.WHITE)
            selectedRow = -1
            message!!.text = "White:  Make your move."
            gameInProgress = true
            newGameButton!!.isEnabled = false
            restartButton!!.isEnabled = true
            repaint()
        }

        fun doRestart() {
            if (!gameInProgress) {
                message!!.text = "There is no game in progress!"
                return
            }
            if (currentPlayer == CheckersData.WHITE)
                gameOver("WHITE give up. BLACK wins.")
            else
                gameOver("BLACK give up. WHITE wins.")
        }

        fun gameOver(str: String) {
            message!!.text = str
            newGameButton!!.isEnabled = true
            restartButton!!.isEnabled = false
            gameInProgress = false
        }

        fun doClickSquare(row: Int, col: Int) {
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
            if (selectedRow < 0) {
                message!!.text = "Click the piece you want to move."
                return
            }
            for (i in legalMoves!!.indices)
                if (legalMoves!![i]!!.fromRow == selectedRow && legalMoves!![i]!!.fromCol == selectedCol
                    && legalMoves!![i]!!.toRow == row && legalMoves!![i]!!.toCol == col
                ) {
                    doMakeMove(legalMoves!![i]!!)
                    return
                }
            message!!.text = "Click the square you want to move to."
        }

        fun doMakeMove(move: CheckersMove) {
            board.makeMove(move)
            if (move.isJump) {
                legalMoves = board.getLegalJumpsFrom(currentPlayer, move.toRow, move.toCol)
                if (legalMoves != null) {
                    if (currentPlayer == CheckersData.WHITE)
                        message!!.text = "WHITE:  You must continue jumping."
                    else
                        message!!.text = "BLACK:  You must continue jumping."
                    selectedRow = move.toRow
                    selectedCol = move.toCol
                    repaint()
                    return
                }
            }
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
            selectedRow = -1
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
            repaint()
        }


        override fun paintComponent(g: Graphics) {
            g.color = Color.black
            g.drawRect(0, 0, size.width - 1, size.height - 1)
            g.drawRect(1, 1, size.width - 3, size.height - 3)
            for (row in 0..7) {
                for (col in 0..7) {
                    if (row % 2 == col % 2)
                        g.color = Color(177, 137, 0)
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

            if (gameInProgress) {
                g.color = Color.blue
                for (i in legalMoves!!.indices) {
                    g.drawRect(2 + legalMoves!![i]!!.fromCol * 20, 2 + legalMoves!![i]!!.fromRow * 20, 19, 19)
                    g.drawRect(3 + legalMoves!![i]!!.fromCol * 20, 3 + legalMoves!![i]!!.fromRow * 20, 17, 17)
                }
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
        }

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


    }

    private class CheckersData internal constructor() {

        var board: Array<IntArray> = Array(8) { IntArray(8) }

        init {
            setUpGame()
        }

        companion object {
            const val EMPTY = 0
            const val WHITE = 1
            const val WHITE_QUEEN = 2
            const val BLACK = 3
            const val BLACK_QUEEN = 4
        }

        fun setUpGame() {
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
        }


        fun pieceAt(row: Int, col: Int): Int {
            return board[row][col]
        }


        fun makeMove(move: CheckersMove) {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol)
        }


        fun makeMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
            board[toRow][toCol] = board[fromRow][fromCol]
            board[fromRow][fromCol] = EMPTY
            if (fromRow - toRow == 2 || fromRow - toRow == -2) {
                val jumpRow = (fromRow + toRow) / 2
                val jumpCol = (fromCol + toCol) / 2
                board[jumpRow][jumpCol] = EMPTY
            }
            if (toRow == 0 && board[toRow][toCol] == WHITE)
                board[toRow][toCol] = WHITE_QUEEN
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_QUEEN
        }


        fun getLegalMoves(player: Int): Array<CheckersMove?>? {
            if (player != WHITE && player != BLACK)
                return null
            val playerQueen: Int = if (player == WHITE)
                WHITE_QUEEN
            else
                BLACK_QUEEN
            val moves = ArrayList<CheckersMove>()
            for (row in 0..7) {
                for (col in 0..7) {
                    if (board[row][col] == player) {
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
            return if (moves.size == 0)
                null
            else {
                val moveArray = arrayOfNulls<CheckersMove>(moves.size)
                for (i in moves.indices)
                    moveArray[i] = moves[i]
                moveArray
            }
        }
        fun getLegalJumpsFrom(player: Int, row: Int, col: Int): Array<CheckersMove?>? {
            if (player != WHITE && player != BLACK)
                return null
            val playerQueen: Int =
                if (player == WHITE)
                    WHITE_QUEEN
                else
                    BLACK_QUEEN
            val moves = ArrayList<CheckersMove>()
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
        }

        private fun canJump(player: Int, r1: Int, c1: Int, r2: Int, c2: Int, r3: Int, c3: Int): Boolean {
            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false
            if (board[r3][c3] != EMPTY)
                return false
            if (player == WHITE) {
                if (board[r1][c1] == WHITE && r3 > r1)
                    return false
                return !(board[r2][c2] != BLACK && board[r2][c2] != BLACK_QUEEN)
            } else {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false
                return !(board[r2][c2] != WHITE && board[r2][c2] != WHITE_QUEEN)
            }
        }

        private fun canMove(player: Int, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false
            if (board[r2][c2] != EMPTY)
                return false
            return if (player == WHITE) {
                !(board[r1][c1] == WHITE && r2 > r1)
            } else {
                !(board[r1][c1] == BLACK && r2 < r1)
            }
        }
    }

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
}