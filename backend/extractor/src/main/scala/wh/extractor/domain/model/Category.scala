package wh.extractor.domain.model

case class Category(name: String, parentCategory: Category = null) {
  private def containsName(targetName: String): Boolean =
    name.equals(targetName) || parentCategory != null && parentCategory.containsName(targetName)
  if (parentCategory != null && parentCategory.containsName(name)) {
    throw new IllegalArgumentException(s"The category parents contain a node '$name' already")
  }
}
