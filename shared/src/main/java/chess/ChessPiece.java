package chess;

import java.util.*;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    public PieceType pieceType;
    public ChessGame.TeamColor teamColor;
    private boolean toBePromoted = false;

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
        return this.teamColor;
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
    public HashSet<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<ChessMove>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        ChessPosition start = new ChessPosition(row, col);
        List<ChessPosition> endPositions = new ArrayList<>();
        if (this.pieceType == PieceType.BISHOP){
            endPositions = bishopMoves(myPosition, endPositions, board);
        } else if (this.pieceType == PieceType.KING) {
            endPositions = kingMoves(myPosition, endPositions, board);
        } else if (this.pieceType == PieceType.QUEEN) {
            endPositions = queenMoves(myPosition, endPositions, board);
        } else if (this.pieceType == PieceType.ROOK) {
            endPositions = rookMoves(myPosition, endPositions, board);
        } else if (this.pieceType == PieceType.KNIGHT) {
            endPositions = knightMoves(myPosition, endPositions, board);
        } else if (this.pieceType == PieceType.PAWN) {
            endPositions = pawnMoves(myPosition, endPositions, board);
        }
        for (ChessPosition endPosition : endPositions) {
            if (toBePromoted) {
                for (int i=0;i<4;i++){
                    if (i==0) {
                        ChessMove newMove = new ChessMove(start, endPosition, PieceType.QUEEN);
                        moves.add((newMove));
                    } else if (i==1) {
                        ChessMove newMove = new ChessMove(start, endPosition, PieceType.ROOK);
                        moves.add((newMove));
                    } else if (i==2) {
                        ChessMove newMove = new ChessMove(start, endPosition, PieceType.KNIGHT);
                        moves.add((newMove));
                    } else if (i==3) {
                        ChessMove newMove = new ChessMove(start, endPosition, PieceType.BISHOP);
                        moves.add((newMove));
                    }
                }
            } else {
                ChessMove move = new ChessMove(start, endPosition, null);
                moves.add(move);
            }
        }
        toBePromoted = false;
        return moves;
    }

    public List<ChessPosition> bishopMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        // Front Right
        endPositions = move(position, endPositions, board, 1, 1, "endOfBoard");
        // Back Right
        endPositions = move(position, endPositions, board, -1, 1, "endOfBoard");
        // Back Left
        endPositions = move(position, endPositions, board, -1, -1, "endOfBoard");
        // Front Left
        endPositions = move(position, endPositions, board, 1, -1, "endOfBoard");
        return endPositions;
    }

    public List<ChessPosition> kingMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        // Front
        endPositions = move(position, endPositions, board, 1, 0, "once");
        // Front Right
        endPositions = move(position, endPositions, board, 1, 1, "once");
        // Right
        endPositions = move(position, endPositions, board, 0, 1, "once");
        // Back Right
        endPositions = move(position, endPositions, board, -1, 1, "once");
        // Back
        endPositions = move(position, endPositions, board, -1, 0, "once");
        // Back Left
        endPositions = move(position, endPositions, board, -1, -1, "once");
        // Left
        endPositions = move(position, endPositions, board, 0, -1, "once");
        // Front Left
        endPositions = move(position, endPositions, board, 1, -1, "once");
        return endPositions;
    }

    public List<ChessPosition> queenMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        // Front
        endPositions = move(position, endPositions, board, 1, 0, "endOfBoard");
        // Front Right
        endPositions = move(position, endPositions, board, 1, 1, "endOfBoard");
        // Right
        endPositions = move(position, endPositions, board, 0, 1, "endOfBoard");
        // Back Right
        endPositions = move(position, endPositions, board, -1, 1, "endOfBoard");
        // Back
        endPositions = move(position, endPositions, board, -1, 0, "endOfBoard");
        // Back Left
        endPositions = move(position, endPositions, board, -1, -1, "endOfBoard");
        // Left
        endPositions = move(position, endPositions, board, 0, -1, "endOfBoard");
        // Front Left
        endPositions = move(position, endPositions, board, 1, -1, "endOfBoard");
        return endPositions;
    }

    public List<ChessPosition> rookMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        // Front
        endPositions = move(position, endPositions, board, 1, 0, "endOfBoard");
        // Right
        endPositions = move(position, endPositions, board, 0, 1, "endOfBoard");
        // Back
        endPositions = move(position, endPositions, board, -1, 0, "endOfBoard");
        // Left
        endPositions = move(position, endPositions, board, 0, -1, "endOfBoard");
        return endPositions;
    }

    public List<ChessPosition> knightMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        // Front Right then up
        endPositions = move(position, endPositions, board, 2, 1, "once");
        // Front Right then right
        endPositions = move(position, endPositions, board, 1, 2, "once");
        // Back Right then right
        endPositions = move(position, endPositions, board, -1, 2, "once");
        // Back Right then down
        endPositions = move(position, endPositions, board, -2, 1, "once");
        // Back Left then down
        endPositions = move(position, endPositions, board, -2, -1, "once");
        // Back Left then left
        endPositions = move(position, endPositions, board, -1, -2, "once");
        // Front Left then left
        endPositions = move(position, endPositions, board, 1, -2, "once");
        // Front Left then up
        endPositions = move(position, endPositions, board, 2, -1, "once");
        return endPositions;
    }

    public List<ChessPosition> pawnMoves(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board) {
        int row = position.getRow();
        int col = position.getColumn();
        ChessPosition frontMiddle = new ChessPosition(row+1, col);
        ChessPosition backMiddle = new ChessPosition(row-1, col);
        ChessPosition frontRight = new ChessPosition(row+1, col+1);
        ChessPosition frontLeft = new ChessPosition(row+1, col-1);
        ChessPosition backRight = new ChessPosition(row-1, col+1);
        ChessPosition backLeft = new ChessPosition(row-1, col-1);
        if (position.getRow() == 2 && this.teamColor == ChessGame.TeamColor.WHITE){
            // White initial
            if (board.getPiece(frontMiddle) == null) {
                endPositions = move(position, endPositions, board, 1, 0, "pawn");
                endPositions = move(position, endPositions, board, 2, 0, "pawn");
            }
        }
        if (position.getRow() == 7 && this.teamColor == ChessGame.TeamColor.BLACK){
            // Black initial
            if (board.getPiece(backMiddle) == null) {
                endPositions = move(position, endPositions, board, -1, 0, "pawn");
                endPositions = move(position, endPositions, board, -2, 0, "pawn");
            }
        }
        if (position.getRow() == 7 && this.teamColor == ChessGame.TeamColor.WHITE){
            // White Promotion
            endPositions = move(position, endPositions, board, 1, 0, "pawn");
            toBePromoted = true;
        }
        if (position.getRow() == 2 && this.teamColor == ChessGame.TeamColor.BLACK){
            // Black Promotion
            endPositions = move(position, endPositions, board, -1, 0, "pawn");
            toBePromoted = true;
        }
        if (board.getPiece(frontRight)!= null && this.teamColor == ChessGame.TeamColor.WHITE){
            // Capture right white
            endPositions = move(position, endPositions, board, 1, 1, "once");
        }
        if (board.getPiece(frontLeft)!= null && this.teamColor == ChessGame.TeamColor.WHITE){
            // Capture left white
            endPositions = move(position, endPositions, board, 1, -1, "once");
        }
        if (board.getPiece(backRight)!= null && this.teamColor == ChessGame.TeamColor.BLACK){
            // Capture right black
            endPositions = move(position, endPositions, board, -1, 1, "once");
        }
        if (board.getPiece(backLeft)!= null && this.teamColor == ChessGame.TeamColor.BLACK){
            // Capture left black
            endPositions = move(position, endPositions, board, -1, -1, "once");
        }
        // White Forward
        if ((position.getRow() != 2 && position.getRow() != 7) && this.teamColor == ChessGame.TeamColor.WHITE){
            endPositions = move(position, endPositions, board, 1, 0, "pawn");
        }
        if ((position.getRow() != 7 && position.getRow() != 2) && this.teamColor == ChessGame.TeamColor.BLACK){
            // Black Forward
            endPositions = move(position, endPositions, board, -1, 0, "pawn");
        }
        return endPositions;
    }

    private List<ChessPosition> move(ChessPosition position, List<ChessPosition> endPositions, ChessBoard board, int rowMoves, int colMoves, String moveType){
        int newRow = position.getRow();
        int newCol = position.getColumn();
        ChessPosition currPos = position;
        if (moveType == "once"){
            newRow = newRow + rowMoves;
            newCol = newCol + colMoves;
            if (!isValidMove(newRow, newCol)){
                return endPositions;
            }
            ChessPosition end = new ChessPosition(newRow, newCol);
            currPos = end;
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor == this.teamColor){
                // Do nothing
            } else if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor) {
                endPositions.add(end);
            } else {
                endPositions.add(end);
            }
            return endPositions;
        } else if (moveType == "endOfBoard") {
            while (true) {
                if (!isValidMove(newRow, newCol)){
                    break;
                }
                if (currPos == position){
                    newRow = newRow + rowMoves;
                    newCol = newCol + colMoves;
                    ChessPosition end = new ChessPosition(newRow, newCol);
                    currPos = end;
                    continue;
                }
                ChessPosition end = new ChessPosition(newRow, newCol);
                currPos = end;
                if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor == this.teamColor){
                    break;
                }
                endPositions.add(end);
                newRow = newRow + rowMoves;
                newCol = newCol + colMoves;
                if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor){
                    break;
                }
            }
        } else if (moveType == "pawn"){
            newRow = newRow + rowMoves;
            newCol = newCol + colMoves;
            if (!isValidMove(newRow, newCol)){
                return endPositions;
            }
            ChessPosition end = new ChessPosition(newRow, newCol);
            currPos = end;
            if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor == this.teamColor){
                // Do nothing
            } else if (board.getPiece(currPos)!=null && board.getPiece(currPos).teamColor != this.teamColor) {
                // Also do nothing for the pawns.
            } else {
                endPositions.add(end);
            }
            return endPositions;
        }
        return endPositions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return pieceType == that.pieceType && teamColor == that.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, teamColor);
    }
    private boolean isValidMove(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
