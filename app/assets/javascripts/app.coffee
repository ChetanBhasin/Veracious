# Main javascript file for the application
# contains the angular module and its main controller

receivers = []      # Set of receiving functions


app = angular.module('Veracious', [])

app.controller('LogController', () ->
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
    # this.AddSetLogs = (serverData) ->
    #     if serverData.logs
    #         this.logs = serverData.logs
    #     else
    #         this.logs.push(serverData.log)
    #     loggingWell.scrollTop = loggingWell.scrollHeight
    #     return

    # simple match for status
    this.isStatus = (status, log) ->
        log.status == status

    # Okay, trying out an IDEA
    # Let us see, If we want to separate everything into different controllers, we need a common receiver for the
    # web-socket...
    # Now, each controller decides to make a function for itself (like a scala partial function) that accepts its kind of
    # data and declares true if it is or else declares false
    # If we add such receiver functions into an array and then make a master receiver that goes through the array with
    # the received server data and stopping till it gets a true, then it might just work right??

    receiveFunction = (data) ->     # TODO:check if the model update reflects correctly on the view
        if data.logs or data.log
            if data.log
                this.logs.push(data.log)
            else this.logs = data.logs
            loggingWell.scrollTop = logginWell.scrollHeight
            true
        else false

    receivers.push(receiveFunction)     # Add this receiver to the

    return
)

app.controller('BatchController', () -> )       # TODO, implement
app.controller('DataController', () -> )

# Now for setting up the websocket connection
masterReceive = (data) ->
    for receiver in receivers
        if receiver data
            break
    return

onWSclose = () ->       # TODO, implement
    alert "Closing application"

onWSerror = (d) ->
    alert "Some error occured"+ JSON.stringify d

webSocket {}

wsCallBack = (ws) -> webSocket = ws
window.connectToApp {
    onMessage: masterReceive
    onError: onWSerror
    onClose: onWSclose
}, wsCallBack
