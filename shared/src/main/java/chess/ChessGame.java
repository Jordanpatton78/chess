package chess;

import java.lang.reflect.Array;
import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    TeamColor teamTurn;
    ChessBoard board;
    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public HashSet<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = this.board.getPiece(startPosition);
        TeamColor teamColor = piece.getTeamColor();
        HashSet<ChessMove> moves = piece.pieceMoves(this.board, startPosition);
        HashSet<ChessMove> new_moves = new HashSet<ChessMove>();
        new_moves = simulate_moves(moves, teamColor);
        if (this.board.getPiece(startPosition) == null){
            return null;
        }
        return new_moves;
    }

    public HashSet<ChessMove> simulate_moves(HashSet<ChessMove> moves, TeamColor teamColor){
        HashSet<ChessMove> new_moves = new HashSet<ChessMove>();
        for (ChessMove move : moves){
            // Make simulated move
            ChessPosition start = move.getStartPosition();
            ChessPosition end = move.getEndPosition();
            ChessPiece piece_to_remove = null;
            if (board.getPiece(end) != null){
                piece_to_remove = this.board.getPiece(end);
                this.board.removePiece(end);
            }
            ChessPiece this_piece = this.board.getPiece(start);
            this.board.addPiece(end, this_piece);
            this.board.removePiece(start);
            if (!isInCheck(teamColor)){
                new_moves.add(move);
            }
            // Undo simulated move
            this.board.removePiece(end);
            this.board.addPiece(start, this_piece);
            if (piece_to_remove != null){
                this.board.addPiece(end, piece_to_remove);
            }
        }
        return new_moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        HashSet<ChessMove> validMoves = validMoves(start);
        try {
            if (validMoves.contains(move)) {
                if (board.getPiece(end) != null){
                    this.board.removePiece(end);
                }
                ChessPiece piece = this.board.getPiece(start);
                this.board.addPiece(end, piece);
                this.board.removePiece(start);
                if (move.promotionPiece != null){
                    ChessPiece promotedPiece = new ChessPiece(piece.getTeamColor(), move.promotionPiece);
                    this.board.removePiece(end);
                    this.board.addPiece(end, promotedPiece);
                }
            } else {
                throw new InvalidMoveException("Invalid move: " + move);
            }
        } catch (InvalidMoveException e){
            System.out.println(e.getMessage());
        }
    }



    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // Find King Position
        ChessPosition king_position = getKingPosition(board, teamColor);
        // Find pieces that will reach King's position
        for (ChessPosition key : board.pieces.keySet()){
            ChessPiece piece = board.pieces.get(key);
            if (piece.getTeamColor() != teamColor){
                HashSet<ChessMove> moves = piece.pieceMoves(board, key);
                for (ChessMove move : moves){
                    ChessPosition endPosition = move.getEndPosition();
                    if (endPosition.equals(king_position)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        // Find king info
        ChessPosition king_position = getKingPosition(board, teamColor);
        ChessPiece king = board.pieces.get(king_position);
        HashSet<ChessMove> king_moves = king.pieceMoves(board, king_position);
        HashSet<ChessMove> new_king_moves = simulate_moves(king_moves, teamColor);
        return (isInCheck(teamColor) && new_king_moves.isEmpty());
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // Find king info
        ChessPosition king_position = getKingPosition(board, teamColor);
        ChessPiece king = board.pieces.get(king_position);
        HashSet<ChessMove> king_moves = king.pieceMoves(board, king_position);
        HashSet<ChessMove> new_king_moves = simulate_moves(king_moves, teamColor);
        return (!isInCheck(teamColor) && new_king_moves.isEmpty());
    }

    public ChessPosition getKingPosition(ChessBoard board, TeamColor teamColor){
        ChessPosition king_position = null;
        for (ChessPosition key : board.pieces.keySet()){
            ChessPiece piece = board.pieces.get(key);
            if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor){
                king_position = key;
            }
        }
        return king_position;
    }

   /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
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
