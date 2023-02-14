import dominio.PosicaoTabela;
import dominio.Resultado;
import impl.CampeonatoBrasileiroImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.IntSummaryStatistics;
import java.util.Map;
import java.util.Set;

public class Aplicacao {

    public static void main(String[] args) throws IOException {

        // obter caminho do arquivo
        Path file = Path.of("campeonato-brasileiro.csv");

        // obter a implementação: (ponto extra - abstrair para interface)
        CampeonatoBrasileiroImpl brasileirao =
                new CampeonatoBrasileiroImpl(file,
                        (jogo) -> jogo.data().data().getYear() == 2020);

        // imprimir estatisticas
        imprimirEstatisticas(brasileirao);

        // imprimir tabela ordenada
        imprimirTabela(brasileirao.getTabela());

    }

    private static void imprimirEstatisticas(CampeonatoBrasileiroImpl brasileirao) {
        IntSummaryStatistics estatisticas = brasileirao.getEstatisticasPorJogo();

        System.out.println("Estatisticas (Total de gols) - " + estatisticas.getSum());
        System.out.println("Estatisticas (Total de jogos) - " + estatisticas.getCount());
        System.out.println("Estatisticas (Media de gols) - " + estatisticas.getAverage());

        Map<Resultado, Long> placarMaisRepetido = brasileirao.getPlacarMaisRepetido();

        for (Map.Entry<Resultado, Long> resultado : placarMaisRepetido.entrySet()) {
            System.out.println("Estatisticas (Placar mais repetido) - "
                    + resultado.getKey() + " (" +resultado.getValue() + " jogo(s))");
        }

        Map<Resultado, Long> placarMenosRepetido = brasileirao.getPlacarMenosRepetido();

        for (Map.Entry<Resultado, Long> resultado : placarMenosRepetido.entrySet()) {
            System.out.println("Estatisticas (Placar menos repetido) - "
                    + resultado.getKey() + " (" +resultado.getValue() + " jogo(s))");
        }

        Long jogosCom3OuMaisGols = brasileirao.getTotalJogosCom3OuMaisGols();
        Long jogosComMenosDe3Gols = brasileirao.getTotalJogosComMenosDe3Gols();

        System.out.println("Estatisticas (3 ou mais gols) - " + jogosCom3OuMaisGols);
        System.out.println("Estatisticas (-3 gols) - " + jogosComMenosDe3Gols);

        Long totalVitoriasEmCasa = brasileirao.getTotalVitoriasEmCasa();
        Long vitoriasForaDeCasa = brasileirao.getTotalVitoriasForaDeCasa();
        Long empates = brasileirao.getTotalEmpates();

        System.out.println("Estatisticas (Vitorias Fora de casa) - " + vitoriasForaDeCasa);
        System.out.println("Estatisticas (Vitorias Em casa) - " + totalVitoriasEmCasa);
        System.out.println("Estatisticas (Empates) - " + empates);
    }

    public static void imprimirTabela(Set<PosicaoTabela> posicoes) {
        System.out.println();
        System.out.println("## TABELA CAMPEONADO BRASILEIRO: ##");
        int colocacao = 1;
        for (PosicaoTabela posicao : posicoes) {
            System.out.println(colocacao +". " + posicao);
            colocacao++;
        }

        System.out.println();
        System.out.println();
    }
}