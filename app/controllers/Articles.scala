package controllers

import play.modules.reactivemongo.MongoController
import play.api.mvc.{ Action, Controller }
import models.Article
import reactivemongo.bson.{ BSONString, BSONDateTime, BSONObjectID, BSONDocument }
import reactivemongo.bson.DefaultBSONHandlers._
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.DateTime
import models.Article.ArticleBSONWriter
import scala.concurrent.Future

object Articles extends Controller with MongoController {

  def index = Action.async {
    implicit val reader = Article.ArticleBSONReader
    //empty query to match all the documents
    val query = BSONDocument()
    val articles = collection.find(query).cursor[Article]
    val list = articles.collect[List]()
    // build (asynchronously) a list containing all the articles
    list map (articles => Ok(views.html.index(articles)))
  }

  def showCreationForm = Action {
    Ok(views.html.editArticle(None, Article.form, None))
  }

  def create = Action.async { implicit request =>
    implicit val writer = ArticleBSONWriter
    Article.form.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.editArticle(None, errors, None))),
      article => {
        val updatedArticle = article.copy(creationDate = Some(new DateTime()), updateDate = Some(new DateTime()))
        collection.insert(updatedArticle) map (_ => Redirect(routes.Articles.index()))
      }
    )
  }

  def collection: BSONCollection = db[BSONCollection]("articles")

  /**
   * we should fetch the article matching the id.
   * If an article is found, the result will be the rendered template; or else it will be NotFound.
   * @param id
   * @return
   */
  def showEditForm(id: String) = Action.async {
    implicit val reader = Article.ArticleBSONReader
    val collection = db[BSONCollection]("articles")
    // get the documents having this id (there will be 0 or 1 result)
    val cursor = collection.find(BSONDocument("_id" -> BSONObjectID(id))).cursor[Article]
    // get a future option of article
    cursor.headOption map {
      case Some(article) => Ok(views.html.editArticle(article.id map (_.stringify), Article.form.fill(article), None))
      case None => NotFound
    }
  }

  def edit(id: String) = Action.async { implicit request =>
    Article.form.bindFromRequest.fold(
      errors => Future.successful(Ok(views.html.editArticle(Some(id), errors, None))),
      article => {
        val objectId = new BSONObjectID(id)
        // create a modifier document,
        // ie a document that contains the update operations to run onto the documents matching the query
        val modifier = BSONDocument(
          // this modifier will set the fields 'updateDate', 'title', 'content', and 'publisher'
          "$set" -> BSONDocument(
            "updateTime" -> BSONDateTime(new DateTime().getMillis),
            "title" -> BSONString(article.title),
            "content" -> BSONString(article.content),
            "publisher" -> BSONString(article.publisher)
          )
        )
        collection.update(BSONDocument("_id" -> objectId), modifier).map(_ => Redirect(routes.Articles.index))

      }
    )
  }

}