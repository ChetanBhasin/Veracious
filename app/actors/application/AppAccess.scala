package actors.application

/**
 * Created by basso on 12/04/15.
 *
 * Trait for the Typed Actor proxy
 */
trait AppAccess {

  /** Get the status of the the application
    * New user logins should only be allowed when the application status is AppRunning
    * During AppSetup, Nothing should happen. Similarly, during AppFinish, all connections
    * should be terminated
    */
  def status: AppState


  // TODO: have to figure out user authentication
  def authenticate (user: String, pass: String): Boolean
  def signUp (user: String, pass: String): Unit
  def userExists (user: String): Boolean
}
