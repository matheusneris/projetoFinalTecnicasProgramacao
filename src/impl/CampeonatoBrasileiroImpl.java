package impl;

import dominio.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CampeonatoBrasileiroImpl {

    private DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Map<Integer, List<Jogo>> brasileirao;
    private List<Jogo> jogos;
    private Predicate<Jogo> filtro;

    public CampeonatoBrasileiroImpl(Path arquivo, Predicate<Jogo> filtro) throws IOException {
        this.jogos = lerArquivo(arquivo);
        this.filtro = filtro;
        this.brasileirao = jogos.stream()
                .filter(filtro) //filtrar por ano
                .collect(Collectors.groupingBy(
                        Jogo::rodada,
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    public List<Jogo> lerArquivo(Path file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file.toFile()));
        String partida[] = bufferedReader.readLine().split(";");
        List<Jogo> jogos = new ArrayList<>();

        while (true){
            LocalDate dataJogo = LocalDate.parse(partida[1], formatadorData);
            Jogo jogo = new Jogo(Integer.parseInt(partida[0]),
                    new DataDoJogo(dataJogo,
                            LocalTime.parse(partida[2].replace('h',':')),
                            dataJogo.getDayOfWeek()),
                    new Time(partida[4]),
                    new Time(partida[5]),
                    new Time(partida[6]),
                    partida[7],
                    Integer.parseInt(partida[8]),
                    Integer.parseInt(partida[9]),
                    partida[10],
                    partida[11],
                    partida[12]
                    );
            jogos.add(jogo);
            try{
                partida = bufferedReader.readLine().split(";");
            }catch (NullPointerException e){
                break;
            }
        }
        return jogos;
    }

    public IntSummaryStatistics getEstatisticasPorJogo() {
        IntStream jogosFiltrados = this.jogos.stream().filter(filtro).mapToInt(jogo -> jogo.visitantePlacar() + jogo.mandantePlacar());
        return jogosFiltrados.summaryStatistics();
    }

    public Long getTotalVitoriasEmCasa() {
        return this.jogos.stream().filter(filtro)
                .filter(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar())
                .count();
    }

    public Long getTotalVitoriasForaDeCasa() {
        return this.jogos.stream().filter(filtro)
                .filter(jogo -> jogo.visitantePlacar() > jogo.mandantePlacar())
                .count();
    }

    public Long getTotalEmpates() {
        return this.jogos.stream().filter(filtro)
                .filter(jogo -> jogo.mandantePlacar() == jogo.visitantePlacar())
                .count();
    }

    public Long getTotalJogosComMenosDe3Gols() {
        return this.jogos.stream().filter(filtro)
                .filter(jogo -> jogo.visitantePlacar()+jogo.mandantePlacar() < 3)
                .count();
    }

    public Long getTotalJogosCom3OuMaisGols() {
        return this.jogos.stream().filter(filtro)
                .filter(jogo -> jogo.visitantePlacar()+jogo.mandantePlacar() >= 3)
                .count();
    }
    public Map<Resultado, Long> getPlacarMaisRepetido() {
        List<Resultado> resultados = resultadosToList();
        Map<Resultado, Long> repeticaoDePlacares = resultadosToMap(resultados);

        Iterator iterator= repeticaoDePlacares.entrySet().iterator();
        Resultado placarMaisRepetido = new Resultado(0,0);
        Map.Entry<Resultado, Long> repeticaoDePlacaresEntry;
        while (iterator.hasNext()){

            repeticaoDePlacaresEntry = (Map.Entry) iterator.next();
            try{
                if(repeticaoDePlacaresEntry.getValue() > repeticaoDePlacares.get(placarMaisRepetido)){
                    placarMaisRepetido = repeticaoDePlacaresEntry.getKey();
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        Map<Resultado, Long> retorno = new HashMap<>();
        retorno.put(placarMaisRepetido, repeticaoDePlacares.get(placarMaisRepetido));
        return retorno;
    }

    private Map<Resultado, Long> resultadosToMap(List<Resultado> resultados) {
        Map<Resultado, Long> repeticaoDePlacares = resultados.stream().collect(Collectors
               .toMap(resultado -> resultado, resultado -> resultado.getUm(),
                       (resultado1, resultado2) -> resultado1 + resultado2));
        return repeticaoDePlacares;
    }

    private List<Resultado> resultadosToList() {
        List<Resultado> resultados = this.jogos.stream().filter(filtro)
                .map(jogo -> new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()))
                .collect(Collectors.toList());
        return resultados;
    }

    public Map<Resultado, Long> getPlacarMenosRepetido() {
        List<Resultado> resultados = resultadosToList();
        Map<Resultado, Long> repeticaoDePlacares = resultadosToMap(resultados);

        Iterator iterator= repeticaoDePlacares.entrySet().iterator();
        Resultado placarMenosRepetido = new Resultado(0,0);
        Map.Entry<Resultado, Long> repeticaoDePlacaresEntry;
        while (iterator.hasNext()){

            repeticaoDePlacaresEntry = (Map.Entry) iterator.next();
            try{
                if(repeticaoDePlacaresEntry.getValue() < repeticaoDePlacares.get(placarMenosRepetido)){
                    placarMenosRepetido = repeticaoDePlacaresEntry.getKey();
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        Map<Resultado, Long> retorno = new HashMap<>();
        retorno.put(placarMenosRepetido, repeticaoDePlacares.get(placarMenosRepetido));
        return retorno;
    }

    private List<Time> getTodosOsTimes() {
        List<Time> times = this.jogos.stream().filter(filtro)
                .map(jogo -> jogo.mandante()).distinct()
                .collect(Collectors.toList());

        return times;
    }

    private  List<Jogo> getTodosOsJogosPorTimeComoMandantes(Time time) {
        List<Jogo> jogosTime = this.jogos.stream().filter(filtro).filter(jogo -> jogo.mandante() == time).toList();

        return jogosTime;
    }

    private List<Jogo> getTodosOsJogosPorTimeComoVisitante(Time time) {
        List<Jogo> jogosTime = this.jogos.stream().filter(filtro).filter(jogo -> jogo.visitante() == time).toList();

        return jogosTime;

    }

    public Long getPontuacaoTotalTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante) {
        Long pontosVitoria = getPontosVitoriasTime(jogosMandante, jogosVisitante);
        Long pontosEmpatesTime = getPontosEmpatesTime(jogosMandante, jogosVisitante);

        return pontosVitoria + pontosEmpatesTime;
    }

    private Long getPontosEmpatesTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante) {
        Long pontosEmpateMandante = jogosMandante.stream().filter(jogo -> jogo.mandantePlacar() == jogo.visitantePlacar()).count();
        Long pontosEmpateVisitante = jogosVisitante.stream().filter(jogo -> jogo.mandantePlacar() == jogo.visitantePlacar()).count();

        return pontosEmpateMandante + pontosEmpateVisitante;
    }

    private Long getPontosVitoriasTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante) {
        Long pontosVitoriaMandante = jogosMandante.stream().filter(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar()).count() * 3;
        Long pontosVitoriaVisitante = jogosVisitante.stream().filter(jogo -> jogo.visitantePlacar() > jogo.mandantePlacar()).count() * 3;

        return pontosVitoriaMandante + pontosVitoriaVisitante;
    }

    private Long getDerrotasTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante){
        Long derrotasMandante = jogosMandante.stream().filter(jogo -> jogo.mandantePlacar() < jogo.visitantePlacar()).count();
        Long derrotasVisitante = jogosVisitante.stream().filter(jogo -> jogo.visitantePlacar() < jogo.mandantePlacar()).count();

        return derrotasMandante + derrotasVisitante;
    }

    public Set<PosicaoTabela> getTabela() {
        List<Time> times = getTodosOsTimes();
        List<PosicaoTabela> posicaoTabela = new ArrayList<>();

        for (Time time: times) {
            List<Jogo> jogosMandante = getTodosOsJogosPorTimeComoMandantes(time);
            List<Jogo> jogosVisitante = getTodosOsJogosPorTimeComoVisitante(time);
            Long golsFeitos = getTotalDeGolsFeitosPorTime(jogosMandante, jogosVisitante);
            Long golsSofridos = getTotalDeGolsSofridosPorTime(jogosMandante, jogosVisitante);

            posicaoTabela.add(new PosicaoTabela(
               time,
               getPontuacaoTotalTime(jogosMandante, jogosVisitante),
                    getPontosVitoriasTime(jogosMandante, jogosVisitante)/3,
                    getDerrotasTime(jogosMandante, jogosVisitante),
                    getPontosEmpatesTime(jogosMandante,jogosVisitante),
                    golsFeitos,
                    golsSofridos,
                    golsFeitos - golsSofridos,
                    getTotalJogosPorTime(jogosMandante, jogosVisitante)
            ));
        }
        Set<PosicaoTabela> posicaoTabelaSet = new HashSet<>(posicaoTabela);

        return posicaoTabelaSet;
    }

    private Long getTotalJogosPorTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante){
        Long totalJogosMandante = jogosMandante.stream().count();
        Long totalJogosVisitante = jogosVisitante.stream().count();

        return totalJogosMandante + totalJogosVisitante;
    }

    private Long getTotalDeGolsFeitosPorTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante) {
        int totalGolsMandante = jogosMandante.stream()
                .map(jogo -> jogo.mandantePlacar())
                .reduce(0, (subtotal, proximo) -> subtotal + proximo);

        int totalGolsVisitante = jogosVisitante.stream()
                .map(jogo -> jogo.visitantePlacar())
                .reduce(0, (subtotal, proximo) -> subtotal + proximo);

        return Long.parseLong(Integer.toString(totalGolsMandante + totalGolsVisitante));
    }

    private Long getTotalDeGolsSofridosPorTime(List<Jogo> jogosMandante, List<Jogo> jogosVisitante){
        int totalGolsMandante = jogosMandante.stream()
                .map(jogo -> jogo.visitantePlacar())
                .reduce(0, (subtotal, proximo) -> subtotal + proximo);

        int totalGolsVisitante = jogosVisitante.stream()
                .map(jogo -> jogo.mandantePlacar())
                .reduce(0, (subtotal, proximo) -> subtotal + proximo);

        return Long.parseLong(Integer.toString(totalGolsMandante + totalGolsVisitante));
    }

}