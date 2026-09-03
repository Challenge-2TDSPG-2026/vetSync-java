package br.com.fiap.VetSync.integration;

import br.com.fiap.VetSync.controller.*;
import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PrescricaoFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClinicaRepository clinicaRepository;

    @Autowired
    private TipoEventoRepository tipoEventoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Test
    @DisplayName("Ponta a ponta: Prescrição - Solicitação pelo Veterinário -> Liberação pelo Admin -> Consulta pelo Tutor")
    void fluxoPrescricao() throws Exception {
        Clinica clinica = clinicaRepository.save(Clinica.builder().nmClinica("Clinica Medicamentos").dsCnpj("99887766000155").build());
        TipoEvento tipo = tipoEventoRepository.save(TipoEvento.builder().nmTipoEvento("Consulta Dermatológica").dsCategoria("CLINICO").build());
        Medicamento med = medicamentoRepository.save(Medicamento.builder().nmMedicamento("Apoquel 16mg").dsPrincipio("Oclacitinib").vlPrecoRef(new BigDecimal("210.00")).build());

        // 1. Admin setup
        var adminReq = new AdminController.AdminBootstrapRequest("Admin Med", "admin.med@vetsync.com", "boot-secret-test-key-12345");
        MvcResult adminRes = mockMvc.perform(post("/admins/bootstrap").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(adminReq))).andReturn();
        String adminPwd = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("senhaTemporaria").asText();

        MvcResult adminLogin = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthController.LoginRequest("admin.med@vetsync.com", adminPwd)))).andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString()).get("token").asText();

        // 2. Vet setup
        MvcResult vetRes = mockMvc.perform(post("/veterinarios")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new VeterinarioController.VeterinarioRequest("Dr. André", "andre.vet@vetsync.com", clinica.getIdClinica())))).andReturn();
        JsonNode vetNode = objectMapper.readTree(vetRes.getResponse().getContentAsString());
        Long idVet = vetNode.get("idVeterinario").asLong();
        String vetPwd = vetNode.get("senhaTemporaria").asText();

        MvcResult vetLogin = mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthController.LoginRequest("andre.vet@vetsync.com", vetPwd)))).andReturn();
        String vetToken = objectMapper.readTree(vetLogin.getResponse().getContentAsString()).get("token").asText();

        // 3. Tutor & Pet setup
        MvcResult tutorRes = mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AuthController.RegistrarRequest("Julio", "julio@teste.com", "senha123", "33344455566", "11966665555")))).andReturn();
        String tutorToken = objectMapper.readTree(tutorRes.getResponse().getContentAsString()).get("token").asText();

        MvcResult petRes = mockMvc.perform(post("/pets")
                .header("Authorization", "Bearer " + tutorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PetController.PetRequest("Thor", EspecieCategoria.CAO, null, "Bulldog", LocalDate.now().minusYears(3), new BigDecimal("14.0"))))).andReturn();
        Long idPet = objectMapper.readTree(petRes.getResponse().getContentAsString()).get("idPet").asLong();

        // 4. Tutor agenda Consulta
        MvcResult evRes = mockMvc.perform(post("/eventos")
                .header("Authorization", "Bearer " + tutorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new EventoController.EventoAgendarRequest(idPet, tipo.getIdTipoEvento(), idVet, LocalDate.now(), "Coceira intensa")))).andReturn();
        Long idEvento = objectMapper.readTree(evRes.getResponse().getContentAsString()).get("idEvento").asLong();

        // 5. Veterinário solicita Prescrição para o evento
        var prescricaoReq = new PrescricaoController.PrescricaoRequest(
                idEvento,
                med.getIdMedicamento(),
                "1 comprimido a cada 24 horas por 14 dias",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                1
        );
        MvcResult pRes = mockMvc.perform(post("/prescricoes")
                        .header("Authorization", "Bearer " + vetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescricaoReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADO"))
                .andReturn();
        Long idPrescricao = objectMapper.readTree(pRes.getResponse().getContentAsString()).get("idPrescricao").asLong();

        // 6. Admin lista prescrições pendentes
        mockMvc.perform(get("/prescricoes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPrescricao").value(idPrescricao))
                .andExpect(jsonPath("$[0].status").value("SOLICITADO"));

        // 7. Admin libera a prescrição (aprovado=true)
        mockMvc.perform(patch("/prescricoes/" + idPrescricao + "/liberar")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PrescricaoController.PrescricaoLiberarRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIBERADO"));

        // 8. Tutor consulta a prescrição liberada
        mockMvc.perform(get("/prescricoes")
                        .header("Authorization", "Bearer " + tutorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idPrescricao").value(idPrescricao))
                .andExpect(jsonPath("$[0].status").value("LIBERADO"))
                .andExpect(jsonPath("$[0].nmMedicamento").value("Apoquel 16mg"));
    }
}
