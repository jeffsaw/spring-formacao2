package med.voll.api.controller;

import jakarta.validation.Valid;
import med.voll.api.medico.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("medicos")
public class MedicosController {

    @Autowired
    private MedicoRepository repository;

    @PostMapping
    @Transactional
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroMedico dados, UriComponentsBuilder uriBuilder){
        var medico = new Medico(dados);
        repository.save(medico);

        var uri = uriBuilder.path("/medicos/{id}").buildAndExpand(medico.getId()).toUri();

        return ResponseEntity.created(uri).body(new DadosDetalhamentoMedico(medico));
    }

//Método antes de implementar a paginação, o proprio tipo Page tem o método stream e o toList,
//
//    @GetMapping
//    public List<DadosListagemMedico> listarMedicos(){
//        return repository.findAll().stream().map(DadosListagemMedico::new).toList();
//    }

    @GetMapping
    public ResponseEntity<Page<DadosListagemMedico>> listarMedicos(@PageableDefault(sort={"nome"}, size=2) Pageable paginacao) {
        var page = repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
        return ResponseEntity.ok(page);
    }

    //O próprio Spring, após a atualização do registro, já faz o commit da transação, e
    //atualiza o Banco de Dados, não sendo necessário o uso do método save.
    //Foi criado um novo DTO excusico para mostrar os dados atualizados do médico.
    @PutMapping
    @Transactional
    public ResponseEntity atualizarCadastro(@RequestBody @Valid DadosAtualizacaoMedico dados){
        var medico = repository.getReferenceById(dados.id());
        medico.atualizarCadastro(dados);
        return ResponseEntity.ok(new DadosDetalhamentoMedico(medico));
    }

//    //Esse método faz a exclusão definitiva do registro, sem a possibilidade de recuperação.
//    @DeleteMapping("/{id}")
//    @Transactional
//    public void deletarCadastro(@PathVariable Long id){
//        repository.deleteById(id);
//    }

    //Esse método faz a exclusão lógica do registro, ou seja, o registro é marcado como inativo,
    //É uma boa prárica na exclusão devolver o cógido 204, que significa que a requisição foi bem sucedida,
    // e não há conteúdo a ser retornado.
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deletarCadastro(@PathVariable Long id){
        var medico = repository.getReferenceById(id);
        medico.excluirLogico();
        return ResponseEntity.noContent().build();
    }

}
