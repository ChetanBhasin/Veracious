package models.messages.client
import play.api.libs.json._

/**
 * Created by basso on 08/04/15.
 *
 * Send object of this class to the client.
 *
 * @param username : The unique username of the client
 * @param msg : The final Json msg data that will be sent to the client as it is
 */
case class MessageToClient (username: String, msg: PushData) extends ClientMgrMessage
case class Push (msg: JsValue)
