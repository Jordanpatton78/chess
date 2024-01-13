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
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceType = type;
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
        endPositions = pieceMovesHelperFrontRight(row, col, endPositions);
        endPositions = pieceMovesHelperBackRight(row, col, endPositions);
        endPositions = pieceMovesHelperBackLeft(row, col, endPositions);
        endPositions = pieceMovesHelperFrontLeft(row, col, endPositions);
        for(ChessPosition endPosition : endPositions){
            ChessMove move = new ChessMove(start, endPosition, this.getPieceType());
            moves.add(move);
        }
        return moves;
    }

    public List<ChessPosition> pieceMovesHelperFrontRight(int row, int col, List<ChessPosition> endPositions){
        if(row>=8){
            return endPositions;
        } else if (col>=8) {
            return endPositions;
        }
        else {
            int new_row = row + 1;
            int new_col = col + 1;
            ChessPosition end = new ChessPosition(new_row, new_col);
            endPositions.add(end);
            endPositions = pieceMovesHelperFrontRight(new_row, new_col, endPositions);
        }
        return endPositions;
    }

    public List<ChessPosition> pieceMovesHelperFrontLeft(int row, int col, List<ChessPosition> endPositions){
        if(row>=8){
            return endPositions;
        } else if (col>=8) {
            return endPositions;
        }
        else {
            int new_row = row + 1;
            int new_col = col - 1;
            ChessPosition end = new ChessPosition(new_row, new_col);
            endPositions.add(end);
            endPositions = pieceMovesHelperFrontLeft(new_row, new_col, endPositions);
        }
        return endPositions;
    }

    public List<ChessPosition> pieceMovesHelperBackRight(int row, int col, List<ChessPosition> endPositions){
        if(row<=1){
            return endPositions;
        } else if (col<=1) {
            return endPositions;
        }
        else {
            int new_row = row - 1;
            int new_col = col + 1;
            ChessPosition end = new ChessPosition(new_row, new_col);
            endPositions.add(end);
            endPositions = pieceMovesHelperBackRight(new_row, new_col, endPositions);
        }
        return endPositions;
    }

    public List<ChessPosition> pieceMovesHelperBackLeft(int row, int col, List<ChessPosition> endPositions){
        if(row<=1){
            return endPositions;
        } else if (col<=1) {
            return endPositions;
        }
        else {
            int new_row = row - 1;
            int new_col = col - 1;
            ChessPosition end = new ChessPosition(new_row, new_col);
            endPositions.add(end);
            endPositions = pieceMovesHelperBackLeft(new_row, new_col, endPositions);
        }
        return endPositions;
    }
}
