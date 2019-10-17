package br.com.clinica.bean.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import br.com.clinica.bean.geral.BeanManagedViewAbstract;
import br.com.clinica.controller.geral.ContasReceberController;
import br.com.clinica.controller.geral.EventoController;
import br.com.clinica.controller.geral.EventoPacienteController;
import br.com.clinica.controller.geral.MedicoController;
import br.com.clinica.controller.geral.PacienteController;
import br.com.clinica.controller.geral.ProntuarioController;
import br.com.clinica.hibernate.InterfaceCrud;
import br.com.clinica.model.cadastro.agendamento.CustomScheduleEvent;
import br.com.clinica.model.cadastro.agendamento.Evento;
import br.com.clinica.model.cadastro.agendamento.EventoPaciente;
import br.com.clinica.model.cadastro.agendamento.TipoEvento;
import br.com.clinica.model.cadastro.pessoa.Medico;
import br.com.clinica.model.cadastro.pessoa.Paciente;
import br.com.clinica.model.cadastro.usuario.Login;
import br.com.clinica.model.financeiro.ContasReceber;
import br.com.clinica.model.prontuario.Prontuario;
import br.com.clinica.utils.DatasUtils;
import br.com.clinica.utils.EmailUtils;

@ManagedBean(name = "scheduleBean")
@Controller
@ViewScoped
public class ScheduleBean extends BeanManagedViewAbstract {

	private static final long serialVersionUID = 1L;

	private ScheduleModel model;
	private Evento evento;
	private ContasReceber contasReceber;
	private EventoPaciente eventoPacienteModel;
	private List<ContasReceber> lstContas;
	private ScheduleEvent event;
	private ArrayList<Medico> lstPrecos; 
	private List<ScheduleEvent> scheduleEvents;
	private Date dataAtual;
	private Calendar dataAtualSchedule;
	private Evento selectedEvento;
	private List<Evento> listaEvento;
	String campoBusca;
	String campoBuscaNome ="";
	String campoBuscaEspecialidade = ""; 
	String campoBuscaAtivo = "T";

	@Autowired
	private EventoController eventoController; /* Injetando */

	@Autowired
	private ContasReceberController contasReceberController; /* Injetando */

	@Autowired
	private PacienteController pacienteController;
	
	@Autowired
	private MedicoController medicoController;

	@Autowired
	private EventoPacienteController eventoPacienteController;

	@Autowired
	private ProntuarioController  prontuarioController;
	
	@Autowired
	private ContextoBean contextoBean;

	public ScheduleBean() {
		event = new CustomScheduleEvent();
		model = new DefaultScheduleModel();
		eventoPacienteModel = new EventoPaciente();
		setDataAtual(new Date());
		evento = new Evento();
		listaEvento = new ArrayList<Evento>();
		contasReceber = new ContasReceber();
		lstContas = new ArrayList<ContasReceber>();
		lstPrecos = new ArrayList<Medico>();
	}
	
	public void buscaPreco(){
		StringBuilder str = new StringBuilder();
		try {
			lstPrecos = new ArrayList<>();
			str.append("from Medico a where 1=1");
			
			if(!campoBuscaNome.equals("")) {
				str.append(" and a.pessoa.pessoaNome LIKE '%"+ campoBuscaNome +"%'");
			}
			
			if(campoBuscaAtivo.equals("A")) {
				str.append(" and a.ativo = 'A'");
			}
			
			if(campoBuscaAtivo.equals("I")) {
				str.append(" and a.ativo = 'I'");
			}
		}catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}

		/*
		 * if(!campoBuscaEspecialidade.equals("")) {
		 * str.append(" and a.especialidades.nomeEspecialidade LIKE '%"+
		 * campoBuscaEspecialidade +"%'"); }
		 */
		
		try {
			lstPrecos = (ArrayList<Medico>) medicoController.findListByQueryDinamica(str.toString());
		} catch (Exception e) {
			System.out.println("Erro ao buscar");
			e.printStackTrace();
		}
	}

	/**
	 * Lista agendamentos (eventos)
	 * 
	 * @return
	 */
	public List<Evento> busca(){
	try {
		listaEvento = eventoController.listarEventos();
	}catch(Exception e) {
		e.printStackTrace();
		e.getMessage();
	}
		return listaEvento;
	}

	/*
	 * @Override public StreamedContent getArquivoReport() throws Exception {
	 * super.setNomeRelatorioJasper("report_calendario");
	 * super.setNomeRelatorioSaida("report_calendario");
	 * super.setListDataBeanCollectionReport(eventoController.findList(getClassImp()
	 * )); return super.getArquivoReport(); }
	 */
	/**
	 * Report com periodo de agendamentos
	 */

	@PostConstruct
	public void init() {
		try {
		buscaPreco();
		}catch (Exception e) {
			System.out.println("Erro no Busca Pre�o do DashBoard");
			e.printStackTrace();
			e.getMessage();
		}
		try {
		if (this.model != null) {
			List<Evento> eventos = this.eventoController.listarEventos();
			if (this.scheduleEvents == null) {
				this.scheduleEvents = new ArrayList<ScheduleEvent>();
			}
			for (Evento eventoAtual : eventos) { // lista que popula os eventos e inseri
				ScheduleEvent newEvent = new CustomScheduleEvent(eventoAtual.getTitulo(), eventoAtual.getDataInicio(),
						eventoAtual.getDataFim(), eventoAtual.getTipoEvento().getCss(), eventoAtual.isDiaInteiro(),
						eventoAtual.getDescricao(), eventoAtual.getMedico(), eventoAtual.getPaciente(), eventoAtual);
				if (!this.scheduleEvents.contains(newEvent)) {
					newEvent.setId(eventoAtual.getId().toString());
					this.scheduleEvents.add(newEvent);
					this.model.addEvent(newEvent);
				}
			}
		}
		}catch (Exception e) {
			System.out.println("Erro ao popular calend�rio no dashboar");
			e.printStackTrace();
			e.getMessage();
		}
	}

	@Override
	public StreamedContent getArquivoReport() throws Exception {
		super.setNomeRelatorioJasper("report_calendario");
		super.setNomeRelatorioSaida("report_calendario");

		String[] parametros = new String[] { "dataInicio", "dataFim" };
		String hql = "FROM Evento e WHERE e.dataInicio BETWEEN :dataInicio AND :dataFim  "
				+ "AND e.dataFim BETWEEN :dataInicio AND :dataFim)";

		List<Evento> lista = eventoController.findListByQueryDinamica(hql, Arrays.asList(parametros),
				evento.getDataInicioRelatorio(), evento.getDataFimRelatorio());

		super.setListDataBeanCollectionReport(lista);
		return super.getArquivoReport();
	}

	public List<Paciente> completePaciente(String q) throws Exception {
		return pacienteController.findListByQueryDinamica(" from Paciente where pessoa.pessoaNome like '%"
				+ q.toUpperCase() + "%' order by pessoa.pessoaNome ASC");
	}

	/**
	 * Adiciona 30 minutos a data inicial selecionada no agendamento
	 */
	public void atualizaValorDataFim() {
		try {

			DateTime dataSelecionadaJoda = new DateTime(evento.getDataInicio().getTime());
			evento.setDataFim(dataSelecionadaJoda.plusMinutes(30).toDate());
			System.out.println("DATA FIM " + evento.getDataFim());
		} catch (Exception e) {
			System.out.println("Erro ao Adicionar 30 minutos");
		}
	}

	/**
	 * Insere o nome do paciente na descri��o do schedule tooltip
	 */
	public void pacienteEtiqueta() {
		try {
			if (evento.getPaciente().getPessoa().getPessoaNome() != null) {
				String etiqueta = evento.getPaciente().getPessoa().getPessoaNome();
				evento.setTitulo(etiqueta);
				System.out.println("Etiqueta Paciente" + etiqueta);
			}
		} catch (Exception e) {

			System.out.println("Erro ao gravar paciente");
		}
	}

	/**
	 * Insere o nome do m�dico na descri��o do schedule tooltip
	 */
	public void medicoEtiqueta() {
		try {
			if (evento.getPaciente().getPessoa().getPessoaNome() != null
					|| evento.getMedico().getPessoa().getPessoaNome() != null) {
				String tooltip = "Paciente: " + evento.getPaciente().getPessoa().getPessoaNome() + "<br/>" + "M�dico: "
						+ evento.getMedico().getPessoa().getPessoaNome();
				evento.setDescricao(tooltip);
				System.out.println("Tooltip M�dico  paciente" + tooltip);
			}
		} catch (Exception e) {
			System.out.println("Erro ao gravar medico");
		}
	}

	public void buscaRegistro() {
		try {
			listaEvento = eventoController
					.findListByQueryDinamica(" FROM Evento WHERE upper(titulo) like upper('%" + campoBusca + "%')");
			campoBusca = "";
		}catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
		}
	}

	/**
	 * Verifica se o M�dico cont�m duas consultas agendadas no mesmo hor�rio
	 * 
	 * @return
	 */
	public boolean validarMedico() {
		String[] param = new String[] { "idMedico", "dataInicio", "dataFim" };
		String hql = "FROM Evento e WHERE e.medico.idMedico = "
				+ ":idMedico AND (e.dataInicio BETWEEN :dataInicio AND :dataFim "
				+ "OR e.dataFim BETWEEN :dataInicio AND :dataFim)";

		try {
		List<Evento> lista = eventoController.findListByQueryDinamica(hql, Arrays.asList(param),
				evento.getMedico().getIdMedico(), evento.getDataInicio(), evento.getDataFim());
		
		if (!lista.isEmpty()) {
			if (evento.getId() == null) {
				return false;
			} else {
				for (Evento e : lista) {
					if (evento.getId() != e.getId()) {
						if (e.getDataInicio().after(evento.getDataInicio())
								&& e.getDataInicio().before(evento.getDataFim())) {
							return false;
						} else if (e.getDataFim().before(evento.getDataFim())
								&& e.getDataFim().after(evento.getDataInicio())) {
							return false;
						}
					}
				}
			}
		}
		}catch (Exception e) {
			System.out.println("Erro ao validar M�dico");
			e.printStackTrace();
			e.getMessage();
		}

		return true;

	}

	/**
	 * Verifica se o Paciente cont�m duas consultas agendadas no mesmo hor�rio
	 * 
	 * @return
	 */
	public boolean validarPaciente(){
		String[] param = new String[] { "idPaciente", "dataInicio", "dataFim" };
		String hql = "FROM Evento e WHERE e.paciente.idPaciente = "
				+ ":idPaciente AND (e.dataInicio BETWEEN :dataInicio AND :dataFim "
				+ "OR e.dataFim BETWEEN :dataInicio AND :dataFim)";
		
		try {
		List<Evento> lista = eventoController.findListByQueryDinamica(hql, Arrays.asList(param),
				evento.getPaciente().getIdPaciente(), evento.getDataInicio(), evento.getDataFim());

		if (!lista.isEmpty()) {
			if (evento.getId() == null) {
				return false;
			} else {
				for (Evento e : lista) {
					if (evento.getId() != e.getId()) {
						if (e.getDataInicio().after(evento.getDataInicio())
								&& e.getDataInicio().before(evento.getDataFim())) {
							return false;
						} else if (e.getDataFim().before(evento.getDataFim())
								&& e.getDataFim().after(evento.getDataInicio())) {
							return false;
						}
					}
				}
			}
		}
		}catch (Exception e) {
			System.out.println("Erro ao validar Paciente");
			e.printStackTrace();
			e.getMessage();
		}

		return true;

	}

	public void geraProntuario() {
		Prontuario prontuarioModel = new Prontuario();
		prontuarioModel.setPaciente(new Paciente(evento.getPaciente().getIdPaciente()));
		prontuarioModel.setMedico(new Medico(evento.getMedico().getIdMedico()));
		prontuarioModel.setDataConsulta(evento.getDataInicio());
		try {
			prontuarioController.merge(prontuarioModel);
		} catch (Exception e) {
			System.out.println("Erro ao gerar prontuario na tabela");
			e.printStackTrace();
		}
		
	}


	public void convenioUnimed()  {
		Long usuarioSessaoId = 0L;
		try {
			usuarioSessaoId = contextoBean.getEntidadeLogada().getIdLogin();
		}catch (Exception e) {
			System.out.println("Erro ao recuperar usuario na sessao Unimed");
			e.printStackTrace();
			e.getMessage();
		}
		
		contasReceber.setLogin(new Login(usuarioSessaoId));
		contasReceber.setPaciente(new Paciente(evento.getPaciente().getIdPaciente()));
		contasReceber.setStatus("P");
		contasReceber.setDataInicioAgendamento(evento.getDataInicio());
		contasReceber.setDataFimAgendamento(evento.getDataFim());
		contasReceber.setValorConsulta(evento.getMedico().getPrecoConsulta().getValor());
		contasReceber.setObservacao("Recolher Assinatura de Paciente!");
		
		try {
			contasReceberController.merge(contasReceber);
		}catch (Exception e) {
			System.out.println("Erro ao salvar convenio unimed na contas a pagar");
			e.printStackTrace();
			e.getMessage();
		}
		
		//Gera um Prontuario 
		geraProntuario();

	}

	public void convenioPrever() {
		Long usuarioSessaoId = 0L; 
		
		try {
			 usuarioSessaoId = contextoBean.getEntidadeLogada().getIdLogin();
		}catch (Exception e) {
			System.out.println("Erro ao recuperar usuario no convenio prever na contas a pagar");
			e.printStackTrace();
			e.getMessage();
		}	
		
		try {
			contasReceber.setLogin(new Login(usuarioSessaoId));
		}catch (Exception e) {
			System.out.println("Erro ao salvar convenio unimed na sessao");
			e.printStackTrace();
			e.getMessage();
		}
		
		contasReceber.setPaciente(new Paciente(evento.getPaciente().getIdPaciente()));
		contasReceber.setStatus("P");
		contasReceber.setValorConsulta(evento.getMedico().getPrecoConsulta().getValor());
		contasReceber.setDataInicioAgendamento(evento.getDataInicio());
		contasReceber.setDataFimAgendamento(evento.getDataFim());
		
		try {
			contasReceberController.merge(contasReceber);
		}catch (Exception e) {
			System.out.println("Erro ao salvar convenio prever na contas a pagar");
			e.printStackTrace();
			e.getMessage();
		}
		//Gera um Prontuario 
				geraProntuario();
	}

	public void convenioParticular() {
		Long usuarioSessaoId = 0L;
		try {
			usuarioSessaoId = contextoBean.getEntidadeLogada().getIdLogin();
		}catch (Exception e) {
			System.out.println("Erro ao recuperar usuario na sessaao");
			e.printStackTrace();
			e.getMessage();
		}
		try {
			contasReceber.setLogin(new Login(usuarioSessaoId));
		}catch (Exception e) {
			System.out.println("Erro ao recuperar usuario na sessao");
			e.printStackTrace();
			e.getMessage();
		}
		
		contasReceber.setPaciente(new Paciente(evento.getPaciente().getIdPaciente()));
		contasReceber.setStatus("P");
		contasReceber.setValorConsulta(evento.getMedico().getPrecoConsulta().getValor());
		contasReceber.setDataInicioAgendamento(evento.getDataInicio());
		contasReceber.setDataFimAgendamento(evento.getDataFim());
		
		try{
			contasReceberController.merge(contasReceber);
		}catch (Exception e) {
			System.out.println("Erro ao salvar convenio prever na contas a pagar");
			e.printStackTrace();
			e.getMessage();
		}
		//Gera um Prontuario 
				geraProntuario();
		
	}

	public void convenioSas() {
		Long usuarioSessaoId = 0L;
		
		try {
			usuarioSessaoId = contextoBean.getEntidadeLogada().getIdLogin();
		}catch (Exception e) {
			System.out.println("Erro ao recuperar da sessao");
			e.printStackTrace();
			e.getMessage();
		}
		
		try {
			contasReceber.setLogin(new Login(usuarioSessaoId));
		}catch (Exception e) {
			System.out.println("Erro ao recuperar usuario da sessao");
			e.printStackTrace();
			e.getMessage();
		}
		
		contasReceber.setPaciente(new Paciente(evento.getPaciente().getIdPaciente()));
		contasReceber.setStatus("P");
		contasReceber.setValorConsulta(evento.getMedico().getPrecoConsulta().getValor());
		contasReceber.setDataInicioAgendamento(evento.getDataInicio());
		contasReceber.setDataFimAgendamento(evento.getDataFim());
		
		try {
			contasReceberController.merge(contasReceber);
		}catch (Exception e) {
			System.out.println("Erro ao salvar convenio Sas na contas a pagar");
			e.printStackTrace();
			e.getMessage();
		}
		//Gera um Prontuario 
				geraProntuario();
	}

	public void adicionarContasReceber() {
		if (evento.getTipoEvento().getDescricao() == "Confirmar") {
			try {
				lstContas = contasReceberController.findListByQueryDinamica("from ContasReceber");
			}catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
			}
			for (ContasReceber contas : lstContas) {
				if (contas.getPaciente().getIdPaciente() == evento.getPaciente().getIdPaciente()
						&& evento.getDataInicio().compareTo(contas.getDataInicioAgendamento()) == 0) {
					try {
						contasReceberController.delete(contas);
					}catch (Exception e) {
						e.getMessage();
						e.printStackTrace();
					}
				}
			}

			// Verifica se o Conv�nio � UNIMED
			if (evento.getPaciente().getConvenio().getNomeConvenio().equals("Unimed")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("UNIMED")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("unimed")) {

				convenioUnimed();
			}
			// Verifica se o Conv�nio � PREVER
			if (evento.getPaciente().getConvenio().getNomeConvenio().equals("Prever")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("PREVER")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("prever")) {

				convenioPrever();
			}
			// Verifica se o Conv�nio � SAS
			if (evento.getPaciente().getConvenio().getNomeConvenio().equals("Sas")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("SAS")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("sas")) {

				convenioSas();
			}
			// Verifica se o Conv�nio � PARTICULAR
			if (evento.getPaciente().getConvenio().getNomeConvenio().equals("Particular")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("PARTICULAR")
					|| evento.getPaciente().getConvenio().getNomeConvenio().equals("particular")) {

				convenioParticular();
			}
		}
	}

	/* Salva */
	public void salvar() {
		try {
		// Salva o construtor que implementa a interface (Custom) do Schedule com os
		// atributos.
		ScheduleEvent newEvent = new CustomScheduleEvent(this.evento.getTitulo(), this.evento.getDataInicio(),
				this.evento.getDataFim(), this.evento.getTipoEvento().getCss(), this.evento.isDiaInteiro(),
				this.evento.getDescricao(), this.evento.getMedico(), this.evento.getPaciente(),
				this.evento.getConfirmaConsulta(), this.evento);
		Date dataHoje = new Date();
		System.out.println("DATA><HOJE "+ dataHoje);
		if(evento.getDataInicio().before(dataHoje) || evento.getDataFim().before(dataHoje)) {
			addMsg("O Agendamento tem que ser feito com data/hora superior a de hoje ");
		}else if (evento.getDataFim().before(evento.getDataInicio())) {/* Verifica se a Datafim est� vindo antes da DataInicio */
			addMsg("Data Final do agendamento  n�o pode ser maior que a Data Inicial do mesmo");
			
		} else if (validarMedico() && validarPaciente()) { /* Se o Evento for novo */
			if (evento.getId() == null) {
				model.addEvent(newEvent);
				// persistirEventoPaciente();
				eventoController.persist(evento);

				// Envio de Email para o paciente
				eventoController.findById(Evento.class, evento.getId());
				try {
					sendEmailAgendamento(evento);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else { /* Se o Evento j� existir */
				newEvent.setId(event.getId());
				model.updateEvent(newEvent);
				// atualizarEventoPaciente();
				eventoController.merge(evento);

				// Envio de Email para o paciente
				evento = (Evento) eventoController.findById(Evento.class, evento.getId());
				try {
				sendEmailAgendamento(evento);
				} catch(Exception e) {
					e.printStackTrace();
				}
				// Busca o Evento para compara��o no Contas a receber
				Long id = this.evento.getId();
				evento = eventoController.findByPorId(Evento.class, id);

				adicionarContasReceber();

			}

			addMsg(" Agendamento Salvo para: " + evento.getPaciente().getPessoa().getPessoaNome() + "Agendamento para: " + evento.getTitulo());
		} else {
			addMsg("J� existe um agendamento cadastrado neste hor�rio para este paciente ou m�dico, Revise o calend�rio!");
		}
		}catch (Exception e) {
			System.out.println("Erro Ao Fazer um agendamento");
			e.printStackTrace();
		}
	}

	public void remover(){
		try {
			/* Deleta evento do agendamento do banco de dados */
			evento = (Evento) eventoController.getSession().get(getClassImp(), evento.getId());
			eventoController.delete(evento);

			/* Deleta etiqueta do Schedule */
			model.deleteEvent(event);

			/* Adiciona Mensagem para o usuario do agendamento removido */
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
					"Agendamento Removido: " + evento.getPaciente().getPessoa().getPessoaNome(),
					"Agendamento Removido :" + evento.getTitulo());
			addMessage(message);
		} catch (Exception e) {
			/* Lan�a erro ao remover etiqueta */
			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Impossivel remover",
					"H� dependencias:" + evento.getTitulo());
			addMessage(message);
		}
		

	}

	// AO SALVAR SELE��O DE UMA AGENDAMENTO
	public void onDateSelect(SelectEvent selectEvent) {
		this.evento = new Evento();
		Date dataSelecionada = (Date) selectEvent.getObject();
		@SuppressWarnings("unused")
		DateTime dataSelecionadaJoda = new DateTime(dataSelecionada.getTime());
		this.evento.setDataInicio(dataSelecionada);
	}

	// EVENTO DE SELE��O DOS HORARIOS AGENDADOS
	public void onEventSelect(SelectEvent selectEvent)  {
		System.out.println("Entrou no evento selecionado");
		this.evento = new Evento();
		event = (CustomScheduleEvent) selectEvent.getObject();
		this.evento = (Evento) event.getData();
		Long id = this.evento.getId();
		System.out.println("Evento Selecionado" + id);
		try {	
			this.evento = eventoController.findByPorId(Evento.class, id);
		}catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
		System.out.println("Evento vindo do banco" + evento);
	}

	private void addMessage(FacesMessage message) {
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public TipoEvento[] getTiposEventos() {
		return TipoEvento.values();
	}

	public void sendEmailAgendamento(Evento t)  {

		long codigoPaciente = t.getPaciente().getIdPaciente();
		List<Evento> emails = new ArrayList<>();
		
		try {	
			emails = eventoController.findListByQueryDinamica("from Evento where paciente.idPaciente =" + codigoPaciente);
		}catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
		
		String email = "";

		for (Evento e : emails) {
			email = e.getPaciente().getEmail();
		}

		String emailTitulo = "";
		emailTitulo += "Um Evento foi Agendado/Atualizada para o paciente: '"
				+ t.getPaciente().getPessoa().getPessoaNome() + "'";

		String emailConteudo = "";

		emailConteudo += "<b>INFORMA��ES DO AGENDAMENTO</b><br />";
		emailConteudo += "<b>NUMERO DO AGENDAMENTO: </b> " + t.getId() + "<br />";
		emailConteudo += "<b>PACIENTE:</b> " + t.getPaciente().getPessoa().getPessoaNome() + "<br />";
		emailConteudo += "<b>M�DICO:</b> " + t.getMedico().getPessoa().getPessoaNome() + "<br /><br /><br />";
		emailConteudo += "<b>DATA/HORA DO INICIO DO AGENDAMENTO :</b> " + DatasUtils.formatDate(t.getDataInicio())
				+ "<br /><br /><br />";
		emailConteudo += "<b>DATA/HORA DO FIM DO AGENDAMENTO :</b> " + DatasUtils.formatDate(t.getDataFim())
				+ "<br /><br /><br />";
		String tipoAgendamento = "";
		if (t.getTipoEvento().ordinal() == 2) {
			tipoAgendamento = "Consulta Confirmada com sucesso";
		}
		if (t.getTipoEvento().ordinal() == 1) {
			tipoAgendamento = "Retorno Confirmado com sucesso";
		}
		if (t.getTipoEvento().ordinal() == 0) {
			tipoAgendamento = "Consulta Agendada com sucesso";
		}

		emailConteudo += "<b>TIPO DE CONSULTA :</b>" + tipoAgendamento;
		emailConteudo += "<b>EMAIL ENVIADO DE FORMA AUTOMATICA POR CLINICA HEALTSTAT, POR FAVOR N�O RESPONDA O EMAIL</b><br />";
		// Enviar Email com o dados coletados dentro desse m�todo
		EmailUtils.enviarEmail(email, emailTitulo, emailConteudo, true);
	}

	// GETTERS E SETTERS

	public void onRowSelect(SelectEvent event) {
		evento = (Evento) event.getObject();
	}

	public ContextoBean getContextoBean() {
		return contextoBean;
	}

	public void setContextoBean(ContextoBean contextoBean) {
		this.contextoBean = contextoBean;
	}

	public ContasReceber getContasReceber() {
		return contasReceber;
	}

	public void setContasReceber(ContasReceber contasReceber) {
		this.contasReceber = contasReceber;
	}

	public List<ContasReceber> getLstContas() {
		return lstContas;
	}

	public void setLstContas(List<ContasReceber> lstContas) {
		this.lstContas = lstContas;
	}

	public ScheduleEvent getEvent() {
		return event;
	}

	public void setEvent(ScheduleEvent event) {
		this.event = event;
	}

	public EventoController getEventoController() {
		return eventoController;
	}

	public void setEventoController(EventoController eventoController) {
		this.eventoController = eventoController;
	}

	public PacienteController getPacienteController() {
		return pacienteController;
	}

	public void setPacienteController(PacienteController pacienteController) {
		this.pacienteController = pacienteController;
	}

	public List<ScheduleEvent> getScheduleEvents() {
		return scheduleEvents;
	}

	public void setScheduleEvents(List<ScheduleEvent> scheduleEvents) {
		this.scheduleEvents = scheduleEvents;
	}

	public String getCampoBusca() {
		return campoBusca;
	}

	public void setCampoBusca(String campoBusca) {
		this.campoBusca = campoBusca;
	}

	@Override
	protected Class<Evento> getClassImp() {
		return Evento.class;
	}

	@Override
	protected InterfaceCrud<Evento> getController() {
		return eventoController;
	}

	public ScheduleModel getModel() {
		return model;
	}

	public void setModel(ScheduleModel model) {
		this.model = model;
	}

	public Evento getEvento() {
		return evento;
	}

	public List<Evento> getListaEvento() {
		return listaEvento;
	}

	public void setListaEvento(List<Evento> listaEvento) {
		this.listaEvento = listaEvento;
	}

	public void setEvento(Evento evento) {
		this.evento = evento;
	}

	public Calendar getDataAtualSchedule() {
		return dataAtualSchedule;
	}

	public void setDataAtualSchedule(Calendar dataAtualSchedule) {
		this.dataAtualSchedule = dataAtualSchedule;
	}

	public Calendar today() {
		return Calendar.getInstance();
	}

	public Evento getSelectedEvento() {
		return selectedEvento;
	}

	public void setSelectedEvento(Evento selectedEvento) {
		this.selectedEvento = selectedEvento;
	}

	public Date getDataAtual() {
		return dataAtual;
	}

	public void setDataAtual(Date dataAtual) {
		LocalDate localDate = new LocalDate();
		dataAtual = localDate.toDate();

		this.dataAtual = dataAtual;
	}

	public ContasReceberController getContasReceberController() {
		return contasReceberController;
	}

	public void setContasReceberController(ContasReceberController contasReceberController) {
		this.contasReceberController = contasReceberController;
	}

	public EventoPacienteController getEventoPacienteController() {
		return eventoPacienteController;
	}

	public void setEventoPacienteController(EventoPacienteController eventoPacienteController) {
		this.eventoPacienteController = eventoPacienteController;
	}

	public EventoPaciente getEventoPacienteModel() {
		return eventoPacienteModel;
	}

	public void setEventoPacienteModel(EventoPaciente eventoPacienteModel) {
		this.eventoPacienteModel = eventoPacienteModel;
	}

	public ArrayList<Medico> getLstPrecos() {
		return lstPrecos;
	}

	public void setLstPrecos(ArrayList<Medico> lstPrecos) {
		this.lstPrecos = lstPrecos;
	}

	public ProntuarioController getProntuarioController() {
		return prontuarioController;
	}

	public void setProntuarioController(ProntuarioController prontuarioController) {
		this.prontuarioController = prontuarioController;
	}

	public MedicoController getMedicoController() {
		return medicoController;
	}

	public void setMedicoController(MedicoController medicoController) {
		this.medicoController = medicoController;
	}

	public String getCampoBuscaNome() {
		return campoBuscaNome;
	}

	public void setCampoBuscaNome(String campoBuscaNome) {
		this.campoBuscaNome = campoBuscaNome;
	}

	public String getCampoBuscaEspecialidade() {
		return campoBuscaEspecialidade;
	}

	public void setCampoBuscaEspecialidade(String campoBuscaEspecialidade) {
		this.campoBuscaEspecialidade = campoBuscaEspecialidade;
	}

	public String getCampoBuscaAtivo() {
		return campoBuscaAtivo;
	}

	public void setCampoBuscaAtivo(String campoBuscaAtivo) {
		this.campoBuscaAtivo = campoBuscaAtivo;
	}
}
