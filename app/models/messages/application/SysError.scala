package models.messages.application

/**
 * This is the error class which will be sent to the mediator. Preferably, the application
 * managing actor will handle handle these messages
 * @param subSys
 * @param msg
 */
case class SysError (subSys: String, msg: String)
