package br.com.clinica.bean.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;

import br.com.clinica.bean.geral.BeanManagedViewAbstract;
import br.com.clinica.controller.geral.AcompanhanteController;
import br.com.clinica.controller.geral.PacienteController;
import br.com.clinica.hibernate.InterfaceCrud;
import br.com.clinica.model.cadastro.pessoa.Acompanhante;
import br.com.clinica.model.cadastro.pessoa.Paciente;
import br.com.clinica.model.cadastro.pessoa.Pessoa;
import br.com.clinica.utils.ValidaCPF;

@Controller
@ViewScoped
@ManagedBean(name = "pacienteBean")
public class PacienteBean extends BeanManagedViewAbstract {

	private static final long serialVersionUID = 1L;

	private Paciente pacienteModel;
	private Acompanhante acompaModel;
	private List<Acompanhante> lstAcompanhante;
	private List<Paciente> lstPaciente;
	private String url = "/cadastro/cadPaciente.jsf?faces-redirect=true";
	private String urlFind = "/cadastro/findPaciente.jsf?faces-redirect=true";
	private String campoBuscaNome;
	private String campoBuscaAtivo = "A";
	private String campoBuscaCPF;
	private int idade = 20;

	@Autowired
	private PacienteController pacienteController;

	@Autowired
	private AcompanhanteController acompanhanteController;

	@PostConstruct
	public void init() throws Exception {
		lstAcompanhante = new ArrayList<Acompanhante>();
		lstPaciente = new ArrayList<Paciente>();
		pacienteModel = new Paciente();
		acompaModel = new Acompanhante();
	}

	// Respons�vel por fazer a lista de pacientes
	public void busca() throws Exception {
		// lstAcompanhante = acompanhanteController.findListByQueryDinamica(" from
		// Acompanhante");
		
		lstPaciente = new ArrayList<Paciente>();
		StringBuilder str = new StringBuilder();
		str.append("from Paciente a where 1=1");

		if (!campoBuscaNome.equals("")) {
			str.append(" and upper(a.pessoa.pessoaNome) like upper('%" + campoBuscaNome + "%')");
		}
		if (!campoBuscaCPF.equals("")) {
			str.append(" and a.pessoa.pessoaCPF like'%" + campoBuscaCPF + "%'");
		}
		
		if (campoBuscaAtivo.equals("A") || campoBuscaAtivo.equals("I")) {
			System.out.println("Entrou no A or I");
			str.append(" and a.ativo = '" + campoBuscaAtivo.toUpperCase() + "'");
		}
		if (campoBuscaAtivo.equals("T")) {
			System.out.println("Entro no T");
			str.append(" and a.ativo = 'A' or a.ativo = 'I' ");
		}
		lstPaciente = pacienteController.findListByQueryDinamica(str.toString());

	}
	
	public void inativar() {

		if (pacienteModel.getAtivo().equals("I")) {
			pacienteModel.setAtivo("A");
		} else {
			pacienteModel.setAtivo("I");
		}

		try {
			pacienteController.saveOrUpdate(pacienteModel);
		} catch (Exception e) {
			System.out.println("Erro ao ativar/inativar");
			e.printStackTrace();
		}
		this.pacienteModel = new Paciente();
		try {
			busca();
		} catch (Exception e) {
			System.out.println("Erro ao buscar atendente");
			e.printStackTrace();
		}
	}


	public void buscaAcompanhante() throws Exception {
		StringBuilder str = new StringBuilder();
		str.append("from Acompanhante a where 1=1");
		str.append(" AND a.idPacienteFK = " + pacienteModel.getIdPaciente());

		lstAcompanhante = acompanhanteController.findListByQueryDinamica(str.toString());
	}

	public void onRowSelect(SelectEvent event) throws Exception {
		pacienteModel = (Paciente) event.getObject();
		buscaAcompanhante();
		/*
		 * FacesContext.getCurrentInstance().getExternalContext()
		 * .redirect("cadastro/e/" + pacienteModel.getIdPaciente());
		 */
	}

	public void removerAcompanhante() throws Exception {
		for (Acompanhante item : lstAcompanhante) {
			acompanhanteController.merge(item);
		}
	}

	public boolean idadeMinimaPaciente() {
		Calendar dateOfBirth = new GregorianCalendar();
		dateOfBirth.setTime(pacienteModel.getPessoa().getPessoaDataNascimento());
		Calendar today = Calendar.getInstance();
		int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
		dateOfBirth.add(Calendar.YEAR, age);
		if (today.before(dateOfBirth)) {
			age--;
		}
		idade = age;
		System.out.println("IDADE RESULTADO DO IDADE MINIMA>>>>" + age);
		if (age < 18) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
					"O paciente deve ser acompanhado por um Respons�vel que tenha idade maior que 18 anos ", "");
			System.out.println(" de menor false IDADE PACIENTE:>>>" + age);
			addMessage(message);

			return false;
		}
		if (age >= 60) {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
					"O paciente deve ser acompanhado por um Respons�vel ", "");
			System.out.println(" de menor false IDADE PACIENTE:>>>" + age);
			addMessage(message);
			return false;
		} else {
			System.out.println(" de maior true IDADE ATENDENTE:>>>" + age);
			return true;
		}

	}

	// RELATORIO
	@Override
	public StreamedContent getArquivoReport() throws Exception {
		super.setNomeRelatorioJasper("report_paciente");
		super.setNomeRelatorioSaida("report_paciente");
		super.setListDataBeanCollectionReport(pacienteController.findList(getClassImp()));
		return super.getArquivoReport();
	}

	// PESQUISA CEP
	public void pesquisarCep(AjaxBehaviorEvent event) throws Exception {
		try {
			URL url = new URL("https://viacep.com.br/ws/" + pacienteModel.getPessoa().getCep() + "/json/");
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream(); // is
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8")); // br

			String cep = "";
			StringBuilder jsonCEP = new StringBuilder();

			while ((cep = bufferedReader.readLine()) != null) {
				jsonCEP.append(cep);

			}

			Pessoa gson = new Gson().fromJson(jsonCEP.toString(), Pessoa.class);

			pacienteModel.getPessoa().setCep(gson.getCep());
			pacienteModel.getPessoa().setLogradouro(gson.getLogradouro());
			pacienteModel.getPessoa().setBairro(gson.getBairro());
			pacienteModel.getPessoa().setComplemento(gson.getComplemento());
			pacienteModel.getPessoa().setUf(gson.getUf());
			pacienteModel.getPessoa().setLocalidade(gson.getLocalidade());
			System.out.println("CEP Saindo " + jsonCEP);

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			addMsg("Cep Inv�lido (Erro ao Buscar|Sem internet");
			error();
		} catch (IOException e) {
			addMsg("Cep Inv�lido");
		}
	}

	public void limpa() {

		/* Dados */
		pacienteModel.getPessoa().setPessoaNome("");
		pacienteModel.getPessoa().setPessoaDataNascimento(new Date());
		pacienteModel.getPessoa().setPessoaSexo("");
		pacienteModel.getPessoa().setPessoaEmail("");
		pacienteModel.getPessoa().setPessoaRG("");
		pacienteModel.getPessoa().setPessoaCPF("");
		pacienteModel.getPessoa().setPessoaObservacao("");
		pacienteModel.getPessoa().setPessoaTelefonePrimario("");
		pacienteModel.getPessoa().setPessoaTelefoneSecundario("");
		/* Endere�o */
		pacienteModel.getPessoa().setCep("");
		pacienteModel.getPessoa().setBairro("");
		pacienteModel.getPessoa().setUf("");
		pacienteModel.getPessoa().setLogradouro("");
		pacienteModel.getPessoa().setComplemento("");
		pacienteModel.getPessoa().setLocalidade("");
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	@Override
	public String save() throws Exception {

		pacienteModel = pacienteController.merge(pacienteModel);
		pacienteModel = new Paciente();
		return "";
	}

	@Override
	public void saveNotReturn() throws Exception {
		//idadeMinimaPaciente();
		if (ValidaCPF.isCPF(pacienteModel.getPessoa().getPessoaCPF())) {
			pacienteModel = pacienteController.merge(pacienteModel);
			System.out.println("CPF V�lido");
			pacienteModel = new Paciente();
			sucesso();
		} else {
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
					"Cpf Inv�lido: " + pacienteModel.getPessoa().getPessoaCPF(), "");
			addMessage(message);
			System.out.println("ERRO CPF INV�LIDO");
		}
	}

	public void salvarAcompanhante() throws Exception {
		/*
		 * idadeMinimaPaciente(); Acompanhante acompanhante = new Acompanhante();
		 * acompanhante = acompaModel; if (pacienteModel.getId() != null) { if
		 * (pacienteModel.getPessoa().getPessoaNome() != "") {
		 * acompaModel.setIdPacienteFK(pacienteModel.getId());
		 * lstAcompanhante.add(acompanhante);
		 * acompanhanteController.merge(acompanhante); acompanhante = new
		 * Acompanhante(); addMsg("Adicionado com sucesso"); } else {
		 * addMsg("Preencha todos os dados do acompanhante "); } }
		 */

		if (pacienteModel.getId() == null) {
			pacienteController.persist(pacienteModel);
		} else if (pacienteModel.getId() != null) {
			pacienteController.merge(pacienteModel);
		}
		Acompanhante a = new Acompanhante();
		a = acompaModel;
		lstAcompanhante.add(a);
		acompaModel = new Acompanhante();
		addMsg("Adicionado");


		lstAcompanhante.add(acompaModel);
		acompaModel = new Acompanhante();
		addMsg("Adicionado");
		buscaAcompanhante();
	}

	@Override
	public void saveEdit() throws Exception {
		saveNotReturn();
	}

	@Override
	public String novo() throws Exception {
		setarVariaveisNulas();
		return getUrl();
	}

	@Override
	public void setarVariaveisNulas() throws Exception {
		pacienteModel = new Paciente();
	}

	@Override
	public String editar() throws Exception {
		return getUrl();
	}

	@Override
	public void excluir() throws Exception {
		pacienteModel = (Paciente) pacienteController.getSession().get(getClassImp(), pacienteModel.getIdPaciente());
		pacienteController.delete(pacienteModel);
		pacienteModel = new Paciente();
		sucesso();
		busca();
	}

	public void excluirAcompanhante(long cod) throws Exception {
		System.out.println(">>>>>>>>>>>" + cod);
		acompaModel.setIdAcompanhante(cod);
		acompanhanteController.delete(acompaModel);
		// acompaModel = new Acompanhante();
		buscaAcompanhante();
	}

	@Override
	protected Class<Paciente> getClassImp() {
		return Paciente.class;
	}

	@Override
	public String redirecionarFindEntidade() throws Exception {
		setarVariaveisNulas();
		return getUrlFind();
	}

	@Override
	protected InterfaceCrud<Paciente> getController() {
		return pacienteController;
	}

	@Override
	public void consultarEntidade() throws Exception {
		pacienteModel = new Paciente();
	}

	public Paciente getPacienteModel() {
		return pacienteModel;
	}

	public void setPacienteModel(Paciente pacienteModel) {
		this.pacienteModel = pacienteModel;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlFind() {
		return urlFind;
	}

	public void setUrlFind(String urlFind) {
		this.urlFind = urlFind;
	}

	public String getCampoBuscaNome() {
		return campoBuscaNome;
	}

	public void setCampoBuscaNome(String campoBuscaNome) {
		this.campoBuscaNome = campoBuscaNome;
	}

	public String getCampoBuscaCPF() {
		return campoBuscaCPF;
	}

	public void setCampoBuscaCPF(String campoBuscaCPF) {
		this.campoBuscaCPF = campoBuscaCPF;
	}

	public List<Acompanhante> getLstAcompanhante() {
		return lstAcompanhante;
	}

	public void setLstAcompanhante(List<Acompanhante> lstAcompanhante) {
		this.lstAcompanhante = lstAcompanhante;
	}

	public List<Paciente> getLstPaciente() {
		return lstPaciente;
	}

	public void setLstPaciente(List<Paciente> lstPaciente) {
		this.lstPaciente = lstPaciente;
	}

	public PacienteController getPacienteController() {
		return pacienteController;
	}

	public void setPacienteController(PacienteController pacienteController) {
		this.pacienteController = pacienteController;
	}

	public AcompanhanteController getAcompanhanteController() {
		return acompanhanteController;
	}

	public void setAcompanhanteController(AcompanhanteController acompanhanteController) {
		this.acompanhanteController = acompanhanteController;
	}

	public Acompanhante getAcompaModel() {
		return acompaModel;
	}

	public void setAcompaModel(Acompanhante acompaModel) {
		this.acompaModel = acompaModel;
	}

	public int getIdade() {
		return idade;
	}

	public void setIdade(int idade) {
		this.idade = idade;
	}

	public String getCampoBuscaAtivo() {
		return campoBuscaAtivo;
	}

	public void setCampoBuscaAtivo(String campoBuscaAtivo) {
		this.campoBuscaAtivo = campoBuscaAtivo;
	}
}
