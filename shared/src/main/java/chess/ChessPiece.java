package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private PieceType pieceType;
    private ChessGame.TeamColor teamColor;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
        this.teamColor = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition start = new ChessPosition(row, col);
        List<ChessPosition> endPositions = new ArrayList<>();
        endPositions = bishopMoves(myPosition, endPositions, board);
        for (ChessPosition endPosition : endPositions) {
            ChessMove move = new ChessMove(start, endPosition, this.getPieceType());
            moves.add(move);
        }
        return moves;
    }

    public List<ChessPosition> bishopMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        // Front Right
        endPositions = moveDiagonally(position, endPositions, board, 1, 1);
        // Back Right
        endPositions = moveDiagonally(position, endPositions, board, -1, 1);
        // Back Left
        endPositions = moveDiagonally(position, endPositions, board, -1, -1);
        // Front Left
        endPositions = moveDiagonally(position, endPositions, board, 1, -1);
        return endPositions;
    }

    private List<ChessPosition> moveDiagonally(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board, int rowMoves, int colMoves){
        int new_row = position.getRow();
        int new_col = position.getColumn();
        ChessPosition currPos = position;
        while (true) {
            if (!isValidMove(new_row, new_col)){
                break;
            }
            if (currPos == position){
                new_row = new_row + rowMoves;
                new_col = new_col + colMoves;
                ChessPosition end = new ChessPosition(new_row, new_col);
                currPos = end;
                continue;
            }
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor == this.teamColor){
                break;
            }
            ChessPosition end = new ChessPosition(new_row, new_col);
            endPositions.add(end);
            currPos = end;
            new_row = new_row + rowMoves;
            new_col = new_col + colMoves;
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor){
                break;
            }
        }
        return endPositions;
    }

    private boolean isValidMove(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
