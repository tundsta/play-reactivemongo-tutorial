package models
import play.api.data.validation.Constraints._
import play.api.data.Forms._
import reactivemongo.bson._
import org.joda.time.DateTime
import reactivemongo.bson.BSONString
import play.api.data.Form
import play.api.data.format.Formats._

case class Article(
  id: Option[BSONObjectID],
  title: String,
  content: String,
  publisher: String,
  creationDate: Option[DateTime],
  updateDate: Option[DateTime])

object Article {
  implicit object ArticleBSONReader extends BSONDocumentReader[Article] {
    def read(document: BSONDocument): Article = {
      Article(
        document.getAs[BSONObjectID]("_id"),
        document.getAs[BSONString]("title").get.value,
        document.getAs[BSONString]("content").get.value,
        document.getAs[BSONString]("publisher").get.value,
        document.getAs[BSONDateTime]("creationDate") map (dt => new DateTime(dt.value)),
        document.getAs[BSONDateTime]("updateDate") map (dt => new DateTime(dt.value))
      )
    }
  }

  implicit object ArticleBSONWriter extends BSONDocumentWriter[Article] {
    def write(article: Article) = {
      val bson = BSONDocument(
        "_id" -> article.id.getOrElse(BSONObjectID.generate),
        "title" -> BSONString(article.title),
        "content" -> BSONString(article.content),
        "publisher" -> BSONString(article.publisher)
      )

      Seq(
        article.creationDate map (cd => "creationDate" -> BSONDateTime(cd.getMillis)),
        article.updateDate map (ud => "updateDate" -> BSONDateTime(ud.getMillis))
      ).flatten.foldLeft(bson)((a, b) => a ++ b)
    }
  }

  val form = Form(
    mapping(
      "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
      "title" -> nonEmptyText,
      "content" -> text,
      "publisher" -> nonEmptyText,
      "creationDate" -> optional(of[Long]),
      "updateDate" -> optional(of[Long])
    ) { (id, title, content, publisher, creationDate, updateDate) =>
        Article(
          id.map(new BSONObjectID(_)),
          title,
          content,
          publisher,
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_)))
      } { article =>
        Some(
          (article.id.map(_.stringify),
            article.title,
            article.content,
            article.publisher,
            article.creationDate.map(_.getMillis),
            article.updateDate.map(_.getMillis)))
      }
  )
}
