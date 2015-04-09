package models.messages.client

import models.messages.GlobalBroadcast

/**
 * Created by basso on 08/04/15.
 *
 * Similar to LogIn, the client sends this on being killed
 */
case class LogOut (username: String) extends GlobalBroadcast
