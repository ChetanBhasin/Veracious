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

        // Make sure that the function is used when the window is loaded properly
        // Creating the chart
        var chart = new CanvasJS.Chart(container, {
            title: {
                text: labels.title,
                fontFamily: "arial black",
                fontColor: "DarkSlateGrey"
            },

            // Enable the animation for pleasant surprise
            animationEnabled: true,

            axisX: {
                title: labels.xtitle
            },
            axisY: {
                title: labels.ytitle
            },

            // Data points with tooltip label all contents in one place
            data: [{
                type: "scatter",
                toolTipContent: labels.tooltip,
                dataPoints: contents
            }]
        });

        // Rendering the chart
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
            axisX: {
                title: labels.xtitle
            },
            axisY: {
                title: labels.ytitle
            },
            data: [{
                type: "column", //change type to bar, line, area, pie, etc
                dataPoints: contents
            }]
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
                title: incoming.title,
                xtitle: "",
                ytitle: "",
                tooltip: "Cluster {name}"
            },
            incoming.data.defaults);
    } else if (incoming.algorithm == "fpm") {
        // Matching for FP-Growth algorithm
        // Generating the bar chart
        chartGen.genBarChart(containerid, {
            title: incoming.name,
            xtitle: "Itemsets",
            ytitle: "Frequence"
        }, incoming.data);
    }
}