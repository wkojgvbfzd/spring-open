/*global d3*/


var colors = [
	'color1',
	'color2',
	'color3',
	'color4',
	'color5',
	'color6',
	'color7',
	'color8',
	'color9',
	'color10',
	'color11',
	'color12',
]

var controllerColorMap = {};



function createTopologyView() {
	return d3.select('#svg-container').append('svg:svg').append('svg:svg').attr('id', 'viewBox').attr('viewBox', '0 0 1000 1000').attr('preserveAspectRatio', 'none').
			attr('id', 'viewbox').append('svg:g').attr('transform', 'translate(500 500)');
}

function updateHeader(model) {
	d3.select('#lastUpdate').text(new Date());
	d3.select('#activeSwitches').text(model.edgeSwitches.length + model.aggregationSwitches.length + model.coreSwitches.length);
	d3.select('#activeFlows').text(model.flows.length);
}

function toRadians (angle) {
  return angle * (Math.PI / 180);
}

function updateTopology(svg, model) {

	// DRAW THE NODES
	var rings = [{
		radius: 3,
		width: 6,
		switches: model.edgeSwitches,
		className: 'edge',
		angles: []
	}, {
		radius: 2.25,
		width: 12,
		switches: model.aggregationSwitches,
		className: 'aggregation',
		angles: []
	}, {
		radius: .75,
		width: 18,
		switches: model.coreSwitches,
		className: 'core',
		angles: []
	}];


	var aggRanges = {};

	// arrange edge switches at equal increments
	var k = 360 / rings[0].switches.length;
	rings[0].switches.forEach(function (s, i) {
		var angle = k * i;

		rings[0].angles[i] = angle;

		// record the angle for the agg switch layout
		var dpid = s.dpid.split(':');
		dpid[7] = '00';
		var aggdpid = dpid.join(':');
		var aggRange = aggRanges[aggdpid];
		if (!aggRange) {
			aggRange = aggRanges[aggdpid] = {};
			aggRange.min = aggRange.max = angle;
		} else {
			aggRange.max = angle;
		}


	});

	// arrange aggregation switches to "fan out" to edge switches
	k = 360 / rings[1].switches.length;
	rings[1].switches.forEach(function (s, i) {
//		rings[1].angles[i] = k * i;
		var range = aggRanges[s.dpid];

		rings[1].angles[i] = (range.min + range.max)/2;
	});

	// arrange core switches at equal increments
	k = 360 / rings[2].switches.length;
	rings[2].switches.forEach(function (s, i) {
		rings[2].angles[i] = k * i;
	});

	function ringEnter(data, i) {
		if (!data.switches.length) {
			return;
		}


		var nodes = d3.select(this).selectAll("g")
			.data(d3.range(data.switches.length).map(function() {
				return data;
			}))
			.enter().append("svg:g")
			.classed('nolabel', true)
			.attr("id", function (_, i) {
				return data.switches[i].dpid;
			})
			.attr("transform", function(_, i) {
				return "rotate(" + data.angles[i]+ ")translate(" + data.radius * 150 + ")rotate(" + (-data.angles[i]) + ")";
			});

		nodes.append("svg:circle")
			.attr('class', function (_, i)  {
				return data.className + ' ' + controllerColorMap[data.switches[i].controller];
			})
			.attr("transform", function(_, i) {
				var m = document.querySelector('#viewbox').getTransformToElement().inverse();
				if (data.scale) {
					m = m.scale(data.scale);
				}
				return "matrix( " + m.a + " " + m.b + " " + m.c + " " + m.d + " " + m.e + " " + m.f + " )";
			})
			.attr("x", -data.width / 2)
			.attr("y", -data.width / 2)
			.attr("r", data.width)
			// .attr("fill", function (_, i) {
			// 	return controllerColorMap[data.switches[i].controller]
			// })

		nodes.append("svg:text")
				.text(function (d, i) {return d.switches[i].dpid})
				.attr("x", 0)
				.attr("y", 0)
				.attr("transform", function(_, i) {
					var m = document.querySelector('#viewbox').getTransformToElement().inverse();
					if (data.scale) {
						m = m.scale(data.scale);
					}
					return "matrix( " + m.a + " " + m.b + " " + m.c + " " + m.d + " " + m.e + " " + m.f + " )";
				})

		function showLabel(data, index) {
			d3.select(document.getElementById(data.switches[index].dpid)).classed('nolabel', false);
		}

		function hideLabel(data, index) {
			d3.select(document.getElementById(data.switches[index].dpid)).classed('nolabel', true);
		}

		nodes.on('mouseover', showLabel);
		nodes.on('mouseout', hideLabel);
	}

	var ring = svg.selectAll("g")
		.data(rings)
		.enter().append("svg:g")
		.attr("class", "ring")
		.each(ringEnter);


	// do mouseover zoom on edge nodes
	function zoom(data, index) {
		var g = d3.select(document.getElementById(data.switches[index].dpid)).select('circle');
			g.transition().duration(100).attr("r", rings[0].width*3);
	}

	svg.selectAll('.edge').on('mouseover', zoom);
	svg.selectAll('.edge').on('mousedown', zoom);

	function unzoom(data, index) {
		var g = d3.select(document.getElementById(data.switches[index].dpid)).select('circle');
			g.transition().duration(100).attr("r", rings[0].width);
	}
	svg.selectAll('.edge').on('mouseout', unzoom);


	// DRAW THE LINKS
	var line = d3.svg.line()
	    .x(function(d) {
	    	return d.x;
	    })
	    .y(function(d) {
	    	return d.y;
	    })
	    .interpolate("basis");

	d3.select('svg').selectAll('path').data(model.links).enter().append("svg:path").attr("d", function (d) {
		var src = d3.select(document.getElementById(d['src-switch']));
		var dst = d3.select(document.getElementById(d['dst-switch']));

		var srcPt = document.querySelector('svg').createSVGPoint();
		srcPt.x = src.attr('x');
		srcPt.y = src.attr('y');

		var dstPt = document.querySelector('svg').createSVGPoint();
		dstPt.x = dst.attr('x');
		dstPt.y = dst.attr('y');

		return line([srcPt.matrixTransform(src[0][0].getCTM()), dstPt.matrixTransform(dst[0][0].getCTM())]);
	});
}

function updateControllers(model) {
	var controllers = d3.select('#controllerList').selectAll('.controller').data(model.controllers);
	controllers.enter().append('div')
		.attr('class', function (d) {
			var color = controllerColorMap[d];
			if (!color) {
				color = controllerColorMap[d] = colors.pop();
			}
			return 'controller ' + color;
		});
	controllers.text(function (d) {
		return d;
	});
	controllers.exit().remove();

	model.controllers.forEach(function (c) {
		d3.select(document.body).classed(controllerColorMap[c] + '-selected', true);
	});

	controllers.on('click', function (c, index) {
		var selected = d3.select(document.body).classed(controllerColorMap[c] + '-selected');
		d3.select(document.body).classed(controllerColorMap[c] + '-selected', !selected);
	});
}

var oldModel;
function sync(svg) {
	updateModel(function (newModel) {

		if (!oldModel && JSON.stringify(oldModel) != JSON.stringify(newModel)) {
			updateControllers(newModel);
			updateTopology(svg, newModel);
		} else {
			console.log('no change');
		}
		updateHeader(newModel);

		oldModel = newModel;

		// do it again in 1s
		setTimeout(function () {
			sync(svg)
		}, 1000);
	});
}

sync(createTopologyView());