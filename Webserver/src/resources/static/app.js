var tree;

//  benötigt für Baum
var root, margin, i, diagonal, svg, zoom;

// Wird nach dem Laden der Seite aufgerufen
document.addEventListener("DOMContentLoaded", function(){
    setListeners();
    buildTree("random");
});

/**
 * Initialisiert den Baum und das Fenster
 */
function init(treeData) {
     margin = {top: 10, right: 0, bottom: 10, left: 160};
     width = Math.max(document.getElementById("diagram").getBoundingClientRect().width, 800) - margin.right - margin.left;
     height = Math.max(document.getElementById("diagram").getBoundingClientRect().height, 800)  - margin.top - margin.bottom;

     i = 0;
     duration = 750;

    tree = d3.layout.tree()
        .size([height, width]);

     diagonal = d3.svg.diagonal()
        .projection(function(d) { return [d.y, d.x]; });

     // zooming
    zoom = d3.behavior.zoom()
        .scale(1)
        .scaleExtent([0.1, 8])
        .on("zoom", zoomed);

    svg = d3.select("#diagram").append("svg")
        .attr("width", width + margin.right + margin.left)
        .attr("height", height + margin.top + margin.bottom)
        .call(zoom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");



    // rectangle for zooming and dragging
    svg.append('svg:rect')
        .attr('width', $("#diagram").innerWidth())
        .attr('height', $("#diagram").innerHeight())
        .attr('class', "drawarea")
        .attr('fill', 'white');


    d3.select(self.frameElement).style("height", "1000px");


    root = treeData[0];
    root.x0 = height / 2;
    root.y0 = 0;

    // Zeichnet den Baum
    update(root);
}

/**
 * Erlaubt das rein und rauszoomen
 */
function zoomed() {
    svg.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}

/**
 * Aktualisiert den Baum vom Knoten source aus
 * @param source Knoten von dem aus aktualisiert werden soll
 */
function update(source) {

    // Compute the new tree layout.
    let nodes = tree.nodes(root).reverse(),
        links = tree.links(nodes);

    // Normalize for fixed-depth.
    nodes.forEach(function(d) { d.y = d.depth * 180; });

    // Update the nodes…
    let node = svg.selectAll("g.node")
        .data(nodes, function(d) { return d.id || (d.id = ++i); });

    // Enter any new nodes at the parent's previous position.
    let nodeEnter = node.enter().append("g")
        .attr("class", "node")
        .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
        .on("click", click);

    nodeEnter.append("circle")
        .attr("r", 1e-6)
        .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

    nodeEnter.append("text")
        .attr("x", function(d) { return d.children || d._children ? -13 : 13; })
        .attr("dy", "0.35em")
        .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
        .text(function(d) { return d.name; })
        .style("fill-opacity", 1e-6);

    // Transition nodes to their new position.
    let nodeUpdate = node.transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

    nodeUpdate.select("circle")
        .attr("r", 10)
        .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

    nodeUpdate.select("text")
        .style("fill-opacity", 1);

    // Transition exiting nodes to the parent's new position.
    let nodeExit = node.exit().transition()
        .duration(duration)
        .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
        .remove();

    nodeExit.select("circle")
        .attr("r", 1e-6);

    nodeExit.select("text")
        .style("fill-opacity", 1e-6);

    // Update the links…
    let link = svg.selectAll("path.link")
        .data(links, function(d) { return d.target.id; });

    // Enter any new links at the parent's previous position.
    link.enter().insert("path", "g")
        .attr("class", "link")
        .attr("d", function(d) {
            var o = {x: source.x0, y: source.y0};
            return diagonal({source: o, target: o});
        });

    // Transition links to their new position.
    link.transition()
        .duration(duration)
        .attr("d", diagonal);

    // Transition exiting nodes to the parent's new position.
    link.exit().transition()
        .duration(duration)
        .attr("d", function(d) {
            var o = {x: source.x, y: source.y};
            return diagonal({source: o, target: o});
        })
        .remove();

    // Stash the old positions for transition.
    nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
    });
}

// Zeigt/Versteckt Kinder von d beim Klick
function click(d) {
    if (!d.children && !d._children) {
        getChildren(d);
    }
    else if (d.children) {
        d._children = d.children;
        d.children = null;
    } else {
        d.children = d._children;
        d._children = null;
    }
    update(d);
}

/**
 *  Fügt ein Kind an einen Parentknoten an
 * @param parent JSON Objekt des Parentknotens
 * @param node Name des Kindes als String
 */
function addNode(parent, node) {
    let newNode = {
        'name': node.toString(),
        'id' :  ++i,
        'depth': parent.depth + 1,
        'height': parent.height - 1,
        'children': [],
        '_children':null,
        'parent': parent
    };

    if (!parent.children) {
        parent.children = [];
        parent._children = [];
    }
    parent.children.push(newNode);

    update(parent);
}

/**
 * Fügt alle in der DB gefundenen Kinder an dem Parent Knoten an
 * @param parent parent JSON Objekt des Parentknotens
 */
function getChildren(parent) {
    $.get('./api?parent=' + parent.name).then(function(result) {
        resultJSON = JSON.parse(result);
        for(let item of resultJSON) {
            if (item == "null") {
                return
            }
            addNode(parent, item);
        }
    });
}

/**
 * Baut den Baum von Knoten root aus auf
 * @param root Wurzelknoten als String
 */
function buildTree(root) {
    $("svg").remove();
    $.get('./api?root=' + root).then(function(result) {
        init(JSON.parse(result));
    });
}

/**
 * Setzt benötigte Listeners für Buttons
 */
function setListeners() {
    $("#searchButton").click(function () {
        query = $("#searchField").val()
        buildTree(query);
    });

    $("#sugButton1, #sugButton2, #sugButton3, #sugButton4").click(function () {
        buildTree($(this).text());
    });

    $("#sugButton5").click(function () {
        // API gibt zufälligen Begriff
        buildTree("random");
    });

    $("#searchField").keyup(function(event) {
        if (event.keyCode === 13) {
            $("#searchButton").click();
        }
    });
}
