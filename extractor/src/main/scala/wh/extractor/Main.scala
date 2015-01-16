package wh.extractor

import java.net.URL

object Main {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      throw new IllegalArgumentException("You should pass the extractor name and URL for parsing")
    }
    val executor = Class.forName("wh.extractor." + args(0).toLowerCase + "." + args(0).capitalize + "Extractor")
    executor.newInstance().asInstanceOf[Extractor].extract(new URL(args(1))).foreach(println)
  }
}
