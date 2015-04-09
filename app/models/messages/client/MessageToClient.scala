package models.messages.client

import play.libs.Json

/**
 * Created by basso on 08/04/15.
 *
 * Send object of this class to the client.
 *
 * @param username : The unique username of the client
 * @param msg : The final Json msg data that will be sent to the client as it is
 */
class MessageToClient (username: String, msg: Json)
