package br.com.srv.implementacao;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import br.com.repository.interfaces.RepositoryPaciente;
import br.com.srv.interfaces.SrvPaciente;

@Service
public class SrvPacienteImp implements SrvPaciente{

	private static final long serialVersionUID = 1L;
	//tdo Imp tem inje��o de dependencia do repositorio
	
	
	@Resource 
	private RepositoryPaciente repositoryPaciente;
}
