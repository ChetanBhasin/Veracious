/** Contains the angular module */
(function(){
    var app = angular.module('Veracious', [])
    // Now controllers

    app.controller('MainController', function(){
    /* ----- Logging Methods ------- */
    /* format: { status: String E { SUCCESS, FAILURE, WARNING, INFO },
                 activity: String E {
                            Batch:$id::$date::numberOfJobs-$n,
                            Job:$id:$description::$k-$v,[$k-$v,..]
                            },
                 message: String
               } */
    /* Parts: #loggingWell -> The area of the log */

      var loggingWell = $("#loggingWell")[0];
      this.logs = []
      this.setLogs = function(serverLogs) {
        this.logs = serverLogs;
        loggingWell.scrollTop = loggingWell.scrollHeight
      }
      this.addLog = function(serverlog) {
        this.logs.push(serverlog);
        loggingWell.scrollTop = loggingWell.scrollHeight
      }
      this.isStatus(status, log) {
        return log.status === status;
      }

    /* ----------------------------- */

    })    // End of MainController

    // TODO: I think the batch submission effort can be put into
    // a separate controller
})();