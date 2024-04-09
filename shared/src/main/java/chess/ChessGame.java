package chess;

import java.util.*;

/**
 * Manages a chess game, making moves on a chessboard.
 * Note: You can add to this class, but you may not alter the signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn = TeamColor.WHITE;
    private ChessBoard board;

    public ChessGame() {
    }

    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the two possible teams in a chess game.
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public HashSet<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = this.board.getPiece(startPosition);
        TeamColor teamColor = piece.getTeamColor();
        HashSet<ChessMove> moves = piece.pieceMoves(this.board, startPosition);
        return simulateMoves(moves, teamColor);
    }

    private HashSet<ChessMove> simulateMoves(HashSet<ChessMove> moves, TeamColor teamColor) {
        HashSet<ChessMove> newMoves = new HashSet<>();

        for (ChessMove move : moves) {
            simulateMove(move, teamColor, newMoves);
        }

        return newMoves;
    }

    private void simulateMove(ChessMove move, TeamColor teamColor, HashSet<ChessMove> newMoves) {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece pieceToRemove = null;

        if (board.getPiece(end) != null) {
            pieceToRemove = this.board.getPiece(end);
            this.board.removePiece(end);
        }

        ChessPiece thisPiece = this.board.getPiece(start);
        this.board.addPiece(end, thisPiece);
        this.board.removePiece(start);

        if (!isInCheck(teamColor)) {
            newMoves.add(move);
        }

        // Undo simulated move
        this.board.removePiece(end);
        this.board.addPiece(start, thisPiece);

        if (pieceToRemove != null) {
            this.board.addPiece(end, pieceToRemove);
        }
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        HashSet<ChessMove> validMoves = validMoves(start);

        try {
            if (validMoves.contains(move)) {
                performValidMove(move, start, end);
            } else {
                throw new InvalidMoveException("Invalid move: " + move);
            }
        } catch (InvalidMoveException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    private void performValidMove(ChessMove move, ChessPosition start, ChessPosition end) throws InvalidMoveException {
        if (board.getPiece(end) != null) {
            this.board.removePiece(end);
        }

        ChessPiece piece = this.board.getPiece(start);

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Wrong team's turn.");
        }

        this.board.addPiece(end, piece);
        this.board.removePiece(start);

        if (move.promotionPiece != null) {
            ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), move.promotionPiece);
            this.board.removePiece(end);
            this.board.addPiece(end, promotedPiece);
        }

        switchTeamTurn();
    }

    private void switchTeamTurn() {
        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(board, teamColor);

//        for (ChessPosition key : board.pieces.keySet()) {
//            ChessPiece piece = board.pieces.get(key);
//
//            if (piece.getTeamColor() != teamColor) {
//                HashSet<ChessMove> moves = piece.pieceMoves(board, key);
//
//                for (ChessMove move : moves) {
//                    ChessPosition endPosition = move.getEndPosition();
//
//                    if (endPosition.equals(kingPosition)) {
//                        return true;
//                    }
//                }
//            }
//        }

        for (int i=1; i<=board.squares.length; i++){
            for (int j=1; j<=board.squares[i].length; j++){
                ChessPosition pos = new ChessPosition(i,j);
                ChessPiece piece = board.getPiece(pos);
                if (piece.getTeamColor() != teamColor) {
                    HashSet<ChessMove> moves = piece.pieceMoves(board, pos);

                    for (ChessMove move : moves) {
                        ChessPosition endPosition = move.getEndPosition();

                        if (endPosition.equals(kingPosition)) {
                            return true;
                        }
                    }
            }
            }
        }

        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(board, teamColor);
        ChessPiece king = board.getPiece(kingPosition);
        HashSet<ChessMove> kingMoves = king.pieceMoves(board, kingPosition);
        HashSet<ChessMove> newKingMoves = simulateMoves(kingMoves, teamColor);

        return isInCheck(teamColor) && newKingMoves.isEmpty();
    }

    public boolean isInStalemate(TeamColor teamColor) {
        ChessPosition kingPosition = getKingPosition(board, teamColor);
//        ChessPiece king = board.pieces.get(kingPosition);
        ChessPiece king = board.getPiece(kingPosition);
        HashSet<ChessMove> kingMoves = king.pieceMoves(board, kingPosition);
        HashSet<ChessMove> newKingMoves = simulateMoves(kingMoves, teamColor);

        return !isInCheck(teamColor) && newKingMoves.isEmpty();
    }

    public ChessPosition getKingPosition(ChessBoard board, TeamColor teamColor) {
//        for (ChessPosition key : board.pieces.keySet()) {
//            ChessPiece piece = board.pieces.get(key);
//
//            if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
//                return key;
//            }
//        }

        for (int i=1; i<=board.squares.length; i++){
            for (int j=1; j<=board.squares[i].length; j++){
                ChessPosition pos = new ChessPosition(i,j);
                ChessPiece piece = board.getPiece(pos);

                if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }

        return null;
    }

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}
