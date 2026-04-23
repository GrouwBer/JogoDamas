

import java.util.ArrayList;
import java.util.List;

/**
 * @author Douglas
 */
public class Tabuleiro implements Cloneable {

    private char[][] matriz;
    private final int TAMANHO = 6;

    public Tabuleiro() {
        this.matriz = new char[TAMANHO][TAMANHO];
        inicializar();
    }

    private void inicializar() {
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                matriz[i][j] = '0';
                if ((i + j) % 2 != 0) {
                    if (i < 2) {
                        matriz[i][j] = '2'; // Pretas
                    } else if (i > 3) {
                        matriz[i][j] = '1'; // Brancas
                    }
                }
            }
        }
    }

    @Override
    public Tabuleiro clone() {
        try {
            Tabuleiro clone = (Tabuleiro) super.clone();
            clone.matriz = new char[TAMANHO][];
            for (int i = 0; i < TAMANHO; i++) {
                clone.matriz[i] = this.matriz[i].clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public static class Jogada {
        int r1, c1;
        int r2, c2;
        List<int[]> capturas;

        public Jogada(int r1, int c1, int r2, int c2) {
            this.r1 = r1; this.c1 = c1;
            this.r2 = r2; this.c2 = c2;
            this.capturas = new ArrayList<>();
        }
    }

    public List<Jogada> getJogadasPossiveis(boolean turnoBrancas) {
        List<Jogada> jogadas = new ArrayList<>();
        int maxCapturas = 0;

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = matriz[i][j];
                if (peca == '0') continue;

                boolean isBranca = (peca == '1' || peca == '3');
                if (isBranca != turnoBrancas) continue;

                boolean isDama = (peca == '3' || peca == '4');

                List<Jogada> jogadasDestaPeca = new ArrayList<>();
                buscarCapturas(i, j, i, j, isDama, isBranca, new ArrayList<>(), jogadasDestaPeca, new boolean[TAMANHO][TAMANHO]);

                for (Jogada jogada : jogadasDestaPeca) {
                    if (jogada.capturas.size() > maxCapturas) {
                        maxCapturas = jogada.capturas.size();
                        jogadas.clear();
                        jogadas.add(jogada);
                    } else if (jogada.capturas.size() == maxCapturas && maxCapturas > 0) {
                        jogadas.add(jogada);
                    }
                }
            }
        }

        if (maxCapturas > 0) {
            return jogadas;
        }

        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char peca = matriz[i][j];
                if (peca == '0') continue;

                boolean isBranca = (peca == '1' || peca == '3');
                if (isBranca != turnoBrancas) continue;

                boolean isDama = (peca == '3' || peca == '4');

                int[] dirsR = {-1, -1, 1, 1};
                int[] dirsC = {-1, 1, -1, 1};

                for (int d = 0; d < 4; d++) {
                    int dr = dirsR[d];
                    int dc = dirsC[d];

                    if (!isDama) {
                        if (isBranca && dr > 0) continue;
                        if (!isBranca && dr < 0) continue;

                        int nr = i + dr;
                        int nc = j + dc;
                        if (nr >= 0 && nr < TAMANHO && nc >= 0 && nc < TAMANHO && matriz[nr][nc] == '0') {
                            jogadas.add(new Jogada(i, j, nr, nc));
                        }
                    } else {
                        // Dama movimento longo
                        for (int k = 1; k < TAMANHO; k++) {
                            int nr = i + dr * k;
                            int nc = j + dc * k;
                            if (nr >= 0 && nr < TAMANHO && nc >= 0 && nc < TAMANHO) {
                                if (matriz[nr][nc] == '0') {
                                    jogadas.add(new Jogada(i, j, nr, nc));
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return jogadas;
    }

    private void buscarCapturas(int rOrig, int cOrig, int rAtual, int cAtual, boolean isDama, boolean isBranca, List<int[]> capturadasAteAqui, List<Jogada> jogadasPossiveis, boolean[][] capturadasMatrix) {
        int[] dirsR = {-1, -1, 1, 1};
        int[] dirsC = {-1, 1, -1, 1};
        boolean encontrouCaptura = false;

        for (int d = 0; d < 4; d++) {
            int dr = dirsR[d];
            int dc = dirsC[d];

            if (!isDama && capturadasAteAqui.isEmpty()) {
                if (isBranca && dr > 0) continue;
                if (!isBranca && dr < 0) continue;
            }

            if (!isDama) {
                int rMeio = rAtual + dr;
                int cMeio = cAtual + dc;
                int rDest = rAtual + 2 * dr;
                int cDest = cAtual + 2 * dc;

                if (rDest >= 0 && rDest < TAMANHO && cDest >= 0 && cDest < TAMANHO) {
                    char pecaMeio = matriz[rMeio][cMeio];
                    if (pecaMeio != '0' && isInimigo(isBranca, pecaMeio) && !capturadasMatrix[rMeio][cMeio]) {
                        if (matriz[rDest][cDest] == '0' || (rDest == rOrig && cDest == cOrig)) {
                            encontrouCaptura = true;
                            capturadasMatrix[rMeio][cMeio] = true;
                            capturadasAteAqui.add(new int[]{rMeio, cMeio});

                            buscarCapturas(rOrig, cOrig, rDest, cDest, isDama, isBranca, capturadasAteAqui, jogadasPossiveis, capturadasMatrix);

                            capturadasAteAqui.remove(capturadasAteAqui.size() - 1);
                            capturadasMatrix[rMeio][cMeio] = false;
                        }
                    }
                }
            } else {
                for (int dist = 1; dist < TAMANHO; dist++) {
                    int rMeio = rAtual + dr * dist;
                    int cMeio = cAtual + dc * dist;

                    if (rMeio < 0 || rMeio >= TAMANHO || cMeio < 0 || cMeio >= TAMANHO) break;

                    char pMeio = matriz[rMeio][cMeio];
                    if (pMeio == '0' || (rMeio == rOrig && cMeio == cOrig)) continue;
                    if (!isInimigo(isBranca, pMeio) || capturadasMatrix[rMeio][cMeio]) break;

                    int rDest = rMeio + dr;
                    int cDest = cMeio + dc;
                    if (rDest >= 0 && rDest < TAMANHO && cDest >= 0 && cDest < TAMANHO) {
                        if (matriz[rDest][cDest] == '0' || (rDest == rOrig && cDest == cOrig)) {
                            encontrouCaptura = true;
                            capturadasMatrix[rMeio][cMeio] = true;
                            capturadasAteAqui.add(new int[]{rMeio, cMeio});

                            buscarCapturas(rOrig, cOrig, rDest, cDest, isDama, isBranca, capturadasAteAqui, jogadasPossiveis, capturadasMatrix);

                            capturadasAteAqui.remove(capturadasAteAqui.size() - 1);
                            capturadasMatrix[rMeio][cMeio] = false;
                        }
                    }
                    break;
                }
            }
        }

        if (!encontrouCaptura && !capturadasAteAqui.isEmpty()) {
            Jogada j = new Jogada(rOrig, cOrig, rAtual, cAtual);
            for (int[] cap : capturadasAteAqui) {
                j.capturas.add(new int[]{cap[0], cap[1]});
            }
            jogadasPossiveis.add(j);
        }
    }

    private boolean isInimigo(boolean isBranca, char peca) {
        if (peca == '0') return false;
        boolean pBranca = (peca == '1' || peca == '3');
        return isBranca != pBranca;
    }

    public void fazerMovimento(Jogada j) {
        char peca = matriz[j.r1][j.c1];
        matriz[j.r1][j.c1] = '0';
        for (int[] cap : j.capturas) {
            matriz[cap[0]][cap[1]] = '0';
        }
        matriz[j.r2][j.c2] = peca;

        if (peca == '1' && j.r2 == 0) {
            matriz[j.r2][j.c2] = '3';
        } else if (peca == '2' && j.r2 == TAMANHO - 1) {
            matriz[j.r2][j.c2] = '4';
        }
    }

    public boolean jogoAcabou() {
        if (getJogadasPossiveis(true).isEmpty()) return true;
        if (getJogadasPossiveis(false).isEmpty()) return true;
        return isEmpate();
    }

    public boolean isEmpate() {
        int countBrancas = 0, countPretas = 0, countDamasBrancas = 0, countDamasPretas = 0;
        for (int i = 0; i < TAMANHO; i++) {
            for (int j = 0; j < TAMANHO; j++) {
                char p = matriz[i][j];
                if (p == '1') countBrancas++;
                if (p == '2') countPretas++;
                if (p == '3') { countBrancas++; countDamasBrancas++; }
                if (p == '4') { countPretas++; countDamasPretas++; }
            }
        }
        if (countBrancas == 1 && countPretas == 1 && countDamasBrancas == 1 && countDamasPretas == 1) {
            boolean temCaptura = false;
            List<Jogada> jb = getJogadasPossiveis(true);
            for (Jogada j : jb) if (!j.capturas.isEmpty()) temCaptura = true;
            List<Jogada> jp = getJogadasPossiveis(false);
            for (Jogada j : jp) if (!j.capturas.isEmpty()) temCaptura = true;
            return !temCaptura;
        }
        return false;
    }

    public char[][] getMatriz() {
        return matriz;
    }

    public void setMatriz(char[][] matriz) {
        this.matriz = matriz;
    }
}
