package br.com.fiap.JornadaPet.security;

import br.com.fiap.JornadaPet.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TutorUserDetailsService implements UserDetailsService {

    private final TutorRepository tutorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return tutorRepository.findByEmail(email)
                .map(tutor -> User.builder()
                        .username(tutor.getEmail())
                        .password(tutor.getSenha() != null ? tutor.getSenha() : "")
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Tutor não encontrado: " + email));
    }
}