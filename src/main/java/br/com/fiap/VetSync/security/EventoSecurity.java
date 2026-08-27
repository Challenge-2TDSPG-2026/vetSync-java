package br.com.fiap.VetSync.security;

import br.com.fiap.VetSync.entity.StatusEvento;
import br.com.fiap.VetSync.repository.EventoSaudeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("eventoSecurity")
@RequiredArgsConstructor
public class EventoSecurity {

    private final EventoSaudeRepository eventoSaudeRepository;

    public boolean isVeterinarioResponsavel(Long idEvento, Authentication authentication) {
        if (authentication == null || idEvento == null) return false;
        return eventoSaudeRepository.findById(idEvento)
                .map(e -> e.getVeterinario() != null
                        && e.getVeterinario().getDsEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    public boolean isTutorDoPet(Long idEvento, Authentication authentication) {
        if (authentication == null || idEvento == null) return false;
        return eventoSaudeRepository.findById(idEvento)
                .map(e -> e.getPet() != null && e.getPet().getTutor() != null
                        && e.getPet().getTutor().getDsEmail().equalsIgnoreCase(authentication.getName()))
                .orElse(false);
    }

    /** Para ler o evento (GET) ou cancelar: qualquer um dos dois lados envolvidos. */
    public boolean isRelacionado(Long idEvento, Authentication authentication) {
        return isVeterinarioResponsavel(idEvento, authentication) || isTutorDoPet(idEvento, authentication);
    }

    /** Deletar (hard delete): veterinário sempre pode; tutor só se
     * ainda estiver SOLICITADO (arrependimento antes do vet responder). */
    public boolean canDelete(Long idEvento, Authentication authentication) {
        if (authentication == null || idEvento == null) return false;
        return eventoSaudeRepository.findById(idEvento).map(e -> {
            boolean isVet = e.getVeterinario() != null
                    && e.getVeterinario().getDsEmail().equalsIgnoreCase(authentication.getName());
            boolean isTutorPendente = e.getPet() != null && e.getPet().getTutor() != null
                    && e.getPet().getTutor().getDsEmail().equalsIgnoreCase(authentication.getName())
                    && e.getDsStatus() == StatusEvento.SOLICITADO;
            return isVet || isTutorPendente;
        }).orElse(false);
    }
}