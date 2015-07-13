/********************cursores********************/
function cursor(cursortype) {
    if (erase == true || water == true || medic == true) {
        map.getDragObject().setDraggableCursor("crosshair");
        $("#map_canvas").mouseover(function(e) {
            $("#divMapCursor").css("visibility", "visible");
            document.getElementById("mapCursor").src = cursortype;

            $("#map_canvas").mousemove(function(e) {
                $("#divMapCursor").css("left", e.pageX).css("top", e.pageY - 20);
            });
        });
        $("#map_canvas").mouseout(function(e) {
            $("#divMapCursor").css("visibility", "hidden");
            $("#map_canvas").unbind("mousemove");
        });
    }
}
/***************************************************/

function encodeSignedNumber(num) {
    var sgn_num = num << 1;

    if (num < 0) {
        sgn_num = ~(sgn_num);
    }
    return (encodeNumber(sgn_num));
}
function encodeNumber(num) {
    var encodeString = "";

    while (num >= 0x20) {
        encodeString += (String.fromCharCode((0x20 | (num & 0x1f)) + 63));
        num >>= 5;
    }
    encodeString += (String.fromCharCode(num + 63));
    return encodeString;
}
/***************************************************/
/*******************decodificação*******************/
function decode(polilinha, nivel) {
    newroute = new GPolyline.fromEncoded({
        color: "#FF0000",
        weight: 3,
        points: polilinha,
        levels: nivel,
        zoomFactor: 32,
        numLevels: 4
    });
    map.addOverlay(newroute);
    bounds = newroute.getBounds();

    map.setCenter(bounds.getCenter(), map.getBoundsZoomLevel(bounds));
}

//Localização de lugares
function showAddress(address) {
    if (geocoder) {
        geocoder.getLocations(address, function(result) {
            if (result.Status.code != 200)
                HelpBox("centralized", "centralized", "Ponto de Partida", "Local não encontrado!", "help", "Exclamacao");
            else {
                lastFoundPoint = new GLatLng(result.Placemark[0].Point.coordinates[1], result.Placemark[0].Point.coordinates[0]);

                map.closeInfoWindow();
                map.setCenter(lastFoundPoint, 16);
                map.openInfoWindowHtml(lastFoundPoint,
                        "<b>Local:</b> " + result.Placemark[0].address + "<br/>" +
                        "<br/>" +
                        "<a href='javascript:startLineFromFoundPoint();'>" + (newroute != undefined ? "Continuar" : "Iniciar") + " rota deste ponto</a> | <a href='javascript:findAnotherPoint();'>Ir para outro ponto</a>"
                    );
            }
        });
    }
}

function findAnotherPoint() {
    map.closeInfoWindow();
    $("#txtPtPartida").val("");
    $("#txtPtPartida").focus();
}

//Função que começa a rota de um ponto achado
function startLineFromFoundPoint() {
    if (lastFoundPoint) {
        map.closeInfoWindow();
        route = true;
        erase = water = medic = false;
        criaRota();
        newroute.insertVertex(newroute.getVertexCount(), lastFoundPoint);
        mudaModo();
    }
}

//Função que cria a polyline newroute
function criaRota() {
    newroute = new GPolyline([], "#067DBE");
    map.addOverlay(newroute);
}

//Função que adiciona o marker de inicio na rota
function adicionaInicio() {
    var baseIcon = new GIcon(G_DEFAULT_ICON);
    baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
    baseIcon.iconSize = new GSize(20, 34);
    baseIcon.shadowSize = new GSize(37, 34);
    baseIcon.iconAnchor = new GPoint(9, 34);
    baseIcon.infoWindowAnchor = new GPoint(9, 2);

    var startIcon = new GIcon(baseIcon);
    startIcon.image = "http://www.google.com/mapfiles/dd-start.png";
    markerInicio = new GMarker(newroute.getVertex(0), startIcon, true);
    map.addOverlay(markerInicio);
}

function mapa() {
    this.rota = newroute.copy();
    this.retorno = retornoTracado; 
}

function adicionaListeners() {
    GEvent.clearListeners(newroute, "lineupdated");
    GEvent.bind(newroute, "lineupdated", document.getElementById("rotaKM"), function() //função que diz a kilometragem da rota
    {
        document.getElementById("rotaKM").innerHTML = (newroute.getLength() / 1000).toFixed(2);
        vertexCount = newroute.getVertexCount();

        rotas.push(new mapa());
        //AQUI DEVE CONTROLAR QUANTIDADE DO DESFAZER

        if (vertexCount > 1 && markerInicio != null) {
            map.removeOverlay(markerInicio);
        }
        adicionaInicio();

    });
    GEvent.clearListeners(newroute, "click");
    GEvent.addListener(newroute, "click", function(latlng, index, overlay) {
        if (index == undefined) {
            if (water == true)
                waterIcon(latlng);

            else if (medic == true)
                medicIcon(latlng);
        }
        if ((typeof index == "number") || (overlay.index != "undefined")) {
            var idx = (typeof index == "number") ? index : overlay.index;
            if (idx == newroute.getVertexCount() - 1 && erase == false) {
                if (!document.getElementById("chkRua").checked)
                    newroute.enableDrawing();
            }
            else if (erase)
                erasePoint(idx); //deleta o vértice clicado
        }
    });
}

//Função para desenho manual das rotas.
function startLine() {
    $("#map_canvas").unbind("mouseover");
    if (!newroute) 
        criaRota();
    adicionaListeners();    
    newroute.enableDrawing();
    newroute.enableEditing({ onEvent: "mouseover" });
    newroute.disableEditing({ onEvent: "mouseout" });

}

//Função para desenho automatico das rotas por cima de ruas.
function startRoadLine() {
    $("#map_canvas").unbind("mouseover");
    if (newroute) {
        newroute.enableEditing();
        newroute.disableEditing();
    } else {
        criaRota();
    }
    adicionaListeners();    
    map.getDragObject().setDraggableCursor("crosshair");
    GEvent.addListener(map, "click", function(overlay, point) {
        // == When the user clicks on a the map, get directions from that point to itself ==
        if (!overlay) {
            if (newroute.getVertexCount() == 0) {
                dirn.loadFromWaypoints([point.toUrlValue(6), point.toUrlValue(6)], { getPolyline: true, travelMode: G_TRAVEL_MODE_WALKING });
            } else {
                dirn.loadFromWaypoints([newroute.getVertex(newroute.getVertexCount() - 1), point.toUrlValue(6)], { getPolyline: true, travelMode: G_TRAVEL_MODE_WALKING });
            }
        }
    });
    GEvent.addListener(dirn, "load", function() {
        // snap to last vertex in the polyline
        var ultimoPonto = null;
    
        if (newroute.getVertexCount() > 0)
            ultimoPonto = newroute.getVertex(newroute.getVertexCount() - 1);
        for (iCont = 0; iCont < dirn.getPolyline().getVertexCount(); iCont++) {
            if (ultimoPonto) {
                if ((ultimoPonto.lat() == dirn.getPolyline().getVertex(iCont).lat()) && (ultimoPonto.lng() == dirn.getPolyline().getVertex(iCont).lng()))
                    continue;
            }
            ultimoPonto = dirn.getPolyline().getVertex(iCont);
            newroute.insertVertex(newroute.getVertexCount(), ultimoPonto);
        }
    });
}

//Muda a criação das rotas do modo automatico para o manual.
function mudaModo() {
    if (document.getElementById("chkRua").checked) {
        GEvent.clearListeners(map, "click");
        GEvent.clearListeners(dirn, "load");
        adicionaListeners();        
        startRoadLine();
    }
    else {
        GEvent.clearListeners(map, "click");
        adicionaListeners();       
        startLine();
    }
}

//Função para deletar os vértices da rota
function erasePoint(vertex) {
    if (newroute != undefined) {
        if (newroute.getVertexCount() <= 1) {
            map.removeOverlay(newroute);
            map.removeOverlay(markerInicio);
            document.getElementById("rotaKM").innerHTML = "0.00";
        }
        else if (newroute.getVertexCount() == 2) {
            var ponto;
            if (vertex == 1)
                ponto = newroute.getVertex(0);
            else
                ponto = newroute.getVertex(1);

            map.removeOverlay(newroute);
            criaRota();
            newroute.insertVertex(0, ponto);
            map.addOverlay(newroute);
            map.removeOverlay(markerInicio);
            adicionaInicio();
            document.getElementById("rotaKM").innerHTML = "0.00";
        }
        else if (newroute.getVertexCount() > 2) {
            newroute.deleteVertex(vertex);
            if (vertex == 0) {
                map.removeOverlay(newroute);
                map.removeOverlay(markerInicio);
                map.addOverlay(newroute);
                adicionaInicio();
            }
        }
    }
}

//Função que adiciona marcas de "ponto de água" no mapa
function waterIcon(ltlg) {
    var waterIcon = new GIcon(G_DEFAULT_ICON);
    waterIcon.image = "../Imagens/ico_agua.png";
    waterIcon.shadow = "";
    waterIcon.iconSize = new GSize(13, 13);
    waterIcon.iconAnchor = new GPoint(5, 5);
    waterIcon.infoWindowAnchor = new GPoint(9, 2);
    
    if (ltlg != 0) {
        waterPoints[waterCounter] = new GMarker(ltlg, { icon: waterIcon });
        map.addOverlay(waterPoints[waterCounter]);
        waterCounter++;
    }
    else {
        GEvent.addListener(map, "click", function(point, src, overlay) {
            waterPoints[waterCounter] = new GMarker(src, { icon: waterIcon });
            if (water == true)
                map.addOverlay(waterPoints[waterCounter]);
            waterCounter++;
        });
    }
}

//Função que adiciona marcas de "ponto médico" no mapa
function medicIcon(ltlg) {
    var medicIcon = new GIcon(G_DEFAULT_ICON);
    medicIcon.image = "../Imagens/ico_medico.png";
    medicIcon.shadow = "";
    medicIcon.iconSize = new GSize(13, 13);
    medicIcon.iconAnchor = new GPoint(5, 5);
    medicIcon.infoWindowAnchor = new GPoint(9, 2);
    
    if (ltlg != 0) {
        medicPoints[medicCounter] = new GMarker(ltlg, { icon: medicIcon });
        map.addOverlay(medicPoints[medicCounter]);
        medicCounter++;
    }
    else {
        GEvent.addListener(map, "click", function(point, src, overlay) {
            medicPoints[medicCounter] = new GMarker(src, { icon: medicIcon });
            if (medic == true)
                map.addOverlay(medicPoints[medicCounter]);
            medicCounter++;
        });
    }
}

//Função que constrói o retorno para o ponto desejado
function routeReturn(pontoIni) {
    if (newroute != "undefined") {
        for (var i = pontoIni; i > 0; i--) 
            newroute.insertVertex(newroute.getVertexCount(i), newroute.getVertex(i - 1));
    }
    map.panTo(newroute.getVertex(0));
}

//Função que constrói o loop
function routeLoop(loopIni) {
    var newPoint = loopIni;
    var loopFim = newroute.getVertexCount();
    var newVertex = newroute.getVertexCount();
    
    while (newPoint < loopFim) {
        newroute.insertVertex(newVertex++, newroute.getVertex(newPoint));
        newPoint++;
    }
}

/***************************************************/
function showDistancePoints() {
    var points = newroute.GetPointsAtDistance(1000);

    var baseIcon = new GIcon(G_DEFAULT_ICON);
    baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
    baseIcon.iconSize = new GSize(20, 34);
    baseIcon.shadowSize = new GSize(37, 34);
    baseIcon.iconAnchor = new GPoint(9, 34);
    baseIcon.infoWindowAnchor = new GPoint(9, 2);

    for (var i = 0; i < points.length; i++) {
        var myIcon = new GIcon(baseIcon);
        myIcon.image = "../Imagens/markers/marker" + (i + 1) + ".png";
        map.addOverlay(new GMarker(points[i], myIcon, true));
    }

    var startIcon = new GIcon(baseIcon);
    startIcon.image = "http://www.google.com/mapfiles/dd-start.png";

    var endIcon = new GIcon(baseIcon);
    endIcon.image = "http://www.google.com/mapfiles/dd-end.png";

    map.addOverlay(new GMarker(newroute.getVertex(0), startIcon, true));
    map.addOverlay(new GMarker(newroute.getVertex(newroute.getVertexCount() - 1), endIcon, true));
}