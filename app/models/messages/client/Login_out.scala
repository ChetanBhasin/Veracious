package models.messages.client

/**
 * The client on startup and shutdown should send LogIn and LogOut respectively
 * mixing in the ClientMgrMessage trait.
 *
 * The Client Manager will send a corresponding LogIn and LogOut mixing in the
 * BatchProcessorMessage trait
 * @param username
 */
case class LogIn (username: String)
case class LogOut (username: String)
