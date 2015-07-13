jQuery.noConflict();

var enviando = 0;

var control;
var listener = Class.create();
var atividades;
var bFileBased = false;
var devices;
var selectedDevice;

function lerGPSNro(id) {
	jQuery("#status").html("Garmin " + devices[id].getDisplayName() + " selecionado! Lendo dados, por favor aguarde...");
	jQuery("#substatus").html("");
	selectedDevice = devices[id];
	control.setDeviceNumber(id);
	lerGPS();
}

listener.prototype = {
	initialize: function () { },
	onStartFindDevices: function (dados) {
		jQuery("#status").html("Localizando equipamentos...");
	},
	onFinishFindDevices: function (dados) {
		if (dados.controller.numDevices > 0) {
			if (dados.controller.numDevices > 1) {
				jQuery("#status").html("Mais de um dispositivo foi reconhecido! Qual deles &eacute; o seu Garmin?");
				var sBotoes = "";
				devices = dados.controller.getDevices();
				for (var i = 0; i < dados.controller.numDevices; i++) {
					sBotoes += " &nbsp; <input type='button' onclick='lerGPSNro(" + i + ")' value='" + devices[i].getDisplayName() + "' class='botao botaoCinza' /> &nbsp;";
				}
				jQuery("#substatus").html(sBotoes);
			}
			else {
				selectedDevice = dados.controller.getDevices()[0];
				jQuery("#status").html("Garmin " + selectedDevice.getDisplayName() + " encontrado! Lendo dados, por favor aguarde...");
				lerGPS();
			}
		}
		else {
			try {
				dados.controller.checkForUpdates();
				jQuery("#status").html("Seu Garmin n&atilde;o parece estar conectado!");
				jQuery("#substatus").html("Conecte-o e <a href=\"javascript:baixarDados();\">tente novamente</a>.");
			}
			catch (ex) {
				jQuery("#status").html("N&atilde;o encontramos seu Garmin, mas talvez isso aconteccedil;a porque seu plugin estaacute; desatualizado!");
				jQuery("#substatus").html("Baixe a nova vers&atilde;o <a href=\"http://www8.garmin.com/products/communicator/\" target=\"blank\">aqui</a>.<br/><br/>Apoacute;s a instalaccedil;&atilde;o, <a href=\"javascript:baixarDados()\">tente novamente</a>!");
			}
		}
	},
	onProgressReadFromDevice: function (json) {
		perc = json.progress.getPercentage();
		if (perc) {
			jQuery("#proxAtualiz").css("width", perc + "%");
		}
	},
	onFinishReadFromDevice: function (dados) {
		if (!bFileBased) {
			jQuery("#status").text("Validando dados...");
			atividades = new Array();
			atividades = Garmin.TcxActivityFactory.parseDocument(dados.controller.gpsData);
			jQuery("#dados").show();

			var maxLinhas = atividades.length;
			
			if ( maxLinhas>10 ) maxLinhas = 10;
			
			jQuery("#status").text("Listando " + maxLinhas + " atividades...");
			for (var iCont = 0; iCont < maxLinhas; iCont++) {
				var activity = atividades[iCont];

				var dataIni = activity.getStartTime().format("#{day}/#{month}/#{year}");
				var timeIni = activity.getStartTime().format("#{hour}:#{minute}");
				jQuery(".tabelaGPS tbody").append("<tr></tr>");
				jQuery(".tabelaGPS tbody tr:last").append("<td style='text-align:center !important;'>" + dataIni + "</td>");
				jQuery(".tabelaGPS tbody tr:last").append("<td style='text-align:center !important;'>" + timeIni + "</td>");
				jQuery(".tabelaGPS tbody tr:last").append("<td style='text-align:center !important;'><b>Enviar</b></td>");
			}
			onActivityClick();
			jQuery("#status").text("Escolha a Atividade a ser enviada:");
			jQuery(".progressoBall span").hide();
		}
	}
};

function baixarDados() {
	jQuery("#substatus").html("");
	jQuery("#status").text("Localizando software da Garmin...");
	debugger;
	try {
		control = new Garmin.DeviceControl();
	}
	catch (e) {
		jQuery("#status").html("Para usar o seu GPS Garmin vocecirc; precisa instalar um programa da proacute;pria Garmin.");
		jQuery("#substatus").html("Baixe o arquivo <a href=\"http://www8.garmin.com/products/communicator/\" target=\"blank\">aqui</a>.<br/><br/>Apoacute;s a instalaccedil;&atilde;o, <a href=\"javascript:baixarDados()\">tente novamente</a>!");
		return;
	}
	jQuery("#status").text("Configurando software...");
	control.register(new listener());
	if (window.location.hostname == 'runplanner.com.br') {
		control.unlock(["http://runplanner.com.br","b15777d2953253e1687b7603f211fd2f"]);
	}
	else if (window.location.hostname == 'www.runplanner.com.br') {
		control.unlock(["http://www.runplanner.com.br","799ec65196ca47dba8f29a9237422655"]);		
	}
	else {
		control.unlock(["http://runplanner.com.br","b15777d2953253e1687b7603f211fd2f"]);
	}
	
	control.findDevices();
}

var giDestPerc = 0;
var giStep = 0;

function lerGPS() {
	if (control.checkDeviceReadSupport(Garmin.DeviceControl.FILE_TYPES.tcxDir))
		control.readHistoryFromFitnessDevice();
	else if (control.checkDeviceReadSupport(Garmin.DeviceControl.FILE_TYPES.fitDir)) {
		bFileBased = true;
		jQuery("#dados").show();
		var files = Garmin.DirectoryFactory.parseString(control.getCurrentDeviceFitDirectoryXml());
		atividades = Garmin.DirectoryFactory.getActivityFiles(files);
		var maxLinhas = atividades.length;
		
		if ( maxLinhas>10 ) maxLinhas = 10;
		
		jQuery("#status").text("Listando " + maxLinhas + " atividades...");
		
		for (var iLinha = 0; iLinha < maxLinhas; iLinha++) {
			var dataIni = atividades[iLinha].getCreationTime().format("#{day}/#{month}/#{year}");
			var timeIni = atividades[iLinha].getCreationTime().format("#{hour}:#{minute}");
						
			jQuery(".tabelaGPS tbody").append("<tr file='" + atividades[iLinha].getAttribute(Garmin.File.ATTRIBUTE_KEYS.path) + "'></tr>");
			jQuery(".tabelaGPS tbody tr:last").append("<td style='text-align:center !important;'>" + dataIni + "</td>");
			jQuery(".tabelaGPS tbody tr:last").append("<td style='text-align:center !important;'>" + timeIni + "</td>");
			jQuery(".tabelaGPS tbody tr:last").append("<td style='text-align:center !important;'><b>Enviar</b></td>");
		}
		onActivityClick();
		jQuery("#status").text("Escolha o treino a ser enviado...");
	}
	else {
		jQuery("#status").html("Desculpe-nos, n&atilde;o conseguimos pegar as informaccedil;otilde;es do seu Garmin.");
		if (jQuery.browser.msie)
			jQuery("#substatus").html("Experimente visitar <a href='http://www8.garmin.com/products/communicator/'>http://www8.garmin.com/products/communicator/</a> e atualizar sua vers&atilde;o do plug-in, ou tente outro navegador.");
		else
			jQuery("#substatus").html("Experimente visitar <a href='http://www8.garmin.com/products/communicator/'>http://www8.garmin.com/products/communicator/</a> e atualizar sua vers&atilde;o do plug-in!<br/>Depois, <a href='#' onclick='return baixarDados();'>tente novamente</a>!");
	}
}


function onActivityClick() {
	
	jQuery(".tabelaGPS tbody td").unbind("click");
	
	jQuery(".tabelaGPS tbody td").click(function () {
		
		if (jQuery('input[name=chkOnlyOne]').is(':checked')) {
			jQuery(".tabelaGPS tbody td").unbind("click");
			jQuery("#dados").hide();
			editar = true;
		}
		else {
			jQuery(this).closest("tr").children("td").unbind("click");
			jQuery(this).closest("tr").children("td:last").html("Aguarde...");
		}
		
		enviando++;
		jQuery(this).closest("tr").children("td").unbind("click");
		jQuery(this).closest("tr").children("td:last").html("Aguarde...");
		iAtividade = jQuery(this).closest("tr").prevAll().length;

		var ativAux = new Array();
		ativAux.push(atividades[iAtividade]);

		if (bFileBased) {
			control.getBinaryFile(0, atividades[iAtividade].getAttribute(Garmin.File.ATTRIBUTE_KEYS.path));
			sDados = control.gpsDataString;
		}
		else
			sDados = encodeURIComponent(Garmin.TcxActivityFactory.produceString(ativAux));
		
		jQuery(".progressoBall span").hide();
		jQuery("#status").html("Enviando dados. Por favor, aguarde...");

		objDados = {
			tipo: (bFileBased ? "fit" : "tcx"),
			dados: sDados
		};
		
		jQuery.post("/runplanner/garminServlet", objDados, function (dados) {
			enviando--;
			window.parent.location.href = "newActivityGarminData.jsf";
			
		}, "json");
		
				
	});
}


function showDetails() {
	jQuery('#aShowMore').fadeOut(50, function () { jQuery('#pnlMais').fadeIn(50); });
}

function confirmaSaida() {
	if (enviando > 0)
		return confirm("Existem dados sendo enviados. Deseja mesmo cancelar a operaccedil;&atilde;o?");
	return true;
}
