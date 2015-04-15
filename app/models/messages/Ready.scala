package models.messages

/**
 * Created by basso on 10/04/15.
 *
 * The Application manager actor receives this message. One from each of the major
 * sub-systems after they are initielised. After all the Ready messages are received,
 * the application will be open to client connections
 */
case class Ready (subsys: String)
