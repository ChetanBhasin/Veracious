/**
* Created by Chetan Bhasin
* The purpose of this file is to render the charts from the incoming JSON data
* while automatically detecting the type of the data, what charts are to be displayed
* and how they must be displayed to the end user.
*/

/**
* Final function to be called to create a scatter plot
* on the screen with the given data
*/
function genScatterPlot(container, labels, contents) {

    // Make sure that the function is used when the window is loaded properly
    window.onload() = function() {
        // Creating the chart
        var chart = new CanvasJS.Chart(container,
        {
            title : {
                text : labels.title,
                fontFamily : "arial black",
                fontColor : "DarkSlateGrey"
            },

            // Enable the animation for pleasant surprise
            animationEnabled : true,

            axisX : {
                title : labels.xtitle
            },
            axisY : {
                title : labels.ytitle
            },

            // Data points with tooltip label all contents in one place
            data: [{
                type : "scatter",
                toolTipContent : labels.tooltip,
                dataPoints : contents
            }]
        });

        // Rendering the chart
        chart.render();
    }
}

function makeChart(incoming) {

    // Match the incoming data with all the available algorithms and act accordigley
    if (incoming.algorithm === "clustering") {
    // Matching for K-Means clustering algorithm
    // Genereate the scatter plot
        genScatterPlot("dataContainer", {
            title : incoming.title,
            xtitle : "",
            ytitle : "",
            tooltip : "Cluster {name}"
        },
        incoming.data.defaults);
    }
}