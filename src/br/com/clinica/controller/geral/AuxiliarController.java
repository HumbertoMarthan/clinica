package br.com.clinica.controller.geral;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;

import br.com.clinica.model.cadastro.estoque.Fornecedor;
import br.com.clinica.model.cadastro.outro.Convenio;
import br.com.clinica.model.cadastro.pessoa.Medico;
import br.com.clinica.model.cadastro.pessoa.Paciente;
import br.com.clinica.model.financeiro.FormaPagamento;
import br.com.clinica.model.prontuario.Medicamento;

@RequestScoped
@ManagedBean(name = "auxController")
public class AuxiliarController implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	
	//VERIFICAR SE A INJE��O EST� CORRETA E SE H� GET E SET
	
	@ManagedProperty("#{pacienteController}")
	private PacienteController pacienteController;
	
	@ManagedProperty("#{medicoController}")
	private MedicoController medicoController;
	
	@ManagedProperty("#{fornecedorController}")
	private FornecedorController fornecedorController;
	
	@ManagedProperty("#{convenioController}")
	private ConvenioController convenioController;
	
	@ManagedProperty("#{medicamentoController}")
	private MedicamentoController medicamentoController;
	
	@ManagedProperty("#{formaPagamentoController}")
	private FormaPagamentoController formaPagamentoController;
	
	
	public List<Convenio>  completeConvenio(String q) throws Exception {
		return convenioController.
				findListByQueryDinamica(" from Convenio where nomeConvenio like '%" + q.toUpperCase()  + "%' order by nomeConvenio  ASC");
	}
	
	public List<Medicamento>  completeMedicamento(String q) throws Exception {
		return medicamentoController.
				findListByQueryDinamica(" from Medicamento where nomeMedicamento like '%" + q.toUpperCase()  + "%' order by nomeMedicamento ASC");
	}
	
	public List<FormaPagamento>  completeFormaPagamento(String q) throws Exception {
		return formaPagamentoController.
				findListByQueryDinamica(" from FormaPagamento where nomePagamento like '%" + q.toUpperCase()  + "%' order by nomePagamento  ASC");
	}
	
	public List<Paciente>  completePaciente(String q) throws Exception {
		return pacienteController.
				findListByQueryDinamica(" from Paciente where ativo='A' and pessoa.pessoaNome like '%" + q.toUpperCase()  + "%' order by pessoa.pessoaNome ASC");
	}
	
  	public List<Medico> completeMedico(String q) throws Exception {
		return medicoController.
				findListByQueryDinamica(" from Medico where  ativo='A' and pessoa.pessoaNome like '%" + q.toUpperCase()  + "%' order by pessoa.pessoaNome ASC ");
	}
	
	public List<Fornecedor> completeFornecedor(String q) throws Exception {
		return fornecedorController.
				findListByQueryDinamica(" from Fornecedor where nomeFornecedor and like '%" + q.toUpperCase()  + "%' order by nomeFornecedor ASC");
	}
	
	public MedicoController getMedicoController() {
		return medicoController;
	}
	public FornecedorController getFornecedorController() {
		return fornecedorController;
	}
	public void setFornecedorController(FornecedorController fornecedorController) {
		this.fornecedorController = fornecedorController;
	}
	
	public void setMedicoController(MedicoController medicoController) {
		this.medicoController = medicoController;
	}

	public PacienteController getPacienteController() {
		return pacienteController;
	}

	public void setPacienteController(PacienteController pacienteController) {
		this.pacienteController = pacienteController;
	}

	
	
	public FormaPagamentoController getFormaPagamentoController() {
		return formaPagamentoController;
	}

	public void setFormaPagamentoController(FormaPagamentoController formaPagamentoController) {
		this.formaPagamentoController = formaPagamentoController;
	}

	public ConvenioController getConvenioController() {
		return convenioController;
	}

	public void setConvenioController(ConvenioController convenioController) {
		this.convenioController = convenioController;
	}

	public MedicamentoController getMedicamentoController() {
		return medicamentoController;
	}

	public void setMedicamentoController(MedicamentoController medicamentoController) {
		this.medicamentoController = medicamentoController;
	}
	
	/*public String getColorStatus(String sigla) {
		List<Status> lstStatus = (List<Status>) 
				genericService.getListParam(" from Status where 1=1 and sta_sigla = '" + sigla + "'");
		if(lstStatus.size() > 0) {
			return lstStatus.get(0).getSta_cor();
		}
		return "#BDBDBD";
	}*/
	/*
	 * @SuppressWarnings("unchecked") public List<Cultura> completeCultura(String q)
	 * { return (List<Cultura>) genericsService.getListParam
	 * ("from Cultura where cul_ativo='A' AND cul_nome like '%" + q.toUpperCase() +
	 * "%' order by cul_nome ASC"); }
	 * 
	 * @SuppressWarnings("unchecked") public List<Seguradora>
	 * completeSeguradora(String q) { return (List<Seguradora>)
	 * genericsService.getListParam
	 * ("from Seguradora where seg_ativo='A' AND seg_nome like '%" + q.toUpperCase()
	 * + "%' order by seg_nome ASC"); }
	 * 
	 * @SuppressWarnings("unchecked") public List<Estado> completeEstado(String q) {
	 * return (List<Estado>) genericsService.getListParam
	 * ("from Estado where est_ativo='A' AND est_nome like '%" + q.toUpperCase() +
	 * "%' order by est_nome ASC"); }
	 * 
	 * @SuppressWarnings("unchecked") public List<Estado> completeCidade(String q) {
	 * return (List<Estado>) genericsService.getListParam
	 * ("from Estado where est_ativo='A' AND est_cidade like '%" + q.toUpperCase() +
	 * "%' order by est_cidade ASC"); }
	 * 
	 * @SuppressWarnings("unchecked") public List<Produto> completeProduto(String q)
	 * { return (List<Produto>) genericsService.getListParam
	 * ("from Produto where prd_ativo='A' AND prd_nome like '%" + q.toUpperCase() +
	 * "%' order by prd_nome ASC"); }
	 */
	

}
