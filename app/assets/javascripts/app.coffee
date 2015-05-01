app = angular.module('Veracious', [])

app.controller('MainController', () ->
    # ----- Logging Methods ------- #
    ###
    format: { status: String E { SUCCESS, FAILURE, WARNING, INFO },
                 activity: String E {
                            Batch:$id::$date::numberOfJobs-$n,
                            Job:$id:$description::$k-$v,[$k-$v,..]
                            },
                 message: String
               }
    ###
    # Parts: #loggingWell -> The area of the log
    loggingWell = $("#loggingWell")[0];
    this.logs = []

    ## Set initial logs or add consectutive logs. Needes the event.data from socket
    this.AddSetLogs = (serverData) ->
        if serverData.logs
            this.logs = serverData.logs
        else
            this.logs.push(serverData.log)
        loggingWell.scrollTop = loggingWell.scrollHeight
        return

    this.isStatus = (status, log) ->
        log.status == status
)
