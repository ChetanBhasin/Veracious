package models.messages.client

import models.messages.GlobalBroadcast

/**
 * Created by basso on 08/04/15.
 *
 * When a client logs in, the web-socket actor once created, must send this message
 * to the mediator which will broadcast it.
 *
 * A number of sub-systems including the logger, batch operator etc are interested
 * in the client's lifecycle.
 */
case class LogIn (username: String) extends GlobalBroadcast
