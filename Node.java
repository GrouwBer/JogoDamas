import java.util.ArrayList;

/**
 * @author Douglas
 */
public class Node {

    private Tabuleiro.Jogada jogada;
    private boolean turn; // TRUE - white; FALSE - black;
    private Tabuleiro tabuleiro;
    private int minMax;
    private ArrayList<Node> children;

    public Node() {
        this.children = new ArrayList<>();
        this.minMax = Integer.MIN_VALUE;
    }

    public int getMinMax() {
        return minMax;
    }

    public void setMinMax(int minMax) {
        this.minMax = minMax;
    }

    public ArrayList<Node> getChild() {
        return this.children;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public Tabuleiro.Jogada getJogada() {
        return jogada;
    }

    public void setJogada(Tabuleiro.Jogada jogada) {
        this.jogada = jogada;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public void setTabuleiro(Tabuleiro tabuleiro) {
        this.tabuleiro = tabuleiro;
    }
}
