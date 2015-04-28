package actors.application

import akka.actor._

/**
 * Created by basso on 24/04/15.
 * This is the Trait for the Typed actor which will give access to the app including
 * security
 */
object AppAccess {
  private def createAct (tsys: TypedActorFactory, appManager: ActorRef, name: String): AppAccess =
    tsys.typedActorOf(
      TypedProps (classOf[AppProxy], new AppProxy(appManager)), name
    )
  def apply (system: ActorSystem, appManager: ActorRef, name: String): AppAccess =
    createAct(TypedActor(system), appManager, name)
  def apply (system: ActorContext, appManager: ActorRef, name: String): AppAccess =
    createAct(TypedActor(system), appManager, name)
}

trait AppAccess {
  val appManager: ActorRef      // An actorRef for Application Manager

  /** The AppStatus should be AppRunning if you need to execute any
    * of the other methods or exceptions will be thrown
    */
  def appStatus: AppState

  /**
   * Authenticate the given user
   * @param username unique username
   * @param password
   * @return True for success and false for not
   */
  def authenticate(username: String, password: String): Boolean

  /**
   * Remove the given user from system, will first check that its authentic
   * @return "Authentication Failure" if it couldn't authenticate user
   *         or "Operation Failed" if the persistence messed up
   *         or Unit if user was removed successfully
   */
  def removeUser(username: String, password: String): Either[String, Unit]

  /**
   * Change password for a given user
   * @param username unique username
   * @param oldP Old password
   * @param newP new password
   * @return "Authentication Failure" if it couldn't authenticate user
   *         or "Operation Failed" if the persistence messed up
   *         or Unit if password was changed successfully
   */
  def changePassword(username: String, oldP: String, newP: String): Either[String, Unit]

  /**
   * Signup a new user to the system
   * @param username new unique username
   * @param password new password
   * @return "User Already Exists" if the given username is not unique
   *         or "Operation Failed" if the persistence messed up
   *         or Unit if user signed up successfully
   */
  def signUp(username: String, password: String): Either[String, Unit]

  /**
   * Check if the given user is already logged in
   * @param username the user trying to log in
   * @return true if its already logged in, else false
   */
  def alreadyLoggedIn(username: String): Boolean

  def shutdown: Unit
}
