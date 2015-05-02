@(conStr: String)

/* This file contains the web-socket connection
function to be used by the required angular controller

the username to connect to will be given by the session
*/

(function(){

  /**
   * @@param config The configuration object for the web-socket connection.
   *               contains the onMessage, onError, onClose functions
   * @@param callback This is the function that will be called back once the socket
   *                 is established. It will receive an object:
   *                 {  send: function( json ) => {},
   *                    close: function() => {}  }
   */
  window.connectToApp = function ( config, callback ) {

    /* ------- Initialise the socket -------- */
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
    var socket = new WS('@conStr');

    /* ------- Attach onMessage function, converting the json value to string */
    if (config.onMessage) {
      socket.onmessage = function(event) {
          config.onMessage(JSON.parse(event.data))
      }
    }

    /* ------ Attach the onError and onClose functions if they exist */
    if (config.onError) socket.onerror = config.onError
    if (config.onClose) socket.onclose = config.onClose

    /* ------ Call back with the result object which contains a send
    function to take json and send it serialised */
    callback({
      send: function(obj) { socket.send(JSON.stringify(obj)) },
      close: function() { socket.close() }
    })
  };
})();