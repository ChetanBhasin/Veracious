/**
 * Created by Chetan Bhasin
 * The purpose of this file is to render the charts from the incoming JSON data
 * while automatically detecting the type of the data, what charts are to be displayed
 * and how they must be displayed to the end user.
 */
/**
 * Usage guide:
 * makeChart(<incoming_object>, <id of container div>);
 */
/**
 * Final function to be called to create a scatter plot
 * on the screen with the given data
 */
var chartGen = {
    genScatterPlot: function(container, labels, contents) {

    var chart = new CanvasJS.Chart(container,
    		{
    			title:{
    				text: labels.title,
    				fontFamily: "arial black",
    				fontColor: "DarkSlateGrey"
    			},
    			exportEnabled: true,
                animationEnabled: true,
    			axisX: {
    				title: labels.xtitle,
    				titleFontFamily: "arial"

    			},
    			axisY:{
    				title: labels.ytitle,
    				titleFontFamily: "arial",
    				valueFormatString:"",
    				titleFontSize: 12
    			},

    			data: [
    			{
    			    type : "scatter",
    			    tooltip : labels.tooltip,
    			    dataPoints : contents
    			}
    			]
    		});

    chart.render();
    },

    /**
     * Final function to be called to create a bar chart with
     * lables on X-axis on the screen with given data
     */
    genBarChart: function(container, labels, contents) {
        var chart = new CanvasJS.Chart(container, {
            animationEnabled: true,
            title: {
                text: labels.title,
                fontFamily: "arial black",
                fontColor: "DarkSlateGrey"
            },
            exportEnabled: true,
            axisX: {
                title: labels.xtitle
            },
            axisY: {
                title: labels.ytitle
            },
            data: [{
                type: "column", //change type to bar, line, area, pie, etc
                dataPoints: contents
            },
            {
                type: "line",
                dataPoints: contents
            }
            ]
        });

        chart.render();
    }
}

/**
 * Function to finally create the  hart with the incoming data
 */
function makeChart(incoming, containerid) {

    // Match the incoming data with all the available algorithms and act accordigley
    if (incoming.algorithm == "clustering") {
        // Matching for K-Means clustering algorithm
        // Genereate the scatter plot
        chartGen.genScatterPlot(containerid, {
                title: incoming.name,
                xtitle: "",
                ytitle: "",
                tooltip: "Cluster {name}"
            },
            incoming.data);
    } else if (incoming.algorithm == "fpm") {
        // Matching for FP-Growth algorithm
        // Generating the bar chart
        chartGen.genBarChart(containerid, {
            title: incoming.name,
            xtitle: "Itemsets",
            ytitle: "Frequence"
        }, incoming.data);
    } else if (incoming.algorithm === "svm") {
        // Matching for SVM
        // Generating scatter plot
        chartGen.genScatterPlot(containerid, {
            title: incoming.name,
            xtitle: "",
            ytitle: "",
            tooltip: "Point {name}"
        }, incoming.data)
    }
}