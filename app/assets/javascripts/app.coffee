# Main javascript file for the application
# contains the angular module and its main controller

receivers = []      # Set of receiving functions

app = angular.module('Veracious', [])

# Navigation Controller
app.controller 'NavigationController', () ->
    this.visible = "logging"

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
    #$scope.showCtrl = true          # Show this controller by default
    # Parts: #loggingWell -> The area of the log
    loggingWell = $("#loggingWell")[0];
    $scope.logs = []

    # simple match for status
    $scope.isStatus = (status, log) ->
        log.status == status

    #$scope.test = () ->
    #    $scope.logs.push status: "SUCCESS", message: "Helloooo", activity: "Yohoooo"
    #    console.log "DEBUG: running test, this.logs: "+JSON.stringify($scope.logs)

    # Okay, trying out an IDEA
    # Let us see, If we want to separate everything into different controllers, we need a common receiver for the
    # web-socket...
    # Now, each controller decides to make a function for itself (like a scala partial function) that accepts its kind of
    # data and declares true if it is or else declares false
    # If we add such receiver functions into an array and then make a master receiver that goes through the array with
    # the received server data and stopping till it gets a true, then it might just work right??

    # UPDATE, got it to work, FINALLLY!!
    $scope.receiveFunction = (data) -> $scope.$apply () ->
        if data.logs or data.log
            if data.log
                $scope.logs.push(data.log)
            else $scope.logs = data.logs
            loggingWell.scrollTop = loggingWell.scrollHeight
            true
        else
            false

    #$scope.toggle = (b) -> $scope.$apply () -> $scope.showCtrl = b

    receivers.push($scope.receiveFunction)     # Add this receiver to the
    #toggleFunc.logging = $scope.toggle

    return

# Batch Controller
app.controller 'BatchController', ($scope) ->

    # Batch Manipulation
    newJob = () ->
        opType: ""
        opName: ""
        optionalTextParam: ""
        textParams: []
        numParams: []

    $scope.currentJob = newJob()

    # -------------- Opertations ---------------
    $scope.operations = [
        { name: "MnALS", pretty: "ALS mining" },
        { name: "MnClustering", pretty: "Cluster Mining" },
        { name: "MnFPgrowth", pretty: "FP growth algorithm" },
        { name: "MnSVM", pretty: "State Vector Machine" },
        { name: "DsAddDirect", pretty: "Upload data-set" },
        { name: "DsAddFromUrl", pretty: "Upload data-set from URL" },
        { name: "DsDelete", pretty: "Delete data-set" },
        { name: "DsRefresh", pretty: "Refresh data-set" }]

    $scope.getPretty = (opName) ->
        return op.pretty for op in $scope.operations when op.name is opName

    $scope.algorithms = $scope.operations[0...4]

    $scope.operationTypes = [
        { name: "MineOp", pretty: "Mining Operations"},
        { name: "DataSetOp", pretty: "Data-set Operations"}]

    $scope.getGroup = (op) ->
        if not op then {}
        else if op.substr(0,2) == "Mn"
            $scope.operationTypes[0]
        else $scope.operationTypes[1]

    $scope.checkName = (name) ->
        $scope.currentJob.opName == name

    # Setup batch here -----------------------------------
    $scope.batch = []
    finaliseJob = (job) ->
        job.opType = $scope.getGroup(job.opName).name
        if (job.optionalTextParam == "")
            delete job.optionalTextParam
        if (job.opName == "DsAddDirect")
            job.file = $("#dsFile")[0].files[0]

        if (job.opName == "DsAddDirect" || job.opName == "DsAddFromUrl")
            optimisticDsList.push({name: job.textParams[0], algo: job.textParams[2]})
        # More
        return job

    $scope.addToBatch = () ->
        $scope.batch.push( finaliseJob($scope.currentJob) )
        $scope.currentJob = newJob()

    $scope.clearBatch = () ->
        $("#batchDisplay").remove()
        $scope.batch = []
        optimisticDsList = []

    createUFormData = (batch) ->
        formData = new FormData()
        for job, i in batch
            str = "jobs[#{i}]."
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

    $scope.submitBatch = () ->
        # call the method on window from connect
        fData = createUFormData ($scope.batch)
        window.submitBatch fData, (status) ->
            if status == 200 then alert "Batch submitted successfully"
            else alert "There was a problem submitting the batch, status: "+status
        $scope.clearBatch()
        return
    #   ----------------------------------------------------

    # ----------- Data-set manipulation
    #$scope.dsList = []                     # Actual Ds list from server
    $scope.dsList = [
        { name: "SampleDsForALS", algo: "MnALS", url: "http://som.sdf.com" },
        { name: "SampleDsForFP", algo: "MnFPgrowth" },
        { name: "SampleDsForALS", algo: "MnALS", url: ""},
        { name: "SampleDsForClustering", algo: "MnClustering" },
        { name: "SampleDsForSVM", algo: "MnSVM", url: "https://www.google.com" },
        { name: "SampleDsForALS", algo: "MnALS" } ]
    optimisticDsList = []           # Names of ds that are entered from previous Job

    $scope.getAllDs = () -> $scope.dsList.concat(optimisticDsList)
    $scope.refreshables = () ->
        ds.name for ds in $scope.dsList when ds.source != ""

    $scope.getValidDs = (algoName) ->
        ds.name for ds in $scope.getAllDs() when ds.algo is algoName && ds.status == "available" # TODO: confirm

    getDataSets = (dsList) ->            # filters out the result types
        ds for ds in dsList when ds.type == "dataset" && ds.status != "removed" #TODO: confirm

    $scope.receiveFunction = (data) -> $scope.$apply () ->
        if data.datasets
            $scope.dsList = convertDataSets getDataSets data.datasets
        false   # The other controller needs this data


    receivers.push($scope.receiveFunction)     # Add this receiver to the line
    return

## A conversion function to make up for the difference in API
convertDataSets = (dsList) ->        # convert each data-set to correct format
    for ds in dsList
        ds.algo = switch ds.algo
            when "clustering" then "MnClustering"
            when "svm" then "MnSVM"
            when "als" then "MnALS"
            else "MnFPgrowth"
# ---------------------------------


app.controller 'ResultController', () ->
    $scope.results = []

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
