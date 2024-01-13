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
        int row = position.getRow();
        int col = position.getColumn();
        // Front Right
        int new_row = row;
        int new_col = col;
        ChessPosition currPos = position;
        while (true) {
            if (!isValidMove(new_row, new_col)){
                break;
            }
            if (currPos == position){
                new_row = new_row + 1;
                new_col = new_col + 1;
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
            new_row = new_row + 1;
            new_col = new_col + 1;
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor){
                break;
            }
        }
        // Back Right
        new_row = row;
        new_col = col;
        currPos = position;
        while (true) {
            if (!isValidMove(new_row, new_col)){
                break;
            }
            if (currPos == position){
                new_row = new_row - 1;
                new_col = new_col + 1;
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
            new_row = new_row - 1;
            new_col = new_col + 1;
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor){
                break;
            }
        }
        // Back Left
        new_row = row;
        new_col = col;
        currPos = position;
        while (true) {
            if (!isValidMove(new_row, new_col)){
                break;
            }
            if (currPos == position){
                new_row = new_row - 1;
                new_col = new_col - 1;
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
            new_row = new_row - 1;
            new_col = new_col - 1;
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor){
                break;
            }
        }
        // Front Left
        new_row = row;
        new_col = col;
        currPos = position;
        while (true) {
            if (!isValidMove(new_row, new_col)){
                break;
            }
            if (currPos == position){
                new_row = new_row + 1;
                new_col = new_col - 1;
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
            new_row = new_row + 1;
            new_col = new_col - 1;
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
