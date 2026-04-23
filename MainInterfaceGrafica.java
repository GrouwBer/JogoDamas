

import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

/**
 * @author Douglas
 */
public final class MainInterfaceGrafica extends JFrame {

    private final int TAMANHO = 6;
    private final CasaBotao[][] tabuleiroInterface = new CasaBotao[TAMANHO][TAMANHO];

    /*
        Vazio: 0
        Brancas: 1
        Pretas: 2
        Damas: 3 (branca) ou 4 (preta)

        -> REGRAS DO JOGO

            - DEFINIR QUEM UTILIZARÁ AS PEÇAS BRANCAS (COMEÇA O JOGO)
            - OBRIGATÓRIO COMER A PEÇA
            - NÃO É PERMITIDO COMER PRA TRÁS
            - UMA PEÇA PODE COMER MÚLTIPLAS PEÇAS, EM QUALQUER
            DIREÇÃO, DESDE QUE A PRIMEIRA SEJA PARA FRENTE
            - A DAMA PODE ANDAR INFINITAS CASAS, RESPEITANDO O LIMITE DO TABULEIRO
            - A DAMA PODE COMER PRA TRÁS
            - A DAMA PODE COMER MÚLTIPLAS PEÇAS
            - A PEÇA A SER COMIDA PELA DAMA INDICA A POSIÇÃO QUE A DAMA DEVERÁ PARAR
            (POSIÇÃO SUBSEQUENTE NA DIREÇÃO DA COMIDA)
            - NA IMPOSSIBILIDADE DE EFETUAR JOGADAS, O JOGADOR TRAVADO PERDE O JOGO


            => SE EXISTIREM SOMENTE DUAS DAMAS E NÃO FOR POSSÍVEL COMER, ENTÃO EMPATE
     */
    private final Tabuleiro tabuleiroLogico;
    private int linhaOrigem = -1, colOrigem = -1;
    private int dificuldade;
    private boolean iaJogaComBrancas;
    private boolean turnoBrancas = true; // brancas começam

    public MainInterfaceGrafica(int dificuldade, boolean iaJogaComBrancas) {

        /*
            TABULEIRO DO JOGO
         */
        tabuleiroLogico = new Tabuleiro();
        this.dificuldade = dificuldade;
        this.iaJogaComBrancas = iaJogaComBrancas;

        setTitle("DISCIPLINA - IA - MINI JOGO DE DAMA");
        setSize(800, 800);
        setLayout(new GridLayout(TAMANHO, TAMANHO));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inicializarComponentes();
        sincronizarInterface();

        setVisible(true);

        if (this.iaJogaComBrancas) {
            jogadaDaIA();
        }
    }

    private void inicializarComponentes() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                tabuleiroInterface[i][j] = new CasaBotao();

                // Cores do tabuleiro
                if ((i + j) % 2 == 0) {
                    tabuleiroInterface[i][j].setBackground(new Color(235, 235, 208)); // Bege
                } else {
                    tabuleiroInterface[i][j].setBackground(new Color(119, 149, 86));  // Verde
                }

                int linha = i;
                int coluna = j;
                tabuleiroInterface[i][j].addActionListener(e -> tratarClique(linha, coluna));
                add(tabuleiroInterface[i][j]);
            }
        }

        /*
            CRIAÇÃO DA ÁRVORE

            - PARA O ESTADO DO TABULEIRO, VERIFICAR JOGADAS POSSÍVEIS;
            - PARA CADA JOGADA POSSÍVEL, CRIA UM NOVO NÓ;
            - ADICIONAMOS OS NÓS NA ÁRVORE;
            - ENTRAMOS RECURSIVAMENTE NOS NÓS FILHOS;
         */
        Node arvore = new Node();

        /*
        this.montarArvoreIA (arvore, profundidade, '1');
        ArrayList<Jogada> jogadasPossiveis = retornaJogadasPossiveis(tabuleiroLogico, '1');
        for (Jogada jogada : jogadasPossiveis) {
            Node no = new Node();
            no.setOrigin(jogada.getOrigem());
            no.setDest(jogada.getDestino());
            no.setMatrix(tabuleiroLogico.clone());
            no.setMovimento();
            no.setTurn(true);
            arvore.addChild(no);
            //this.montarArvoreIA (no, profundidade++, '2');
        }
         */
    }

    private int minimo(java.util.ArrayList<Node> nodes) {
        int min = Integer.MAX_VALUE;
        for (Node n : nodes) {
            if (n.getMinMax() < min) min = n.getMinMax();
        }
        return min;
    }

    private int maximo(java.util.ArrayList<Node> nodes) {
        int max = Integer.MIN_VALUE;
        for (Node n : nodes) {
            if (n.getMinMax() > max) max = n.getMinMax();
        }
        return max;
    }

private int aplicarHeuristicaVerificacaoGanhador(Node node) {
    Tabuleiro tab = node.getTabuleiro();

    if (tab.jogoAcabou()) {
        if (tab.isEmpate()) return 0;
        
        boolean brancasSemJogadas = tab.getJogadasPossiveis(true).isEmpty();
        boolean pretasSemJogadas = tab.getJogadasPossiveis(false).isEmpty();
        
        if (brancasSemJogadas) {
            return iaJogaComBrancas ? -1000000 : 1000000;
        } else if (pretasSemJogadas) {
            return iaJogaComBrancas ? 1000000 : -1000000;
        }
    }
    int pontuacaoBrancas = 0;
    int pontuacaoPretas = 0;
    char[][] matriz = tab.getMatriz();

    for (int linha = 0; linha < 6; linha++) {
        for (int col = 0; col < 6; col++) {
            char peca = matriz[linha][col];
            if (peca == '0') continue; // Casa vazia

            boolean ehBranca = (peca == '1' || peca == '3');
            boolean ehDama = (peca == '3' || peca == '4');
            
            int valorPeca = 0;

            if (ehDama) {
                valorPeca = 4000; 
                if ((linha == 2 || linha == 3) && (col == 2 || col == 3)) {
                    valorPeca += 150;
                } 
                else if (linha == 0 || linha == 5 || col == 0 || col == 5) {
                    valorPeca -= 50;
                }
            } else {
                valorPeca = 1000; 
                int avanco = ehBranca ? (5 - linha) : linha;
                valorPeca += (avanco * 20);
                if (avanco == 0) {
                    valorPeca += 100;
                }
                if (col == 0 || col == 5) {
                    valorPeca += 50;
                }
                else if ((col == 2 || col == 3) && avanco > 2) {
                    valorPeca += 30;
                }
            }
            if (ehBranca) {
                pontuacaoBrancas += valorPeca;
            } else {
                pontuacaoPretas += valorPeca;
            }
        }
    }

    if (iaJogaComBrancas) {
        return pontuacaoBrancas - pontuacaoPretas;
    } else {
        return pontuacaoPretas - pontuacaoBrancas;
    }
}
    private void minMaxJogoDama(Node node) {

        if (node.getChild().isEmpty()) {

            int minMax = aplicarHeuristicaVerificacaoGanhador(node);
            node.setMinMax(minMax);

        } else {

            for (int i = 0; i < node.getChild().size(); i++) {
                Node child = node.getChild().get(i);
                if (child.getMinMax() == Integer.MIN_VALUE){
                    minMaxJogoDama (child);
                }
            }

            if (node.isTurn() != iaJogaComBrancas) {
                int min = minimo(node.getChild());
                node.setMinMax(min);
            }
                else {
                int max = maximo(node.getChild());
                node.setMinMax(max);
            }
        }

    }
private void tratarClique(int linha, int col) {

        if (linhaOrigem == -1) {
            if (iaJogaComBrancas == turnoBrancas) return;

            char peca = tabuleiroLogico.getMatriz()[linha][col];
            if (peca != '0') {
                boolean ehBranca = (peca == '1' || peca == '3');
                if (ehBranca == turnoBrancas) {
                    linhaOrigem = linha;
                    colOrigem = col;
                    tabuleiroInterface[linha][col].setBackground(Color.YELLOW); 

                    java.util.List<Tabuleiro.Jogada> possiveis = tabuleiroLogico.getJogadasPossiveis(turnoBrancas);
                    boolean temMovimentoValido = false;

                    for (Tabuleiro.Jogada j : possiveis) {
                        if (j.r1 == linha && j.c1 == col) {
                            tabuleiroInterface[j.r2][j.c2].setBackground(Color.CYAN); 
                            temMovimentoValido = true;
                        }
                    }

                    if (!temMovimentoValido) {
                        cancelarSelecao();
                    }
                }
            }
        }
        else {
            if (linhaOrigem == linha && colOrigem == col) {
                cancelarSelecao();
                return;
            }

            boolean sucesso = moverPecaLogica(linhaOrigem, colOrigem, linha, col);

            if (sucesso) {
                cancelarSelecao();
                sincronizarInterface();
                turnoBrancas = !turnoBrancas;
                if (tabuleiroLogico.jogoAcabou()) {
                    JOptionPane.showMessageDialog(this, "Fim de Jogo!");
                } else if (iaJogaComBrancas == turnoBrancas) {
                    SwingUtilities.invokeLater(this::jogadaDaIA);
                }
            } else {
                cancelarSelecao();
            }
        }
    }

    private void cancelarSelecao() {
        linhaOrigem = -1;
        colOrigem = -1;
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                if ((i + j) % 2 == 0) {
                    tabuleiroInterface[i][j].setBackground(new Color(235, 235, 208));
                } else {
                    tabuleiroInterface[i][j].setBackground(new Color(119, 149, 86));
                }
            }
        }
    }

    private boolean moverPecaLogica(int r1, int c1, int r2, int c2) {
        java.util.List<Tabuleiro.Jogada> possiveis = tabuleiroLogico.getJogadasPossiveis(turnoBrancas);
        for (Tabuleiro.Jogada j : possiveis) {
            if (j.r1 == r1 && j.c1 == c1 && j.r2 == r2 && j.c2 == c2) {
                tabuleiroLogico.fazerMovimento(j);
                return true;
            }
        }
        return false;
    }

    private void construirArvore(Node node, int nivelAtual) {
        
        if (nivelAtual >= dificuldade || node.getTabuleiro().jogoAcabou()) {
            return;
        }

        java.util.List<Tabuleiro.Jogada> jogadas = node.getTabuleiro().getJogadasPossiveis(node.isTurn());
        if (jogadas.isEmpty()) return;

        for (Tabuleiro.Jogada j : jogadas) {
            Node child = new Node();
            child.setJogada(j);
            child.setTurn(!node.isTurn()); // Alterna o turno
            Tabuleiro childTab = node.getTabuleiro().clone();
            childTab.fazerMovimento(j);
            child.setTabuleiro(childTab);
            node.addChild(child);
            construirArvore(child, nivelAtual + 1);
        }
    }

    private void jogadaDaIA() {
        if (tabuleiroLogico.jogoAcabou()) return;

        Node root = new Node();
        root.setTabuleiro(tabuleiroLogico.clone());
        root.setTurn(iaJogaComBrancas);

        construirArvore(root, 0);

        if (!root.getChild().isEmpty()) {
            for (Node child : root.getChild()) {
                minMaxJogoDama(child);
            }

            Node melhorNo = null;
            int melhorValor = Integer.MIN_VALUE;
            java.util.List<Node> melhores = new java.util.ArrayList<>();

            for (Node child : root.getChild()) {
                if (child.getMinMax() > melhorValor) {
                    melhorValor = child.getMinMax();
                    melhores.clear();
                    melhores.add(child);
                } else if (child.getMinMax() == melhorValor) {
                    melhores.add(child);
                }
            }

            if (!melhores.isEmpty()) {
                melhorNo = melhores.get(new java.util.Random().nextInt(melhores.size()));
                tabuleiroLogico.fazerMovimento(melhorNo.getJogada());
                sincronizarInterface();
                turnoBrancas = !turnoBrancas;
                if (tabuleiroLogico.jogoAcabou()) {
                    JOptionPane.showMessageDialog(this, "Fim de Jogo!");
                }
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int dif = -1;
        while (dif < 1 || dif > 9) {
            System.out.print("Escolha a dificuldade (1 a 9): ");
            if (scanner.hasNextInt()) {
                dif = scanner.nextInt();
            } else {
                scanner.next();
            }
        }
        boolean iaBrancas = false;
        int escolha = 0;
        while (escolha != 1 && escolha != 2) {
            System.out.print("Quem jogara com as brancas (comeca o jogo)? (1 - Usuario, 2 - IA): ");
            if (scanner.hasNextInt()) {
                escolha = scanner.nextInt();
            } else {
                scanner.next();
            }
        }
        if (escolha == 2) {
            iaBrancas = true;
        }
        final int finalDif = dif;
        final boolean finalIaBrancas = iaBrancas;
        SwingUtilities.invokeLater(() -> new MainInterfaceGrafica(finalDif, finalIaBrancas));
    }

    public void sincronizarInterface() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = tabuleiroLogico.getMatriz()[i][j];
                tabuleiroInterface[i][j].setTipoPeca(peca);
            }
        }
    }

    private class CasaBotao extends JButton {

        private int tipoPeca = '0';

        public void setTipoPeca(char tipo) {
            this.tipoPeca = tipo;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int margem = 10;
            // Brancas
            if (tipoPeca == '1' || tipoPeca == '3') {
                g2.setColor(Color.WHITE);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                g2.setColor(Color.BLACK);
                g2.drawOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
                // Pretas
            } else if (tipoPeca == '2' || tipoPeca == '4') {
                g2.setColor(Color.BLACK);
                g2.fillOval(margem, margem, getWidth() - 2 * margem, getHeight() - 2 * margem);
            }

            // Representação de Dama (uma borda dourada)
            if (tipoPeca == '3' || tipoPeca == '4') {
                g2.setColor(Color.YELLOW);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(margem + 5, margem + 5, getWidth() - 2 * margem - 10, getHeight() - 2 * margem - 10);
            }
        }
    }
}
