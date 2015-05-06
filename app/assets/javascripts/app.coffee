# Main javascript file for the application
# contains the angular module and its main controller

# Recievers
# A global set of receivers is in action which takes the data from an incomming we-socket push
# One by one, each function in this array is evaluated against the data and the bolean result is taken. If true, the sequence stops
# so, each function may do whatever it needs with the data, and if the data happens to be exclusive for it, then it returns true else false
receivers = []      # Set of receiving functions

app = angular.module('Veracious', [])

# Navigation Controller
# There are 4 sections of the interface:
#   Logging: Where the user logs are constantly being displayed. Its directly tied to the LogController
#   Data: Here the user data is shown. Which icludes the source data-sets (from the BatchController) and result data-sets (from the ResultController)
#   Batch: This section is used to create and submit batches. Tied to the BatchController
#   Result: This section is used to submit result requests and generate charts. Tied to the ResultController
app.controller 'NavigationController', () ->
    this.visible = "logging"        # We start with logging

    this.setLogging = () -> this.visible = "logging"
    this.setData = () -> this.visible = "data"      # Will have data-sets and results
    this.setBatch = () -> this.visible = "batch"
    this.setResult = () -> this.visible = "result"

    this.isLogging = () -> this.visible == "logging"
    this.isData = () -> this.visible == "data"
    this.isBatch = () -> this.visible == "batch"
    this.isResult = () -> this.visible == "result"

# Logging Controller
app.controller 'LogController', ($scope) ->
    ###
    format: { status: String E { SUCCESS, FAILURE, WARNING, INFO },
                 activity: String E {
                            Batch:$id::$date::numberOfJobs-$n,
                            Job:$id:$description::$k-$v,[$k-$v,..]
                            },
                 message: String
               }
    ###

    loggingWell = $("#loggingWell")[0];     # The div id of the area of log
    $scope.logs = []                        # Actual logs data

    # simple match for status
    $scope.isStatus = (status, log) ->
        log.status == status

    # Now for the recieve method
    # It needs to call $scope.$apply or else the changes to scope may not be digested
    # by the model immediately as this function will be called outside the control of angularJs
    $scope.receiveFunction = (data) -> $scope.$apply () ->
        if data.logs or data.log                                # we only accept logs or log messages
            if data.log
                $scope.logs.push(data.log)
            else $scope.logs = data.logs
            loggingWell.scrollTop = loggingWell.scrollHeight    # scroll to the bottom of the well
            true
        else
            false

    receivers.push($scope.receiveFunction)     # Add this receiver to the global queue
    return


# A global set for operations
operations = [
    { name: "MnALS", pretty: "ALS mining" },
    { name: "MnClustering", pretty: "Cluster Mining" },
    { name: "MnFPM", pretty: "FP growth algorithm" },
    { name: "MnSVM", pretty: "Support Vector Machine" },
    { name: "DsAddDirect", pretty: "Upload data-set" },
    { name: "DsAddFromUrl", pretty: "Upload data-set from URL" },
    { name: "DsDelete", pretty: "Delete data-set" },
    { name: "DsRefresh", pretty: "Refresh data-set" }]

# Batch Controller
# This heavy weight is responsible for handling batch creations, submissions and data-source updates
app.controller 'BatchController', ($scope) ->
    # -------------- Opertations ---------------
    $scope.operations = operations      # Op names

    # Get full name for UI perposes
    $scope.getPretty = (opName) ->      # this function needs to be present in the Result controller as well, but with a different implementation
        return op.pretty for op in $scope.operations when op.name is opName

    $scope.algorithms = $scope.operations[0...4]        # Algorithm names,official

    $scope.operationTypes = [          # OpTypes
        { name: "MineOp", pretty: "Mining Operations"},
        { name: "DataSetOp", pretty: "Data-set Operations"}]

    $scope.getGroup = (op) ->          # Finding the opType from opName
        if not op then {}
        else if op.substr(0,2) == "Mn"
            $scope.operationTypes[0]
        else $scope.operationTypes[1]

    # Batch Manipulation
    newJob = () ->
        opType: ""
        opName: ""
        optionalTextParam: ""
        textParams: []
        numParams: []

    $scope.currentJob = newJob()        # We manipulate the current job to get the dynamic form

    $scope.checkName = (name) ->        # for UI perposes
        $scope.currentJob.opName == name

    # Setup batch here -----------------------------------
    $scope.batch = []                   # basically we add currentJob to this
    optimisticDsList = []           # Names of ds that are entered from previous Job

    # We did a lot of jugad, now we formalize the job description so that the batch
    # is automatically submission ready
    finaliseJob = (job) ->
        job.opType = $scope.getGroup(job.opName).name
        if (job.optionalTextParam == "")    # The optionalTextParam is marked as Option[String] at server. A missing attribute will give None
            delete job.optionalTextParam
        if (job.opName == "DsAddDirect")    # The only operation where we allow for a file upload, we need to handle that appropriately
            job.file = $("#dsFile")[0].files[0]

        # Adding a data-set means that the user may require it for a subsequent job in the batch
        # so we add the data-set name to optimisticDsList (optimistic because we have no garuntee that the ds operation will succeed)
        if (job.opName == "DsAddDirect" || job.opName == "DsAddFromUrl")
            optimisticDsList.push({name: job.textParams[0], algo: job.textParams[2], status: "available" })
        return job

    $scope.addToBatch = () ->       # Add the job to the batch on button click
        $scope.batch.push( finaliseJob($scope.currentJob) )
        alert "$scope.batch = "+JSON.stringify($scope.batch)
        $scope.currentJob = newJob()

    $scope.clearBatch = () ->       # clear the current batch and lets start over. on button click
        $("#batchDisplay").remove() # this was a trial hack that failed. I think we can delete this line, todo
        $scope.batch = []
        optimisticDsList = []

    # Convert the batch to a multi-part form data in compliance with the joblist mapping in models.batch.job
    createUFormData = (batch) ->
        formData = new FormData()
        for job, i in batch
            str = "jobs[#{i}]."                         # Don't ask, got this trough trial and error
            formData.append(str+"opType", job.opType)
            formData.append(str+"opName", job.opName)
            for text in job.textParams
                formData.append(str+"textParams[]", text)
            job.numParams.push(1) # just to be safe, a hack
            for num in job.numParams
                formData.append(str+"numParams[]", num)
            if (job.file)
                formData.append(str+"file", job.file, job.file.name)
            if (job.optionalTextParam)
                formData.append(str+"optionalTextParam", job.optionalTextParam)
        return formData

    # Lets make the official submission
    $scope.submitBatch = () ->
        fData = createUFormData ($scope.batch)
        window.submitBatch fData, (status) ->               # from connect.js
            if status == 200 then alert "Batch submitted successfully"
            else alert "There was a problem submitting the batch, status: "+status
        $scope.clearBatch()
        return
    #   ----------------------------------------------------

    # ----------- Data-set manipulation
    $scope.dsList = []                     # Actual Ds list from server
    #$scope.dsList = [                       # Sample dsList for testing perposes
    #    { name: "SampleDsForALS", algo: "MnALS", desc: "Short description for the set", type: "dataset", status: "available", source: "http://som.sdf.com" },
    #    { name: "SampleDsForFP", desc: "Short description for the set", type: "dataset", status: "unavailable",  algo: "MnFPgrowth" },
    #    { name: "SampleDsForALS", algo: "MnALS", desc: "Short description for the set", type: "dataset", status: "available",  source: ""},
    #    { name: "SampleDsForClustering", desc: "Short description for the set", type: "dataset", status: "available",  algo: "MnClustering" },
    #    { name: "SampleDsForSVM", desc: "Short description for the set", type: "dataset", status: "unavailable",  algo: "MnSVM", source: "https://www.google.com" },
    #    { name: "SampleDsForALS", desc: "Short description for the set", type: "dataset", status: "removed",  algo: "MnALS" } ]


    $scope.getAllDs = () -> $scope.dsList.concat(optimisticDsList)
    $scope.refreshables = () ->
        ds.name for ds in $scope.dsList when ds.source != ""

    # this is for selecting data-sets for a perticular algorithm.. the system is a little fool proof you know :D
    $scope.getValidDs = (algoName) ->
        ds.name for ds in $scope.getAllDs() when ds.algo is algoName && ds.status == "available"

    getDataSets = (dsList) ->            # filters out the result types
        ds for ds in dsList when ds.type == "dataset" && ds.status != "removed"


    # The BatchController's receiver
    $scope.receiveFunction = (data) -> $scope.$apply () ->
        if data.datasets                                        # Only concerned with the data-set list
            $scope.dsList = getDataSets data.datasets      # conversions necessary because of API difference (algo naming), courtesy of @Chetan
        false   # The other controller needs this data

    receivers.push($scope.receiveFunction)     # Add this receiver to the line
    return

## A conversion function to make up for the difference in API TODO, no need now
#
#getOfficialName = (opname) ->           # Official as per me you know
#    switch opname
#        when "clustering" then "MnClustering"
#        when "svm" then "MnSVM"
#        when "als" then "MnALS"
#        else "MnFPgrowth"
#
#convertDataSets = (dsList) ->        # convert each data-set to correct format
#    for ds in dsList
#        ds.algo = getOfficialName(ds.algo)
# ---------------------------------


# Simple controller to show results, and request visualisation
app.controller 'ResultController', ($scope) ->

    chartMaker = new dataDisplay("chartCanvas")
    $scope.results = []
    #$scope.results = [
    #    { name: "JobAres", algo: "als", desc: "Short description for the set", type: "dataset", status: "available", source: "http://som.sdf.com" },
    #    { name: "Some other res", desc: "Short description for the set", type: "dataset", status: "unavailable",  algo: "fpm" },
    #    { name: "res3", algo: "als", desc: "Short description for the set", type: "dataset", status: "available",  source: ""},
    #    { name: "BatchRes3", desc: "Short description for the set", type: "dataset", status: "available",  algo: "clustering" },
    #    { name: "sampless", desc: "Short description for the set", type: "dataset", status: "unavailable",  algo: "svm", source: "https://www.google.com" },
    #    { name: "res321", desc: "Short description for the set", type: "dataset", status: "removed",  algo: "als" } ]

    $scope.getAvailResults = () ->
        res for res in $scope.results when res.status is "available"

    $scope.getPretty = (opName) ->
        return op.pretty for op in operations when op.name is opName

    $scope.receiveFunction = (data) -> $scope.$apply () ->
        if data.datasets
            $scope.results = (ds for ds in data.datasets when ds.type is "result")
            true
        else if data.result
            chartMaker.makeChart(data.result)
            true
        else false   # The other controller needs this data

    $scope.target = ""

    $scope.submit = () ->
        # call window function
        console.log "Calling submit with #{JSON.stringify($scope.target)}"
        #formData = new FormData()
        #formData.append("datasetName", $scope.target)
        formData = { datasetName: $scope.target }
        window.submitResultRequest formData, (status) ->
            if status == 200 then alert "Request submitted successfuly"
            else alert "Server error on receiving request"
        $scope.target = ""

    receivers.push($scope.receiveFunction)     # Add this receiver to the line
    return

# Now for setting up the websocket connection
testReceiver = (data) ->
    if (data.test)
        #console.log "Testing message : "+JSON.stringify(data.test)
        true
    else false

receivers.push(testReceiver)

masterReceive = (data) ->
    for receiver in receivers
        if receiver data
            break
    return

onWSclose = () ->       # TODO, implement
    console.log "Closing application"

onWSerror = (d) ->
    console.log "Some error occured"+ JSON.stringify d

webSocket = {}

wsCallBack = (ws) -> webSocket = ws
window.connectToApp {
    onMessage: masterReceive
    onError: onWSerror
    onClose: onWSclose
}, wsCallBack
