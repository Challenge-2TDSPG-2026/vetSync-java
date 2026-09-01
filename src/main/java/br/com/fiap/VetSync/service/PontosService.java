package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.AdminRepository;
import br.com.fiap.VetSync.repository.LancamentoPontosRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PontosService {

    private final LancamentoPontosRepository lancamentoPontosRepository;
    private final AdminRepository adminRepository;


    public LancamentoPontos lancarPendente(EventoSaude evento) {
        int pontos = evento.getTipoEvento() != null && evento.getTipoEvento().getNrPontos() != null
                ? evento.getTipoEvento().getNrPontos() : 0;
        LancamentoPontos lancamento = LancamentoPontos.builder()
                .evento(evento)
                .nrPontos(pontos)
                .dsStatus(StatusLancamentoPontos.PENDENTE)
                .build();
        return lancamentoPontosRepository.save(lancamento);
    }

    public LancamentoPontos buscarPorId(Long id) {
        return lancamentoPontosRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lançamento de pontos não encontrado com id: " + id));
    }

    public List<LancamentoPontos> listarPendentes() {
        return lancamentoPontosRepository.findByDsStatusOrderByDtLancamentoAsc(StatusLancamentoPontos.PENDENTE);
    }

    public List<LancamentoPontos> listarParaTutor(String email) {
        return lancamentoPontosRepository.findByEvento_Pet_Tutor_DsEmailOrderByDtLancamentoDesc(email);
    }

    /** Admin libera um lançamento pendente, creditando os pontos no saldo do tutor. */
    public LancamentoPontos liberar(Long idLancamento, Long idAdmin) {
        LancamentoPontos lancamento = buscarPorId(idLancamento);
        if (lancamento.getDsStatus() != StatusLancamentoPontos.PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esse lançamento já está " + lancamento.getDsStatus());
        }
        Admin admin = adminRepository.findById(idAdmin).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin não encontrado"));

        lancamento.setAdminValidador(admin);
        lancamento.setDsStatus(StatusLancamentoPontos.LIBERADO);
        return lancamentoPontosRepository.save(lancamento);
    }

    /** Soma só os pontos já liberados pelo admin — é isso que compõe o saldo do tutor. */
    public int calcularPontosLiberados(Long idTutor) {
        return lancamentoPontosRepository.findByEvento_Pet_Tutor_IdTutorAndDsStatus(idTutor, StatusLancamentoPontos.LIBERADO)
                .stream()
                .mapToInt(LancamentoPontos::getNrPontos)
                .sum();
    }
}