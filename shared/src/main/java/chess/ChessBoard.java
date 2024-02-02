package chess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessBoard {

    private ChessPiece[][] squares = new ChessPiece[8][8];
    public HashMap<ChessPosition, ChessPiece> pieces = new HashMap<>();

    public ChessBoard() {
        // Empty constructor
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
        pieces.put(position, piece);
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (position.getRow() < 1 || position.getColumn() < 1 || position.getRow() > 8 || position.getColumn() > 8) {
            return null;
        }
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    public void removePiece(ChessPosition position) {
        squares[position.getRow() - 1][position.getColumn() - 1] = null;
        pieces.remove(position);
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        Map<Integer, ChessPiece.PieceType> pieceTypeMap = getPieceFromMap();

        // White Non-pawns
        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(1, col);
            ChessPiece.PieceType pieceType = pieceTypeMap.get(col);
            ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, pieceType);
            addPiece(position, piece);
        }

        // White Pawns
        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(2, col);
            ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            addPiece(position, piece);
        }

        // Black Pawns
        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(7, col);
            ChessPiece piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            addPiece(position, piece);
        }

        // Black Non-Pawns
        for (int col = 1; col <= 8; col++) {
            ChessPosition position = new ChessPosition(8, col);
            ChessPiece.PieceType pieceType = pieceTypeMap.get(col);
            ChessPiece piece = new ChessPiece(ChessGame.TeamColor.BLACK, pieceType);
            addPiece(position, piece);
        }
    }

    public Map<Integer, ChessPiece.PieceType> getPieceFromMap() {
        Map<Integer, ChessPiece.PieceType> map = new HashMap<>();
        map.put(1, ChessPiece.PieceType.ROOK);
        map.put(2, ChessPiece.PieceType.KNIGHT);
        map.put(3, ChessPiece.PieceType.BISHOP);
        map.put(4, ChessPiece.PieceType.QUEEN);
        map.put(5, ChessPiece.PieceType.KING);
        map.put(6, ChessPiece.PieceType.BISHOP);
        map.put(7, ChessPiece.PieceType.KNIGHT);
        map.put(8, ChessPiece.PieceType.ROOK);
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(squares);
    }
}
