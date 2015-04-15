package models.messages.logger

/**
 * Created by basso on 09/04/15.
 *
 * This is the activity being logged
 */
trait LogEvent {
  def logWrite: String      // description of the activity
}
