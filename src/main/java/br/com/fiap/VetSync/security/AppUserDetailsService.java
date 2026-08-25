package br.com.fiap.VetSync.security;

import br.com.fiap.VetSync.repository.TutorRepository;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * Autentica tanto TUTOR quanto VETERINARIO a partir do mesmo e-mail de
 * login. Cada perfil tem sua própria tabela e senha (TB_TUTOR /
 * TB_VETERINARIO) — este serviço só decide qual delas usar, tentando
 * TUTOR primeiro e, se não achar, VETERINARIO.
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return tutorRepository.findByDsEmail(email)
                .map(tutor -> User.builder()
                        .username(tutor.getDsEmail())
                        .password(tutor.getDsSenha() != null ? tutor.getDsSenha() : "")
                        .roles("TUTOR")
                        .build())
                .or(() -> veterinarioRepository.findByDsEmail(email)
                        .map(vet -> User.builder()
                                .username(vet.getDsEmail())
                                .password(vet.getDsSenha() != null ? vet.getDsSenha() : "")
                                .roles("VETERINARIO")
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}